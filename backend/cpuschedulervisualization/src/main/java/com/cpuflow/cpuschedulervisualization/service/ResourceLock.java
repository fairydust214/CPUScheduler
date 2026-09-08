package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.model.Resource;
import com.cpuflow.cpuschedulervisualization.model.ResourceRequest;
import com.cpuflow.cpuschedulervisualization.model.Task;

final class ResourceLock {

    final Task holder;
    final ResourceRequest request;
    final Resource resource;

    ResourceLock(Task holder, ResourceRequest request, Resource resource) {
        this.holder = holder;
        this.request = request;
        this.resource = resource;
    }
}
