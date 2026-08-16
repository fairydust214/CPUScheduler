package com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs;

import java.util.List;

public class SimulationResultDTO {
    private SimulationType algorithm;
    private Integer quantum;
    private int totalTime;
    private List<TimeNodeDTO> timeline;

    // Metrics
    private Double avgWaitingTime;
    private Double avgTurnaroundTime;
    private Double cpuUtilization;
    private Integer missedDeadlines;

    public SimulationResultDTO() {
    }

    public SimulationType getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(SimulationType algorithm) {
        this.algorithm = algorithm;
    }

    public Integer getQuantum() {
        return quantum;
    }

    public void setQuantum(Integer quantum) {
        this.quantum = quantum;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public List<TimeNodeDTO> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimeNodeDTO> timeline) {
        this.timeline = timeline;
    }

    public Double getAvgWaitingTime() {
        return avgWaitingTime;
    }

    public void setAvgWaitingTime(Double avgWaitingTime) {
        this.avgWaitingTime = avgWaitingTime;
    }

    public Double getAvgTurnaroundTime() {
        return avgTurnaroundTime;
    }

    public void setAvgTurnaroundTime(Double avgTurnaroundTime) {
        this.avgTurnaroundTime = avgTurnaroundTime;
    }

    public Double getCpuUtilization() {
        return cpuUtilization;
    }

    public void setCpuUtilization(Double cpuUtilization) {
        this.cpuUtilization = cpuUtilization;
    }

    public Integer getMissedDeadlines() {
        return missedDeadlines;
    }

    public void setMissedDeadlines(Integer missedDeadlines) {
        this.missedDeadlines = missedDeadlines;
    }
}
