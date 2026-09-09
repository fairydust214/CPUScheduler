package com.cpuflow.cpuschedulervisualization;

import com.cpuflow.cpuschedulervisualization.model.Resource;
import com.cpuflow.cpuschedulervisualization.model.ResourceRequest;
import com.cpuflow.cpuschedulervisualization.model.Scenario;
import com.cpuflow.cpuschedulervisualization.model.Task;
import com.cpuflow.cpuschedulervisualization.repo.ScenarioRepo;
import com.cpuflow.cpuschedulervisualization.service.DtoMapper;
import com.cpuflow.cpuschedulervisualization.service.ScenarioService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({ScenarioService.class, DtoMapper.class})
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ScenarioDeleteTest {

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private ScenarioRepo scenarioRepo;

    @Autowired
    private EntityManager entityManager;

    private UUID persistScenarioWithRequests() {
        Scenario scenario = new Scenario();
        scenario.setName("to delete");
        scenario.setTasks(new LinkedList<>());
        scenario.setResources(new LinkedList<>());

        Resource resource = new Resource();
        resource.setName("R1");
        resource.setScenario(scenario);
        resource.setResourceRequests(new ArrayList<>());
        scenario.getResources().add(resource);

        Task task = new Task();
        task.setName("T1");
        task.setArrivalTime(0);
        task.setDuration(5);
        task.setDeadline(10);
        task.setPriority(1);
        task.setScenario(scenario);
        task.setResourceRequests(new ArrayList<>());
        scenario.getTasks().add(task);

        ResourceRequest request = new ResourceRequest();
        request.setName("T1->R1");
        request.setTask(task);
        request.setResource(resource);
        request.setStartOffset(0);
        request.setDuration(3);
        task.getResourceRequests().add(request);
        resource.getResourceRequests().add(request);

        Scenario saved = this.scenarioRepo.save(scenario);
        this.entityManager.flush();
        this.entityManager.clear();
        return saved.getId();
    }

    private long count(String table) {
        return ((Number) this.entityManager
                .createNativeQuery("select count(*) from " + table)
                .getSingleResult()).longValue();
    }

    @Test
    void deletingAScenarioRemovesItsTasksResourcesAndRequests() {
        UUID id = persistScenarioWithRequests();

        assertEquals(1, count("resource_requests"));

        this.scenarioService.delete(id);
        this.entityManager.flush();

        assertEquals(0, count("resource_requests"), "resource requests");
        assertEquals(0, count("tasks"), "tasks");
        assertEquals(0, count("resources"), "resources");
        assertEquals(0, count("scenarios"), "scenarios");
    }

    @Test
    void deletingAnUnknownScenarioIsNotFound() {
        assertThrows(ResponseStatusException.class,
                () -> this.scenarioService.delete(UUID.randomUUID()));
    }
}
