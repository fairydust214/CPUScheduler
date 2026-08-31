import {ResourceRequest} from "./resource-request";

export interface TaskDetailed{
    id: string;
    name: string;
    status: TaskStatus;
    arrivalTime: number;
    duration: number;
    deadline: number;
    priority: number | null;
    resourceRequests: ResourceRequest[];
    scenarioDTOID: string;
}

export type TaskStatus =  "RUNNING" | "WAITING" | "BLOCKED";