package com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceRequestDTO;
import com.cpuflow.cpuschedulervisualization.model.TaskStatus;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;



public class TaskDTOPC extends TaskDTO{

    private List<ResourceRequestDTO> listWithResouceRequests = new LinkedList<>();

    public TaskDTOPC(UUID id, String name, TaskStatus status, int remainingTime, List<ResourceRequestDTO> newListWithResouceRequests) {
        super(id, name, status, remainingTime);
        this.listWithResouceRequests = newListWithResouceRequests;
    }

    public List<ResourceRequestDTO> getListWithResouceRequests() {
        return listWithResouceRequests;
    }

    public void setListWithResouceRequests(List<ResourceRequestDTO> listWithResouceRequests) {
        this.listWithResouceRequests = listWithResouceRequests;
    }
}
