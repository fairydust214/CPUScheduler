package com.cpuflow.cpuschedulervisualization.repo;

import com.cpuflow.cpuschedulervisualization.model.ResourceRequest;
import com.cpuflow.cpuschedulervisualization.model.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScenarioRepo extends JpaRepository<Scenario, UUID> {
}
