import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { CreateProjectRequest } from '../../../../api-client/model/createProjectRequest';
import { ProjectResponse } from '../../../../api-client/model/projectResponse';
import { ProjectService } from '../../../../core/services/project.service';

@Component({
  selector: 'app-project-list-page',
  imports: [CommonModule, DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './project-list-page.html',
  styleUrl: './project-list-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProjectListPage implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly projectService = inject(ProjectService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  readonly createProjectForm = this.formBuilder.nonNullable.group({
    name: ['', Validators.required],
    rootUrl: ['', Validators.required],
  });

  loading = true;
  error: string | null = null;
  projects: ProjectResponse[] = [];
  submitting = false;
  submitError: string | null = null;

  get nameControl() {
    return this.createProjectForm.controls.name;
  }

  get rootUrlControl() {
    return this.createProjectForm.controls.rootUrl;
  }

  ngOnInit(): void {
    this.loadProjects();
  }

  submit(): void {
    if (this.createProjectForm.invalid) {
      this.createProjectForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.submitError = null;

    const request: CreateProjectRequest = this.createProjectForm.getRawValue();

    this.projectService.createProject(request).subscribe({
      next: () => {
        this.createProjectForm.reset({
          name: '',
          rootUrl: '',
        });
        this.submitting = false;
        this.submitError = null;
        this.changeDetectorRef.markForCheck();
        this.loadProjects();
      },
      error: () => {
        this.submitError = 'Could not create project. Please try again.';
        this.submitting = false;
        this.changeDetectorRef.markForCheck();
      },
    });
  }

  private loadProjects(): void {
    this.loading = true;
    this.error = null;

    this.projectService.getProjects().subscribe({
      next: (projects) => {
        this.projects = projects;
        this.loading = false;
        this.changeDetectorRef.markForCheck();
      },
      error: () => {
        this.error = 'Could not load projects. Please try again.';
        this.loading = false;
        this.changeDetectorRef.markForCheck();
      },
    });
  }
}