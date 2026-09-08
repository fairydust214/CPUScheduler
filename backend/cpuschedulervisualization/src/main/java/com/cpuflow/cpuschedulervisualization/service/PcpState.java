package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceRequestDTO;
import com.cpuflow.cpuschedulervisualization.model.ResourceRequest;
import com.cpuflow.cpuschedulervisualization.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class PcpState {

    final List<Task> tasks;

    final Map<UUID, Integer> ceilings;

    final List<Task> ready = new ArrayList<>();
    final List<Task> blocked = new ArrayList<>();

    final Map<UUID, ResourceLock> locks = new LinkedHashMap<>();

    final Map<UUID, ResourceRequest> pendingRequests = new HashMap<>();
    final Map<UUID, ResourceRequestDTO> liveRequests = new LinkedHashMap<>();

    final Set<UUID> grantedRequests = new HashSet<>();

    int blockingEvents;
    int priorityInheritances;

    PcpState(List<Task> tasks, Map<UUID, Integer> ceilings) {
        this.tasks = tasks;
        this.ceilings = ceilings;
    }
}
