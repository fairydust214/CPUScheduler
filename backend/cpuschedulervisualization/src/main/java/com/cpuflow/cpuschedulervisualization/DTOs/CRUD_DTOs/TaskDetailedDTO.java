package com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs;

import com.cpuflow.cpuschedulervisualization.model.TaskStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public class TaskDetailedDTO {
    private UUID id;

    @NotBlank(message = "a task needs a name")
    @Size(max = 255, message = "a task name can be at most 255 characters long")
    private String name;

    private TaskStatus status;

    @Min(value = 0, message = "an arrival time cannot be negative")
    @Max(value = 1000, message = "an arrival time can be at most 1000 ticks")
    private int arrivalTime;

    @Min(value = 1, message = "a duration has to be at least 1 tick")
    @Max(value = 1000, message = "a duration can be at most 1000 ticks")
    private int duration;

    @Min(value = 0, message = "a deadline cannot be negative")
    private int deadline;

    @Min(value = 0, message = "a priority cannot be negative")
    private Integer priority;

    @Valid
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

    public UUID getScenarioDTOID() {
        return scenarioDTOID;
    }

    public void setScenarioDTOID(UUID scenarioDTOID) {
        this.scenarioDTOID = scenarioDTOID;
    }
}