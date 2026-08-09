package com.cpuflow.cpuschedulervisualization;

import com.cpuflow.cpuschedulervisualization.model.Process;
import com.cpuflow.cpuschedulervisualization.model.Resource;
import com.cpuflow.cpuschedulervisualization.service.ProcessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resource")
@CrossOrigin(origins = "http://localhost:4200")
public class ProcessResource {

    private final ProcessService processService;

    public ProcessResource(ProcessService processService) {
        this.processService = processService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Process>> getAllProcesses(){
        List<Process> processes = this.processService.findAllProcesses();
        return new ResponseEntity<>(processes, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Process> createProcess(@RequestBody Process process){
        Process newProcess = processService.createProcess(process);
        return new ResponseEntity<>(newProcess, HttpStatus.CREATED);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Process> deleteProcess(@PathVariable("id")UUID id){
        processService.deleteProcess(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
