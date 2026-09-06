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

@Component({
  selector: 'app-generate-resource-requests',
  imports: [FormsModule],
  templateUrl: './generate-resource-requests.html',
  styleUrl: './generate-resource-requests.css',
})
export class GenerateResourceRequests {

  private resourceRequestIDCounter = 0;
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

  onResourceRequestChange(): void {
    const focused = document.activeElement as HTMLElement;
    this.manageRows(
      this.resourceRequests,
      () => this.createResourceRequest(),
      (r) => this.isResourceRequestEmpty(r)
    );
    setTimeout(() => focused?.focus());
  }

  createResourceRequests(): void {
    const scenarioData = this.scenario();
    if (!scenarioData) return;

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
            name: rr.name,
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
            name: rr.name,
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
      },
    });
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