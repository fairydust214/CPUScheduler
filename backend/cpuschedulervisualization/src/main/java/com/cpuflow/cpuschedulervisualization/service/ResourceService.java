package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceDetailedDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceRequestDTO;
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
public class ResourceService {


    private final ResourceRepo resourceRepo;
    private final DtoMapper mapper;


    @Autowired
    public ResourceService(ResourceRepo resourceRepo, DtoMapper mapper) {
        this.resourceRepo = resourceRepo;
        this.mapper = mapper;
    }

    public ResourceDetailedDTO createResource(ResourceDetailedDTO resourceDetailedDTO){
        Resource toSave = this.mapper.toEntity(resourceDetailedDTO);
        toSave = this.resourceRepo.save(toSave);
        ResourceDetailedDTO newDto = this.mapper.entityToDto(toSave);
        return newDto;
    }

    public ResourceDetailedDTO getResourceByID(UUID id){
        Resource foundResource = resourceRepo.findById(id).orElseThrow(()-> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Resource not found with id: " + id
        ));
        ResourceDetailedDTO dto = this.mapper.entityToDto(foundResource);
        return dto;
    }

    public List<ResourceDetailedDTO> getAllResources(){
        List<Resource> listWithResources = this.resourceRepo.findAll();
        List<ResourceDetailedDTO> listWithDtos = new LinkedList<>();
        for(Resource r: listWithResources){
            listWithDtos.add(this.mapper.entityToDto(r));
        }
        return listWithDtos;
    }

    public ResourceDetailedDTO updateResource(UUID id, ResourceDetailedDTO resourceDetailedDTO){
        Resource foundResource = this.resourceRepo.findById(id).orElseThrow(()-> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Resource to update not found with id: " + id
        ));

        this.mapper.updateResource(foundResource,resourceDetailedDTO);
        Resource saved = this.resourceRepo.save(foundResource);
        return this.mapper.entityToDto(saved);
    }

    public void deleteResource (UUID id){
        Resource foundResource = this.resourceRepo.findById(id).orElseThrow(()-> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Resource to update not found with id: " + id
        ));

        // because of orpahnRemoval, RR is delted
        for (ResourceRequest rr : foundResource.getResourceRequests()) {
            rr.getTask().getResourceRequests().remove(rr);
        }


        this.resourceRepo.delete(foundResource);
    }



}
