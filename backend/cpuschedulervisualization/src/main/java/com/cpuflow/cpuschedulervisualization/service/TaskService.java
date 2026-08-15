package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceRequestDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.TaskDetailedDTO;
import com.cpuflow.cpuschedulervisualization.model.Resource;
import com.cpuflow.cpuschedulervisualization.model.ResourceRequest;
import com.cpuflow.cpuschedulervisualization.model.Task;
import com.cpuflow.cpuschedulervisualization.repo.ResourceRepo;
import com.cpuflow.cpuschedulervisualization.repo.ScenarioRepo;
import com.cpuflow.cpuschedulervisualization.repo.TaskRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TaskService {

    private final TaskRepo taskRepo;
    private  final DtoMapper mapper;


    @Autowired
    public TaskService(TaskRepo taskRepo, DtoMapper mapper) {
        this.taskRepo = taskRepo;
        this.mapper = mapper;

    }

    public TaskDetailedDTO createTask(TaskDetailedDTO newTaskDetailedDTO){
        Task newTask = this.mapper.toEntity(newTaskDetailedDTO);
        Task savedTask = taskRepo.save(newTask);
        return this.mapper.convertToDTO(savedTask);
    }

    public TaskDetailedDTO getTaskById(UUID id) {
        Task task = taskRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found with id: " + id
                ));
        return this.mapper.convertToDTO(task);
    }

    public List<TaskDetailedDTO> findAllTasks(){

        List<Task> foundTasks = taskRepo.findAll();
        List<TaskDetailedDTO> dtoList = new java.util.LinkedList<>(List.of());
        for (Task t: foundTasks){
            dtoList.add(this.mapper.convertToDTO(t));
        }
        return dtoList;
    }

    public TaskDetailedDTO updateTask(UUID id, TaskDetailedDTO dto) {
        Task task = taskRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found with id: " + id
                ));

        this.mapper.updateTask(task,dto);
        Task saved = taskRepo.save(task);
        return this.mapper.convertToDTO(saved);
    }

    public void deleteTask(UUID id){
        taskRepo.deleteTaskById(id);
    }



}
