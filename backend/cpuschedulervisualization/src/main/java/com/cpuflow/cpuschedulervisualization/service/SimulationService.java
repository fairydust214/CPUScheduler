package com.cpuflow.cpuschedulervisualization.service;

import com.cpuflow.cpuschedulervisualization.DTOs.CRUD_DTOs.ScenarioDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.SimulationResultDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.SimulationType;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.TaskDTO;
import com.cpuflow.cpuschedulervisualization.DTOs.SimulationDTOs.TimeNodeDTO;
import com.cpuflow.cpuschedulervisualization.model.Scenario;
import com.cpuflow.cpuschedulervisualization.model.Task;
import com.cpuflow.cpuschedulervisualization.model.TaskStatus;
import com.cpuflow.cpuschedulervisualization.repo.ScenarioRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario for FCFS not found with id:"));

        LinkedList<Task> queue = new LinkedList<>(scenario.getTasks());
        queue.sort(Comparator.comparing(Task::getArrivalTime)); //TODO Learn this

        for(Task task: queue){
            task.setRemainingTime(task.getDuration());
        }

        SimulationResultDTO resultDTO = new SimulationResultDTO();
        resultDTO.setQuantum(null);
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
                startTimes.put(current.getId(),currentTime);
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
        resultDTO.setTotalTime(currentTime);

        resultDTO.setAvgWaitingTime((double) totalWaitingTime / numberOfTasks);
        resultDTO.setAvgTurnaroundTime((double) totalTurnaround / numberOfTasks);
        resultDTO.setCpuUtilization((double) utilizedTime / currentTime *100);
        resultDTO.setMissedDeadlines(missedDeadlines);
        resultDTO.setTimeline(timeline);
        return resultDTO;

    }

    public SimulationResultDTO createRoundRobin(UUID id, int quantum){
        Scenario scenario = this.scenarioRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario for Round Robin not found with id:"));

        LinkedList<Task> waitingToArrive = new LinkedList<>(scenario.getTasks());
        waitingToArrive.sort(Comparator.comparingInt(Task::getArrivalTime));

        for(Task task: waitingToArrive){
            task.setRemainingTime(task.getDuration());
        }

        SimulationResultDTO resultDTO = new SimulationResultDTO();
        resultDTO.setAlgorithm(SimulationType.RR);
        resultDTO.setQuantum(quantum);

        List<TimeNodeDTO> timeLine = new ArrayList<>();
        Map<UUID, Integer> staringTimes = new HashMap<>();
        Map<UUID, Integer> completionTimes = new HashMap<>();

        LinkedList<Task> readyQueue = new LinkedList<>();
        int currentTime = 0;
        int busyTicks = 0;

        while(!waitingToArrive.isEmpty() || !readyQueue.isEmpty()){
            while(!waitingToArrive.isEmpty() && waitingToArrive.peek().getArrivalTime()<= currentTime){
                readyQueue.add(waitingToArrive.poll());
            }
            //Idle state
            if(readyQueue.isEmpty()){
                timeLine.add(new TimeNodeDTO(currentTime,null));
                currentTime++;
                continue;
            }
            Task current = readyQueue.poll();
            staringTimes.putIfAbsent(current.getId(),currentTime);

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
            if(current.getRemainingTime() > 0){
                readyQueue.add(current);
            } else {
                completionTimes.put(current.getId(),currentTime);
            }
        }

        List<Task> allTasks = new LinkedList<>(scenario.getTasks());
        int totalWaiting = 0;
        int totalTurnaround = 0;
        int missedDeadlines = 0;

        for(Task task: allTasks){
            int completion = completionTimes.getOrDefault(task.getId(),0);
            totalWaiting+=(completion - task.getArrivalTime() - task.getDuration());
            totalTurnaround+= (completion - task.getArrivalTime());

            if(completion > task.getArrivalTime() + task.getDeadline()){
                missedDeadlines++;
            }
        }

        int numberOfTasks = allTasks.size();
        resultDTO.setTotalTime(currentTime);
        resultDTO.setTimeline(timeLine);
        resultDTO.setAvgWaitingTime((double) totalWaiting / numberOfTasks);
        resultDTO.setAvgTurnaroundTime((double) totalTurnaround / numberOfTasks);
        resultDTO.setCpuUtilization((double) busyTicks/currentTime *100);
        resultDTO.setMissedDeadlines(missedDeadlines);

        return resultDTO;

    }
    /*
    public SimulationResultDTO createFCFS(UUID id){
        Scenario scenario = this.scenarioRepo.findById(id).orElseThrow(()->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario for FCFS not found with id:"));

        SimulationResultDTO resultDTO = new SimulationResultDTO();
        resultDTO.setAlgorithm(SimulationType.FCFS);

        LinkedList<Task> listWithTasks = scenario.getTasks();
        listWithTasks.sort(Comparator.comparingInt(Task::getArrivalTime));
        int numberOfTasks = listWithTasks.size();

        int duration = scenario.getTasks().stream()
                .mapToInt(Task::getDuration)
                .sum();
        resultDTO.setTotalTime(duration);

        List<TimeNodeDTO> timeline = new LinkedList<>();

        for(int i=0; i< duration; i++){
            Task currentTask = listWithTasks.poll();
            TaskDTO taskDTO = new TaskDTO(currentTask.getId(),
                    currentTask.getName(),
                    TaskStatus.RUNNING,
                    currentTask.getRemainingTime()-1);

            currentTask.setRemainingTime(currentTask.getRemainingTime()-1);
            TimeNodeDTO curentTNDTO = new TimeNodeDTO(0,taskDTO);

            if(currentTask.getRemainingTime() >0){
                listWithTasks.add(currentTask);
            }
            timeline.add(curentTNDTO);
        }


        return resultDTO; // TODO

    }

     */


}
