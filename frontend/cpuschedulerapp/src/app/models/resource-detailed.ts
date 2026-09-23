import { ResourceRequest } from "./resource-request";

export interface ResourceDetailed {
    id: string;
    name: string;
    priorityCealing: number | null;
    resourceRequestDTOList: ResourceRequest[];
    scenarioDTOID: string;
}
