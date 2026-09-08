package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ResourceRequestDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ScenarioDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.*;
import com.cpuflow.cpuschedulervisualization.model.*;
import com.cpuflow.cpuschedulervisualization.repo.ScenarioRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Transactional
public class SimulationService {


    private final ScenarioRepo scenarioRepo;

    @Autowired
    public SimulationService(ScenarioRepo scenarioRepo) {
        this.scenarioRepo = scenarioRepo;
    }


    public SimulationResultDTO createFCFS(UUID id) {
        Scenario scenario = this.scenarioRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario for FCFS not found with id:" + id));

        List<Task> allTasks = new ArrayList<>(scenario.getTasks());
        SchedulingSupport.prepareForRun(allTasks);

        LinkedList<Task> queue = new LinkedList<>(allTasks);
        queue.sort(SchedulingSupport.ARRIVAL_ORDER);

        SimulationResultDTO resultDTO = new SimulationResultDTO();
        resultDTO.setAlgorithm(SimulationType.FCFS);

        List<TimeNodeDTO> timeline = new ArrayList<>();
        int currentTime = 0;
        int utilizedTime = 0;
        Task current = null;

        Map<UUID, Integer> startTimes = new HashMap<>();
        Map<UUID, Integer> completionTimes = new HashMap<>();

        while(!queue.isEmpty() || current != null){

            if(current == null && !queue.isEmpty()){
                if(queue.peek().getArrivalTime() > currentTime){
                    timeline.add(new TimeNodeDTO(currentTime, null,
                            convertQueue(SchedulingSupport.arrived(queue, currentTime))));
                    currentTime++;
                    continue;
                }

                current = queue.poll();
                startTimes.putIfAbsent(current.getId(), currentTime);

                if(SchedulingSupport.isFinished(current)){
                    completionTimes.putIfAbsent(current.getId(), currentTime);
                    current = null;
                    continue;
                }
            }

            if(current != null){
                current.setRemainingTime(current.getRemainingTime() - 1);
                utilizedTime++;
                timeline.add(new TimeNodeDTO(currentTime,
                        new TaskDTO(current.getId(), current.getName(), TaskStatus.RUNNING, current.getRemainingTime()),
                        convertQueue(SchedulingSupport.arrived(queue, currentTime))));

                if(SchedulingSupport.isFinished(current)){
                    completionTimes.putIfAbsent(current.getId(), currentTime + 1);
                    current = null;
                }
            }
            currentTime++;
        }

        int totalWaitingTime = 0;
        int totalTurnaround = 0;
        int missedDeadlines = 0;

        for(Task task: allTasks){
            int start = startTimes.getOrDefault(task.getId(), 0);
            int completion = completionTimes.getOrDefault(task.getId(), 0);
            totalWaitingTime += (start - task.getArrivalTime());
            totalTurnaround += (completion - task.getArrivalTime());

            if(completion > task.getDeadline()){
                missedDeadlines++;
            }
        }

        this.calculateTelemetry(resultDTO, allTasks.size(), currentTime, utilizedTime, timeline,
                totalWaitingTime, totalTurnaround, missedDeadlines);
        return resultDTO;

    }

    public SimulationResultDTO createRoundRobin(UUID id, int quantum){
        Scenario scenario = this.scenarioRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario for Round Robin not found with id:" + id));

        if(quantum < 1){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Round Robin needs a quantum of at least one tick but got " + quantum);
        }

        List<Task> allTasks = new ArrayList<>(scenario.getTasks());
        SchedulingSupport.prepareForRun(allTasks);

        LinkedList<Task> waitingToArrive = new LinkedList<>(allTasks);
        waitingToArrive.sort(SchedulingSupport.ARRIVAL_ORDER);

        SimularionResultRRDTO resultDTO = new SimularionResultRRDTO();
        resultDTO.setAlgorithm(SimulationType.RR);
        resultDTO.setQuantum(quantum);

        List<TimeNodeDTO> timeLine = new ArrayList<>();
        Map<UUID, Integer> staringTimes = new HashMap<>();
        Map<UUID, Integer> completionTimes = new HashMap<>();

        LinkedList<Task> readyQueue = new LinkedList<>();
        int currentTime = 0;
        int busyTicks = 0;
        int contextSwitches = 0;
        int preemptions = 0;
        UUID lastTaskId = null;


        while(!waitingToArrive.isEmpty() || !readyQueue.isEmpty()){
            while(!waitingToArrive.isEmpty() && waitingToArrive.peek().getArrivalTime()<= currentTime){
                Task waiting = waitingToArrive.poll();
                waiting.setStatus(TaskStatus.WAITING);
                readyQueue.add(waiting);
            }
            //Idle state
            if(readyQueue.isEmpty()){
                timeLine.add(new TimeNodeDTO(currentTime,null,convertQueue(readyQueue)));
                currentTime++;
                lastTaskId = null;
                continue;
            }
            Task current = readyQueue.poll();
            staringTimes.putIfAbsent(current.getId(),currentTime);

            if(SchedulingSupport.isFinished(current)){
                completionTimes.putIfAbsent(current.getId(), currentTime);
                continue;
            }

            //Context switch if lastTaskId different is different from current
            if(lastTaskId != null && !lastTaskId.equals(current.getId())){
                contextSwitches++;
            }

            int runTime = Math.min(quantum,current.getRemainingTime());

            for(int t = 0; t<runTime; t++){
                current.setRemainingTime((current.getRemainingTime()-1));
                busyTicks++;
                timeLine.add(new TimeNodeDTO(currentTime,
                        new TaskDTO(current.getId(),current.getName(),
                                TaskStatus.RUNNING,current.getRemainingTime()), convertQueue(readyQueue)));
                currentTime++;

                while(!waitingToArrive.isEmpty() && waitingToArrive.peek().getArrivalTime()<=currentTime){
                    Task waiting = waitingToArrive.poll();
                    waiting.setStatus(TaskStatus.WAITING);
                    readyQueue.add(waiting);
                }

            }
            lastTaskId = current.getId();
            if(current.getRemainingTime() > 0){
                current.setStatus(TaskStatus.WAITING);
                readyQueue.add(current);
                preemptions++;
            } else {
                completionTimes.putIfAbsent(current.getId(),currentTime);
            }
        }

        int totalWaiting = 0;
        int totalTurnaround = 0;
        int totalResponseTime = 0;
        int missedDeadlines = 0;

        for(Task task: allTasks){
            int start = staringTimes.getOrDefault(task.getId(),0);
            int completion = completionTimes.getOrDefault(task.getId(),0);

            totalWaiting+=(completion - task.getArrivalTime() - task.getDuration());
            totalTurnaround+= (completion - task.getArrivalTime());
            totalResponseTime += (start - task.getArrivalTime());

            if(completion > task.getDeadline()){
                missedDeadlines++;
            }
        }

        int numberOfTasks = allTasks.size();


        this.calculateTelemetry(resultDTO,numberOfTasks,currentTime,busyTicks,timeLine,
                totalWaiting,totalTurnaround,missedDeadlines);

        resultDTO.setContextSwitches(contextSwitches);
        resultDTO.setPreemptions(preemptions);
        resultDTO.setAvgResponseTime(SchedulingSupport.average(totalResponseTime, numberOfTasks));

        return resultDTO;

    }

    public SimulationResultDTO createEDF(UUID id) {

        Scenario scenario = this.scenarioRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario for EDF not found with id:" + id));

        List<Task> allTasks = new ArrayList<>(scenario.getTasks());
        SchedulingSupport.prepareForRun(allTasks);

        LinkedList<Task> arrivalQueue = new LinkedList<>(allTasks);
        arrivalQueue.sort(SchedulingSupport.ARRIVAL_ORDER);

        SimulationResultDTO resultDTO = new SimulationResultDTO();
        resultDTO.setAlgorithm(SimulationType.EDF);
        List<TimeNodeDTO> timeLine = new ArrayList<>();
        int currentTime = 0;
        int utilizedTime = 0;
        Task current = null;

        Map<UUID, Integer> startTimes = new HashMap<>();
        Map<UUID, Integer> completionTimes = new HashMap<>();

        PriorityQueue<Task> readyQueue = new PriorityQueue<>(SchedulingSupport.EARLIEST_DEADLINE_ORDER);

        while(!arrivalQueue.isEmpty() || !readyQueue.isEmpty() || current != null){

            while(!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime() <=currentTime){
                readyQueue.add(arrivalQueue.poll());
            }
            //preempt
            if(current != null && !readyQueue.isEmpty()){
                if(readyQueue.peek().getDeadline() < current.getDeadline()){
                    readyQueue.add(current);
                    current = readyQueue.poll();

                }
            }

            if(current == null && !readyQueue.isEmpty()){
                current = readyQueue.poll();
            }

            if(current == null){
                timeLine.add(new TimeNodeDTO(currentTime,null,convertQueue(readyQueue)));
                currentTime++;
                continue;
            }
            startTimes.putIfAbsent(current.getId(),currentTime);

            if(SchedulingSupport.isFinished(current)){
                completionTimes.putIfAbsent(current.getId(), currentTime);
                current = null;
                continue;
            }

            current.setRemainingTime(current.getRemainingTime()-1);
            utilizedTime++;
            timeLine.add(new TimeNodeDTO(currentTime, new TaskDTO(current.getId(),
                    current.getName(), TaskStatus.RUNNING,current.getRemainingTime()), convertQueue(readyQueue)));
            currentTime++;

            if(SchedulingSupport.isFinished(current)){
                completionTimes.putIfAbsent(current.getId(),currentTime);
                current=null;
            }
        }

        int totalWaiting = 0;
        int totalTurnaround = 0;
        int missedDeadlines = 0;

        for(Task task:allTasks){
            int completion = completionTimes.getOrDefault(task.getId(),0);
            totalWaiting += (completion -task.getArrivalTime() - task.getDuration());
            totalTurnaround += (completion - task.getArrivalTime());

            if(completion > task.getDeadline()){
                missedDeadlines++;
            }
        }
        int numberOfTasks = allTasks.size();

        this.calculateTelemetry(resultDTO,numberOfTasks,currentTime,utilizedTime,
                timeLine,totalWaiting,totalTurnaround,missedDeadlines);

        return resultDTO;

    }

    public SimulationResultDTO createLST(UUID uuid){
        Scenario scenario = this.scenarioRepo.findById(uuid).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario for LST not found with id:" + uuid));

        List<Task> allTasks = new ArrayList<>(scenario.getTasks());
        SchedulingSupport.prepareForRun(allTasks);

        LinkedList<Task> arrivalQueue = new LinkedList<>(allTasks);
        arrivalQueue.sort(SchedulingSupport.ARRIVAL_ORDER);

        SimulationResultLSTDTO resultDTO = new SimulationResultLSTDTO();
        resultDTO.setAlgorithm(SimulationType.LST);

        int currentTime = 0;
        int utilizedTime = 0;
        int contextSwitches = 0;
        int preemptions = 0;
        int negativeSlackEvents = 0;
        Task current = null;
        UUID lastTaskId = null;
        boolean lastTaskFinished = false;

        List<TimeNodeDTO> timeLine = new ArrayList<>();
        Map<UUID, Integer> startTimes = new HashMap<>();
        Map<UUID, Integer> completionTimes = new HashMap<>();
        Set<UUID> negativeSlackTasks = new HashSet<>();

        PriorityQueue<Task> priorityQueue = new PriorityQueue<>(SchedulingSupport.LEAST_SLACK_ORDER);

        while(!arrivalQueue.isEmpty() || !priorityQueue.isEmpty() || current != null){

            while(!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime() <=currentTime){
                priorityQueue.add(arrivalQueue.poll());
            }

            if(current != null && !priorityQueue.isEmpty()
                    && SchedulingSupport.slack(priorityQueue.peek(), currentTime)
                            < SchedulingSupport.slack(current, currentTime)){
                priorityQueue.add(current);
                current = priorityQueue.poll();
            }

            if(current == null && !priorityQueue.isEmpty()){
                current = priorityQueue.poll();
            }

            if(current == null){
                timeLine.add(new TimeNodeDTO(currentTime,null,convertQueue(priorityQueue)));
                currentTime++;
                lastTaskId =null;
                continue;
            }

            startTimes.putIfAbsent(current.getId(),currentTime);

            if(SchedulingSupport.isFinished(current)){
                completionTimes.putIfAbsent(current.getId(), currentTime);
                current = null;
                continue;
            }

            if(lastTaskId != null && !lastTaskId.equals(current.getId())){
                contextSwitches++;
                if(!lastTaskFinished){
                    preemptions++;
                }
            }

            if(SchedulingSupport.slack(current, currentTime) < 0){
                negativeSlackEvents++;
                negativeSlackTasks.add(current.getId());
            }

            for(Task t: priorityQueue){
                if(SchedulingSupport.slack(t, currentTime) < 0){
                    negativeSlackEvents++;
                    negativeSlackTasks.add(t.getId());
                }

            }

            current.setRemainingTime(current.getRemainingTime()-1);
            utilizedTime++;
            timeLine.add(new TimeNodeDTO(currentTime, new TaskDTO(current.getId(),
                    current.getName(), TaskStatus.RUNNING,current.getRemainingTime()),convertQueue(priorityQueue)));
            currentTime++;
            lastTaskId = current.getId();
            lastTaskFinished = false;
            if(SchedulingSupport.isFinished(current)){
                completionTimes.putIfAbsent(current.getId(),currentTime);
                current=null;
                lastTaskFinished = true;
            }
        }

        int totalWaiting = 0;
        int totalTurnaround = 0;
        int totalResponseTime = 0;
        int missedDeadlines = 0;

        for(Task task: allTasks){
            int start = startTimes.getOrDefault(task.getId(),0);
            int completion = completionTimes.getOrDefault(task.getId(),0);
            totalWaiting += (completion - task.getArrivalTime() - task.getDuration());
            totalTurnaround += (completion - task.getArrivalTime());
            totalResponseTime += (start - task.getArrivalTime());

            if(completion > task.getDeadline()){
                missedDeadlines++;
            }
        }

        int numberOfTasks = allTasks.size();
        this.calculateTelemetry(resultDTO, numberOfTasks, currentTime,utilizedTime,timeLine,totalWaiting,
                totalTurnaround,missedDeadlines);
        resultDTO.setContextSwitches(contextSwitches);
        resultDTO.setPreemptions(preemptions);
        resultDTO.setNegativeSlackEvents(negativeSlackEvents);
        resultDTO.setNegativeSlackTasks(negativeSlackTasks.size());
        resultDTO.setAvgResponseTime(SchedulingSupport.average(totalResponseTime, numberOfTasks));
        return resultDTO;
    }

    private static final int NO_CEILING = Integer.MIN_VALUE;


    private static final Comparator<Task> READY_ORDER =
            Comparator.comparingInt(SimulationService::effectivePriority).reversed()
                    .thenComparingInt(Task::getArrivalTime)
                    .thenComparing(task -> String.valueOf(task.getId()));

    public SimulationResultDTO createPCP(UUID id){
        Scenario scenario = this.scenarioRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario for PCP not found with id:" + id));

        SimulationResultPCDTO resultDTO = new SimulationResultPCDTO();
        resultDTO.setAlgorithm(SimulationType.PC);

        List<Task> allTasks = new ArrayList<>(scenario.getTasks());
        if(allTasks.isEmpty()){
            this.calculateTelemetry(resultDTO, 1, 0, 0, new ArrayList<>(), 0, 0, 0);
            resultDTO.setCpuUtilization(0.0);
            resultDTO.setAvgResponseTime(0.0);
            return resultDTO;
        }

        PcpState state = new PcpState(allTasks, this.computeCeilings(scenario, allTasks));

        LinkedList<Task> notArrivedYet = new LinkedList<>(allTasks);
        notArrivedYet.sort(Comparator.comparingInt(Task::getArrivalTime));

        for(Task task: allTasks){
            task.setRemainingTime(task.getDuration());
            task.setEffectivePriority(basePriority(task));
            task.setStatus(TaskStatus.WAITING);
            task.setBlockedOnResource(null);
            if(task.getResourceRequests() != null){
                task.getResourceRequests().sort(Comparator.comparingInt(ResourceRequest::getStartOffset)
                        .thenComparingInt(ResourceRequest::getDuration));
            }
        }

        List<TimeNodeDTO> timeline = new ArrayList<>();
        Map<UUID, Integer> startTimes = new HashMap<>();
        Map<UUID, Integer> completionTimes = new HashMap<>();

        int currentTime = 0;
        int busyTicks = 0;
        int contextSwitches = 0;
        int preemptions = 0;
        int completedTasks = 0;
        UUID lastTaskId = null;
        boolean lastTaskFinished = false;

        int timeLimit = simulationTimeLimit(allTasks);

        while(completedTasks < allTasks.size()){

            if(currentTime > timeLimit){
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "PCP simulation of scenario " + id + " did not finish within " + timeLimit
                                + " ticks. Check the scenario for resource requests that reach past the end of"
                                + " their task or for a request the protocol can never grant.");
            }

            while(!notArrivedYet.isEmpty() && notArrivedYet.peek().getArrivalTime() <= currentTime){
                Task arrived = notArrivedYet.poll();
                arrived.setStatus(TaskStatus.WAITING);
                state.ready.add(arrived);
            }

            this.unblockWhatIsPossible(state);

            Task current = this.selectRunnableTask(state);


            if(current == null){
                timeline.add(new TimeNodeDTO(currentTime, null, this.queueSnapshot(state, null)));
                currentTime++;
                lastTaskId = null;
                lastTaskFinished = false;
                continue;
            }

            if(lastTaskId != null && !lastTaskId.equals(current.getId())){
                contextSwitches++;

                if(!lastTaskFinished && !state.pendingRequests.containsKey(lastTaskId)){
                    preemptions++;
                }
            }

            current.setStatus(TaskStatus.RUNNING);
            startTimes.putIfAbsent(current.getId(), currentTime);
            current.setRemainingTime(current.getRemainingTime() - 1);
            busyTicks++;


            timeline.add(new TimeNodeDTO(currentTime,
                    new TaskDTOPC(current.getId(), current.getName(), TaskStatus.RUNNING,
                            current.getRemainingTime(), effectivePriority(current),
                            this.tickCriticalSections(state, current),
                            this.heldResources(state, current)),
                    this.queueSnapshot(state, current)));

            currentTime++;
            lastTaskId = current.getId();
            lastTaskFinished = false;

            this.releaseFinishedCriticalSections(state, current);

            if(current.getRemainingTime() == 0){
                completionTimes.put(current.getId(), currentTime);
                this.releaseAllLocks(state, current);
                state.ready.remove(current);
                completedTasks++;
                lastTaskFinished = true;
            }
        }

        int totalWaiting = 0;
        int totalTurnaround = 0;
        int totalResponseTime = 0;
        int missedDeadlines = 0;

        for(Task task: allTasks){
            int start = startTimes.getOrDefault(task.getId(), 0);
            int completion = completionTimes.getOrDefault(task.getId(), 0);
            totalWaiting += (completion - task.getArrivalTime() - task.getDuration());
            totalTurnaround += (completion - task.getArrivalTime());
            totalResponseTime += (start - task.getArrivalTime());

            if(completion > task.getDeadline()){
                missedDeadlines++;
            }
        }

        int numberOfTasks = allTasks.size();
        this.calculateTelemetry(resultDTO, numberOfTasks, currentTime, busyTicks, timeline,
                totalWaiting, totalTurnaround, missedDeadlines);

        resultDTO.setContextSwitches(contextSwitches);
        resultDTO.setPreemptions(preemptions);
        resultDTO.setBlockingEvents(state.blockingEvents);
        resultDTO.setPriorityInheritances(state.priorityInheritances);
        resultDTO.setAvgResponseTime((double) totalResponseTime / numberOfTasks);

        return resultDTO;
    }


    private Task selectRunnableTask(PcpState state){
        while(true){
            Task candidate = null;

            for(Task task: state.ready){
                if(candidate == null || READY_ORDER.compare(task, candidate) < 0){
                    candidate = task;
                }
            }

            if(candidate == null){
                return null;
            }
            if(this.acquireDueLocks(state, candidate)){
                return candidate;
            }
        }
    }

    private boolean acquireDueLocks(PcpState state, Task task){
        int executedTime = task.getDuration() - task.getRemainingTime();

        for(ResourceRequest request: requestsOf(task)){
            Resource resource = request.getResource();

            if(resource == null || request.getDuration() <= 0){
                continue;
            }
            if(request.getStartOffset() != executedTime || state.grantedRequests.contains(request.getId())){
                continue;
            }

            if(this.canAcquireLock(state, task, resource)){
                this.grantLock(state, task, request);
                continue;
            }

            Task blocker = this.findBlockingTask(state, task, resource);
            boolean donatesPriority = blocker != null && effectivePriority(blocker) < effectivePriority(task);

            state.blockingEvents++;
            state.ready.remove(task);
            state.blocked.add(task);
            state.pendingRequests.put(task.getId(), request);
            task.setStatus(TaskStatus.BLOCKED);
            task.setBlockedOnResource(resource);

            this.recomputeEffectivePriorities(state);
            if(donatesPriority){
                state.priorityInheritances++;
            }
            return false;
        }
        return true;
    }


    private boolean canAcquireLock(PcpState state, Task task, Resource resource){
        ResourceLock existing = state.locks.get(resource.getId());

        if(existing != null){
            return existing.holder.getId().equals(task.getId());
        }

        int systemCeiling = this.systemCeilingExcluding(state, task);
        return systemCeiling == NO_CEILING || effectivePriority(task) > systemCeiling;
    }


    private int systemCeilingExcluding(PcpState state, Task task){
        int systemCeiling = NO_CEILING;

        for(ResourceLock lock: state.locks.values()){
            if(lock.holder.getId().equals(task.getId())){
                continue;
            }
            systemCeiling = Math.max(systemCeiling, this.ceilingOf(state, lock.resource));
        }
        return systemCeiling;
    }


    private Task findBlockingTask(PcpState state, Task blockedTask, Resource resource){
        ResourceLock direct = state.locks.get(resource.getId());
        if(direct != null){
            return direct.holder;
        }

        Task blocker = null;
        int highestCeiling = NO_CEILING;

        for(ResourceLock lock: state.locks.values()){
            if(lock.holder.getId().equals(blockedTask.getId())){
                continue;
            }
            int ceiling = this.ceilingOf(state, lock.resource);
            if(blocker == null || ceiling > highestCeiling){
                highestCeiling = ceiling;
                blocker = lock.holder;
            }
        }
        return blocker;
    }

    private void grantLock(PcpState state, Task task, ResourceRequest request){
        Resource resource = request.getResource();

        resource.setStatus(ResourceStatus.TAKEN);
        state.locks.put(resource.getId(), new ResourceLock(task, request, resource));
        state.grantedRequests.add(request.getId());

        ResourceRequestDTO liveRequest = new ResourceRequestDTO(request.getId(), request.getName(),
                resource.getId(), task.getId(), request.getStartOffset(), request.getDuration());
        liveRequest.setRemainingTime(request.getDuration());
        state.liveRequests.put(request.getId(), liveRequest);

        this.recomputeEffectivePriorities(state);
    }

    private void releaseFinishedCriticalSections(PcpState state, Task task){
        int executedTime = task.getDuration() - task.getRemainingTime();

        for(ResourceLock lock: new ArrayList<>(state.locks.values())){
            if(!lock.holder.getId().equals(task.getId())){
                continue;
            }
            if(executedTime >= lock.request.getStartOffset() + lock.request.getDuration()){
                this.releaseLock(state, lock);
            }
        }
    }

    private void releaseAllLocks(PcpState state, Task task){
        for(ResourceLock lock: new ArrayList<>(state.locks.values())){
            if(lock.holder.getId().equals(task.getId())){
                this.releaseLock(state, lock);
            }
        }
    }

    private void releaseLock(PcpState state, ResourceLock lock){
        state.locks.remove(lock.resource.getId());
        state.grantedRequests.remove(lock.request.getId());
        state.liveRequests.remove(lock.request.getId());
        lock.resource.setStatus(ResourceStatus.FREE);

        this.recomputeEffectivePriorities(state);
    }

    private void unblockWhatIsPossible(PcpState state){
        boolean unblockedSomething = true;

        while(unblockedSomething){
            unblockedSomething = false;

            List<Task> candidates = new ArrayList<>(state.blocked);
            candidates.sort(READY_ORDER);

            for(Task blockedTask: candidates){
                ResourceRequest pending = state.pendingRequests.get(blockedTask.getId());
                boolean needsLock = pending != null && pending.getResource() != null;

                if(needsLock && !this.canAcquireLock(state, blockedTask, pending.getResource())){
                    continue;
                }

                state.pendingRequests.remove(blockedTask.getId());
                state.blocked.remove(blockedTask);
                blockedTask.setBlockedOnResource(null);
                blockedTask.setStatus(TaskStatus.WAITING);
                state.ready.add(blockedTask);

                if(needsLock){
                    this.grantLock(state, blockedTask, pending);
                } else {
                    this.recomputeEffectivePriorities(state);
                }


                unblockedSomething = true;
                break;
            }
        }
    }

    private void recomputeEffectivePriorities(PcpState state){
        for(Task task: state.tasks){
            task.setEffectivePriority(basePriority(task));
        }

        boolean changed = true;
        int guard = state.tasks.size() + 1;

        while(changed && guard-- > 0){
            changed = false;

            for(Task blockedTask: state.blocked){
                ResourceRequest pending = state.pendingRequests.get(blockedTask.getId());
                if(pending == null || pending.getResource() == null){
                    continue;
                }

                Task blocker = this.findBlockingTask(state, blockedTask, pending.getResource());
                if(blocker != null && effectivePriority(blocker) < effectivePriority(blockedTask)){
                    blocker.setEffectivePriority(effectivePriority(blockedTask));
                    changed = true;
                }
            }
        }
    }


    private Map<UUID, Integer> computeCeilings(Scenario scenario, List<Task> tasks){
        Map<UUID, Integer> ceilings = new HashMap<>();

        for(Task task: tasks){
            for(ResourceRequest request: requestsOf(task)){
                if(request.getResource() == null){
                    continue;
                }
                ceilings.merge(request.getResource().getId(), basePriority(task), Math::max);
            }
        }

        for(Resource resource: scenario.getResources()){
            resource.setStatus(ResourceStatus.FREE);
            Integer ceiling = ceilings.get(resource.getId());
            if(ceiling != null){
                resource.setPriorityCeiling(ceiling);
            }
        }
        return ceilings;
    }

    private List<ResourceRequestDTO> tickCriticalSections(PcpState state, Task current){
        List<ResourceRequestDTO> snapshot = new LinkedList<>();

        for(ResourceRequestDTO liveRequest: state.liveRequests.values()){
            if(liveRequest.getTaskID().equals(current.getId()) && liveRequest.getRemainingTime() > 0){
                liveRequest.setRemainingTime(liveRequest.getRemainingTime() - 1);
            }

            ResourceRequestDTO copy = new ResourceRequestDTO(liveRequest.getId(), liveRequest.getName(),
                    liveRequest.getResourceId(), liveRequest.getTaskID(),
                    liveRequest.getStartOffset(), liveRequest.getDuration());
            copy.setRemainingTime(liveRequest.getRemainingTime());
            snapshot.add(copy);
        }
        return snapshot;
    }

    private List<ResourceDTO> heldResources(PcpState state, Task task){
        List<ResourceDTO> held = new LinkedList<>();

        for(ResourceLock lock: state.locks.values()){
            if(lock.holder.getId().equals(task.getId())){
                held.add(new ResourceDTO(lock.resource.getId(), lock.resource.getName(),
                        lock.resource.getStatus(), task.getId()));
            }
        }
        return held;
    }

    private List<TaskDTO> queueSnapshot(PcpState state, Task running){
        List<Task> waiting = new ArrayList<>(state.ready);
        waiting.remove(running);
        waiting.sort(READY_ORDER);

        List<Task> blocked = new ArrayList<>(state.blocked);
        blocked.sort(READY_ORDER);

        List<TaskDTO> snapshot = new LinkedList<>();

        for(Task task: waiting){
            task.setStatus(TaskStatus.WAITING);
            snapshot.add(new TaskDTO(task.getId(), task.getName(), TaskStatus.WAITING, task.getRemainingTime()));
        }
        for(Task task: blocked){
            task.setStatus(TaskStatus.BLOCKED);
            snapshot.add(new TaskDTO(task.getId(), task.getName(), TaskStatus.BLOCKED, task.getRemainingTime()));
        }
        return snapshot;
    }

    private int simulationTimeLimit(List<Task> tasks){
        int totalDuration = 0;
        int lastArrival = 0;

        for(Task task: tasks){
            totalDuration += Math.max(0, task.getDuration());
            lastArrival = Math.max(lastArrival, task.getArrivalTime());
        }
        return lastArrival + totalDuration + tasks.size() + 1;
    }

    private int ceilingOf(PcpState state, Resource resource){
        return state.ceilings.getOrDefault(resource.getId(), NO_CEILING);
    }

    private static List<ResourceRequest> requestsOf(Task task){
        List<ResourceRequest> requests = task.getResourceRequests();
        return requests == null ? Collections.emptyList() : requests;
    }

    private static int basePriority(Task task){
        Integer priority = task.getPriority();
        return priority != null ? priority : 0;
    }

    private static int effectivePriority(Task task){
        Integer priority = task.getEffectivePriority();
        return priority != null ? priority : basePriority(task);
    }

    private void calculateTelemetry(SimulationResultDTO resultDTO, int numberOfTasks,
                                    int currentTime,
                                    int busyTicks,
                                    List<TimeNodeDTO> timeLine,
                                    int totalWaiting,
                                    int totalTurnaround,
                                    int missedDeadlines){
        resultDTO.setTotalTime(currentTime);
        resultDTO.setTimeline(timeLine);
        resultDTO.setAvgWaitingTime(SchedulingSupport.average(totalWaiting, numberOfTasks));
        resultDTO.setAvgTurnaroundTime(SchedulingSupport.average(totalTurnaround, numberOfTasks));
        resultDTO.setCpuUtilization(SchedulingSupport.percentage(busyTicks, currentTime));
        resultDTO.setMissedDeadlines(missedDeadlines);
    }

    private List<TaskDTO> convertQueue(Collection<Task> queue){
        List<TaskDTO> result = new LinkedList<>();

        for(Task t: queue){
            result.add(new TaskDTO(t.getId(),t.getName(),t.getStatus(),t.getRemainingTime()));
        }

        return result;
    }

}
