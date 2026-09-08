package com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs;

public class SimulationResultPCDTO extends SimulationResultDTO {
    private int contextSwitches;
    private int preemptions;
    private int blockingEvents;
    private int priorityInheritances;
    private double avgResponseTime;

    public int getContextSwitches() { return contextSwitches; }
    public void setContextSwitches(int contextSwitches) { this.contextSwitches = contextSwitches; }

    public int getPreemptions() { return preemptions; }
    public void setPreemptions(int preemptions) { this.preemptions = preemptions; }

    public int getBlockingEvents() { return blockingEvents; }
    public void setBlockingEvents(int blockingEvents) { this.blockingEvents = blockingEvents; }

    public int getPriorityInheritances() { return priorityInheritances; }
    public void setPriorityInheritances(int priorityInheritances) { this.priorityInheritances = priorityInheritances; }

    public double getAvgResponseTime() { return avgResponseTime; }
    public void setAvgResponseTime(double avgResponseTime) { this.avgResponseTime = avgResponseTime; }
}