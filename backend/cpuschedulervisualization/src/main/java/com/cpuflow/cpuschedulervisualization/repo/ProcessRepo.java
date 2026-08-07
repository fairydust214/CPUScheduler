package com.cpuflow.cpuschedulervisualization.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cpuflow.cpuschedulervisualization.model.Process;
import java.util.UUID;

public interface ProcessRepo extends JpaRepository<Process, UUID> {
    void deleteProcessById(UUID id);
}
