import {Injectable, inject} from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

import { Scenario } from "../models/scenario";

@Injectable({
    providedIn: "root"
})

export class ScenarioService {
    private http = inject(HttpClient);

    private apiUrl = "http://localhost:8080/scenario";
    getAll(): Observable<Scenario[]>{
        return this.http.get<Scenario[]>(this.apiUrl);
    }
}