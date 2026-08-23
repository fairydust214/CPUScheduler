package com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs;

public class SimularionResultRRDTO extends SimulationResultDTO{

    private Integer contextSwitches;
    private Integer preemptions;
    private Double avgResponseTime;
    private Integer quantum;

    public SimularionResultRRDTO() {
        super();
    }

    public Integer getContextSwitches() {
        return contextSwitches;
    }

    public void setContextSwitches(Integer contextSwitches) {
        this.contextSwitches = contextSwitches;
    }

    public Integer getPreemptions() {
        return preemptions;
    }

    public void setPreemptions(Integer preemptions) {
        this.preemptions = preemptions;
    }

    public Double getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(Double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public Integer getQuantum() {
        return quantum;
    }

    public void setQuantum(Integer quantum) {
        this.quantum = quantum;
    }
}
