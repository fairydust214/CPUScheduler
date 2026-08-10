package com.cpuflow.cpuschedulervisualization.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    private String name;

    @Column(nullable = false)
    private int arrivalTime;
    @Column(nullable = false)
    private int duration;
    @Column(nullable = false)
    private int deadline;
    @Column(nullable = true)
    private Integer priority;

    @OneToMany(mappedBy = "task")
    private List<ResourceRequest> resourceRequests;



    @Transient
    private int remainingTime;
    @Transient
    private TaskStatus status;
    @Transient
    private int startTime;
    @Transient
    private int completionTime;
    @Transient
    private int waitingTime;
    @Transient
    private int turnaroundTime; // completionTime - arrivalTime
    @Transient
    private int responseTime; // startTime - arrivalTime



    public Task(){}

    public Task(UUID id, String name, int arrivalTime, int duration, int deadline, int priority) {
        this.id = id;
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.duration = duration;
        this.deadline = deadline;
        this.priority = priority;
    }
}









