package com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class ResourceRequestDTO {
    private UUID id;

    @NotBlank(message = "a resource request needs a name")
    @Size(max = 255, message = "a resource request name can be at most 255 characters long")
    private String name;

    @NotNull(message = "a resource request needs a resource")
    private UUID resourceId;

    @NotNull(message = "a resource request needs a task")
    private UUID taskID;

    @Min(value = 0, message = "a start offset cannot be negative")
    @Max(value = 1000, message = "a start offset can be at most 1000 ticks")
    private int startOffset;

    @Min(value = 1, message = "a resource request has to last at least 1 tick")
    @Max(value = 1000, message = "a resource request can last at most 1000 ticks")
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
