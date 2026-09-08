export interface ResourceRequest{
    id: string;
    name: string;
    resourceId: string;
    taskID: string;
    startOffset: number;
    duration: number;
    remainingTime: number;
}