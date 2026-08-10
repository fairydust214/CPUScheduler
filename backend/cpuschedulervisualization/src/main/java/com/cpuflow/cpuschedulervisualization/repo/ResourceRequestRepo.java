package com.cpuflow.cpuschedulervisualization.repo;

import com.cpuflow.cpuschedulervisualization.model.Resource;
import com.cpuflow.cpuschedulervisualization.model.ResourceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResourceRequestRepo extends JpaRepository<ResourceRequest, UUID> {
}
