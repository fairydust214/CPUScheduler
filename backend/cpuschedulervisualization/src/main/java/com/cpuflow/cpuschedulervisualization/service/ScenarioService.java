package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceDetailedDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ScenarioDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.TaskDetailedDTO;
import com.cpuflow.cpuschedulervisualization.model.Resource;
import com.cpuflow.cpuschedulervisualization.model.Scenario;
import com.cpuflow.cpuschedulervisualization.model.Task;
import com.cpuflow.cpuschedulervisualization.repo.ScenarioRepo;
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
public class ScenarioService {

    private final ScenarioRepo scenarioRepo;
    private final DtoMapper mapper;


    @Autowired
    public ScenarioService(ScenarioRepo scenarioRepo, DtoMapper mapper) {
        this.scenarioRepo = scenarioRepo;
        this.mapper = mapper;
    }

    public ScenarioDTO create(ScenarioDTO dto) {
        Scenario toSave = dtoToEntity(dto);
        toSave = this.scenarioRepo.save(toSave);
        return entityToDto(toSave);
    }

    public List<ScenarioDTO> findAll() {
        List<Scenario> allScenarios = this.scenarioRepo.findAll();
        List<ScenarioDTO> dtoList = new LinkedList<>();
        for(Scenario s: allScenarios){
            dtoList.add(entityToDto(s));
        }
        return dtoList;
    }

    public ScenarioDTO findById(UUID id) {
        Scenario found = this.scenarioRepo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Scenario not found with id:" + id));

        return entityToDto(found);

    }

    public ScenarioDTO update(UUID id, ScenarioDTO dto) {
        Scenario scenarioToUpdate = this.scenarioRepo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Scenario not found with id:" + id));

        scenarioToUpdate.setName(dto.getName());
        scenarioToUpdate = this.scenarioRepo.save(scenarioToUpdate);
        return entityToDto(scenarioToUpdate);
    }

    public void delete(UUID id) {
        this.scenarioRepo.deleteById(id);
    }

    private Scenario dtoToEntity(ScenarioDTO dto) {

        Scenario scenario = new Scenario();
        scenario.setName(dto.getName());

        if(dto.getResources() != null && !dto.getResources().isEmpty()){
            List<ResourceDetailedDTO> dtoList = dto.getResources();
            LinkedList<Resource> resultList = new LinkedList<>();

            for(ResourceDetailedDTO rDTO : dtoList){
                resultList.add(this.mapper.toEntity(rDTO));
            }
            scenario.setResources(resultList);
        }

        if(dto.getTasks() != null && !dto.getTasks().isEmpty()){
            List<TaskDetailedDTO> dtoList = dto.getTasks();
            LinkedList<Task> taskList = new LinkedList<>();

            for(TaskDetailedDTO tDTO : dtoList){
                taskList.add(this.mapper.toEntity(tDTO));
            }
            scenario.setTasks(taskList);
        }

        return scenario;
    }

    private ScenarioDTO entityToDto(Scenario scenario){
        ScenarioDTO scenarioDTO = new ScenarioDTO();
        scenarioDTO.setId(scenario.getId());
        scenarioDTO.setName(scenario.getName());

        if(scenario.getResources() != null && !scenario.getResources().isEmpty()){
            List<Resource> resourceList = scenario.getResources();
            List<ResourceDetailedDTO> rDTOList = new LinkedList<>();

            for(Resource resource: resourceList){
                rDTOList.add(this.mapper.entityToDto(resource));
            }

            scenarioDTO.setResources(rDTOList);
        }

        if(scenario.getTasks() != null || !scenario.getTasks().isEmpty()){
            List<Task> taskList = scenario.getTasks();
            List<TaskDetailedDTO> tDTO = new LinkedList<>();

            for(Task task: taskList){
                tDTO.add(this.mapper.convertToDTO(task));
            }
            scenarioDTO.setTasks(tDTO);
        }

        return scenarioDTO;
    }
}
