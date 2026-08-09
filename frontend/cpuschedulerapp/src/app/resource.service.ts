import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Resource } from "./resource";
import { environment } from "../environments/environment.development";

@Injectable({ providedIn: "root"})

export class ResourceService{

    private apiServerUrl = environment.apiBaseUrl;

    constructor(private http: HttpClient){}

    public getAllResources(): Observable<Resource[]>{
        return this.http.get<Resource[]>(`${this.apiServerUrl}/resource/all`);
    }

    public addResource(process: Resource): Observable<Resource> {
        return this.http.post<Resource>(`${this.apiServerUrl}/resource/add}`, process);
    }

    public deleteResource(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiServerUrl}/resource/delete/${id}`);
    }
}