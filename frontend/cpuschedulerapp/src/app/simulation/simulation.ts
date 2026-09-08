import { DecimalPipe } from '@angular/common';
import { Component, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { Scenario } from '../models/scenario';
import {
  SimulationResultDTO,
  SimulationService,
  SimulationType,
  TimeNodeDTO
} from '../services/simulation.service';
import { ScenarioService } from '../services/scenario.service';

@Component({
  selector: 'app-simulation',
  imports: [FormsModule, DecimalPipe],
  templateUrl: './simulation.html',
  styleUrl: './simulation.css'
})
export class Simulation {

  readonly thMetric = 'py-2 px-3 text-sage text-center relative cursor-help group';
  readonly tdMetric = 'py-2 px-3 text-center';
  readonly metricTooltip = [
    'invisible opacity-0 group-hover:visible group-hover:opacity-100',
    'absolute top-full left-1/2 -translate-x-1/2 mt-2',
    'px-3 py-2 w-max max-w-60',
    'text-[11px] font-normal leading-snug text-left whitespace-normal',
    'text-offWhite bg-dark border border-sand rounded-md',
    'shadow-lg z-[50] transition-all duration-200 pointer-events-none'
  ].join(' ');

  readonly tableWrapper = 'rounded-lg border border-offWhite mt-4 p-4 overflow-visible';
  readonly tableRow = 'flex flex-wrap items-start gap-4 mt-4';
  readonly tableCard = 'rounded-lg border border-offWhite p-4 overflow-visible';
  readonly tableCardWide = 'rounded-lg border border-offWhite p-4 overflow-visible flex-1';
  readonly thGroup = 'py-2 px-3 text-sage text-center';
  readonly thGroupSpan = 'py-2 px-3 text-sage text-center border-l border-sage/40';
  readonly thSub = 'py-2 px-3 text-sage text-center text-xs font-normal border-l border-sage/40';
  readonly tdCell = 'py-2 px-3 text-center';
  readonly tdSubCell = 'py-2 px-3 text-center border-l border-sage/40';
  readonly tableClass = 'w-full text-offWhite font-cascadia text-sm border-collapse';
  readonly headerRow = 'border-b border-sage';
  readonly chartBlock = 'w-fit max-w-full mx-auto';
  readonly chartWrapper = 'chart-container rounded-lg border border-offWhite mt-4 overflow-x-auto w-fit max-w-full mx-auto px-10 py-4';
  readonly resourceRow = 'w-0 min-w-full flex flex-wrap items-center gap-2 mt-4';
  readonly resourceHeader = 'text-offWhite font-cascadia font-semibold mr-1';
  readonly resourcePillBase = 'rounded-full border px-4 py-1 text-sm font-cascadia transition-colors duration-200';
  readonly resourcePillInUse = 'bg-sand text-offWhite border-sand';
  readonly resourcePillFree = 'bg-dark text-black border-black';
  readonly chartSvg = 'block mx-auto';
  readonly legendWrapper = 'flex flex-wrap justify-center gap-4 mt-4';
  readonly legendItem = 'flex items-center gap-2';
  readonly legendSwatch = 'w-4 h-4 rounded';
  readonly legendText = 'text-offWhite text-sm font-cascadia';
  readonly legendPriority = 'text-sage';
  readonly legendInheritedSwatch = 'w-4 h-4 rounded border-2 border-dashed border-[#39FF14]';
  readonly controlsWrapper = 'flex justify-center mt-4';

  readonly playbackWrapper = 'flex flex-wrap items-center justify-center gap-3 mt-4';
  readonly playbackButton = [
    'font-cascadia bg-sand text-dark rounded-lg',
    'px-4 py-2 min-w-14 text-lg font-semibold',
    'transition-colors duration-200 hover:bg-offWhite',
    'disabled:opacity-40 disabled:hover:bg-sand disabled:cursor-not-allowed'
  ].join(' ');
  readonly playbackCounter = 'text-offWhite text-sm font-cascadia ml-2';

  readonly CELL_W = 40;
  readonly CELL_H = 40;
  readonly MARGIN_TOP = 20;
  readonly MARGIN_BOTTOM = 40;
  readonly MARGIN_RIGHT = 20;

  readonly TASK_COLORS = [
    '#c0b89b',
    '#8fa3a0',
    '#b5838d',
    '#9a9f7a',
    '#a89ab0',
    '#c49a6c',
    '#7a94a8',
    '#8aab95',
    '#a3917a',
    '#b0a07a',
    '#a08080',
    '#90a8a0'
  ];

  scenarioID = '';
  selectedAlgorithm: SimulationType = 'FCFS';
  quantum = 2;

  scenario = signal<Scenario>({
    id: '',
    name: '',
    tasks: [],
    resources: []
  });

  result = signal<SimulationResultDTO | null>(null);

  step = signal(0);
  isPlaying = signal(false);

  private readonly PLAYBACK_INTERVAL_MS = 500;
  private playbackTimer: ReturnType<typeof setInterval> | null = null;

  isPCP = computed(() => {
    const r = this.result();
    return r !== null && r !== undefined && r.algorithm === 'PC';
  });

  marginLeft = computed(() => {
    return this.isPCP() ? 80 : 40;
  });

  priorityLevels = computed(() => {
    const priorities = new Set<number>();

    const s = this.scenario();
    for (const t of s?.tasks ?? []) {
      if (t.priority !== undefined && t.priority !== null) {
        priorities.add(t.priority);
      }
    }

  
    const r = this.result();
    for (const node of r?.timeline ?? []) {
      const effective = node.runningTask?.effectivePriority;
      if (effective !== undefined && effective !== null) {
        priorities.add(effective);
      }
    }

    return [...priorities].sort((a, b) => b - a);
  });

  taskPriorityMap = computed(() => {
    const s = this.scenario();
    if (!s || !s.tasks) return new Map<string, number>();
    const map = new Map<string, number>();
    for (const t of s.tasks) {
      if (t.priority !== undefined && t.priority !== null) {
        map.set(t.name, t.priority);
      }
    }
    return map;
  });

  taskNames = computed(() => {
    const scenario = this.scenario();
    if (scenario && scenario.tasks.length > 0) {
      return scenario.tasks.map(task => task.name).reverse();
    }
    const result = this.result();
    if (!result) return [];
    const names: string[] = [];
    const seen = new Set<string>();
    for (const node of result.timeline) {
      if (node.runningTask && !seen.has(node.runningTask.name)) {
        seen.add(node.runningTask.name);
        names.push(node.runningTask.name);
      }
    }
    return names.reverse();
  });

  yLabels = computed(() => {
    if (this.isPCP()) {
      return this.priorityLevels().map(p => `Priority ${p}`);
    }
    return this.taskNames();
  });

  yCount = computed(() => this.yLabels().length);

  legendNames = computed(() => {
    return [...this.taskNames()].reverse();
  });

  hasInheritedTicks = computed(() => {
    const r = this.result();
    if (!this.isPCP() || !r) return false;
    return r.timeline.some(node => this.isInherited(node));
  });

  ticks = computed(() => {
    const result = this.result();
    if (!result) return [];
    return Array.from(
      { length: result.totalTime + 1 },
      (_, index) => index
    );
  });

  chartWidth = computed(() => {
    const result = this.result();
    if (!result) return 0;
    return (
      this.marginLeft() +
      result.totalTime * this.CELL_W +
      this.MARGIN_RIGHT
    );
  });

  lastStep = computed(() => {
    const result = this.result();
    if (!result || result.totalTime <= 0) return 0;
    return result.totalTime - 1;
  });

  visibleTimeline = computed(() => {
    const result = this.result();
    if (!result) return [];
    const step = this.step();
    return result.timeline.filter(node => node.time <= step);
  });

  playheadX = computed(() => {
    return this.marginLeft() + (this.step() + 1) * this.CELL_W;
  });

  resources = computed(() => {
    const scenario = this.scenario();
    return scenario?.resources ?? [];
  });

  resourceRequestRows = computed(() => {
    const scenario = this.scenario();
    const resourceNames = new Map(this.resources().map(resource => [resource.id, resource.name]));

    const rows: {
      key: string;
      taskName: string;
      requestName: string;
      startOffset: string;
      duration: string;
      resourceName: string;
    }[] = [];

    for (const task of scenario?.tasks ?? []) {
      const requests = [...(task.resourceRequests ?? [])]
        .sort((a, b) => a.startOffset - b.startOffset);

      if (requests.length === 0) {
        rows.push({
          key: task.id,
          taskName: task.name,
          requestName: '-',
          startOffset: '-',
          duration: '-',
          resourceName: '-'
        });
        continue;
      }

      for (const request of requests) {
        rows.push({
          key: request.id,
          taskName: task.name,
          requestName: request.name ?? '-',
          startOffset: String(request.startOffset),
          duration: String(request.duration),
          resourceName: resourceNames.get(request.resourceId) ?? '-'
        });
      }
    }
    return rows;
  });

  timelineRequestRows = computed(() => {
    const result = this.result();
    const resourceNames = new Map(this.resources().map(resource => [resource.id, resource.name]));

    const rows: {
      key: string;
      time: number;
      taskName: string;
      requestName: string;
      startOffset: string;
      duration: string;
      resourceName: string;
    }[] = [];

    for (const node of result?.timeline ?? []) {
      const task = node.runningTask;
      if (!task || !task.listWithResourceRequests) continue;

      for (const request of task.listWithResourceRequests) {
        if (request.taskID !== task.id) continue;

        rows.push({
          key: `${node.time}-${request.id}`,
          time: node.time,
          taskName: task.name,
          requestName: request.name ?? '-',
          startOffset: String(request.startOffset),
          duration: String(request.duration),
          resourceName: resourceNames.get(request.resourceId) ?? '-'
        });
      }
    }
    return rows;
  });

  hasResourceRequests = computed(() => {
    const scenario = this.scenario();
    return (scenario?.tasks ?? []).some(task => (task.resourceRequests ?? []).length > 0);
  });

  currentNode = computed(() => {
    const result = this.result();
    if (!result) return null;
    const step = this.step();
    return result.timeline.find(node => node.time === step) ?? null;
  });

  activeResourceIds = computed(() => {
    const ids = new Set<string>();
    const task = this.currentNode()?.runningTask;
    if (!task || !task.currentlyUsedResources) return ids;

    for (const resource of task.currentlyUsedResources) {
      ids.add(resource.id);
    }
    return ids;
  });

  chartHeight = computed(() => {
    return (
      this.MARGIN_TOP +
      this.yCount() * this.CELL_H +
      this.MARGIN_BOTTOM
    );
  });

  constructor(
    private route: ActivatedRoute,
    private scenarioService: ScenarioService,
    private simulationService: SimulationService
  ) {}

  ngOnInit() {
    const scenarioId = this.route.snapshot.paramMap.get('id');
    if (scenarioId !== null) {
      this.scenarioID = scenarioId;
      this.scenarioService.getById(scenarioId).subscribe({
        next: (retrievedScenario) => {
          this.scenario.set(retrievedScenario);
        },
        error: (err) => {
          console.error('Failed to load scenario', err);
        }
      });
    }
  }

  getTaskIndex(name: string): number {
    return this.taskNames().indexOf(name);
  }

  getTaskColor(name: string): string {
    const index = this.getTaskIndex(name);
    if (index < 0) return '#FFFFFF';
    return this.TASK_COLORS[index % this.TASK_COLORS.length];
  }

  
  getRunPriority(node: TimeNodeDTO): number | undefined {
    const task = node.runningTask;
    if (!task) return undefined;
    if (task.effectivePriority !== undefined && task.effectivePriority !== null) {
      return task.effectivePriority;
    }
    return this.taskPriorityMap().get(task.name);
  }

  
  isInherited(node: TimeNodeDTO): boolean {
    const task = node.runningTask;
    if (!task) return false;
    const effective = this.getRunPriority(node);
    const base = this.taskPriorityMap().get(task.name);
    return effective !== undefined && base !== undefined && effective > base;
  }

  
  getYIndex(node: TimeNodeDTO): number {
    const task = node.runningTask;
    if (!task) return -1;
    if (this.isPCP()) {
      const priority = this.getRunPriority(node);
      if (priority === undefined) return -1;
      return this.priorityLevels().indexOf(priority);
    }
    return this.getTaskIndex(task.name);
  }

  blockTooltip(node: TimeNodeDTO): string {
    const task = node.runningTask;
    if (!task) return '';
    if (!this.isPCP()) {
      return `${task.name} @ t=${node.time}`;
    }
    const base = this.taskPriorityMap().get(task.name);
    if (this.isInherited(node)) {
      return `${task.name} @ t=${node.time} - inherited priority ${this.getRunPriority(node)}, own priority ${base}`;
    }
    return `${task.name} @ t=${node.time} - priority ${this.getRunPriority(node)}`;
  }

  resourcePillClass(resourceId: string): string {
    const state = this.activeResourceIds().has(resourceId)
      ? this.resourcePillInUse
      : this.resourcePillFree;
    return `${this.resourcePillBase} ${state}`;
  }

  goToStart(): void {
    this.pause();
    this.step.set(0);
  }

  stepBackward(): void {
    this.pause();
    this.step.update(step => Math.max(0, step - 1));
  }

  stepForward(): void {
    this.pause();
    this.step.update(step => Math.min(this.lastStep(), step + 1));
  }

  goToEnd(): void {
    this.pause();
    this.step.set(this.lastStep());
  }

  togglePlay(): void {
    if (this.isPlaying()) {
      this.pause();
      return;
    }
    if (this.step() >= this.lastStep()) {
      this.step.set(0);
    }

    this.isPlaying.set(true);
    this.playbackTimer = setInterval(() => {
      if (this.step() >= this.lastStep()) {
        this.pause();
        return;
      }
      this.step.update(step => step + 1);
    }, this.PLAYBACK_INTERVAL_MS);
  }

  pause(): void {
    if (this.playbackTimer !== null) {
      clearInterval(this.playbackTimer);
      this.playbackTimer = null;
    }
    this.isPlaying.set(false);
  }

  ngOnDestroy(): void {
    this.pause();
  }

  onRun(): void {
    this.simulationService
      .runSimulation(
        this.scenarioID,
        this.selectedAlgorithm,
        this.quantum
      )
      .subscribe({
        next: (simulationResult) => {
          this.pause();
          this.result.set(simulationResult);
          this.step.set(this.lastStep());
        },
        error: (err) => {
          console.error('Simulation failed', err);
        }
      });
  }
}