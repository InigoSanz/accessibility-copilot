import { TestBed } from '@angular/core/testing';
import { firstValueFrom, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { AccessibilityIssueControllerService } from '../../api-client/api/accessibilityIssueController.service';
import { ScanControllerService } from '../../api-client/api/scanController.service';
import { ScanSummaryControllerService } from '../../api-client/api/scanSummaryController.service';
import { AccessibilityIssueResponse } from '../../api-client/model/accessibilityIssueResponse';
import { ScanResponse } from '../../api-client/model/scanResponse';
import { ScanSummaryResponse } from '../../api-client/model/scanSummaryResponse';
import { ScanService } from './scan.service';

describe('ScanService', () => {
  let service: ScanService;
  let scanControllerSpy: {
    findById: ReturnType<typeof vi.fn>;
  };
  let scanSummaryControllerSpy: {
    getSummary: ReturnType<typeof vi.fn>;
  };
  let issueControllerSpy: {
    findByScanId: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    scanControllerSpy = {
      findById: vi.fn(),
    };
    scanSummaryControllerSpy = {
      getSummary: vi.fn(),
    };
    issueControllerSpy = {
      findByScanId: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        ScanService,
        { provide: ScanControllerService, useValue: scanControllerSpy as unknown as ScanControllerService },
        {
          provide: ScanSummaryControllerService,
          useValue: scanSummaryControllerSpy as unknown as ScanSummaryControllerService,
        },
        {
          provide: AccessibilityIssueControllerService,
          useValue: issueControllerSpy as unknown as AccessibilityIssueControllerService,
        },
      ],
    });

    service = TestBed.inject(ScanService);
  });

  it('returns composed scan detail data', async () => {
    const scan = { id: 10, status: 'COMPLETED' } as ScanResponse;
    const summary = { criticalCount: 1 } as ScanSummaryResponse;
    const issues = [{ id: 101, ruleId: 'image-alt' } as AccessibilityIssueResponse];

    scanControllerSpy.findById.mockReturnValue(of(scan));
    scanSummaryControllerSpy.getSummary.mockReturnValue(of(summary));
    issueControllerSpy.findByScanId.mockReturnValue(of(issues));

    const result = await firstValueFrom(service.getScanDetailData(10));

    expect(result.scan).toEqual(scan);
    expect(result.summary).toEqual(summary);
    expect(result.issues).toEqual(issues);
  });

  it('returns null summary when summary endpoint fails', async () => {
    scanSummaryControllerSpy.getSummary.mockReturnValue(throwError(() => new Error('summary error')));

    const result = await firstValueFrom(service.getSummaryByScanId(10));

    expect(result).toBeNull();
  });

  it('returns empty issue list when issues endpoint fails', async () => {
    issueControllerSpy.findByScanId.mockReturnValue(throwError(() => new Error('issues error')));

    const result = await firstValueFrom(service.getIssuesByScanId(10));

    expect(result).toEqual([]);
  });

  it('detects running and terminal statuses', () => {
    expect(service.isRunning('RUNNING')).toBe(true);
    expect(service.isTerminal('COMPLETED')).toBe(true);
    expect(service.isTerminal('FAILED')).toBe(true);
    expect(service.isTerminal('RUNNING')).toBe(false);
  });
});
