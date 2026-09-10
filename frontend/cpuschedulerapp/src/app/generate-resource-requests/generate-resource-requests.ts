import { ChangeDetectorRef, Component, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ScenarioService } from '../services/scenario.service';
import { Scenario } from '../models/scenario';
import { FormsModule } from '@angular/forms';

interface ResourceRequestRow {
  id: number;
  name: string;
  startOffSet: number | null;
  duration: number | null;
  taskID: string;
  resourceID: string;
}

interface Notification {
  message: string;
  type: 'success' | 'error';
}

@Component({
  selector: 'app-generate-resource-requests',
  imports: [FormsModule],
  templateUrl: './generate-resource-requests.html',
  styleUrl: './generate-resource-requests.css',
})
export class GenerateResourceRequests {

  private resourceRequestIDCounter = 0;
  private notificationTimeout: any;

  resourceRequests: ResourceRequestRow[] = [
    this.createResourceRequest(),
    this.createResourceRequest()
  ];

  scenario = signal<Scenario>({
    id: '',
    name: '',
    tasks: [],
    resources: []
  });

  notification: Notification | null = null;

  errors: string[] = [];
  private invalidFields = new Set<string>();

  constructor(
    private route: ActivatedRoute,
    private scenarioService: ScenarioService,
    private cdr: ChangeDetectorRef,
    private router: Router,
  ) {}

  ngOnInit() {
    const scenarioId = this.route.snapshot.paramMap.get('id');
    if (scenarioId != null) {
      this.scenarioService.getById(scenarioId).subscribe({
        next: (data) => {
          this.scenario.set(data);
        },
        error: (err) => {
          console.error("Failed to load scenario", err);
        }
      });
    }
  }

  fieldId(rowId: number, field: string): string {
    return 'request-' + rowId + '-' + field;
  }

  isInvalid(fieldId: string): boolean {
    return this.invalidFields.has(fieldId);
  }

  onResourceRequestChange(row: ResourceRequestRow): void {
    this.clearRowErrors(row.id);
    this.manageRows(
      this.resourceRequests,
      () => this.createResourceRequest(),
      (r) => this.isResourceRequestEmpty(r),
      row
    );
  }

  createResourceRequests(): void {
    const scenarioData = this.scenario();
    if (!scenarioData) return;

    if (!this.validate()) {
      this.showNotification('Please complete every resource request.', 'error');
      this.focusFirstInvalidField();
      return;
    }

    const filledRequests = this.resourceRequests.filter(
      r => !this.isResourceRequestEmpty(r)
    );

    const payload = {
      id: scenarioData.id,
      name: scenarioData.name,
      tasks: scenarioData.tasks.map(task => ({
        id: task.id,
        name: task.name,
        status: null,
        arrivalTime: task.arrivalTime,
        duration: task.duration,
        deadline: task.deadline,
        priority: task.priority,
        resourceRequests: filledRequests
          .filter(rr => rr.taskID === task.id)
          .map(rr => ({
            name: rr.name.trim(),
            resourceId: rr.resourceID,
            taskID: rr.taskID,
            startOffset: rr.startOffSet,
            duration: rr.duration,
          })),
        scenarioDTOID: scenarioData.id,
      })),
      resources: scenarioData.resources.map(resource => ({
        id: resource.id,
        name: resource.name,
        priorityCealing: resource.priorityCealing,
        resourceRequestDTOList: filledRequests
          .filter(rr => rr.resourceID === resource.id)
          .map(rr => ({
            name: rr.name.trim(),
            resourceId: rr.resourceID,
            taskID: rr.taskID,
            startOffset: rr.startOffSet,
            duration: rr.duration,
          })),
        status: null,
        scenarioDTOID: scenarioData.id,
      })),
    };


    this.scenarioService.createResourceRequests(scenarioData.id, payload).subscribe({
      next: (response) => {
        console.log('Resource requests created', response);
        this.router.navigate(['/simulation/scenario/', scenarioData.id]);
      },
      error: (err) => {
        console.error('Failed to create resource requests', err);
        this.showNotification('Failed to create the resource requests.', 'error');
      },
    });
  }

  private validate(): boolean {
    this.errors = [];
    this.invalidFields.clear();

    const filledRequests = this.resourceRequests.filter(r => !this.isResourceRequestEmpty(r));
    if (filledRequests.length === 0) {
      this.errors.push('Add at least one resource request.');
      this.invalidFields.add(this.fieldId(this.resourceRequests[0].id, 'name'));
    }

    this.resourceRequests.forEach((row, index) => {
      if (this.isResourceRequestEmpty(row)) return;

      const label = 'Resource request ' + (index + 1);

      if (row.name.trim() === '') {
        this.addError(label + ': a name is required.', this.fieldId(row.id, 'name'));
      }
      if (row.startOffSet == null || row.startOffSet < 0) {
        this.addError(label + ': a start offset of 0 or more is required.',
          this.fieldId(row.id, 'startOffSet'));
      }
      if (row.duration == null || row.duration < 1) {
        this.addError(label + ': a duration of at least 1 tick is required.',
          this.fieldId(row.id, 'duration'));
      }
      if (row.taskID === '') {
        this.addError(label + ': choose a task.', this.fieldId(row.id, 'taskID'));
      }
      if (row.resourceID === '') {
        this.addError(label + ': choose a resource to use.', this.fieldId(row.id, 'resourceID'));
      }

      this.rejectRequestPastEndOfTask(row, label);
    });

    return this.errors.length === 0;
  }

  /**
   * A critical section that reaches past the end of its task can never be released, which the
   * Priority Ceiling simulation rejects with an error instead of a timeline.
   */
  private rejectRequestPastEndOfTask(row: ResourceRequestRow, label: string): void {
    if (row.taskID === '' || row.startOffSet == null || row.duration == null) return;
    if (row.startOffSet < 0 || row.duration < 1) return;

    const task = this.scenario().tasks.find(t => t.id === row.taskID);
    if (!task) return;

    if (row.startOffSet + row.duration > task.duration) {
      this.addError(label + ': it runs to tick ' + (row.startOffSet + row.duration)
        + ' of ' + task.name + ', which only runs for ' + task.duration + ' ticks. ',
        this.fieldId(row.id, 'startOffSet'));
      this.invalidFields.add(this.fieldId(row.id, 'duration'));
    }
  }

  private addError(message: string, fieldId: string): void {
    this.errors.push(message);
    this.invalidFields.add(fieldId);
  }

  private focusFirstInvalidField(): void {
    const firstInvalid = this.invalidFields.values().next().value;
    if (!firstInvalid) return;

    setTimeout(() => document.getElementById(firstInvalid)?.focus());
  }

  private clearRowErrors(rowId: number): void {
    const prefix = 'request-' + rowId + '-';

    for (const fieldId of [...this.invalidFields]) {
      if (fieldId.startsWith(prefix)) {
        this.invalidFields.delete(fieldId);
      }
    }
  }

  private showNotification(message: string, type: 'success' | 'error'): void {
    if (this.notificationTimeout) {
      clearTimeout(this.notificationTimeout);
    }

    this.notification = { message, type };
    this.cdr.detectChanges();

    this.notificationTimeout = setTimeout(() => {
      this.notification = null;
      this.notificationTimeout = null;
      this.cdr.detectChanges();
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

  private createResourceRequest(): ResourceRequestRow {
    return {
      id: ++this.resourceRequestIDCounter,
      name: "",
      startOffSet: null,
      duration: null,
      taskID: "",
      resourceID:"",
    };
  }

  private isResourceRequestEmpty(r: ResourceRequestRow): boolean {
    return r.name.trim() === ''
      && r.startOffSet == null
      && r.duration == null
      && r.taskID === ""
      && r.resourceID === "";
  }
}
