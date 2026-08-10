package com.cpuflow.cpuschedulervisualization.model;

import jakarta.persistence.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "timeNodes")
public class TimeNode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(mappedBy = "timenode")
    private Set<CPU> setOfCPUS;
    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
