import { DecimalPipe, NgTemplateOutlet } from '@angular/common';
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
  imports: [FormsModule, DecimalPipe, NgTemplateOutlet],
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
  readonly tableRowCentered = 'flex flex-wrap items-start justify-center gap-4 mt-4';
  readonly tableCard = 'rounded-lg border border-offWhite p-4 overflow-visible';
  readonly tableCardWide = 'rounded-lg border border-offWhite p-4 overflow-visible flex-1';
  readonly tableCaption = 'caption-top text-center font-cascadia text-lg font-bold text-sage pb-2';
  readonly timelineRowActive = 'bg-slate/20 transition-colors duration-200';
  readonly thGroup = 'py-2 px-3 text-sage text-center';
  readonly thGroupSpan = 'py-2 px-3 text-sage text-center border-l border-sage/40';
  readonly thSub = 'py-2 px-3 text-sage text-center text-xs font-normal border-l border-sage/40';
  readonly tdCell = 'py-2 px-3 text-center';
  readonly tdSubCell = 'py-2 px-3 text-center border-l border-sage/40';
  readonly tableClass = 'w-full text-offWhite font-cascadia text-sm border-collapse';
  readonly headerRow = 'border-b border-sage';
  readonly chartBlock = 'w-fit max-w-full mx-auto';
  readonly chartWrapper = 'chart-container rounded-lg border border-offWhite mt-4 overflow-x-auto w-fit max-w-full mx-auto px-10 py-4';
  readonly aboveChartRow = 'w-0 min-w-full flex flex-wrap items-end gap-4 mt-4';
  readonly resourceRow = 'flex flex-wrap items-center gap-2';
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
  readonly legendInterruptSwatch = 'w-1 h-4 rounded-sm bg-[#ff4d4f] mx-1.5';
  readonly controlsWrapper = 'flex justify-center mt-4';

  readonly playbackWrapper = 'flex flex-wrap items-center justify-center gap-3 mt-4';
  readonly playbackButton = [
    'font-cascadia bg-sand text-dark rounded-lg',
    'px-4 py-2 min-w-14 text-lg font-semibold',
    'transition-colors duration-200 hover:bg-offWhite',
    'disabled:opacity-40 disabled:hover:bg-sand disabled:cursor-not-allowed'
  ].join(' ');
  readonly playbackCounter = 'text-offWhite text-sm font-cascadia ml-2';

  readonly queueWrapper = 'flex flex-col items-end ml-auto';
  readonly queueLabel = 'font-cascadia text-offWhite font-semibold mb-1';
  readonly queueBlock = 'transition-all duration-300 animate-queue-enter';

  readonly QUEUE_HEIGHT = 70;
  readonly QUEUE_MIN_WIDTH = 340;
  readonly QUEUE_PAD = 16;
  readonly QUEUE_GAP = 14;
  readonly QUEUE_BLOCK_W = 70;
  readonly QUEUE_BLOCK_H = 34;
  readonly QUEUE_BLOCK_Y = 18;
  readonly QUEUE_TOP_LINE = 10;
  readonly QUEUE_BOTTOM_LINE = 60;
  readonly QUEUE_ARROW_STEP = 48;

  readonly CELL_W = 40;
  readonly CELL_H = 40;
  readonly MARGIN_TOP = 20;
  readonly MARGIN_BOTTOM = 40;
  readonly MARGIN_RIGHT = 20;

  /** the little checkered flag that marks the tick a task ran out of work in */
  readonly FLAG_W = 9;
  readonly FLAG_H = 6;
  readonly FLAG_POLE_H = 13;
  readonly FLAG_INSET = 3;
  readonly FLAG_CHECKERS = [
    { dx: 0, dy: 0 },
    { dx: 6, dy: 0 },
    { dx: 3, dy: 3 }
  ];

  /** the pale name strip laid over a task's blocks so the chart reads without telling colors apart */
  readonly STRIP_H = 16;
  readonly STRIP_INSET = 2;
  readonly STRIP_PAD_X = 3;
  readonly STRIP_FONT_MAX = 11;
  readonly STRIP_FONT_MIN = 8;
  readonly STRIP_CHAR_W = 0.62;
  readonly STRIP_FILL = '#e6e7e3';
  readonly STRIP_TEXT = '#31353f';
  readonly STRIP_OPACITY = 0.6;
  readonly STRIP_MAX_CHARS = 10;
  /** one glyph, not three periods: in a monospace font each period would take a whole cell
   *  and the dots would drift away from the name they belong to */
  readonly STRIP_ELLIPSIS = '\u2026';

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
  hasPlayed = signal(false);

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

  /**
   * boundaries where a task was cut off before it was done and another task took the CPU over.
   * a task that stepped aside because it got blocked on a resource is not an interruption,
   * so it is skipped here just like the backend skips it in the preemption count.
   */
  interruptions = computed(() => {
    const result = this.result();
    if (!result) return [];

    const marks: {
      key: string;
      time: number;
      x: number;
      y1: number;
      y2: number;
      tooltip: string;
    }[] = [];

    for (let i = 0; i < result.timeline.length - 1; i++) {
      const node = result.timeline[i];
      const next = result.timeline[i + 1];

      const interrupted = node.runningTask;
      const takesOver = next.runningTask;

      if (!interrupted || !takesOver) continue;
      if (interrupted.id === takesOver.id) continue;
      if (interrupted.remainingTime <= 0) continue;

      const blocked = (next.currentTimeline ?? []).some(
        task => task.id === interrupted.id && task.status === 'BLOCKED'
      );
      if (blocked) continue;

      const interruptedRow = this.getYIndex(node);
      const takesOverRow = this.getYIndex(next);
      if (interruptedRow < 0 || takesOverRow < 0) continue;

      marks.push({
        key: `${node.time}-${interrupted.id}-${takesOver.id}`,
        time: next.time,
        x: this.marginLeft() + next.time * this.CELL_W,
        y1: this.MARGIN_TOP + Math.min(interruptedRow, takesOverRow) * this.CELL_H,
        y2: this.MARGIN_TOP + (Math.max(interruptedRow, takesOverRow) + 1) * this.CELL_H,
        tooltip: `${interrupted.name} interrupted by ${takesOver.name} @ t=${next.time}`
          + ` - ${interrupted.remainingTime} tick(s) left`
      });
    }
    return marks;
  });

  visibleInterruptions = computed(() => {
    const step = this.step();
    return this.interruptions().filter(mark => mark.time <= step);
  });

  hasInterruptions = computed(() => this.interruptions().length > 0);

  /**
   * the tick a task ran its last unit of work in. remainingTime on a node is what is left
   * after that tick ran, so a zero there means the task completed at node.time + 1.
   */
  finishMarks = computed(() => {
    const result = this.result();
    if (!result) return [];

    const marks: {
      key: string;
      time: number;
      poleX: number;
      flagX: number;
      top: number;
      tooltip: string;
    }[] = [];

    const alreadyFinished = new Set<string>();

    for (const node of result.timeline) {
      const task = node.runningTask;
      if (!task || task.remainingTime > 0) continue;
      if (alreadyFinished.has(task.id)) continue;

      const row = this.getYIndex(node);
      if (row < 0) continue;

      alreadyFinished.add(task.id);

      const cellRight = this.marginLeft() + (node.time + 1) * this.CELL_W;
      const poleX = cellRight - this.FLAG_INSET - this.FLAG_W;

      marks.push({
        key: `${task.id}-${node.time}`,
        time: node.time,
        poleX,
        flagX: poleX + 1,
        top: this.MARGIN_TOP + row * this.CELL_H + this.FLAG_INSET,
        tooltip: `${task.name} finished @ t=${node.time + 1}`
      });
    }
    return marks;
  });

  visibleFinishMarks = computed(() => {
    const step = this.step();
    return this.finishMarks().filter(mark => mark.time <= step);
  });

  hasFinishMarks = computed(() => this.finishMarks().length > 0);

  /**
   * a pale strip with the task name on it, laid over the middle of the blocks a task runs in.
   * one strip per uninterrupted run in a row, so the chart can be followed without
   * telling the task colors apart.
   */
  taskStrips = computed(() => {
    const nodes = this.visibleTimeline();

    const strips: {
      key: string;
      x: number;
      y: number;
      width: number;
      textX: number;
      textY: number;
      label: string;
      fontSize: number;
    }[] = [];

    let i = 0;
    while (i < nodes.length) {
      const node = nodes[i];
      const task = node.runningTask;
      const row = this.getYIndex(node);

      if (!task || row < 0) {
        i++;
        continue;
      }

      // walk on as long as the same task keeps the CPU on the same row without a gap
      let end = i;
      while (end + 1 < nodes.length) {
        const next = nodes[end + 1];
        if (!next.runningTask) break;
        if (next.runningTask.id !== task.id) break;
        if (next.time !== nodes[end].time + 1) break;
        if (this.getYIndex(next) !== row) break;
        end++;
      }

      const span = nodes[end].time - node.time + 1;
      const runX = this.marginLeft() + node.time * this.CELL_W + this.STRIP_INSET;
      const runWidth = span * this.CELL_W - this.STRIP_INSET * 2;
      const y = this.MARGIN_TOP + row * this.CELL_H + (this.CELL_H - this.STRIP_H) / 2;

      // the strip is only as wide as the name needs it to be, sitting in the middle of the run
      const fitted = this.fitStripLabel(task.name, runWidth);
      const centerX = runX + runWidth / 2;

      strips.push({
        key: `${task.id}-${node.time}-${row}`,
        x: centerX - fitted.width / 2,
        y,
        width: fitted.width,
        textX: centerX,
        textY: y + this.STRIP_H / 2 + Math.round(fitted.fontSize * 0.36),
        label: fitted.label,
        fontSize: fitted.fontSize
      });

      i = end + 1;
    }
    return strips;
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
      const held = (task?.listWithResourceRequests ?? [])
        .filter(request => request.taskID === task!.id);

      // every tick gets a row, so a task that runs without holding anything still shows up
      if (held.length === 0) {
        rows.push({
          key: `${node.time}-idle`,
          time: node.time,
          taskName: task?.name ?? '-',
          requestName: '-',
          startOffset: '-',
          duration: '-',
          resourceName: '-'
        });
        continue;
      }

      for (const request of held) {
        rows.push({
          key: `${node.time}-${request.id}`,
          time: node.time,
          taskName: task!.name,
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

  supportsPriorityCeiling = computed(() => {
    return this.resources().length > 0 && this.hasResourceRequests();
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

  queuedTasks = computed(() => {
    return this.currentNode()?.currentTimeline ?? [];
  });

  queueCapacity = computed(() => {
    return Math.max(this.scenario().tasks.length, this.queuedTasks().length, 1);
  });

  queueWidth = computed(() => {
    const count = this.queueCapacity();
    const needed = this.QUEUE_PAD * 2
      + count * this.QUEUE_BLOCK_W
      + Math.max(0, count - 1) * this.QUEUE_GAP;

    return Math.max(this.QUEUE_MIN_WIDTH, needed);
  });

  queueArrows = computed(() => {
    const positions: number[] = [];
    for (let x = 20; x < this.queueWidth() - 12; x += this.QUEUE_ARROW_STEP) {
      positions.push(x);
    }
    return positions;
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

  queueBlockX(index: number): number {
    return this.queueWidth() - this.QUEUE_PAD
      - (index + 1) * this.QUEUE_BLOCK_W
      - index * this.QUEUE_GAP;
  }

  arrowPath(x: number, y: number): string {
    return 'M ' + x + ' ' + (y - 5) + ' L ' + (x + 7) + ' ' + y + ' L ' + x + ' ' + (y + 5);
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

  timelineRowClass(time: number): string {
    if (!this.hasPlayed()) return '';

    return time === this.step() ? this.timelineRowActive : '';
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

  /**
   * cuts a name longer than STRIP_MAX_CHARS down to that many characters and marks the cut with
   * STRIP_ELLIPSIS, then shrinks the font - and only if that is still not enough, clips further -
   * so the label fits the run it labels. also reports how wide the strip has to be to hold it.
   * the strip lets hovers through, so the full name is still on the block tooltip underneath.
   */
  private fitStripLabel(
    name: string,
    maxWidth: number
  ): { label: string; fontSize: number; width: number } {
    const wanted = name.length > this.STRIP_MAX_CHARS
      ? name.slice(0, this.STRIP_MAX_CHARS) + this.STRIP_ELLIPSIS
      : name;

    const available = maxWidth - this.STRIP_PAD_X * 2;
    if (available <= 0) {
      return { label: '', fontSize: this.STRIP_FONT_MIN, width: 0 };
    }

    let fontSize = this.STRIP_FONT_MAX;
    while (
      fontSize > this.STRIP_FONT_MIN &&
      wanted.length * fontSize * this.STRIP_CHAR_W > available
    ) {
      fontSize--;
    }

    const maxChars = Math.floor(available / (fontSize * this.STRIP_CHAR_W));
    if (maxChars <= 0) return { label: '', fontSize, width: 0 };

    let label = wanted;
    if (wanted.length > maxChars) {
      // too narrow a run even at the smallest font, so clip the name but keep the marker
      label = maxChars <= this.STRIP_ELLIPSIS.length
        ? name.slice(0, maxChars)
        : name.slice(0, maxChars - this.STRIP_ELLIPSIS.length) + this.STRIP_ELLIPSIS;
    }

    const width = Math.round(label.length * fontSize * this.STRIP_CHAR_W) + this.STRIP_PAD_X * 2;
    return { label, fontSize, width };
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

    this.hasPlayed.set(true);
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
          this.hasPlayed.set(false);
          this.result.set(simulationResult);
          this.step.set(this.lastStep());
        },
        error: (err) => {
          console.error('Simulation failed', err);
        }
      });
  }
}