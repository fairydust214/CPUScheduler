import { Component, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ScenarioService } from '../services/scenario.service';
import {Router } from '@angular/router';

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
  imports: [FormsModule],
  templateUrl: './create-scenario.html',
  styleUrl: './create-scenario.css',
})
export class CreateScenario {
  private taskIdCounter = 0;
  private resourceIdCounter = 0;
  private notificationTimeout: any;

  scenarioName = '';
  usePriorityCeiling = false;

  tasks: TaskRow[] = [
    this.createTask(),
    this.createTask(),
  ];

  resources: ResourceRow[] = [
    this.createResource(),
    this.createResource(),
  ];

  notification: Notification | null = null;

  errors: string[] = [];
  private invalidFields = new Set<string>();

  constructor(
    private scenarioService: ScenarioService,
    private cdr: ChangeDetectorRef,
    private router: Router,
  ) {}

  fieldId(kind: 'task' | 'resource', rowId: number, field: string): string {
    return kind + '-' + rowId + '-' + field;
  }

  isInvalid(fieldId: string): boolean {
    return this.invalidFields.has(fieldId);
  }

  onScenarioNameChange(): void {
    this.invalidFields.delete('scenario-name');
  }

  onPriorityCeilingChange(): void {
    this.errors = [];
    this.invalidFields.clear();
  }

  onTaskChange(task: TaskRow): void {
    this.clearRowErrors('task', task.id);
    this.manageRows(this.tasks, () => this.createTask(), (t) => this.isTaskEmpty(t), task);
  }

  onResourceChange(resource: ResourceRow): void {
    this.clearRowErrors('resource', resource.id);
    this.manageRows(this.resources, () => this.createResource(), (r) => this.isResourceEmpty(r), resource);
  }

  createScenario(): void {
    if (!this.validate()) {
      this.showNotification('Please fill in the required fields.', 'error');
      this.focusFirstInvalidField();
      return;
    }

    const filledTasks = this.tasks.filter(t => !this.isTaskEmpty(t));
    const filledResources = this.resources.filter(r => !this.isResourceEmpty(r));

    const payload = {
      name: this.scenarioName.trim(),
      tasks: filledTasks.map(t => ({
        name: t.name.trim(),
        arrivalTime: t.arrivalTime ?? 0,
        duration: t.duration ?? 0,
        deadline: t.deadline ?? 0,
        priority: this.usePriorityCeiling ? t.priority : null,
        resourceRequests: [],
      })),
      resources: this.usePriorityCeiling ? filledResources.map(r => ({
        name: r.name.trim(),
        priorityCealing: r.priorityCealing,
        status: 'FREE',
        resourceRequestDTOList: [],
      })) : [],
    };

    const savedName = this.scenarioName.trim();
    const usePriorityCeiling = this.usePriorityCeiling;

    this.scenarioService.create(payload).subscribe({
      next: (response) => {
        const scenarioId = response.id;
        this.resetForm();                    // ← reset FIRST
        this.showNotification(               // ← notify SECOND
          `Scenario: ${savedName} created successfully!`,
          'success'
        );
        this.cdr.detectChanges();
        this.router.navigate(usePriorityCeiling
          ? ['/GenerateResourceRequests/', scenarioId]
          : ['/simulation/scenario/', scenarioId]);
      },
      error: () => {
        this.showNotification(
          'Failed to create scenario.',
          'error'
        );
      },
    });

  }

  private validate(): boolean {
    this.errors = [];
    this.invalidFields.clear();

    if (this.scenarioName.trim() === '') {
      this.errors.push('The scenario needs a name.');
      this.invalidFields.add('scenario-name');
    }

    const filledTasks = this.tasks.filter(t => !this.isTaskEmpty(t));
    if (filledTasks.length < 2) {
      this.errors.push('Add at least two tasks.');
      const nextTask = this.tasks.find(t => this.isTaskEmpty(t)) ?? this.tasks[0];
      this.invalidFields.add(this.fieldId('task', nextTask.id, 'name'));
    }

    this.tasks.forEach((task, index) => {
      if (this.isTaskEmpty(task)) return;

      const label = 'Task ' + (index + 1);
      this.requireText(task.name, label + ': a name is required.',
        this.fieldId('task', task.id, 'name'));
      this.requireNumber(task.arrivalTime, 0, label + ': an arrival time of 0 or more is required.',
        this.fieldId('task', task.id, 'arrivalTime'));
      this.requireNumber(task.duration, 1, label + ': a duration of at least 1 tick is required.',
        this.fieldId('task', task.id, 'duration'));
      this.requireNumber(task.deadline, 0, label + ': a deadline of 0 or more is required.',
        this.fieldId('task', task.id, 'deadline'));
      if (this.usePriorityCeiling) {
        this.requireNumber(task.priority, 0, label + ': a priority of 0 or more is required.',
          this.fieldId('task', task.id, 'priority'));
      }
    });

    if (this.usePriorityCeiling) {
      const filledResources = this.resources.filter(r => !this.isResourceEmpty(r));
      if (filledResources.length === 0) {
        this.errors.push('Add at least one resource.');
        this.invalidFields.add(this.fieldId('resource', this.resources[0].id, 'name'));
      }

      this.resources.forEach((resource, index) => {
        if (this.isResourceEmpty(resource)) return;

        this.requireText(resource.name, 'Resource ' + (index + 1) + ': a name is required.',
          this.fieldId('resource', resource.id, 'name'));
      });

      this.rejectDuplicateNames(this.resources, (r) => this.isResourceEmpty(r), 'resource');
    }

    this.rejectDuplicateNames(this.tasks, (t) => this.isTaskEmpty(t), 'task');

    return this.errors.length === 0;
  }

  private requireText(value: string, error: string, fieldId: string): void {
    if (value.trim() === '') {
      this.errors.push(error);
      this.invalidFields.add(fieldId);
    }
  }

  private requireNumber(value: number | null, minimum: number, error: string, fieldId: string): void {
    if (value == null || value < minimum) {
      this.errors.push(error);
      this.invalidFields.add(fieldId);
    }
  }

  private rejectDuplicateNames<T extends { id: number; name: string }>(
    rows: T[],
    isEmpty: (row: T) => boolean,
    kind: 'task' | 'resource'
  ): void {
    const byName = new Map<string, T[]>();

    for (const row of rows) {
      if (isEmpty(row) || row.name.trim() === '') continue;

      const name = row.name.trim();
      const sameName = byName.get(name) ?? [];
      sameName.push(row);
      byName.set(name, sameName);
    }

    for (const [name, sameName] of byName) {
      if (sameName.length < 2) continue;

      const what = kind === 'task' ? 'Task' : 'Resource';
      this.errors.push(what + ' names must be unique, "' + name + '" is used ' + sameName.length + ' times.');

      for (const row of sameName) {
        this.invalidFields.add(this.fieldId(kind, row.id, 'name'));
      }
    }
  }

  private focusFirstInvalidField(): void {
    const firstInvalid = this.invalidFields.values().next().value;
    if (!firstInvalid) return;

    setTimeout(() => document.getElementById(firstInvalid)?.focus());
  }

  private clearRowErrors(kind: 'task' | 'resource', rowId: number): void {
    const prefix = kind + '-' + rowId + '-';

    for (const fieldId of [...this.invalidFields]) {
      if (fieldId.startsWith(prefix)) {
        this.invalidFields.delete(fieldId);
      }
    }
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
    const emptyBesidesPriority = task.name.trim() === ''
      && task.arrivalTime == null
      && task.duration == null
      && task.deadline == null;

    if (!this.usePriorityCeiling) {
      return emptyBesidesPriority;
    }
    return emptyBesidesPriority && task.priority == null;
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
    isEmpty: (row: T) => boolean,
    keep: T
  ): void {
    for (let i = rows.length - 1; i >= 0; i--) {
      if (rows[i] !== keep && isEmpty(rows[i])) {
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

    while (trailingEmpty > 2 && rows[rows.length - 1] !== keep) {
      rows.pop();
      trailingEmpty--;
    }
    while (trailingEmpty < 2) { rows.push(createRow()); trailingEmpty++; }
  }

  private resetForm(): void {
    this.scenarioName = '';
    this.tasks = [this.createTask(), this.createTask()];
    this.resources = [this.createResource(), this.createResource()];
    this.usePriorityCeiling = false;
    this.errors = [];
    this.invalidFields.clear();
  }
}
