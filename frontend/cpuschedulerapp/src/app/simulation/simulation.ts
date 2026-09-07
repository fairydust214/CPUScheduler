import { Component,signal } from '@angular/core';
import { Scenario } from '../models/scenario';
import { ActivatedRoute } from '@angular/router';
import { ScenarioService } from '../services/scenario.service';
import { SimulationService, SimulationResultDTO, SimulationType } from '../services/simulation.service';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-simulation',
  imports: [FormsModule, DecimalPipe],
  templateUrl: './simulation.html',
  styleUrl: './simulation.css',
})
export class Simulation {


  scenarioID: string = '';
  selectedAlgorithm: SimulationType = 'FCFS';
  quantum: number = 2;

  constructor (
    private route: ActivatedRoute,
    private scenarioService: ScenarioService,
    private simulationService: SimulationService
  ){}
  scenario = signal<Scenario>({
    id: '',
    name: '',
    tasks: [],
    resources: []
  });

  result = signal<SimulationResultDTO | null>(null);

  ngOnInit(){
    const scenarioId = this.route.snapshot.paramMap.get('id')

    if (scenarioId != null) {
      this.scenarioID = scenarioId;
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

  onRun():void {
    console.log(this.scenarioID)
    this.simulationService.runSimulation(this.scenarioID, this.selectedAlgorithm, this.quantum).subscribe({
      next: (simulationResult) => {
        this.result.set(simulationResult);
      },
      error : (err) => {
        console.error('Simulation failed', err);
      }
    })
  }


}
