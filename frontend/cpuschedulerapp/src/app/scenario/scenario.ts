import { Component, OnInit, inject, signal } from '@angular/core';
import { ScenarioService } from '../services/scenario.service';
import { Scenario as ScenarioModel } from '../models/scenario';

@Component({
  selector: 'app-scenario',
  imports: [],
  templateUrl: './scenario.html',
  styleUrl: './scenario.css',
})
export class Scenario implements OnInit{

  private scenarioService = inject(ScenarioService);

  scenarios = signal<ScenarioModel[]>([]);
  
  ngOnInit():void{
    this.loadScenarios();
  }

  loadScenarios(): void {
    console.log("Scenario initialized");
    this.scenarioService.getAll().subscribe({
      next: (scenarios) => {
        this.scenarios.set(scenarios);
        console.log("Scenarios :", scenarios);
      },
      error: (error)=>{
        console.error("Failed to load scenarios:", error);
      } 
    });
  }
}
