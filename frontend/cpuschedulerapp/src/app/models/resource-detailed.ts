import { ResourceRequest } from "./resource-request";

export interface ResourceDetailed {
    id: string;
    name: string;
    priorityCealing: number | null;
    resourceRequestDTOList: ResourceRequest[];
    status: string;
    scenarioDTOID: string;
}

export type ResourceStatus = "TAKEN" | "FREE";