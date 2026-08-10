package com.cpuflow.cpuschedulervisualization.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resources")
public class Resource implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    private String name;

    @Column(nullable = true)
    private Integer priorityCeiling;

    @OneToMany(mappedBy = "resource")
    private List<ResourceRequest> resourceRequests;
}
