package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.model.Task;
import com.cpuflow.cpuschedulervisualization.repo.TaskRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TaskService {

    private final TaskRepo taskRepo;

    @Autowired
    public TaskService(TaskRepo taskRepo) {
        this.taskRepo = taskRepo;
    }

    public Task createTask(Task newTask){
        return taskRepo.save(newTask);
    }

    public List<Task> findAllTasks(){
        return taskRepo.findAll();
    }

    public void deleteTask(UUID id){
        taskRepo.deleteTaskById(id);
    }
}
