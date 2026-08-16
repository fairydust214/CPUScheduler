package com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs;

import com.cpuflow.cpuschedulervisualization.model.TaskStatus;

import java.util.UUID;

public class TaskDTO {
    private UUID id;
    private String name;
    private TaskStatus status;
    private int remainingTime;

    public TaskDTO(UUID id, String name, TaskStatus status, int remainingTime) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.remainingTime = remainingTime;
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

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
}
