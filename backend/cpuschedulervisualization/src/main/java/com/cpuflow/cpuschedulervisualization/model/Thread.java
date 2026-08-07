package com.cpuflow.cpuschedulervisualization.model;

import jakarta.persistence.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name="Thread")
public class Thread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // generates and increments the value automatically
    @Column(nullable = false, updatable = false)
    private UUID id;


    @OneToMany(mappedBy = "thread")
    private Set<Process> processSet;


}
