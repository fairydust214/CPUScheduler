package com.cpuflow.cpuschedulervisualization.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="Tasks")
public class Task implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // generates and increments the value automatically
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column (nullable = true) // Strings are nullable per default
    private String name;


    @ManyToOne
    @JoinColumn(name = "cpu_id", nullable = true) // TODO: Correct version @JoinColumn(name = "cpu_id", nullable = false)
    private CPU cpu;


    // Timing
    @Column(nullable = false)
    private int arrivalTime;
    @Column(nullable = false)
    private int startTime;
    @Column(nullable = false)
    private int duration;
    @Column(nullable = false)
    private int deadline;

    @Column(nullable = true)
    private Integer priority; // for Priority Ceiling

    @Column
    @OneToMany(mappedBy = "task")
    private List<ResourceRequest> resourceRequest;



    @Transient
    private int remainingTime;
    @Transient
    private TaskStatus status;

    @Transient
    private int completionTime;
    @Transient
    private int waitingTime;
    @Transient
    private int turnaroundTime; // completionTime - arrivalTime
    @Transient
    private int responseTime; // startTime - arrivalTime



    public Task(){}

    public Task(UUID id, String name, CPU cpu, int arrivalTime, int duration, int deadline, int priority) {
        this.id = id;
        this.name = name;
        this.cpu = cpu;
        this.arrivalTime = arrivalTime;
        this.duration = duration;
        this.deadline = deadline;
        this.priority = priority;
    }
}









