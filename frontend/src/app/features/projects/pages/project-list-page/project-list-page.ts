import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';

import { Project } from '../../../../core/models/project.model';
import { ProjectService } from '../../../../core/services/project.service';

@Component({
  selector: 'app-project-list-page',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './project-list-page.html',
  styleUrl: './project-list-page.scss',
})
export class ProjectListPage implements OnInit {
  private readonly projectService = inject(ProjectService);

  loading = true;
  error: string | null = null;
  projects: Project[] = [];

  ngOnInit(): void {
    this.loadProjects();
  }

  private loadProjects(): void {
    this.loading = true;
    this.error = null;

    this.projectService.getProjects().subscribe({
      next: (projects) => {
        this.projects = projects;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading projects', err);
        this.error = 'Could not load projects. Please try again.';
        this.loading = false;
      },
    });
  }
}