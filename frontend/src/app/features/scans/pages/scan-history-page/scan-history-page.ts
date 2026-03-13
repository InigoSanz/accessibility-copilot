import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

import { ProjectResponse } from '../../../../api-client/model/projectResponse';
import { ScanResponse } from '../../../../api-client/model/scanResponse';
import { ProjectService } from '../../../../core/services/project.service';

interface ScanHistoryItem {
  scanId: number | null;
  projectId: number | null;
  projectName: string;
  projectUrl: string;
  status: string;
  startedAt: string | null;
  finishedAt: string | null;
}

interface ProjectFilterOption {
  id: number;
  name: string;
}

@Component({
  selector: 'app-scan-history-page',
  imports: [CommonModule, DatePipe, RouterLink],
  templateUrl: './scan-history-page.html',
  styleUrl: './scan-history-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ScanHistoryPage implements OnInit {
  private readonly projectService = inject(ProjectService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  readonly statusOptions = ['ALL', 'RUNNING', 'COMPLETED', 'FAILED'] as const;

  loading = true;
  error: string | null = null;
  scans: ScanHistoryItem[] = [];
  selectedStatus: (typeof this.statusOptions)[number] = 'ALL';
  selectedProjectId: string = 'ALL';

  ngOnInit(): void {
    this.loadHistory();
  }

  get filteredScans(): ScanHistoryItem[] {
    return this.scans.filter((scan) => {
      const matchesStatus = this.selectedStatus === 'ALL' || scan.status === this.selectedStatus;
      const matchesProject =
        this.selectedProjectId === 'ALL' ||
        (scan.projectId !== null && String(scan.projectId) === this.selectedProjectId);

      return matchesStatus && matchesProject;
    });
  }

  get projectOptions(): ProjectFilterOption[] {
    const projectsById = new Map<number, string>();

    for (const scan of this.scans) {
      if (scan.projectId !== null && !projectsById.has(scan.projectId)) {
        projectsById.set(scan.projectId, scan.projectName);
      }
    }

    return Array.from(projectsById.entries())
      .map(([id, name]) => ({ id, name }))
      .sort((left, right) => left.name.localeCompare(right.name));
  }

  setStatusFilter(event: Event): void {
    const value = (event.target as HTMLSelectElement | null)?.value;

    if (value && this.statusOptions.includes(value as (typeof this.statusOptions)[number])) {
      this.selectedStatus = value as (typeof this.statusOptions)[number];
    }
  }

  setProjectFilter(event: Event): void {
    const value = (event.target as HTMLSelectElement | null)?.value;
    this.selectedProjectId = value && value.length > 0 ? value : 'ALL';
  }

  statusClass(status: string | undefined): string {
    if (status === 'RUNNING') {
      return 'status-badge status-badge--running';
    }

    if (status === 'COMPLETED') {
      return 'status-badge status-badge--completed';
    }

    if (status === 'FAILED') {
      return 'status-badge status-badge--failed';
    }

    return 'status-badge';
  }

  private loadHistory(): void {
    this.loading = true;
    this.error = null;

    this.projectService
      .getProjects()
      .pipe(
        switchMap((projects) => this.loadScansForProjects(projects)),
      )
      .subscribe({
        next: (scanItems) => {
          this.scans = [...scanItems].sort((left, right) => {
            const leftTime = left.startedAt ? new Date(left.startedAt).getTime() : 0;
            const rightTime = right.startedAt ? new Date(right.startedAt).getTime() : 0;
            return rightTime - leftTime;
          });
          this.selectedStatus = 'ALL';
          this.selectedProjectId = 'ALL';
          this.loading = false;
          this.changeDetectorRef.markForCheck();
        },
        error: () => {
          this.error = 'Could not load scan history. Please try again.';
          this.loading = false;
          this.changeDetectorRef.markForCheck();
        },
      });
  }

  private loadScansForProjects(projects: ProjectResponse[]) {
    const validProjects = projects.filter((project): project is ProjectResponse & { id: number } =>
      typeof project.id === 'number',
    );

    if (validProjects.length === 0) {
      return of([] as ScanHistoryItem[]);
    }

    return forkJoin(
      validProjects.map((project) =>
        this.projectService.getScansByProjectId(project.id).pipe(
          catchError(() => of([] as ScanResponse[])),
          map((scans) =>
            scans.map((scan) => ({
              scanId: scan.id ?? null,
              projectId: project.id,
              projectName: project.name ?? 'Unnamed project',
              projectUrl: project.rootUrl ?? 'N/A',
              status: scan.status ?? 'UNKNOWN',
              startedAt: scan.startedAt ?? null,
              finishedAt: scan.finishedAt ?? null,
            })),
          ),
        ),
      ),
    ).pipe(map((nestedScans) => nestedScans.flat()));
  }
}
