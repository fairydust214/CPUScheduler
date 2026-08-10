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

    @Column(nullable = true)
    private String name;

    // Priority Ceiling Protocol
    @Column(nullable = true)
    private Integer priorityCeiling;  // Highest priority of any task that uses it

    // Which tasks need this resource (and when)
    @OneToMany(mappedBy = "resource")
    private List<ResourceRequest> resourceRequests;

    // Runtime state (for simulation, not persisted OR separate column)
    @Transient                        // Not saved to DB — only used during simulation
    private UUID heldByTaskId;

    @Transient
    private ResourceStatus status;    // FREE, LOCKED
}
