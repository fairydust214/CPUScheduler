package com.cpuflow.cpuschedulervisualization.model;

import jakarta.persistence.*;
import org.yaml.snakeyaml.nodes.ScalarNode;

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

    @ManyToOne
    @JoinColumn(name = "scenario_id", nullable = false)
    private Scenario scenario;


    public Resource(UUID id, String name, Integer priorityCeiling, List<ResourceRequest> resourceRequests,
                    Scenario scenario) {
        this.id = id;
        this.name = name;
        this.priorityCeiling = priorityCeiling;
        this.resourceRequests = resourceRequests;
        this.scenario = scenario;
    }

    public Resource() {

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

    public Integer getPriorityCeiling() {
        return priorityCeiling;
    }

    public void setPriorityCeiling(Integer priorityCeiling) {
        this.priorityCeiling = priorityCeiling;
    }

    public List<ResourceRequest> getResourceRequests() {
        return resourceRequests;
    }

    public void setResourceRequests(List<ResourceRequest> resourceRequests) {
        this.resourceRequests = resourceRequests;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public Integer findPriorityCeiling(){
        Integer heighest = -1;
        if(this.resourceRequests != null && !this.resourceRequests.isEmpty()){
            for(ResourceRequest rr : this.resourceRequests){
                Integer current = rr.getTask().getPriority();

                if(current > heighest){
                    heighest = current;
                }
            }
        }
        this.priorityCeiling = heighest;

        return heighest;
    }
}
