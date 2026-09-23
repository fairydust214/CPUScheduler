package com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs;

import com.cpuflow.cpuschedulervisualization.model.ResourceStatus;

import java.util.UUID;

public class ResourceDTO {
    private UUID id;
    private String name;
    private UUID heldByTaskId;

    public ResourceDTO() {
    }

    public ResourceDTO(UUID id, String name, UUID heldByTaskId) {
        this.id = id;
        this.name = name;
        this.heldByTaskId = heldByTaskId;
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

    public UUID getHeldByTaskId() {
        return heldByTaskId;
    }

    public void setHeldByTaskId(UUID heldByTaskId) {
        this.heldByTaskId = heldByTaskId;
    }
}
