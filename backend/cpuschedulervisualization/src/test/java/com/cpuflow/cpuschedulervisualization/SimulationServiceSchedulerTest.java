package com.cpuflow.cpuschedulervisualization;

import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.SimulationResultDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.SimulationResultLSTDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.SimularionResultRRDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.TaskDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.TimeNodeDTO;
import com.cpuflow.cpuschedulervisualization.model.Scenario;
import com.cpuflow.cpuschedulervisualization.model.Task;
import com.cpuflow.cpuschedulervisualization.model.TaskStatus;
import com.cpuflow.cpuschedulervisualization.repo.ScenarioRepo;
import com.cpuflow.cpuschedulervisualization.service.SimulationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SimulationServiceSchedulerTest {

    private static Scenario scenario(String name) {
        return new Scenario(UUID.randomUUID(), name, new LinkedList<>(), new LinkedList<>());
    }

    private static Task task(Scenario scenario, String name, int arrival, int duration, int deadline) {
        Task task = new Task(UUID.randomUUID(), name, arrival, duration, deadline, null, scenario);
        task.setResourceRequests(new ArrayList<>());
        scenario.getTasks().add(task);
        return task;
    }

    private static SimulationService serviceFor(Scenario scenario) {
        ScenarioRepo repo = Mockito.mock(ScenarioRepo.class);
        Mockito.when(repo.findById(scenario.getId())).thenReturn(Optional.of(scenario));
        return new SimulationService(repo);
    }

    private static String gantt(SimulationResultDTO result) {
        StringBuilder sb = new StringBuilder();
        for (TimeNodeDTO node : result.getTimeline()) {
            sb.append(node.getRunningTask() == null ? "-" : node.getRunningTask().getName());
        }
        return sb.toString();
    }

    @Test
    void fcfsRunsTasksInArrivalOrderAndIdlesInBetween() {
        Scenario scenario = scenario("fcfs");
        task(scenario, "A", 0, 2, 10);
        task(scenario, "B", 5, 2, 10);

        SimulationResultDTO result = serviceFor(scenario).createFCFS(scenario.getId());

        assertEquals("AA---BB", gantt(result));
        assertEquals(7, result.getTotalTime());
        assertEquals(4.0 / 7 * 100, result.getCpuUtilization(), 1e-9);
        assertEquals(0, result.getMissedDeadlines());
    }

    @Test
    void fcfsNeverPreemptsALongRunningTask() {
        Scenario scenario = scenario("fcfs-nonpreemptive");
        task(scenario, "A", 0, 4, 100);
        task(scenario, "B", 1, 1, 2);

        SimulationResultDTO result = serviceFor(scenario).createFCFS(scenario.getId());

        assertEquals("AAAAB", gantt(result));
        assertEquals(1, result.getMissedDeadlines());
    }

    @Test
    void fcfsQueueSnapshotHoldsOnlyArrivedTasks() {
        Scenario scenario = scenario("fcfs-queue");
        task(scenario, "A", 0, 3, 10);
        task(scenario, "B", 2, 1, 10);

        SimulationResultDTO result = serviceFor(scenario).createFCFS(scenario.getId());

        assertTrue(result.getTimeline().get(0).getCurrentTimeline().isEmpty());
        assertEquals(1, result.getTimeline().get(2).getCurrentTimeline().size());
        assertEquals("B", result.getTimeline().get(2).getCurrentTimeline().get(0).getName());

        for (TimeNodeDTO node : result.getTimeline()) {
            for (TaskDTO queued : node.getCurrentTimeline()) {
                assertNotNull(queued.getStatus());
            }
        }
    }

    @Test
    void roundRobinSlicesTheCpuByQuantum() {
        Scenario scenario = scenario("rr");
        task(scenario, "A", 0, 4, 100);
        task(scenario, "B", 0, 4, 100);

        SimularionResultRRDTO result = (SimularionResultRRDTO)
                serviceFor(scenario).createRoundRobin(scenario.getId(), 2);

        assertEquals("AABBAABB", gantt(result));
        assertEquals(2, result.getQuantum());
        assertEquals(3, result.getContextSwitches());
        assertEquals(2, result.getPreemptions());
    }

    @Test
    void roundRobinQueuesArrivalsBeforeThePreemptedTask() {
        Scenario scenario = scenario("rr-arrival");
        task(scenario, "A", 0, 4, 100);
        task(scenario, "B", 2, 2, 100);

        SimulationResultDTO result = serviceFor(scenario).createRoundRobin(scenario.getId(), 2);

        assertEquals("AABBAA", gantt(result));
    }

    @Test
    void roundRobinReadsDeadlinesAsAbsolute() {
        Scenario scenario = scenario("rr-deadline");
        task(scenario, "A", 2, 2, 3);

        SimulationResultDTO result = serviceFor(scenario).createRoundRobin(scenario.getId(), 2);

        assertEquals("--AA", gantt(result));
        assertEquals(1, result.getMissedDeadlines());
    }

    @Test
    void roundRobinRejectsAQuantumBelowOne() {
        Scenario scenario = scenario("rr-quantum");
        task(scenario, "A", 0, 2, 10);

        SimulationService service = serviceFor(scenario);
        UUID id = scenario.getId();

        assertThrows(ResponseStatusException.class, () -> service.createRoundRobin(id, 0));
    }

    @Test
    void edfPreemptsForTheEarlierDeadline() {
        Scenario scenario = scenario("edf");
        task(scenario, "A", 0, 4, 20);
        task(scenario, "B", 2, 2, 6);

        SimulationResultDTO result = serviceFor(scenario).createEDF(scenario.getId());

        assertEquals("AABBAA", gantt(result));
        assertEquals(0, result.getMissedDeadlines());
    }

    @Test
    void edfKeepsTheRunningTaskWhenNobodyIsMoreUrgent() {
        Scenario scenario = scenario("edf-hysteresis");
        task(scenario, "A", 0, 3, 10);
        task(scenario, "B", 1, 2, 10);

        SimulationResultDTO result = serviceFor(scenario).createEDF(scenario.getId());

        assertEquals("AAABB", gantt(result));
    }

    @Test
    void edfIsDeterministicAcrossRuns() {
        Scenario scenario = scenario("edf-stable");
        for (int i = 1; i <= 6; i++) {
            task(scenario, "T" + i, 0, 2, 30);
        }

        SimulationService service = serviceFor(scenario);
        String first = gantt(service.createEDF(scenario.getId()));

        for (int run = 0; run < 20; run++) {
            assertEquals(first, gantt(service.createEDF(scenario.getId())));
        }
        assertEquals("T1T1T2T2T3T3T4T4T5T5T6T6", first);
    }

    @Test
    void lstGivesTheCpuToTheTaskWithTheSmallestSlack() {
        Scenario scenario = scenario("lst");
        task(scenario, "A", 0, 4, 20);
        task(scenario, "B", 2, 2, 5);

        SimulationResultLSTDTO result = (SimulationResultLSTDTO)
                serviceFor(scenario).createLST(scenario.getId());

        assertEquals("AABBAA", gantt(result));
        assertEquals(0, result.getNegativeSlackEvents());
        assertEquals(0, result.getNegativeSlackTasks());
    }

    @Test
    void lstHandsTheCpuOverOnlyWhenSomebodyIsStrictlyMoreUrgent() {
        Scenario scenario = scenario("lst-hysteresis");
        task(scenario, "A", 0, 3, 12);
        task(scenario, "B", 0, 3, 12);

        SimulationResultLSTDTO result = (SimulationResultLSTDTO)
                serviceFor(scenario).createLST(scenario.getId());

        assertEquals("ABBAAB", gantt(result));
        assertEquals(3, result.getContextSwitches());
        assertEquals(2, result.getPreemptions());
    }

    @Test
    void lstIsDeterministicAcrossRuns() {
        Scenario scenario = scenario("lst-stable");
        for (int i = 1; i <= 5; i++) {
            task(scenario, "T" + i, 0, 2, 40);
        }

        SimulationService service = serviceFor(scenario);
        String first = gantt(service.createLST(scenario.getId()));

        for (int run = 0; run < 20; run++) {
            assertEquals(first, gantt(service.createLST(scenario.getId())));
        }
    }

    @Test
    void lstReportsNegativeSlack() {
        Scenario scenario = scenario("lst-negative");
        task(scenario, "A", 0, 3, 2);

        SimulationResultLSTDTO result = (SimulationResultLSTDTO)
                serviceFor(scenario).createLST(scenario.getId());

        assertEquals("AAA", gantt(result));
        assertEquals(3, result.getNegativeSlackEvents());
        assertEquals(1, result.getNegativeSlackTasks());
        assertEquals(1, result.getMissedDeadlines());
    }

    @Test
    @Timeout(10)
    void aTaskWithoutWorkNeverStallsTheSimulation() {
        for (String algorithm : new String[]{"FCFS", "RR", "EDF", "LST"}) {
            Scenario scenario = scenario("empty-duration-" + algorithm);
            task(scenario, "A", 0, 0, 5);
            task(scenario, "B", 0, 2, 5);

            SimulationService service = serviceFor(scenario);
            SimulationResultDTO result = switch (algorithm) {
                case "FCFS" -> service.createFCFS(scenario.getId());
                case "RR" -> service.createRoundRobin(scenario.getId(), 2);
                case "EDF" -> service.createEDF(scenario.getId());
                default -> service.createLST(scenario.getId());
            };

            assertEquals("BB", gantt(result), algorithm);
            assertEquals(2, result.getTotalTime(), algorithm);
        }
    }

    @Test
    void anEmptyScenarioReportsNumbersInsteadOfNaN() {
        for (String algorithm : new String[]{"FCFS", "RR", "EDF", "LST"}) {
            Scenario scenario = scenario("empty-" + algorithm);
            SimulationService service = serviceFor(scenario);

            SimulationResultDTO result = switch (algorithm) {
                case "FCFS" -> service.createFCFS(scenario.getId());
                case "RR" -> service.createRoundRobin(scenario.getId(), 2);
                case "EDF" -> service.createEDF(scenario.getId());
                default -> service.createLST(scenario.getId());
            };

            assertFalse(Double.isNaN(result.getAvgWaitingTime()), algorithm);
            assertFalse(Double.isNaN(result.getAvgTurnaroundTime()), algorithm);
            assertFalse(Double.isNaN(result.getCpuUtilization()), algorithm);
            assertEquals(0, result.getTotalTime(), algorithm);
        }
    }

    @Test
    void everyAlgorithmLeavesTheStoredScenarioUntouched() {
        Scenario scenario = scenario("no-side-effects");
        Task a = task(scenario, "A", 0, 3, 10);

        SimulationService service = serviceFor(scenario);
        service.createFCFS(scenario.getId());
        service.createRoundRobin(scenario.getId(), 2);
        service.createEDF(scenario.getId());
        service.createLST(scenario.getId());

        assertEquals(3, a.getDuration());
        assertEquals(0, a.getArrivalTime());
        assertEquals(10, a.getDeadline());
        assertEquals(TaskStatus.WAITING, a.getStatus());
    }
}
