package com.cpuflow.cpuschedulervisualization;

import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.ResourceDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.SimulationResultDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.SimulationResultPCDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.TaskDTOPC;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.TimeNodeDTO;
import com.cpuflow.cpuschedulervisualization.model.*;
import com.cpuflow.cpuschedulervisualization.repo.ScenarioRepo;
import com.cpuflow.cpuschedulervisualization.service.SimulationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SimulationServicePcpTest {

    private static Task task(Scenario scenario, String name, int arrival, int duration, int deadline, int priority) {
        Task task = new Task(UUID.randomUUID(), name, arrival, duration, deadline, priority, scenario);
        task.setResourceRequests(new ArrayList<>());
        scenario.getTasks().add(task);
        return task;
    }

    private static Resource resource(Scenario scenario, String name) {
        Resource resource = new Resource(UUID.randomUUID(), name, null, new ArrayList<>(), ResourceStatus.FREE, scenario);
        scenario.getResources().add(resource);
        return resource;
    }

    private static void request(Task task, Resource resource, int startOffset, int duration) {
        ResourceRequest rr = new ResourceRequest(UUID.randomUUID(),
                task.getName() + "->" + resource.getName(), task, resource, startOffset, duration);
        task.getResourceRequests().add(rr);
        resource.getResourceRequests().add(rr);
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

    private static String priorities(SimulationResultDTO result) {
        StringBuilder sb = new StringBuilder();
        for (TimeNodeDTO node : result.getTimeline()) {
            if (node.getRunningTask() instanceof TaskDTOPC pc) {
                sb.append(pc.getEffectivePriority());
            } else {
                sb.append("-");
            }
        }
        return sb.toString();
    }

    /**
     * Direct blocking: the low priority task already owns the resource the high priority task needs.
     * T1(prio 1) holds R1 for its whole run, T2(prio 3) arrives at t=2 and wants R1.
     */
    @Test
    void directBlockingMakesTheHolderInheritThePriority() {
        Scenario scenario = new Scenario(UUID.randomUUID(), "direct", new LinkedList<>(), new LinkedList<>());
        Resource r1 = resource(scenario, "R1");

        Task t1 = task(scenario, "T1", 0, 8, 20, 1);
        Task t2 = task(scenario, "T2", 2, 3, 15, 3);
        Task t3 = task(scenario, "T3", 20, 2, 30, 2);
        request(t1, r1, 0, 8);
        request(t2, r1, 0, 3);

        SimulationResultPCDTO result =
                (SimulationResultPCDTO) serviceFor(scenario).createPCP(scenario.getId());

        System.out.println("[direct]   gantt = " + gantt(result));
        System.out.println("[direct]   ceiling(R1) = " + r1.getPriorityCeiling());
        System.out.println("[direct]   blocking=" + result.getBlockingEvents()
                + " inheritances=" + result.getPriorityInheritances()
                + " preemptions=" + result.getPreemptions()
                + " switches=" + result.getContextSwitches()
                + " total=" + result.getTotalTime());

        // ceiling = highest priority among all jobs that may acquire R1
        assertEquals(3, r1.getPriorityCeiling());
        assertEquals("T1T1T1T1T1T1T1T1T2T2T2---------T3T3", gantt(result));
        System.out.println("[direct]   effPrio = " + priorities(result));
        // T1 owns its base priority 1 until T2 blocks at t=2, then runs at the inherited 3
        assertEquals("11333333333---------22", priorities(result));
        assertEquals(1, result.getBlockingEvents());
        assertEquals(1, result.getPriorityInheritances());
        assertEquals(0, result.getPreemptions(), "blocking on a lock is not a preemption");
        assertEquals(22, result.getTotalTime());
        assertEquals(0, result.getMissedDeadlines());
        // every lock is handed back
        assertEquals(ResourceStatus.FREE, r1.getStatus());
        // inherited priority is dropped again
        assertEquals(t1.getPriority(), t1.getEffectivePriority());
        assertEquals(t2.getPriority(), t2.getEffectivePriority());
        assertEquals(t3.getPriority(), t3.getEffectivePriority());
    }

    /**
     * The rule that separates PCP from plain priority inheritance: TMid is blocked on a resource that
     * is FREE, because its priority is not strictly greater than the ceiling of the lock TLow holds.
     */
    @Test
    void ceilingRuleBlocksOnAFreeResource() {
        Scenario scenario = new Scenario(UUID.randomUUID(), "ceiling", new LinkedList<>(), new LinkedList<>());
        Resource r1 = resource(scenario, "R1");
        Resource r2 = resource(scenario, "R2");

        Task low = task(scenario, "L", 0, 5, 30, 1);
        Task mid = task(scenario, "M", 1, 3, 30, 2);
        Task high = task(scenario, "H", 10, 2, 30, 3);
        request(low, r2, 0, 4);
        request(mid, r1, 0, 2);
        request(high, r1, 0, 2);
        request(high, r2, 0, 2);

        SimulationResultPCDTO result =
                (SimulationResultPCDTO) serviceFor(scenario).createPCP(scenario.getId());

        System.out.println("[ceiling]  gantt = " + gantt(result));
        System.out.println("[ceiling]  ceiling(R1) = " + r1.getPriorityCeiling()
                + ", ceiling(R2) = " + r2.getPriorityCeiling());
        System.out.println("[ceiling]  blocking=" + result.getBlockingEvents()
                + " inheritances=" + result.getPriorityInheritances()
                + " preemptions=" + result.getPreemptions()
                + " switches=" + result.getContextSwitches()
                + " total=" + result.getTotalTime());

        assertEquals(3, r1.getPriorityCeiling());
        assertEquals(3, r2.getPriorityCeiling());
        // M is blocked at t=1 although R1 is free, so L keeps the CPU until it leaves its critical section
        assertEquals("LLLLMMML--HH", gantt(result));
        System.out.println("[ceiling]  effPrio = " + priorities(result));
        // L runs at the inherited 2 from t=1, and is back at its own 1 once it frees R2
        assertEquals("12222221--33", priorities(result));
        assertEquals(1, result.getBlockingEvents());
        assertEquals(1, result.getPriorityInheritances());
        assertEquals(12, result.getTotalTime());
        assertEquals(ResourceStatus.FREE, r1.getStatus());
        assertEquals(ResourceStatus.FREE, r2.getStatus());
        assertEquals(low.getPriority(), low.getEffectivePriority());
        assertEquals(mid.getPriority(), mid.getEffectivePriority());
    }

    /**
     * The worst case named in the protocol description: the highest priority job is blocked at most
     * once per semaphore it needs, and never by a chain of lower priority jobs.
     */
    @Test
    void nestedCriticalSectionsAndPlainTasksStillTerminate() {
        Scenario scenario = new Scenario(UUID.randomUUID(), "nested", new LinkedList<>(), new LinkedList<>());
        Resource r1 = resource(scenario, "R1");
        Resource r2 = resource(scenario, "R2");

        Task low = task(scenario, "L", 0, 6, 40, 1);
        Task high = task(scenario, "H", 3, 4, 40, 3);
        Task plain = task(scenario, "P", 2, 2, 40, 2);
        request(low, r1, 0, 5);
        request(low, r2, 1, 2);   // nested inside R1
        request(high, r1, 1, 2);

        SimulationResultPCDTO result =
                (SimulationResultPCDTO) serviceFor(scenario).createPCP(scenario.getId());

        System.out.println("[nested]   gantt = " + gantt(result));
        System.out.println("[nested]   blocking=" + result.getBlockingEvents()
                + " inheritances=" + result.getPriorityInheritances()
                + " preemptions=" + result.getPreemptions()
                + " switches=" + result.getContextSwitches()
                + " total=" + result.getTotalTime());

        int totalWork = low.getDuration() + high.getDuration() + plain.getDuration();
        int busy = 0;
        for (TimeNodeDTO node : result.getTimeline()) {
            if (node.getRunningTask() != null) {
                busy++;
            }
        }
        assertEquals(totalWork, busy, "every task must run for exactly its duration");
        assertEquals(result.getTimeline().size(), result.getTotalTime());
        assertEquals(ResourceStatus.FREE, r1.getStatus());
        assertEquals(ResourceStatus.FREE, r2.getStatus());
        assertEquals(low.getPriority(), low.getEffectivePriority());
        assertEquals(high.getPriority(), high.getEffectivePriority());
    }

    /** A scenario without any resource request degenerates to plain preemptive priority scheduling. */
    @Test
    void withoutResourcesItIsPlainPriorityScheduling() {
        Scenario scenario = new Scenario(UUID.randomUUID(), "plain", new LinkedList<>(), new LinkedList<>());
        task(scenario, "A", 0, 3, 10, 1);
        task(scenario, "B", 1, 2, 10, 3);

        SimulationResultPCDTO result =
                (SimulationResultPCDTO) serviceFor(scenario).createPCP(scenario.getId());

        System.out.println("[plain]    gantt = " + gantt(result));
        assertEquals("ABBAA", gantt(result));
        assertEquals(1, result.getPreemptions());
        assertEquals(0, result.getBlockingEvents());
        assertEquals(0, result.getPriorityInheritances());
    }

    @Test
    void theRunningTaskCarriesTheResourcesItCurrentlyHolds() {
        Scenario scenario = new Scenario(UUID.randomUUID(), "held", new LinkedList<>(), new LinkedList<>());
        Resource r1 = resource(scenario, "R1");
        Task a = task(scenario, "A", 0, 5, 20, 1);
        request(a, r1, 0, 3);

        SimulationResultPCDTO result =
                (SimulationResultPCDTO) serviceFor(scenario).createPCP(scenario.getId());

        StringBuilder held = new StringBuilder();
        for (TimeNodeDTO node : result.getTimeline()) {
            held.append(((TaskDTOPC) node.getRunningTask()).getCurrentlyUsedResources().size());
        }
        System.out.println("[held]     gantt = " + gantt(result) + ", held = " + held);

        assertEquals("AAAAA", gantt(result));
        assertEquals("11100", held.toString());

        ResourceDTO used = ((TaskDTOPC) result.getTimeline().get(0).getRunningTask())
                .getCurrentlyUsedResources().get(0);
        assertEquals(r1.getId(), used.getId());
        assertEquals("R1", used.getName());
        assertEquals(ResourceStatus.TAKEN, used.getStatus());
        assertEquals(a.getId(), used.getHeldByTaskId());

        assertTrue(((TaskDTOPC) result.getTimeline().get(4).getRunningTask())
                .getCurrentlyUsedResources().isEmpty());
    }
}
