import { Component, ChangeDetectorRef } from '@angular/core';  // ← add ChangeDetectorRef
import { FormsModule } from '@angular/forms';
import { ScenarioService } from '../services/scenario.service';
import { RouterOutlet, Router } from '@angular/router';
import { GenerateResourceRequests } from '../generate-resource-requests/generate-resource-requests';

interface TaskRow {
  id: number;
  name: string;
  arrivalTime: number | null;
  duration: number | null;
  deadline: number | null;
  priority: number | null;
}

interface ResourceRow {
  id: number;
  name: string;
  priorityCealing: number | null;
}

interface Notification {
  message: string;
  type: 'success' | 'error';
}

@Component({
  selector: 'app-create-scenario',
  imports: [FormsModule, RouterOutlet],
  templateUrl: './create-scenario.html',
  styleUrl: './create-scenario.css',
})
export class CreateScenario {
  private taskIdCounter = 0;
  private resourceIdCounter = 0;
  private notificationTimeout: any;

  scenarioName = '';

  tasks: TaskRow[] = [
    this.createTask(),
    this.createTask(),
  ];

  resources: ResourceRow[] = [
    this.createResource(),
    this.createResource(),
  ];

  notification: Notification | null = null;

  constructor(
    private scenarioService: ScenarioService,
    private cdr: ChangeDetectorRef,
    private router: Router,
  ) {}

  onTaskChange(): void {
    const focused = document.activeElement as HTMLElement;
    this.manageRows(this.tasks, () => this.createTask(), (t) => this.isTaskEmpty(t));
    setTimeout(() => focused?.focus());
  }

  onResourceChange(): void {
    const focused = document.activeElement as HTMLElement;
    this.manageRows(this.resources, () => this.createResource(), (r) => this.isResourceEmpty(r));
    setTimeout(() => focused?.focus());
  }

  createScenario(): void {
    const filledTasks = this.tasks.filter(t => !this.isTaskEmpty(t));
    const filledResources = this.resources.filter(r => !this.isResourceEmpty(r));

    const payload = {
      name: this.scenarioName,
      tasks: filledTasks.map(t => ({
        name: t.name,
        arrivalTime: t.arrivalTime ?? 0,
        duration: t.duration ?? 0,
        deadline: t.deadline ?? 0,
        priority: t.priority,
        resourceRequests: [],
      })),
      resources: filledResources.map(r => ({
        name: r.name,
        priorityCealing: r.priorityCealing,
        status: 'FREE',
        resourceRequestDTOList: [],
      })),
    };

    const savedName = this.scenarioName;

    this.scenarioService.create(payload).subscribe({
      next: (response) => {
        const scenarioId = response.id;
        this.resetForm();                    // ← reset FIRST
        this.showNotification(               // ← notify SECOND
          `Scenario: ${savedName} created successfully!`,
          'success'
        );
        this.cdr.detectChanges();
        this.router.navigate(['/GenerateResourceRequests/', scenarioId]);
      },
      error: () => {
        this.showNotification(
          'Failed to create scenario.',
          'error'
        );
      },
    });
    
  }

  private createTask(): TaskRow {
    return {
      id: ++this.taskIdCounter,
      name: '',
      arrivalTime: null,
      duration: null,
      deadline: null,
      priority: null,
    };
  }

  private createResource(): ResourceRow {
    return {
      id: ++this.resourceIdCounter,
      name: '',
      priorityCealing: null,
    };
  }

  private isTaskEmpty(task: TaskRow): boolean {
    return task.name.trim() === ''
      && task.arrivalTime == null
      && task.duration == null
      && task.deadline == null
      && task.priority == null;
  }

  private isResourceEmpty(resource: ResourceRow): boolean {
    return resource.name.trim() === ''
      && resource.priorityCealing == null;
  }

  private showNotification(message: string, type: 'success' | 'error'): void {
    if (this.notificationTimeout) {
      clearTimeout(this.notificationTimeout);
    }

    this.notification = { message, type };
    this.cdr.detectChanges();  // ← FORCE Angular to see the change NOW

    this.notificationTimeout = setTimeout(() => {
      this.notification = null;
      this.notificationTimeout = null;
      this.cdr.detectChanges();  // ← FORCE Angular to see removal too
    }, 4000);
  }

  private manageRows<T>(
    rows: T[],
    createRow: () => T,
    isEmpty: (row: T) => boolean
  ): void {
    for (let i = rows.length - 1; i >= 0; i--) {
      if (isEmpty(rows[i])) {
        const hasFilledAfter = rows.slice(i + 1).some(r => !isEmpty(r));
        if (hasFilledAfter) {
          rows.splice(i, 1);
        }
      }
    }

    let trailingEmpty = 0;
    for (let i = rows.length - 1; i >= 0; i--) {
      if (isEmpty(rows[i])) trailingEmpty++;
      else break;
    }

    while (trailingEmpty > 2) { rows.pop(); trailingEmpty--; }
    while (trailingEmpty < 2) { rows.push(createRow()); trailingEmpty++; }
  }

  private resetForm(): void {
    this.scenarioName = '';
    this.tasks = [this.createTask(), this.createTask()];
    this.resources = [this.createResource(), this.createResource()];
  }
}