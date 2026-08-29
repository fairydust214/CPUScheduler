package com.cpuflow.cpuschedulervisualization.service;

import java.util.UUID;

public class HighestPriorityInfo {
    private Integer priority = -1;
    private UUID resourceRequestID;
    private UUID resourceID;
    private UUID taskID;


    public void reset(){
        this.priority = -1;
        this.resourceRequestID = null;
        this.resourceID = null;
        this.taskID = null;
    }

    public HighestPriorityInfo() {
    }

    public HighestPriorityInfo(Integer priority, UUID resourceRequestID, UUID resourceID, UUID taskID) {
        this.priority = priority;
        this.resourceRequestID = resourceRequestID;
        this.resourceID = resourceID;
        this.taskID = taskID;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public UUID getResourceRequestID() {
        return resourceRequestID;
    }

    public void setResourceRequestID(UUID resourceRequestID) {
        this.resourceRequestID = resourceRequestID;
    }

    public UUID getResourceID() {
        return resourceID;
    }

    public void setResourceID(UUID resourceID) {
        this.resourceID = resourceID;
    }

    public UUID getTaskID() {
        return taskID;
    }

    public void setTaskID(UUID taskID) {
        this.taskID = taskID;
    }
}
