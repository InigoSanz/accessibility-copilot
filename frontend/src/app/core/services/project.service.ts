import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

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
    return this.projectController.findAll();
  }

  createProject(request: CreateProjectRequest): Observable<ProjectResponse> {
    return this.projectController.create(request);
  }

  getProjectById(id: number): Observable<ProjectResponse> {
    return this.projectController.findById1(id);
  }

  getScansByProjectId(projectId: number): Observable<ScanResponse[]> {
    return this.scanController.findByProjectId(projectId);
  }

  runScan(projectId: number): Observable<Scan> {
    return this.scanController.create1(projectId);
  }

  getScanById(scanId: number): Observable<Scan> {
    return this.scanController.findById(scanId);
  }
}
