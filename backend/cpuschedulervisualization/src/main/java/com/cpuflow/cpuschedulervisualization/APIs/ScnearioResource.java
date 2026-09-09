package com.cpuflow.cpuschedulervisualization.APIs;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ScenarioDTO;
import com.cpuflow.cpuschedulervisualization.service.ScenarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/scenario")
@CrossOrigin(origins = "http://localhost:4200")
public class ScnearioResource {

    private final ScenarioService scenarioService;

    @Autowired
    public ScnearioResource(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @PostMapping
    public ResponseEntity<ScenarioDTO> create(@RequestBody ScenarioDTO dto) {
        ScenarioDTO created = scenarioService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ScenarioDTO>> getAll() {
        return ResponseEntity.ok(scenarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScenarioDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(scenarioService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScenarioDTO> update(@PathVariable UUID id,
                                              @RequestBody ScenarioDTO dto) {
        return ResponseEntity.ok(scenarioService.update(id, dto));
    }

    @PutMapping("/{id}/resource-requests")
    public ResponseEntity<ScenarioDTO> createResourceRequests(@PathVariable UUID id,
                                              @RequestBody ScenarioDTO dto) {
        return ResponseEntity.ok(scenarioService.createResourceRequests(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        this.scenarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
