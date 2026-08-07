package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.model.Process;
import com.cpuflow.cpuschedulervisualization.repo.ProcessRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProcessService {

    private final ProcessRepo processRepo;

    @Autowired
    public ProcessService(ProcessRepo processRepo) {
        this.processRepo = processRepo;
    }

    public Process createProcess(Process newProcess){
        return processRepo.save(newProcess);
    }

    public List<Process> findAllProcesses(){
        return processRepo.findAll();
    }

    public void deleteProcess(UUID id){
        processRepo.deleteProcessById(id);
    }
}
