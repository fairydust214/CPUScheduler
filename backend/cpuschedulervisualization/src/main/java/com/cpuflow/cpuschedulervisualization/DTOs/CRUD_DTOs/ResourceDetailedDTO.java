package com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs;

import com.cpuflow.cpuschedulervisualization.model.ResourceStatus;

import java.util.List;
import java.util.UUID;

public class ResourceDetailedDTO {
    private UUID id;
    private String name;
    private Integer priorityCealing;
    private List<ResourceRequestDTO> resourceRequestDTOList;
    private ResourceStatus status;
    private UUID scenarioDTOID;

    public ResourceDetailedDTO() {
    }

    public ResourceDetailedDTO(UUID id, String name, Integer priorityCealing,
                               List<ResourceRequestDTO> resourceRequestDTOList,
                               ResourceStatus status, UUID scenarioDTOID) {
        this.id = id;
        this.name = name;
        this.priorityCealing = priorityCealing;
        this.resourceRequestDTOList = resourceRequestDTOList;
        this.status = status;
        this.scenarioDTOID = scenarioDTOID;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPriorityCealing() {
        return priorityCealing;
    }

    public void setPriorityCealing(Integer priorityCealing) {
        this.priorityCealing = priorityCealing;
    }

    public List<ResourceRequestDTO> getResourceRequestDTOList() {
        return resourceRequestDTOList;
    }

    public void setResourceRequestDTOList(List<ResourceRequestDTO> resourceRequestDTOList) {
        this.resourceRequestDTOList = resourceRequestDTOList;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    public UUID getScenarioDTOID() {
        return scenarioDTOID;
    }

    public void setScenarioDTOID(UUID scenarioDTOID) {
        this.scenarioDTOID = scenarioDTOID;
    }
}
