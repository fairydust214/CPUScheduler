package com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs;

import com.cpuflow.cpuschedulervisualization.model.Resource;
import com.cpuflow.cpuschedulervisualization.model.Task;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScenarioDTO {

    private UUID id;

    @NotBlank(message = "a scenario needs a name")
    @Size(max = 255, message = "a scenario name can be at most 255 characters long")
    private String name;

    @Valid
    private List<TaskDetailedDTO> tasks = new ArrayList<>();

    @Valid
    private List<ResourceDetailedDTO> resources = new ArrayList<>();

    public ScenarioDTO() {
    }

    public ScenarioDTO(UUID id, String name, List<TaskDetailedDTO> tasks, List<ResourceDetailedDTO> resources) {
        this.id = id;
        this.name = name;
        this.tasks = tasks;
        this.resources = resources;
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

    public List<TaskDetailedDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskDetailedDTO> tasks) {
        this.tasks = tasks;
    }

    public List<ResourceDetailedDTO> getResources() {
        return resources;
    }

    public void setResources(List<ResourceDetailedDTO> resources) {
        this.resources = resources;
    }
}
