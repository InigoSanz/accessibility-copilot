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

  loading = true;
  error: string | null = null;
  scans: ScanHistoryItem[] = [];

  ngOnInit(): void {
    this.loadHistory();
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
