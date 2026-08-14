package com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs;

import com.cpuflow.cpuschedulervisualization.model.TaskStatus;

import java.util.List;
import java.util.UUID;

public class TaskDetailedDTO {
    private UUID id;
    private String name;
    private TaskStatus status;
    private int arrivalTime;
    private int duration;
    private int deadline;
    private Integer priority;
    private List<ResourceRequestDTO> resourceRequests;
    private UUID scenarioDTOID;


    public TaskDetailedDTO(UUID id, String name, TaskStatus status, int arrivalTime, int duration,
                           int deadline, Integer priority, List<ResourceRequestDTO> resourceRequests,
                           UUID scenarioDTO) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.arrivalTime = arrivalTime;
        this.duration = duration;
        this.deadline = deadline;
        this.priority = priority;
        this.resourceRequests = resourceRequests;
        this.scenarioDTOID = scenarioDTO;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public List<ResourceRequestDTO> getResourceRequests() {
        return resourceRequests;
    }

    public void setResourceRequests(List<ResourceRequestDTO> resourceRequests) {
        this.resourceRequests = resourceRequests;
    }
}