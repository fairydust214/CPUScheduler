package com.cpuflow.cpuschedulervisualization.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "resources")
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column (nullable = false, updatable = true)
    private ResourceStatus status;

    @Column (nullable = true, updatable = true)
    private String name;

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }


}
