import { inject, Injectable } from '@angular/core';
import { from, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ProjectControllerService } from '../../api-client/api/projectController.service';
import { ScanControllerService } from '../../api-client/api/scanController.service';
import { CreateProjectRequest } from '../../api-client/model/createProjectRequest';
import { ProjectResponse } from '../../api-client/model/projectResponse';
import { ScanResponse } from '../../api-client/model/scanResponse';

type Scan = ScanResponse;

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly projectController = inject(ProjectControllerService);
  private readonly scanController = inject(ScanControllerService);

  getProjects(): Observable<ProjectResponse[]> {
    return this.projectController
      .findAll()
      .pipe(mergeMap((response) => this.normalizeResponse<ProjectResponse[]>(response)));
  }

  createProject(request: CreateProjectRequest): Observable<ProjectResponse> {
    return this.projectController
      .create(request)
      .pipe(mergeMap((response) => this.normalizeResponse<ProjectResponse>(response)));
  }

  getProjectById(id: number): Observable<ProjectResponse> {
    return this.projectController
      .findById1(id)
      .pipe(mergeMap((response) => this.normalizeResponse<ProjectResponse>(response)));
  }

  getScansByProjectId(projectId: number): Observable<ScanResponse[]> {
    return this.scanController
      .findByProjectId(projectId)
      .pipe(mergeMap((response) => this.normalizeResponse<ScanResponse[]>(response)));
  }

  runScan(projectId: number): Observable<Scan> {
    return this.scanController
      .create1(projectId)
      .pipe(mergeMap((response) => this.normalizeResponse<Scan>(response)));
  }

  getScanById(scanId: number): Observable<Scan> {
    return this.scanController
      .findById(scanId)
      .pipe(mergeMap((response) => this.normalizeResponse<Scan>(response)));
  }

  private normalizeResponse<T>(response: unknown): Observable<T> {
    if (response instanceof Blob) {
      return from(response.text()).pipe(
        mergeMap((content) => of(JSON.parse(content) as T)),
      );
    }

    return of(response as T);
  }
}
