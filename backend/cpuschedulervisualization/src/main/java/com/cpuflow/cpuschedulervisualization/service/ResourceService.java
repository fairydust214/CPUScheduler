package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceDetailedDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceRequestDTO;
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
public class ResourceService {


    private final ResourceRepo resourceRepo;
    private final TaskRepo taskRepo;

    @Autowired
    public ResourceService(ResourceRepo resourceRepo, TaskRepo taskRepo) {
        this.resourceRepo = resourceRepo;
        this.taskRepo = taskRepo;
    }

    public ResourceDetailedDTO createResource(ResourceDetailedDTO resourceDetailedDTO){
        Resource toSave = dtoToEntity(resourceDetailedDTO);
        toSave = this.resourceRepo.save(toSave);
        ResourceDetailedDTO newDto = entityToDto(toSave);
        return newDto;
    }

    public ResourceDetailedDTO getResourceByID(UUID id){
        Resource foundResource = resourceRepo.findById(id).orElseThrow(()-> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Resource not found with id: " + id
        ));
        ResourceDetailedDTO dto = entityToDto(foundResource);
        return dto;
    }

    public List<ResourceDetailedDTO> getAllResources(){
        List<Resource> listWithResources = this.resourceRepo.findAll();
        List<ResourceDetailedDTO> listWithDtos = new LinkedList<>();
        for(Resource r: listWithResources){
            listWithDtos.add(entityToDto(r));
        }
        return listWithDtos;
    }

    public ResourceDetailedDTO updateResource(UUID id, ResourceDetailedDTO resourceDetailedDTO){
        Resource foundResource = this.resourceRepo.findById(id).orElseThrow(()-> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Resource to update not found with id: " + id
        ));

        Resource temp = dtoToEntity(resourceDetailedDTO);

        foundResource.setName(resourceDetailedDTO.getName());
        foundResource.setPriorityCeiling(resourceDetailedDTO.getPriorityCealing());
        foundResource.setStatus(resourceDetailedDTO.getStatus());
        foundResource.setResourceRequests(temp.getResourceRequests());


        ResourceDetailedDTO result = entityToDto(this.resourceRepo.save(foundResource));
        return result;
    }

    public void deleteResource (UUID id){
        Resource foundResource = this.resourceRepo.findById(id).orElseThrow(()-> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Resource to update not found with id: " + id
        ));

        this.resourceRepo.delete(foundResource);
    }

    private Resource dtoToEntity(ResourceDetailedDTO resourceDetailedDTO){
        Resource newResource = new Resource();
        newResource.setId(resourceDetailedDTO.getId());
        newResource.setName(resourceDetailedDTO.getName());
        newResource.setPriorityCeiling(resourceDetailedDTO.getPriorityCealing());
        newResource.setStatus(resourceDetailedDTO.getStatus());


        if(resourceDetailedDTO.getResourceRequestDTOList() != null ){
            List<ResourceRequestDTO> dtoLinkedList = resourceDetailedDTO.getResourceRequestDTOList();
            List<ResourceRequest> newList = new LinkedList<>();
            for(ResourceRequestDTO rrDTO: dtoLinkedList){
                ResourceRequest newRR = new ResourceRequest();
                UUID taskID = rrDTO.getTaskID();
                Task task = taskRepo.findById(taskID)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Task not found with id: " + taskID
                        ));
                newRR.setTask(task);
                newRR.setResource(newResource);
                newRR.setStartOffset(rrDTO.getStartOffset());
                newRR.setDuration(rrDTO.getDuration());
                newList.add(newRR);
            }
            newResource.setResourceRequests(newList);

        }
        return newResource;
    }

    private ResourceDetailedDTO entityToDto(Resource resource) {
        ResourceDetailedDTO rDto = new ResourceDetailedDTO();
        rDto.setId(resource.getId());
        rDto.setName(resource.getName());
        rDto.setPriorityCealing(resource.getPriorityCeiling());
        rDto.setStatus(resource.getStatus());


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

}
