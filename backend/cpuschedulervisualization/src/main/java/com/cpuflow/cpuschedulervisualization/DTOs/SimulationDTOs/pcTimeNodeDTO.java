package com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs;

import com.cpuflow.cpuschedulervisualization.model.Event;

import java.util.List;

public class pcTimeNodeDTO extends TimeNodeDTO{
    private List<ResourceDTO> resources;
    private List<Event> events;

    public pcTimeNodeDTO(int time, TaskDTO runningTask, List<ResourceDTO> resources, List<Event> events) {
        super(time, runningTask);
        this.resources = resources;
        this.events = events;
    }

    public pcTimeNodeDTO() {
        super();
    }

    public List<ResourceDTO> getResources() {
        return resources;
    }

    public void setResources(List<ResourceDTO> resources) {
        this.resources = resources;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}
