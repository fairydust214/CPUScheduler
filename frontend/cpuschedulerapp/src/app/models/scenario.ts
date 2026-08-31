import {TaskDetailed} from "./task-detailed";
import {ResourceDetailed} from "./resource-detailed";

export interface Scenario{
    id: string;
    name: string;
    tasks: TaskDetailed[];
    resources:ResourceDetailed[];
}