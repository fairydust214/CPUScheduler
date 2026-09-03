package com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs;

import java.util.UUID;

public class ResourceRequestDTO {
    private UUID id;
    private String name;
    private UUID resourceId;
    private UUID taskID;
    private int startOffset;
    private int duration;
    private int remainingTime;

    public ResourceRequestDTO() {
    }

    public ResourceRequestDTO(UUID id, String name, UUID resourceId, UUID taskID, int startOffset, int duration) {
        this.id = id;
        this.name = name;
        this.resourceId = resourceId;
        this.taskID = taskID;
        this.startOffset = startOffset;
        this.duration = duration;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public UUID getTaskID() {
        return taskID;
    }

    public void setTaskID(UUID taskID) {
        this.taskID = taskID;
    }

    public void setResourceId(UUID resourceId) {
        this.resourceId = resourceId;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
