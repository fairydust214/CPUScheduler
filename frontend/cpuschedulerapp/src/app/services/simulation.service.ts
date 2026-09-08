import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http'
import { Observable } from 'rxjs';
import { TaskStatus } from '../models/task-detailed';
import { ResourceRequest } from '../models/resource-request';
import { ResourceStatus } from '../models/resource-detailed';


export type SimulationType = 'LST' | 'EDF' | 'FCFS' | 'RR' | 'PC';

export interface TimeNodeDTO {
    time: number;
    runningTask : TaskDTO | null;
    currentTimeline: TaskDTO[];
}

export interface ResourceDTO {
    id: string;
    name: string;
    status: ResourceStatus;
    heldByTaskId: string | null;
}

export interface TaskDTO {
    id: string;
    name: string;
    status: TaskStatus;
    remainingTime: number;
    /** PCP only: the priority the task actually ran with in this tick, an inherited one included */
    effectivePriority?: number;
    /** PCP only: the resources this task holds while it runs in this tick */
    currentlyUsedResources?: ResourceDTO[];
    listWithResourceRequests?: ResourceRequest[];
}

export interface SimulationResultDTO {
    algorithm: string;
    totalTime: number;
    avgWaitingTime: number | null;
    avgTurnaroundTime: number | null;
    cpuUtilization: number | null;
    missedDeadlines: number | null;
    timeline: TimeNodeDTO[];
    quantum?: number;
    contextSwitches?: number;
    preemptions?: number;
    avgResponseTime?: number;
    negativeSlackEvents?: number;
    negativeSlackTasks?: number;
    blockingEvents?: number;
    priorityInheritances?: number;
}

@Injectable ({
    providedIn: 'root'
})

export class SimulationService{

    private readonly baseUrl = 'http://localhost:8080/simulation/scenario'

    constructor(private http: HttpClient){}

    private runFCFS(scenarioId: string): Observable<SimulationResultDTO>{
        return this.http.get<SimulationResultDTO>(`${this.baseUrl}/FCFS/${scenarioId}`);
    }

    private runRoundRobin(scenarioId: string, quantum?:number): Observable<SimulationResultDTO> {
        let params = new HttpParams;
        if(quantum != null){
            params = params.set('quantum', quantum.toString());
        }

        return this.http.get<SimulationResultDTO>(
            `${this.baseUrl}/RR/${scenarioId}`, {params}
        );
    }

    private runEDF(scenarioId: string): Observable<SimulationResultDTO>{
        return this.http.get<SimulationResultDTO>(`${this.baseUrl}/EDF/${scenarioId}`);
    }
    private runLST(scenarioId: string): Observable<SimulationResultDTO>{
        return this.http.get<SimulationResultDTO>(`${this.baseUrl}/LST/${scenarioId}`);
    }
    private runPCP(scenarioId: string): Observable<SimulationResultDTO>{
        return this.http.get<SimulationResultDTO>(`${this.baseUrl}/PCP/${scenarioId}`);
    }

    runSimulation(scenarioId: string, algorithm: SimulationType, quantum?: number): Observable<SimulationResultDTO>
    {
        switch(algorithm){
            case "FCFS" : return this.runFCFS(scenarioId);
            case "RR" : return this.runRoundRobin(scenarioId, quantum);
            case "EDF" : return this.runEDF(scenarioId);
            case "LST" : return this.runLST(scenarioId);
            case "PC" : return this.runPCP(scenarioId);
        }
    }
}