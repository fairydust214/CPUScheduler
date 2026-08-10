package com.cpuflow.cpuschedulervisualization.APIs;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.TaskDetailedDTO;
import com.cpuflow.cpuschedulervisualization.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/task")
@CrossOrigin(origins = "http://localhost:4200")
public class TaskResource {

    private final TaskService taskService;

    @Autowired
    public TaskResource(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<TaskDetailedDTO>> getAllTasks(){
        List<TaskDetailedDTO> tasks = this.taskService.findAllTasks();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<TaskDetailedDTO> createTask(@RequestBody TaskDetailedDTO task){
        TaskDetailedDTO newTask = taskService.createTask(task);
        return new ResponseEntity<>(newTask, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDetailedDTO> getTaskById(@PathVariable("id")UUID id) {
        TaskDetailedDTO result = this.taskService.getTaskById(id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDetailedDTO> updateTask(@PathVariable("id")UUID id, @RequestBody TaskDetailedDTO dtoTask){
        TaskDetailedDTO updatedTask = this.taskService.updateTask(id,dtoTask);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id")UUID id){
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

}
