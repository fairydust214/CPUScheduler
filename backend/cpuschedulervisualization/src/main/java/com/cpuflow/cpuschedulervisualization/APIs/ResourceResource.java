package com.cpuflow.cpuschedulervisualization.APIs;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceDetailedDTO;
import com.cpuflow.cpuschedulervisualization.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resource")
@CrossOrigin(origins = "http://localhost:4200")
public class ResourceResource {

    private final ResourceService resourceService;

    @Autowired
    public ResourceResource(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<ResourceDetailedDTO>> getAllResources(){
        List<ResourceDetailedDTO> resources = this.resourceService.getAllResources();
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResourceDetailedDTO> getResourceById(@PathVariable("id")UUID id){
        ResourceDetailedDTO result = this.resourceService.getResourceByID(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/add")
    public ResponseEntity<ResourceDetailedDTO> createResource(@Valid @RequestBody ResourceDetailedDTO rDto){
        ResourceDetailedDTO saved = this.resourceService.createResource(rDto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable("id")UUID id){
        this.resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();

    }
}
