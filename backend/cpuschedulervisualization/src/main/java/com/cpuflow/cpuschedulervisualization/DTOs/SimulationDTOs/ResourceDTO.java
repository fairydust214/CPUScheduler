package com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs;

import com.cpuflow.cpuschedulervisualization.model.ResourceStatus;

import java.util.UUID;

public class ResourceDTO {
    private UUID id;
    private String name;
    private ResourceStatus status;
    private UUID heldByTaskId;
}
