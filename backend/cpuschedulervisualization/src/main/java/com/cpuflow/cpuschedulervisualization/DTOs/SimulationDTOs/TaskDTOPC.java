package com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceRequestDTO;
import com.cpuflow.cpuschedulervisualization.model.TaskStatus;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;



public class TaskDTOPC extends TaskDTO{

    private List<ResourceRequestDTO> listWithResourceRequests = new LinkedList<>();

    private List<ResourceDTO> currentlyUsedResources = new LinkedList<>();

    private int effectivePriority;

    public TaskDTOPC(UUID id, String name, TaskStatus status, int remainingTime, int effectivePriority,
                     List<ResourceRequestDTO> newListWithResourceRequests,
                     List<ResourceDTO> newCurrentlyUsedResources) {
        super(id, name, status, remainingTime);
        this.effectivePriority = effectivePriority;
        this.listWithResourceRequests = newListWithResourceRequests;
        this.currentlyUsedResources = newCurrentlyUsedResources;
    }

    public int getEffectivePriority() {
        return effectivePriority;
    }

    public void setEffectivePriority(int effectivePriority) {
        this.effectivePriority = effectivePriority;
    }

    public List<ResourceRequestDTO> getListWithResourceRequests() {
        return listWithResourceRequests;
    }

    public void setListWithResourceRequests(List<ResourceRequestDTO> listWithResourceRequests) {
        this.listWithResourceRequests = listWithResourceRequests;
    }

    public List<ResourceDTO> getCurrentlyUsedResources() {
        return currentlyUsedResources;
    }

    public void setCurrentlyUsedResources(List<ResourceDTO> currentlyUsedResources) {
        this.currentlyUsedResources = currentlyUsedResources;
    }
}
