package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceRequestDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.TaskDetailedDTO;
import com.cpuflow.cpuschedulervisualization.model.Resource;
import com.cpuflow.cpuschedulervisualization.model.ResourceRequest;
import com.cpuflow.cpuschedulervisualization.model.Task;
import com.cpuflow.cpuschedulervisualization.repo.ResourceRepo;
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
    private final ResourceRepo resourceRepo;


    @Autowired
    public TaskService(TaskRepo taskRepo, ResourceRepo resourceRepo) {
        this.taskRepo = taskRepo;
        this.resourceRepo = resourceRepo;
    }

    public TaskDetailedDTO createTask(TaskDetailedDTO newTaskDetailedDTO){
        Task newTask = convertToEntity(newTaskDetailedDTO);
        Task savedTask = taskRepo.save(newTask);
        return convertToDTO(savedTask);
    }

    public TaskDetailedDTO getTaskById(UUID id) {
        Task task = taskRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found with id: " + id
                ));
        return convertToDTO(task);
    }

    public List<TaskDetailedDTO> findAllTasks(){

        List<Task> foundTasks = taskRepo.findAll();
        List<TaskDetailedDTO> dtoList = new java.util.LinkedList<>(List.of());
        for (Task t: foundTasks){
            dtoList.add(convertToDTO(t));
        }
        return dtoList;
    }

    public TaskDetailedDTO updateTask(UUID id, TaskDetailedDTO dto) {
        Task task = taskRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found with id: " + id
                ));

        task.setName(dto.getName());
        task.setArrivalTime(dto.getArrivalTime());
        task.setDuration(dto.getDuration());
        task.setDeadline(dto.getDeadline());
        task.setPriority(dto.getPriority());

        Task saved = taskRepo.save(task);
        return convertToDTO(saved);
    }

    public void deleteTask(UUID id){
        taskRepo.deleteTaskById(id);
    }


    public static TaskDetailedDTO convertToDTO(Task task) {
        List<ResourceRequest> listWithResources = task.getResourceRequests();
        List<ResourceRequestDTO> listWithResourceDTOs = new LinkedList<>();
        for (ResourceRequest req: listWithResources){
            listWithResourceDTOs.add(ResourceRequestService.resourceRequestEntityToDTO(req));
        }

        return new TaskDetailedDTO(task.getId(), task.getName(), task.getStatus(), task.getArrivalTime(),
                task.getDuration(),task.getDeadline(),task.getPriority(),listWithResourceDTOs,
                task.getScenario().getId());
    }
    private Task convertToEntity(TaskDetailedDTO dto) {
        Task currentTask = new Task();
        currentTask.setName(dto.getName());
        currentTask.setArrivalTime(dto.getArrivalTime());
        currentTask.setDuration(dto.getDuration());
        currentTask.setDeadline(dto.getDeadline());
        currentTask.setPriority(dto.getPriority());

        if (dto.getResourceRequests() != null) {
            List<ResourceRequestDTO> dtoRRList = dto.getResourceRequests();
            List<ResourceRequest> rrList = new LinkedList<>();

            for(ResourceRequestDTO dtoRR : dtoRRList){
                ResourceRequest rr = new ResourceRequest();
                rr.setTask(currentTask);
                Resource resource = resourceRepo.findById(dtoRR.getResourceId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Resource not found: " + dtoRR.getResourceId()
                        ));
                rr.setResource(resource);
                rr.setStartOffset(dtoRR.getStartOffset());
                rr.setDuration(dtoRR.getDuration());
                rrList.add(rr);

            }
            currentTask.setResourceRequests(rrList);
        }

        return currentTask;
    }
}
