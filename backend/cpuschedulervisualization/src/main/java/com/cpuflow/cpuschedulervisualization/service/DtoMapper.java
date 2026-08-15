package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceDetailedDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceRequestDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ScenarioDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.TaskDetailedDTO;
import com.cpuflow.cpuschedulervisualization.model.Resource;
import com.cpuflow.cpuschedulervisualization.model.ResourceRequest;
import com.cpuflow.cpuschedulervisualization.model.Scenario;
import com.cpuflow.cpuschedulervisualization.model.Task;
import com.cpuflow.cpuschedulervisualization.repo.ResourceRepo;
import com.cpuflow.cpuschedulervisualization.repo.ScenarioRepo;
import com.cpuflow.cpuschedulervisualization.repo.TaskRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedList;
import java.util.List;

@Component
public class DtoMapper {

    private final ScenarioRepo scenarioRepo;
    private final TaskRepo taskRepo;
    private final ResourceRepo resourceRepo;

    public DtoMapper(ScenarioRepo scenarioRepo,
                     TaskRepo taskRepo,
                     ResourceRepo resourceRepo) {
        this.scenarioRepo = scenarioRepo;
        this.taskRepo = taskRepo;
        this.resourceRepo = resourceRepo;
    }

    // --- Scenario ---
    public Scenario toEntity(ScenarioDTO dto) {
        Scenario scenario = new Scenario();
        scenario.setName(dto.getName());

        if (dto.getResources() != null) {
            LinkedList<Resource> resources = new LinkedList<>(dto.getResources().stream()
                    .map(r -> toEntity(r, scenario))
                    .toList());
            scenario.setResources(resources);
        }

        if (dto.getTasks() != null) {
            LinkedList<Task> tasks = new LinkedList<>(dto.getTasks().stream()
                    .map(t -> toEntity(t, scenario))
                    .toList());
            scenario.setTasks(tasks);
        }

        return scenario;
    }

    // --- Task (standalone, looks up Scenario from DB) ---
    public Task toEntity(TaskDetailedDTO dto) {
        Scenario scenario = scenarioRepo.findById(dto.getScenarioDTOID())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Scenario not found"));
        return toEntity(dto, scenario);
    }

    // --- Task (with known Scenario, no extra DB call) ---
    public Task toEntity(TaskDetailedDTO dto, Scenario scenario) {
        Task task = new Task();
        task.setName(dto.getName());
        task.setArrivalTime(dto.getArrivalTime());
        task.setDuration(dto.getDuration());
        task.setDeadline(dto.getDeadline());
        task.setPriority(dto.getPriority());
        task.setScenario(scenario);

        if (dto.getResourceRequests() != null) {
            List<ResourceRequest> rrList = dto.getResourceRequests().stream()
                    .map(rrDto -> {
                        ResourceRequest rr = new ResourceRequest();
                        rr.setTask(task);
                        rr.setResource(resourceRepo.findById(rrDto.getResourceId())
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Resource not found")));
                        rr.setStartOffset(rrDto.getStartOffset());
                        rr.setDuration(rrDto.getDuration());
                        return rr;
                    }).toList();
            task.setResourceRequests(rrList);
        }

        return task;
    }

    // --- Resource (standalone) ---
    public Resource toEntity(ResourceDetailedDTO dto) {
        Scenario scenario = scenarioRepo.findById(dto.getScenarioDTOID())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Scenario not found"));
        return toEntity(dto, scenario);
    }

    // --- Resource (with known Scenario) ---
    public Resource toEntity(ResourceDetailedDTO dto, Scenario scenario) {
        Resource resource = new Resource();
        resource.setName(dto.getName());
        resource.setPriorityCeiling(dto.getPriorityCealing());
        resource.setStatus(dto.getStatus());
        resource.setScenario(scenario);

        if (dto.getResourceRequestDTOList() != null) {
            List<ResourceRequest> rrList = dto.getResourceRequestDTOList().stream()
                    .map(rrDto -> {
                        ResourceRequest rr = new ResourceRequest();
                        rr.setResource(resource);
                        rr.setTask(taskRepo.findById(rrDto.getTaskID())
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Task not found")));
                        rr.setStartOffset(rrDto.getStartOffset());
                        rr.setDuration(rrDto.getDuration());
                        return rr;
                    }).toList();
            resource.setResourceRequests(rrList);
        }

        return resource;
    }

    public TaskDetailedDTO convertToDTO(Task task) {
        List<ResourceRequest> listWithResources = task.getResourceRequests();
        List<ResourceRequestDTO> listWithResourceDTOs = new LinkedList<>();
        for (ResourceRequest req: listWithResources){
            listWithResourceDTOs.add(ResourceRequestService.resourceRequestEntityToDTO(req));
        }

        return new TaskDetailedDTO(task.getId(), task.getName(), task.getStatus(), task.getArrivalTime(),
                task.getDuration(),task.getDeadline(),task.getPriority(),listWithResourceDTOs,
                task.getScenario().getId());
    }

    public ResourceDetailedDTO entityToDto(Resource resource) {
        ResourceDetailedDTO rDto = new ResourceDetailedDTO();
        rDto.setId(resource.getId());
        rDto.setName(resource.getName());
        rDto.setPriorityCealing(resource.getPriorityCeiling());
        rDto.setStatus(resource.getStatus());
        rDto.setScenarioDTOID(resource.getScenario().getId());


        if (resource.getResourceRequests() != null){
            List<ResourceRequest> rrList = resource.getResourceRequests();
            List<ResourceRequestDTO> newList = new LinkedList<>();
            for(ResourceRequest rr: rrList){
                newList.add(ResourceRequestService.resourceRequestEntityToDTO(rr));
            }
            rDto.setResourceRequestDTOList(newList);
        }
        return rDto;

    }

    public void updateTask(Task task, TaskDetailedDTO dto) {

        task.setName(dto.getName());
        task.setArrivalTime(dto.getArrivalTime());
        task.setDuration(dto.getDuration());
        task.setDeadline(dto.getDeadline());
        task.setPriority(dto.getPriority());


        task.getResourceRequests().clear();

        if (dto.getResourceRequests() != null) {
            for (ResourceRequestDTO rrDto : dto.getResourceRequests()) {
                ResourceRequest rr = new ResourceRequest();
                rr.setTask(task);
                rr.setResource(resourceRepo.findById(rrDto.getResourceId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Resource not found")));
                rr.setStartOffset(rrDto.getStartOffset());
                rr.setDuration(rrDto.getDuration());
                task.getResourceRequests().add(rr);
            }
        }
    }

    public void updateResource(Resource resource, ResourceDetailedDTO dto) {
        resource.setName(dto.getName());
        resource.setPriorityCeiling(dto.getPriorityCealing());

        //TODO: potentially not important
        resource.getResourceRequests().clear();

        if (dto.getResourceRequestDTOList() != null) {
            for (ResourceRequestDTO rrDto : dto.getResourceRequestDTOList()) {
                ResourceRequest rr = new ResourceRequest();
                rr.setResource(resource);
                rr.setTask(taskRepo.findById(rrDto.getTaskID())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Task not found")));
                rr.setStartOffset(rrDto.getStartOffset());
                rr.setDuration(rrDto.getDuration());
                resource.getResourceRequests().add(rr);
            }
        }
    }
}