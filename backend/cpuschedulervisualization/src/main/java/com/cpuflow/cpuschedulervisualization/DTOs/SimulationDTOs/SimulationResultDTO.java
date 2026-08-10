package com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs;

import java.util.List;

public class SimulationResultDTO {
    private String algorithm;
    private Integer quantum;
    private int totalTime;
    private List<TimeNodeDTO> timeline;

    // Metrics
    private Double avgWaitingTime;
    private Double avgTurnaroundTime;
    private Double cpuUtilization;
    private Integer missedDeadlines;
}
