package com.cpuflow.cpuschedulervisualization.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cpuflow.cpuschedulervisualization.model.Task;
import java.util.UUID;

public interface TaskRepo extends JpaRepository<Task, UUID> {
    void deleteTaskById (UUID id);
}
