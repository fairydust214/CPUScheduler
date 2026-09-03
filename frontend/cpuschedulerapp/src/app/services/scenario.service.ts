import {Injectable, inject} from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

import { Scenario } from "../models/scenario";



export interface ScenarioPayload {
  name: string;
  tasks: {
    name: string;
    arrivalTime: number;
    duration: number;
    deadline: number;
    priority: number | null;
    resourceRequests: any[];
  }[];
  resources: {
    name: string;
    status: string;
    resourceRequestDTOList: any[];
  }[];
}

@Injectable({
    providedIn: "root"
})


export class ScenarioService {

    constructor(private http: HttpClient) {}

    private apiUrl = "http://localhost:8080/scenario";
    getAll(): Observable<Scenario[]>{
        return this.http.get<Scenario[]>(this.apiUrl);
    }

    create(scenario: ScenarioPayload): Observable<any> {
        return this.http.post(this.apiUrl, scenario);
    }

    getById(id: string): Observable<Scenario>{
      return this.http.get<Scenario>(`${this.apiUrl}/${id}`)
    }

    createResourceRequests(id: string, payload: any): Observable<any> {
      return this.http.put(`${this.apiUrl}/${id}/resource-requests`, payload);
    }
}