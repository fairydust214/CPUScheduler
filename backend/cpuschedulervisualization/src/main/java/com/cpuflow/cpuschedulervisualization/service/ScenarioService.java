package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.APIs.ScnearioResource;
import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceDetailedDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ScenarioDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.TaskDetailedDTO;
import com.cpuflow.cpuschedulervisualization.model.Resource;
import com.cpuflow.cpuschedulervisualization.model.Scenario;
import com.cpuflow.cpuschedulervisualization.model.Task;
import com.cpuflow.cpuschedulervisualization.repo.ScenarioRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ScenarioService {

    private final ScenarioRepo scenarioRepo;


    @Autowired
    public ScenarioService(ScenarioRepo scenarioRepo) {
        this.scenarioRepo = scenarioRepo;
    }

    public ScenarioDTO create(ScenarioDTO dto) {
        Scenario toSave = dtoToEntity(dto);
        toSave = this.scenarioRepo.save(toSave);
        return scenarioDTO(toSave);
    }

    public List<ScenarioDTO> findAll() {
        //TODO
        return null;
    }

    public ScenarioDTO findById(UUID id) {
        //TODO
        return null;
    }

    public ScenarioDTO update(UUID id, ScenarioDTO dto) {
        //TODO
        return null;
    }

    public void delete(UUID id) {
        //TODO
    }

    private Scenario dtoToEntity(ScenarioDTO dto) {

        Scenario scenario = new Scenario();
        scenario.setName(dto.getName());

        return scenario;
    }

    private ScenarioDTO scenarioDTO(Scenario scenario){
        ScenarioDTO scenarioDTO = new ScenarioDTO();
        scenarioDTO.setId(scenario.getId());
        scenarioDTO.setName(scenario.getName());

        if(scenario.getResources() != null || scenario.getResources().isEmpty()){
            List<Resource> resourceList = scenario.getResources();
            List<ResourceDetailedDTO> rDTOList = new LinkedList<>();

            for(Resource resource: resourceList){
                rDTOList.add(ResourceService.entityToDto(resource));
            }

            scenarioDTO.setResources(rDTOList);

            List<Task> taskList = scenario.getTasks();
            List<TaskDetailedDTO> tDTO = new LinkedList<>();

            for(Task task: taskList){
                tDTO.add(TaskService.convertToDTO(task));
            }
            scenarioDTO.setTasks(tDTO);
        }

        return scenarioDTO;
    }
}
