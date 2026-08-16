package com.cpuflow.cpuschedulervisualization.APIs;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ScenarioDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.SimulationResultDTO;
import com.cpuflow.cpuschedulervisualization.model.Scenario;
import com.cpuflow.cpuschedulervisualization.repo.ScenarioRepo;
import com.cpuflow.cpuschedulervisualization.service.SimulationService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/simulation/scenario/")
@CrossOrigin(origins = "http://localhost:4200")
public class SimulationResource {


    private final SimulationService simulationService;


    public SimulationResource(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/{id}")
    public SimulationResultDTO createFCFS(@PathVariable UUID id){
        SimulationResultDTO resultDTO = this.simulationService.createFCFS(id);
        return resultDTO;
    }
}
