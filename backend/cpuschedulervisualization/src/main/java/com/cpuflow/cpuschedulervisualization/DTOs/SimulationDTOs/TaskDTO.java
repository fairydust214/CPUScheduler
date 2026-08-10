package com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs;

import com.cpuflow.cpuschedulervisualization.model.TaskStatus;

import java.util.UUID;

public class TaskDTO {
    private UUID id;
    private String name;
    private TaskStatus status;
}
