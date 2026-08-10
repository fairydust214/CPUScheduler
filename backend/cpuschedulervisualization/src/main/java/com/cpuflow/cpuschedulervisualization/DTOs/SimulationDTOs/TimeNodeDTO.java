package com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs;

import com.cpuflow.cpuschedulervisualization.model.Event;

import java.util.List;

public class TimeNodeDTO {
    private int time;
    private TaskDTO runningTask;
    private List<ResourceDTO> resources;
    private List<Event> events;
}
