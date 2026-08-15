package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.SimulationResultDTO;
import com.cpuflow.cpuschedulervisualization.repo.ScenarioRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class SimulationService {

    private final ScenarioRepo scenarioRepo;

    @Autowired
    public SimulationService(ScenarioRepo scenarioRepo) {
        this.scenarioRepo = scenarioRepo;
    }


}
