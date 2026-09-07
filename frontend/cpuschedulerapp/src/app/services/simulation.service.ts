import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http'
import { Observable } from 'rxjs';
import { TaskStatus } from '../models/task-detailed';
import { ResourceRequest } from '../models/resource-request';


export type SimulationType = 'LST' | 'EDF' | 'FCFS' | 'RR' | 'PC';

export interface TimeNodeDTO {
    time: number;
    runningTask : TaskDTO | null;
}

export interface TaskDTO {
    id: string;
    name: string;
    status: TaskStatus;
    remainingTime: number;
    listWithResourceRequests?: ResourceRequest[];
}

export interface SimulationResultDTO {
    algorithm: SimulationType;
    totalTime: number;
    avgWaitingTime: number;
    avgTurnaroundTime: number;
    cpuUtilization: number;
    missedDeaedlines: number;
    timeline: TimeNodeDTO[];
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