package com.cpuflow.cpuschedulervisualization.model;

public enum Event {
    TASK_ARRIVED,          // "Task A arrived"
    TASK_STARTED,          // "Task A started executing"
    TASK_COMPLETED,        // "Task A completed"
    TASK_PREEMPTED,        // "Task A preempted by Task B"
    TASK_RESUMED,          // "Task A resumed"
    TASK_BLOCKED,          // "Task A blocked (waiting for Printer)"
    TASK_UNBLOCKED,        // "Task A unblocked (Printer released)"
    TASK_DEADLINE_MISSED,  // "Task A missed deadline"
    RESOURCE_ACQUIRED,     // "Task A acquired Printer"
    RESOURCE_RELEASED,     // "Task A released Printer"
    PRIORITY_INHERITED,    // "Task A inherited priority from Task B"
    PRIORITY_RESTORED,     // "Task A priority restored"
    QUANTUM_EXPIRED,       // "Time quantum expired for Task A"
    CPU_IDLE               // "CPU idle"
}
