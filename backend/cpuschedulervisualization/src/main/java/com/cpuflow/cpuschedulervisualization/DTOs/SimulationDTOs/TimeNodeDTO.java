package com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs;


import java.util.List;

public class TimeNodeDTO {
    private int time;
    private TaskDTO runningTask;
    private List<TaskDTO> currentTimeline;

    public TimeNodeDTO(int time, TaskDTO runningTask, List<TaskDTO> currentTimeline) {
        this.time = time;
        this.runningTask = runningTask;
        this.currentTimeline = currentTimeline;

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

    public List<TaskDTO> getCurrentTimeline() {
        return currentTimeline;
    }

    public void setCurrentTimeline(List<TaskDTO> currentTimeline) {
        this.currentTimeline = currentTimeline;
    }
}
