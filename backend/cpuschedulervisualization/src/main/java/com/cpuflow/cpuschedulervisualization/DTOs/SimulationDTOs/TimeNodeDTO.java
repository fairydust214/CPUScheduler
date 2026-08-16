package com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs;

import com.cpuflow.cpuschedulervisualization.model.Event;

import java.util.List;

public class TimeNodeDTO {
    private int time;
    private TaskDTO runningTask;

    public TimeNodeDTO(int time, TaskDTO runningTask) {
        this.time = time;
        this.runningTask = runningTask;

    }

    public TimeNodeDTO() {
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public TaskDTO getRunningTask() {
        return runningTask;
    }

    public void setRunningTask(TaskDTO runningTask) {
        this.runningTask = runningTask;
    }
}
