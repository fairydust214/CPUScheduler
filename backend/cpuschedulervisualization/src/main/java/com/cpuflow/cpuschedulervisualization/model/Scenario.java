package com.cpuflow.cpuschedulervisualization.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Entity
public class Scenario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column()
    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new LinkedList<>();

    @Column
    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resource> resources = new LinkedList<>();

    public Scenario() {}

    public Scenario(UUID id, String name, LinkedList<Task> tasks, LinkedList<Resource> resources) {
        this.id = id;
        this.name = name;
        this.tasks = tasks;
        this.resources = resources;
    }

    public Scenario(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(LinkedList<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(LinkedList<Resource> resources) {
        this.resources = resources;
    }
}