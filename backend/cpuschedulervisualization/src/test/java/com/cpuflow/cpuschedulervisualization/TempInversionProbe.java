package com.cpuflow.cpuschedulervisualization;

import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.SimulationResultDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.SimulationResultPCDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.TaskDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.TaskDTOPC;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.TimeNodeDTO;
import com.cpuflow.cpuschedulervisualization.model.*;
import com.cpuflow.cpuschedulervisualization.repo.ScenarioRepo;
import com.cpuflow.cpuschedulervisualization.service.SimulationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

class TempInversionProbe {

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

    @Test
    void probe() {
        Scenario scenario = new Scenario(UUID.randomUUID(), "inversion", new LinkedList<>(), new LinkedList<>());
        Resource r1 = resource(scenario, "R1");

        Task low = task(scenario, "T1", 0, 4, 12, 1);
        Task mid = task(scenario, "T2", 2, 3, 12, 2);
        Task high = task(scenario, "T3", 3, 3, 8, 3);
        request(low, r1, 1, 2);
        request(high, r1, 0, 2);

        SimulationResultPCDTO r = (SimulationResultPCDTO) serviceFor(scenario).createPCP(scenario.getId());

        StringBuilder g = new StringBuilder();
        StringBuilder p = new StringBuilder();
        StringBuilder b = new StringBuilder();
        for (TimeNodeDTO node : r.getTimeline()) {
            g.append(node.getRunningTask() == null ? "--" : node.getRunningTask().getName());
            g.append(' ');
            p.append(node.getRunningTask() instanceof TaskDTOPC pc ? pc.getEffectivePriority() : "-");
            p.append("  ");
            String blocked = "..";
            for (TaskDTO q : node.getQueue()) {
                if (q.getStatus() == TaskStatus.BLOCKED) {
                    blocked = q.getName();
                }
            }
            b.append(blocked).append(' ');
        }
        System.out.println("PROBE gantt    = " + g);
        System.out.println("PROBE effPrio  = " + p);
        System.out.println("PROBE blocked  = " + b);
        System.out.println("PROBE ceiling(R1) = " + r1.getPriorityCeiling());
        System.out.println("PROBE blocking=" + r.getBlockingEvents()
                + " inheritances=" + r.getPriorityInheritances()
                + " preemptions=" + r.getPreemptions()
                + " switches=" + r.getContextSwitches()
                + " total=" + r.getTotalTime()
                + " missed=" + r.getMissedDeadlines());
        System.out.println("PROBE wait=" + r.getAvgWaitingTime()
                + " turn=" + r.getAvgTurnaroundTime()
                + " resp=" + r.getAvgResponseTime());
    }
}
