package com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs;

public class SimulationResultLSTDTO extends SimulationResultDTO{

    private Integer contextSwitches;
    private Integer preemptions;
    private Integer negativeSlackEvents;
    private Integer negativeSlackTasks;

    public SimulationResultLSTDTO() {
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

    public Integer getNegativeSlackEvents() {
        return negativeSlackEvents;
    }

    public void setNegativeSlackEvents(Integer negativeSlackEvents) {
        this.negativeSlackEvents = negativeSlackEvents;
    }

    public Integer getNegativeSlackTasks() {
        return negativeSlackTasks;
    }

    public void setNegativeSlackTasks(Integer negativeSlackTasks) {
        this.negativeSlackTasks = negativeSlackTasks;
    }
}
