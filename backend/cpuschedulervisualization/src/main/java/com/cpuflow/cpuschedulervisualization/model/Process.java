package com.cpuflow.cpuschedulervisualization.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name="Processes")
public class Process implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // generates and increments the value automatically
    @Column(nullable = false, updatable = false)
    private UUID id;


    @ManyToOne
    @JoinColumn(name = "thread_id", nullable = false)
    private Thread thread;


    @Column (nullable = true) // Strings are nullable per default
    private String name;
    @Column(nullable = false)
    private int arrivalTime;
    @Column(nullable = false)
    private int deadline;
    @Column(nullable = false)
    private int duration;
    @Column (nullable = true)
    private ThreadStatus status;

    public Process(){}
    public Process(UUID id, Thread thread, String name, int arrivalTime, int deadline, int duration, ThreadStatus status) {
        this.id = id;
        this.thread = thread;
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.deadline = deadline;
        this.duration = duration;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public ThreadStatus getStatus() {
        return status;
    }

    public void setStatus(ThreadStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Process{" +
                "id=" + id +
                ", thread=" + thread +
                ", name='" + name + '\'' +
                ", arrivalTime=" + arrivalTime +
                ", deadline=" + deadline +
                ", duration=" + duration +
                ", status=" + status +
                '}';
    }
}
