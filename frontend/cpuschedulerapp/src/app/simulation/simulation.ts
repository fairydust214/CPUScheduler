import { Component,signal } from '@angular/core';
import { Scenario } from '../models/scenario';
import { ActivatedRoute } from '@angular/router';
import { ScenarioService } from '../services/scenario.service';

@Component({
  selector: 'app-simulation',
  imports: [],
  templateUrl: './simulation.html',
  styleUrl: './simulation.css',
})
export class Simulation {


  constructor (
    private route: ActivatedRoute,
    private scenarioService: ScenarioService,
  ){}
  scenario = signal<Scenario>({
    id: '',
    name: '',
    tasks: [],
    resources: []
  });

  ngOnInit(){
    const scenarioId = this.route.snapshot.paramMap.get('id')

    if (scenarioId != null) {
      this.scenarioService.getById(scenarioId).subscribe({
        next: (retrievedScenario) => {
          this.scenario.set(retrievedScenario);
        },
        error : (err) => {
          console.error("Failed to load scenario", err);
        }
      })
    }
  }


}
