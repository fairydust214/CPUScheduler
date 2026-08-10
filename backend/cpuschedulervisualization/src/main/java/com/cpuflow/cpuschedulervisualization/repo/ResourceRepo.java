package com.cpuflow.cpuschedulervisualization.repo;

import com.cpuflow.cpuschedulervisualization.model.Resource;
import com.cpuflow.cpuschedulervisualization.model.ResourceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResourceRepo extends JpaRepository<Resource, UUID> {

    Optional<Resource> findById(UUID id);
}
