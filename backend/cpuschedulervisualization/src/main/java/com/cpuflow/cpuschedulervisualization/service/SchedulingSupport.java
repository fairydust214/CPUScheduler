package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.model.Task;
import com.cpuflow.cpuschedulervisualization.model.TaskStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

final class SchedulingSupport {

    static final Comparator<Task> STABLE_ORDER =
            Comparator.comparing((Task task) -> String.valueOf(task.getName()))
                    .thenComparing(task -> String.valueOf(task.getId()));

    static final Comparator<Task> ARRIVAL_ORDER =
            Comparator.comparingInt(Task::getArrivalTime)
                    .thenComparing(STABLE_ORDER);

    static final Comparator<Task> EARLIEST_DEADLINE_ORDER =
            Comparator.comparingInt(Task::getDeadline)
                    .thenComparingInt(Task::getArrivalTime)
                    .thenComparing(STABLE_ORDER);

    static final Comparator<Task> LEAST_SLACK_ORDER =
            Comparator.comparingInt((Task task) -> task.getDeadline() - task.getRemainingTime())
                    .thenComparingInt(Task::getDeadline)
                    .thenComparingInt(Task::getArrivalTime)
                    .thenComparing(STABLE_ORDER);

    private SchedulingSupport() {
    }

    static void prepareForRun(Collection<Task> tasks) {
        for (Task task : tasks) {
            task.setRemainingTime(Math.max(0, task.getDuration()));
            task.setStatus(TaskStatus.WAITING);
            task.setEffectivePriority(task.getPriority());
            task.setBlockedOnResource(null);
        }
    }

    static List<Task> arrived(Collection<Task> tasks, int currentTime) {
        List<Task> result = new ArrayList<>();

        for (Task task : tasks) {
            if (task.getArrivalTime() <= currentTime) {
                result.add(task);
            }
        }
        return result;
    }

    static int slack(Task task, int currentTime) {
        return task.getDeadline() - currentTime - task.getRemainingTime();
    }

    static boolean isFinished(Task task) {
        return task.getRemainingTime() <= 0;
    }

    static double average(int total, int count) {
        return count <= 0 ? 0.0 : (double) total / count;
    }

    static double percentage(int part, int whole) {
        return whole <= 0 ? 0.0 : (double) part / whole * 100;
    }
}
