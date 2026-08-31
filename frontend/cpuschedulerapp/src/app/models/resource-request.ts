export interface ResourceRequest{
    id: string;
    resourceId: string;
    taskID: string;
    startOffset: number;
    duration: number;
    remainingTime: number;
}