import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

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
  private readonly projectService = inject(ProjectService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  loadingProject = true;
  loadingScans = true;
  projectError: string | null = null;
  scansError: string | null = null;
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
}
