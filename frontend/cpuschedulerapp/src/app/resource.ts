export interface Resource {
    id: string;              
    thread: string | null;      
    name: string;
    arrivalTime: number;
    deadline: number;
    duration: number; 
}