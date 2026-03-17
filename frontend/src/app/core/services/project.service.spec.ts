import { TestBed } from '@angular/core/testing';
import { firstValueFrom, of } from 'rxjs';
import { vi } from 'vitest';

import { ProjectControllerService } from '../../api-client/api/projectController.service';
import { ScanControllerService } from '../../api-client/api/scanController.service';
import { CreateProjectRequest } from '../../api-client/model/createProjectRequest';
import { ProjectResponse } from '../../api-client/model/projectResponse';
import { ScanResponse } from '../../api-client/model/scanResponse';
import { ProjectService } from './project.service';

describe('ProjectService', () => {
  let service: ProjectService;
  let projectControllerSpy: {
    findAll: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    findById1: ReturnType<typeof vi.fn>;
  };
  let scanControllerSpy: {
    findByProjectId: ReturnType<typeof vi.fn>;
    create1: ReturnType<typeof vi.fn>;
    findById: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    projectControllerSpy = {
      findAll: vi.fn(),
      create: vi.fn(),
      findById1: vi.fn(),
    };

    scanControllerSpy = {
      findByProjectId: vi.fn(),
      create1: vi.fn(),
      findById: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        ProjectService,
        { provide: ProjectControllerService, useValue: projectControllerSpy as unknown as ProjectControllerService },
        { provide: ScanControllerService, useValue: scanControllerSpy as unknown as ScanControllerService },
      ],
    });

    service = TestBed.inject(ProjectService);
  });

  it('returns project list from API client', async () => {
    const projects = [{ id: 1, name: 'Demo', rootUrl: 'https://example.com' } as ProjectResponse];
    projectControllerSpy.findAll.mockReturnValue(of(projects));

    const result = await firstValueFrom(service.getProjects());

    expect(result).toEqual(projects);
    expect(projectControllerSpy.findAll).toHaveBeenCalled();
  });

  it('creates project through API client', async () => {
    const request: CreateProjectRequest = {
      name: 'Portal',
      rootUrl: 'https://example.com',
    };

    const created = { id: 3, ...request } as ProjectResponse;
    projectControllerSpy.create.mockReturnValue(of(created));

    const result = await firstValueFrom(service.createProject(request));

    expect(result).toEqual(created);
    expect(projectControllerSpy.create).toHaveBeenCalledWith(request);
  });

  it('runs scan for project through API client', async () => {
    const scan = { id: 7, projectId: 1, status: 'RUNNING' } as ScanResponse;
    scanControllerSpy.create1.mockReturnValue(of(scan));

    const result = await firstValueFrom(service.runScan(1));

    expect(result).toEqual(scan);
    expect(scanControllerSpy.create1).toHaveBeenCalledWith(1);
  });
});
