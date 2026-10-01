import { Component, OnInit, inject, signal } from '@angular/core';
import { ScenarioService } from '../services/scenario.service';
import { Scenario as ScenarioModel } from '../models/scenario';
import {RouterLink} from '@angular/router';

interface Notification {
  message: string;
  type: 'success' | 'error';
}

@Component({
  selector: 'app-scenario',
  imports: [RouterLink],
  templateUrl: './scenario.html',
  styleUrl: './scenario.css',
})
export class Scenario implements OnInit{

  private scenarioService = inject(ScenarioService);

  scenarios = signal<ScenarioModel[]>([]);
  scenarioToDelete = signal<ScenarioModel | null>(null);
  notification = signal<Notification | null>(null);

  private notificationTimeout: any;

  ngOnInit():void{
    this.loadScenarios();
  }

  loadScenarios(): void {
    console.log("Scenario initialized");
    this.scenarioService.getAll().subscribe({
      next: (scenarios) => {
        this.scenarios.set(
          [...scenarios].sort((a, b) =>
            a.name.localeCompare(b.name, undefined, { sensitivity: 'base', numeric: true })
          )
        );
        console.log("Scenarios :", scenarios);
      },
      error: (error)=>{
        console.error("Failed to load scenarios:", error);
      } 
    });
  }

  askDelete(scenario: ScenarioModel): void {
    this.scenarioToDelete.set(scenario);
  }

  cancelDelete(): void {
    this.scenarioToDelete.set(null);
  }

  confirmDelete(scenario: ScenarioModel): void {
    this.scenarioToDelete.set(null);

    this.scenarioService.delete(scenario.id).subscribe({
      next: () => {
        this.scenarios.update(list => list.filter(s => s.id !== scenario.id));
        this.showNotification(`Scenario: ${scenario.name} was deleted`, 'success');
      },
      error: (error) => {
        console.error("Failed to delete scenario:", error);
        this.showNotification(`Error: ${this.errorMessage(error)}`, 'error');
      }
    });
  }

  private errorMessage(error: any): string {
    return error?.error?.message
      ?? error?.error?.error
      ?? error?.message
      ?? 'the scenario could not be deleted';
  }

  private showNotification(message: string, type: 'success' | 'error'): void {
    if (this.notificationTimeout) {
      clearTimeout(this.notificationTimeout);
    }

    this.notification.set({ message, type });

    this.notificationTimeout = setTimeout(() => {
      this.notification.set(null);
      this.notificationTimeout = null;
    }, 4000);
  }
}
