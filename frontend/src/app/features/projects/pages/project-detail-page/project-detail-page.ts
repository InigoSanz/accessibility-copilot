import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ProjectResponse } from '../../../../api-client/model/projectResponse';
import { ScanResponse } from '../../../../api-client/model/scanResponse';
import { ProjectService } from '../../../../core/services/project.service';

@Component({
  selector: 'app-project-detail-page',
  imports: [CommonModule, DatePipe, RouterLink],
  templateUrl: './project-detail-page.html',
  styleUrl: './project-detail-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProjectDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly projectService = inject(ProjectService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  loadingProject = true;
  loadingScans = true;
  projectError: string | null = null;
  scansError: string | null = null;
  runningScan = false;
  runScanError: string | null = null;
  runScanSuccess: string | null = null;
  project: ProjectResponse | null = null;
  scans: ScanResponse[] = [];

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const projectId = Number(idParam);

    if (!idParam || !Number.isInteger(projectId) || projectId <= 0) {
      this.projectError = 'Invalid project id.';
      this.scansError = 'Could not load scans because project id is invalid.';
      this.loadingProject = false;
      this.loadingScans = false;
      this.changeDetectorRef.markForCheck();
      return;
    }

    this.loadProject(projectId);
    this.loadScans(projectId);
  }

  private loadProject(projectId: number): void {
    this.loadingProject = true;
    this.projectError = null;

    this.projectService.getProjectById(projectId).subscribe({
      next: (project) => {
        this.project = project;
        this.loadingProject = false;
        this.changeDetectorRef.markForCheck();
      },
      error: () => {
        this.projectError = 'Could not load project details. Please try again.';
        this.loadingProject = false;
        this.changeDetectorRef.markForCheck();
      },
    });
  }

  private loadScans(projectId: number): void {
    this.loadingScans = true;
    this.scansError = null;

    this.projectService.getScansByProjectId(projectId).subscribe({
      next: (scans) => {
        this.scans = scans;
        this.loadingScans = false;
        this.changeDetectorRef.markForCheck();
      },
      error: () => {
        this.scansError = 'Could not load scans. Please try again.';
        this.loadingScans = false;
        this.changeDetectorRef.markForCheck();
      },
    });
  }

  runScan(): void {
    if (!this.project || typeof this.project.id !== 'number') {
      return;
    }

    const projectId = this.project.id;

    this.runningScan = true;
    this.runScanError = null;
    this.runScanSuccess = null;
    this.changeDetectorRef.markForCheck();

    this.projectService.runScan(projectId).subscribe({
      next: (scan) => {
        this.runningScan = false;
        this.runScanSuccess = 'Scan started. Waiting for backend processing...';
        this.changeDetectorRef.markForCheck();
        this.loadScans(projectId);

        if (typeof scan.id === 'number') {
          this.router.navigate(['/scans', scan.id]);
        }
      },
      error: () => {
        this.runningScan = false;
        this.runScanError = 'Could not run scan. Please try again.';
        this.changeDetectorRef.markForCheck();
      },
    });
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
}
