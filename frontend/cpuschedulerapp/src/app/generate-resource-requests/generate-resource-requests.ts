import { Component, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ScenarioService } from '../services/scenario.service';
import { Scenario } from '../models/scenario';
import { TaskDetailed } from '../models/task-detailed';
import { ResourceDetailed } from '../models/resource-detailed';


interface ResourceRequestRow {
  id:number;
  name: String;
  startOffSet: number;
  duration: number;
  taskID: string;
  resource: string;
  
}

@Component({
  selector: 'app-generate-resource-requests',
  imports: [],
  templateUrl: './generate-resource-requests.html',
  styleUrl: './generate-resource-requests.css',
})

export class GenerateResourceRequests {

  scenario = signal<Scenario>({
    id: '',
    name: '',
    tasks: [],
    resources: []
  });
  constructor(
    private route: ActivatedRoute,
    private scenarioService: ScenarioService,
  ){}

  ngOnInit(){
    const scenarioId = this.route.snapshot.paramMap.get('id');
    console.log(scenarioId);

    if(scenarioId != null){
      this.scenarioService.getById(scenarioId).subscribe({
        next:(data) =>{
          this.scenario.set(data);
        },
        error:(err) => {
          console.error("Failed to load scenario", err);
        }
      }

      )
    }
    
  }
}
