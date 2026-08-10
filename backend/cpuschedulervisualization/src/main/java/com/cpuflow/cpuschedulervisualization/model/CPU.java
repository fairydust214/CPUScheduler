package com.cpuflow.cpuschedulervisualization.model;

import jakarta.persistence.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name="CPUs")
public class CPU {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // generates and increments the value automatically
    @Column(nullable = false, updatable = false)
    private UUID id;


    @OneToMany(mappedBy = "cpu")
    private Set<Task> taskSet;



    @ManyToOne
    @JoinColumn(name = "timenode_id", nullable = true)
    private TimeNode timenode;




}
