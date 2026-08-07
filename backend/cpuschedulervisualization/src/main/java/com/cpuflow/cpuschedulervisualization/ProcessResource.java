package com.cpuflow.cpuschedulervisualization;

import com.cpuflow.cpuschedulervisualization.model.Process;
import com.cpuflow.cpuschedulervisualization.service.ProcessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/resource")
public class ProcessResource {

    private final ProcessService processService;

    public ProcessResource(ProcessService processService) {
        this.processService = processService;
    }

    @GetMapping
    public ResponseEntity<List<Process>> getAllProcesses(){
        List<Process> processes = this.processService.findAllProcesses();
        return new ResponseEntity<>(processes, HttpStatus.OK);
    }
}
