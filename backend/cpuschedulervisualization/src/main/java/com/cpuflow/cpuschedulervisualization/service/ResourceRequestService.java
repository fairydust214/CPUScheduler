package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceRequestDTO;
import com.cpuflow.cpuschedulervisualization.model.ResourceRequest;
import com.cpuflow.cpuschedulervisualization.repo.ResourceRepo;
import com.cpuflow.cpuschedulervisualization.repo.ResourceRequestRepo;
import com.cpuflow.cpuschedulervisualization.repo.TaskRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@Transactional
public class ResourceRequestService {


    private final ResourceRequestRepo resourceRequestRepo;
    private final TaskRepo taskRepo;
    private final ResourceRepo resourceRepo;

    @Autowired
    public ResourceRequestService(ResourceRequestRepo resourceRequestRepo, TaskRepo taskRepo, ResourceRepo resourceRepo) {
        this.resourceRequestRepo = resourceRequestRepo;
        this.taskRepo = taskRepo;
        this.resourceRepo = resourceRepo;
    }

    public ResourceRequestDTO createResourceRequest(ResourceRequestDTO rrDto){
        ResourceRequest toSave = dtoToEntity(rrDto);
        toSave = this.resourceRequestRepo.save(toSave);
        ResourceRequestDTO result = resourceRequestEntityToDTO(toSave);
        return result;
    }

    public ResourceRequestDTO getResourceRequest(UUID id){
        ResourceRequest found = this.resourceRequestRepo.findById(id).orElseThrow(()-> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "ResourceRequest not found with id: "+ id));
        ResourceRequestDTO result = resourceRequestEntityToDTO(found);
        return result;
    }

    public void deleteResourceRequest(UUID id){
        this.resourceRequestRepo.deleteById(id);
    }

    private ResourceRequest dtoToEntity(ResourceRequestDTO rrDTO){
        ResourceRequest newRR = new ResourceRequest();

        newRR.setTask(this.taskRepo.findById(rrDTO.getTaskID()).orElseThrow(()-> new ResponseStatusException(
                HttpStatus.NOT_FOUND,"Task for ResourceRequest not found with id: " + rrDTO.getTaskID()
        )));
        newRR.setResource(this.resourceRepo.findById(rrDTO.getResourceId()).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Resource for ResourceRequest not found with id: " + rrDTO.getResourceId()
        )));

        newRR.setStartOffset(rrDTO.getStartOffset());
        newRR.setDuration(rrDTO.getDuration());

        return newRR;
    }

    public static ResourceRequestDTO resourceRequestEntityToDTO(ResourceRequest resourceRequest){
        return new ResourceRequestDTO(
                resourceRequest.getId(),
                resourceRequest.getName(),
                resourceRequest.getResource().getId(),
                resourceRequest.getTask().getId(),
                resourceRequest.getStartOffset(),
                resourceRequest.getDuration());
    }
}
