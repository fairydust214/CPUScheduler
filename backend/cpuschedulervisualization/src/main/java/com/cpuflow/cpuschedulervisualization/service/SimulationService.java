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

        LinkedList<Task> queue = new LinkedList<>(scenario.getTasks());
        queue.sort(Comparator.comparing(Task::getArrivalTime)); //TODO Learn this

        for(Task task: queue){
            task.setRemainingTime(task.getDuration());
        }

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
                    timeline.add(new TimeNodeDTO(currentTime, null));
                    currentTime++;
                    continue;
                }
                current = queue.poll();
                startTimes.putIfAbsent(current.getId(),currentTime);
            }

            if (current != null){
                current.setRemainingTime(current.getRemainingTime()-1);
                utilizedTime++;
                timeline.add(new TimeNodeDTO(currentTime,
                        new TaskDTO(current.getId(),current.getName(),TaskStatus.RUNNING,current.getRemainingTime())));

                if(current.getRemainingTime() == 0){
                    completionTimes.put(current.getId(),currentTime+1);
                    current = null;
                }
            }
            currentTime++;
        }
        List<Task> allTasks = new ArrayList<>(scenario.getTasks());
        int totalWaitingTime = 0;
        int totalTurnaround = 0;
        int missedDeadlines = 0;

        for(Task task: allTasks){
            int start = startTimes.getOrDefault(task.getId(),0);
            int completion = completionTimes.getOrDefault(task.getId(),0);
            int waitingTimeTask = start - task.getArrivalTime();
            int turnaroundTimeTask = completion - task.getArrivalTime();
            totalWaitingTime += waitingTimeTask;
            totalTurnaround += turnaroundTimeTask;

            if(completion > task.getDeadline()){
                missedDeadlines++;
            }
        }
        int numberOfTasks = allTasks.size();

        this.calculateTelemetry(resultDTO,numberOfTasks,currentTime,utilizedTime,timeline,
                totalWaitingTime,totalTurnaround,missedDeadlines);
        return resultDTO;

    }

    public SimulationResultDTO createRoundRobin(UUID id, int quantum){
        Scenario scenario = this.scenarioRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario for Round Robin not found with id:" + id));

        LinkedList<Task> waitingToArrive = new LinkedList<>(scenario.getTasks());
        waitingToArrive.sort(Comparator.comparingInt(Task::getArrivalTime));

        for(Task task: waitingToArrive){
            task.setRemainingTime(task.getDuration());
        }

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
                readyQueue.add(waitingToArrive.poll());
            }
            //Idle state
            if(readyQueue.isEmpty()){
                timeLine.add(new TimeNodeDTO(currentTime,null));
                currentTime++;
                lastTaskId = null;
                continue;
            }
            Task current = readyQueue.poll();
            staringTimes.putIfAbsent(current.getId(),currentTime);

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
                                TaskStatus.RUNNING,current.getRemainingTime())));
                currentTime++;

                while(!waitingToArrive.isEmpty() && waitingToArrive.peek().getArrivalTime()<=currentTime){
                    readyQueue.add(waitingToArrive.poll());
                }

            }
            lastTaskId = current.getId();
            if(current.getRemainingTime() > 0){
                readyQueue.add(current);
                preemptions++;
            } else {
                completionTimes.put(current.getId(),currentTime);
            }
        }

        List<Task> allTasks = new LinkedList<>(scenario.getTasks());
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

            if(completion > task.getArrivalTime() + task.getDeadline()){
                missedDeadlines++;
            }
        }

        int numberOfTasks = allTasks.size();


        this.calculateTelemetry(resultDTO,numberOfTasks,currentTime,busyTicks,timeLine,
                totalWaiting,totalTurnaround,missedDeadlines);

        resultDTO.setContextSwitches(contextSwitches);
        resultDTO.setPreemptions(preemptions);
        resultDTO.setAvgResponseTime((double) totalResponseTime/numberOfTasks);

        return resultDTO;

    }

    public SimulationResultDTO createEDF(UUID id) {

        Scenario scenario = this.scenarioRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario for EDF not found with id:" + id));

        LinkedList<Task> arrivalQueue = new LinkedList<>(scenario.getTasks());
        arrivalQueue.sort(Comparator.comparing(Task::getArrivalTime));

        for(Task task: arrivalQueue){
            task.setRemainingTime(task.getDuration());
        }

        SimulationResultDTO resultDTO = new SimulationResultDTO();
        resultDTO.setAlgorithm(SimulationType.EDF);
        List<TimeNodeDTO> timeLine = new ArrayList<>();
        int currentTime = 0;
        int utilizedTime = 0;
        Task current = null;

        Map<UUID, Integer> startTimes = new HashMap<>();
        Map<UUID, Integer> completionTimes = new HashMap<>();

        PriorityQueue<Task> readyQueue = new PriorityQueue<>(Comparator.comparing(Task::getDeadline));

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
                timeLine.add(new TimeNodeDTO(currentTime,null));
                currentTime++;
                continue;
            }
            startTimes.putIfAbsent(current.getId(),currentTime);

            current.setRemainingTime(current.getRemainingTime()-1);
            utilizedTime++;
            timeLine.add(new TimeNodeDTO(currentTime, new TaskDTO(current.getId(),
                    current.getName(), TaskStatus.RUNNING,current.getRemainingTime())));
            currentTime++;

            if(current.getRemainingTime()==0){
                completionTimes.putIfAbsent(current.getId(),currentTime);
                current=null;
            }
        }

        List<Task> allTasks = new LinkedList<>(scenario.getTasks());
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

        LinkedList<Task> arrivalQueue = new LinkedList<>(scenario.getTasks());
        arrivalQueue.sort(Comparator.comparing(Task::getArrivalTime));

        for(Task task: arrivalQueue){
            task.setRemainingTime(task.getDuration());
        }

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

        PriorityQueue<Task> priorityQueue = new PriorityQueue<>(Comparator.comparing
                ((Task t)-> t.getDeadline() - t.getRemainingTime()).thenComparingInt(Task::getArrivalTime));

        while(!arrivalQueue.isEmpty() || !priorityQueue.isEmpty() || current != null){

            while(!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime() <=currentTime){
                priorityQueue.add(arrivalQueue.poll());
            }

            if(current != null){
                priorityQueue.add(current);
                current = null;
            }
            if(!priorityQueue.isEmpty()){
                current = priorityQueue.poll();
            }

            if(current == null){
                timeLine.add(new TimeNodeDTO(currentTime,null));
                currentTime++;
                lastTaskId =null;
                continue;
            }

            if(lastTaskId != null && !lastTaskId.equals(current.getId())){
                contextSwitches++;
                if(!lastTaskFinished){
                    preemptions++;
                }
            }
            int currentSlack = current.getDeadline() - currentTime - current.getRemainingTime();
            if(currentSlack < 0){
                negativeSlackEvents++;
                negativeSlackTasks.add(current.getId());
            }

            for(Task t: priorityQueue){
                int slack = t.getDeadline() - currentTime - t.getRemainingTime();
                if(slack < 0){
                    negativeSlackEvents++;
                    negativeSlackTasks.add(t.getId());
                }

            }

            startTimes.putIfAbsent(current.getId(),currentTime);

            current.setRemainingTime(current.getRemainingTime()-1);
            utilizedTime++;
            timeLine.add(new TimeNodeDTO(currentTime, new TaskDTO(current.getId(),
                    current.getName(), TaskStatus.RUNNING,current.getRemainingTime())));
            currentTime++;
            lastTaskId = current.getId();
            lastTaskFinished = false;
            if(current.getRemainingTime()==0){
                completionTimes.putIfAbsent(current.getId(),currentTime);
                current=null;
                lastTaskFinished = true;
            }
        }

        List<Task> allTasks = new LinkedList<>(scenario.getTasks());
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
        resultDTO.setAvgResponseTime((double) totalResponseTime / numberOfTasks);
        return resultDTO;
    }

    public SimulationResultDTO createPCP(UUID id){
        Scenario scenario = this.scenarioRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario for PCP not found with id:" +id));

        LinkedList<Task> queue = new LinkedList<>(scenario.getTasks());
        queue.sort(Comparator.comparing(Task::getArrivalTime));

        LinkedList<Resource> resourceList = new LinkedList<>(scenario.getResources());
        for(Resource resource: resourceList){
            resource.setPriorityCeiling(resource.findPriorityCeiling());
            resource.setStatus(ResourceStatus.FREE);
        }

        for(Task t: queue){
            t.setRemainingTime(t.getDuration());
            t.setEffectivePriority(t.getPriority());
            t.getResourceRequests().sort(Comparator.comparing(ResourceRequest::getStartOffset));
        }

        SimulationResultDTO resultDTO = new SimulationResultDTO();
        resultDTO.setAlgorithm(SimulationType.PC);

        PriorityQueue<Task> readyQueue = new PriorityQueue<>(Comparator.comparing(Task::getEffectivePriority).reversed().thenComparingInt(Task::getArrivalTime));
        List<TimeNodeDTO> timeline = new LinkedList<>();
        Task current = null;
        int currentTime = 0;

        int totalTasks = queue.size();
        int completedTasks=0;


        HighestPriorityInfo heighestInfo = new HighestPriorityInfo();
        List<ResourceRequestDTO> currentTakenResources = new LinkedList<>();
        Map<Resource, ResourceRequest> mapOfTakenResources = new HashMap<>();
        while(completedTasks < totalTasks){

            while(!queue.isEmpty() && queue.peek().getArrivalTime() <= currentTime){
                readyQueue.add(queue.poll());
            }
            if(current == null){
                if(readyQueue.isEmpty()){
                    timeline.add(new TimeNodeDTO(currentTime,null));
                    currentTime++;
                    continue;
                }
                current = readyQueue.poll();
            }
            if(current != null && !readyQueue.isEmpty() && readyQueue.peek().getEffectivePriority() > current.getEffectivePriority()){
                readyQueue.add(current);
                current = readyQueue.poll();

            }

            int executedTime = current.getDuration() - current.getRemainingTime();

            for(ResourceRequest rr: current.getResourceRequests()){
                int start = rr.getStartOffset();
                int end = start + rr.getDuration();

                if(executedTime == end){
                    freeResource(rr,currentTakenResources,mapOfTakenResources,current);
                    recalculateHeighestPriority(mapOfTakenResources,heighestInfo);
                }
                if(executedTime == start){
                    if(canAcquireLock(current,rr.getResource(),heighestInfo)){
                        rr.getResource().setStatus(ResourceStatus.TAKEN);
                        mapOfTakenResources.put(rr.getResource(),rr);
                        currentTakenResources.add(
                                new ResourceRequestDTO(rr.getId(),rr.getResource().getId(),current.getId(),rr.getStartOffset(),rr.getDuration()));
                        recalculateHeighestPriority(mapOfTakenResources,heighestInfo);
                    } else{
                        inheritPriority(current,rr.getResource(),mapOfTakenResources,heighestInfo, readyQueue);
                        readyQueue.add(current);
                        current=null;
                        break;
                    }
                }
            }

            if (current == null){
                continue;
            }

            current.setRemainingTime(current.getRemainingTime()-1);
            timeline.add(new TimeNodeDTO(currentTime,
                    new TaskDTOPC(current.getId(), current.getName(), TaskStatus.RUNNING,current.getRemainingTime(),new LinkedList<>(currentTakenResources))));

            currentTime++;

            if(current.getRemainingTime() == 0){
                for(ResourceRequest rr: current.getResourceRequests()){
                    freeResource(rr,currentTakenResources,mapOfTakenResources,current);
                }
                recalculateHeighestPriority(mapOfTakenResources,heighestInfo);
                completedTasks++;
                current =null;
            }


        }

        resultDTO.setTotalTime(currentTime);
        resultDTO.setTimeline(timeline);
        return resultDTO;
    }

    private void updatePriority(PriorityQueue<Task> readyQueue, Task task, int newPriority){
        readyQueue.remove(task);
        task.setEffectivePriority(newPriority);
        readyQueue.add(task);
    }

    private void recalculateHeighestPriority(Map<Resource, ResourceRequest> mapOfTakenResources, HighestPriorityInfo info){
        info.reset();

        for(Map.Entry<Resource, ResourceRequest> entry : mapOfTakenResources.entrySet()){
            Resource res = entry.getKey();
            ResourceRequest rr = entry.getValue();

            if(res.getPriorityCeiling()>info.getPriority()){
                info.setPriority(res.getPriorityCeiling());
                info.setResourceID(res.getId());
                info.setResourceRequestID(rr.getId());
                info.setTaskID(rr.getTask().getId());
            }
        }
    }

    private void freeResource(ResourceRequest rr, List<ResourceRequestDTO> currentTakenResources,Map<Resource, ResourceRequest> mapOfTakenResources, Task current){
        rr.getResource().setStatus(ResourceStatus.FREE);
        currentTakenResources.removeIf(rrDTO -> rrDTO.getId().equals(rr.getId()));
        mapOfTakenResources.remove(rr.getResource());
        if(mapOfTakenResources.values().stream().noneMatch(r -> r.getTask().getId().equals(current.getId()))){
            current.resetEffectivePriority();;
        }
        /*
        while(it.hasNext()){
            ResourceRequestDTO rrDTO = it.next();
            if(rrDTO.getId().equals(rr.getId())){
                it.remove();
                break;
            }
        }

         */
    }
    private void inheritPriority(Task blockedTask, Resource resource, Map<Resource,
            ResourceRequest> mapOfTakenResources, HighestPriorityInfo heighestInfo, PriorityQueue<Task> readyQueue){
        Task holder = null;
        if(resource.getStatus() == ResourceStatus.TAKEN){
            ResourceRequest holdingRR = mapOfTakenResources.get(resource);
            if(holdingRR != null){
                holder = holdingRR.getTask();
            }
        } else{
            //system ceiling
            for(ResourceRequest rr: mapOfTakenResources.values()){
                if(rr.getTask().getId().equals(heighestInfo.getTaskID())){
                    holder = rr.getTask();
                    break;
                }

            }
        }
        if(holder != null && blockedTask.getEffectivePriority() > holder.getEffectivePriority()){
            updatePriority(readyQueue,holder, blockedTask.getEffectivePriority());
        }

    }

    private boolean canAcquireLock(Task task, Resource resource, HighestPriorityInfo heighestInfo){
        if(resource.getStatus() != ResourceStatus.FREE){
            return false;
        }

        if( heighestInfo == null || heighestInfo.getPriority() == -1 || heighestInfo.getTaskID() == null){
            return true;
        }

        if(heighestInfo.getTaskID().equals(task.getId())){
            return true;
        }

        return task.getEffectivePriority() > heighestInfo.getPriority();

    }
    /*
    private boolean canAcquireLock(Task task, Resource resource, Map<Resource,ResourceRequest> mapOfTakenResources){
        if(resource.getStatus() != ResourceStatus.FREE){
            return false;
        }

        int highestCeilingOfOthers = -1;
        for(Map.Entry<Resource, ResourceRequest> entry: mapOfTakenResources.entrySet()){
            Resource heldResource = entry.getKey();
            ResourceRequest heldRR = entry.getValue();

            if(heldRR.getTask().getId().equals(task.getId())){
                continue;
            }
            highestCeilingOfOthers = Math.max(highestCeilingOfOthers, heldResource.getPriorityCeiling());
        }

        return task.getPriority() > highestCeilingOfOthers;

    }

     */

    private void calculateTelemetry(SimulationResultDTO resultDTO, int numberOfTasks,
                                    int currentTime,
                                    int busyTicks,
                                    List<TimeNodeDTO> timeLine,
                                    int totalWaiting,
                                    int totalTurnaround,
                                    int missedDeadlines){
        resultDTO.setTotalTime(currentTime);
        resultDTO.setTimeline(timeLine);
        resultDTO.setAvgWaitingTime((double) totalWaiting / numberOfTasks);
        resultDTO.setAvgTurnaroundTime((double) totalTurnaround / numberOfTasks);
        resultDTO.setCpuUtilization((double) busyTicks/currentTime *100);
        resultDTO.setMissedDeadlines(missedDeadlines);
    }


}
