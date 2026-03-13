import { inject, Injectable } from '@angular/core';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { AccessibilityIssueControllerService } from '../../api-client/api/accessibilityIssueController.service';
import { ScanControllerService } from '../../api-client/api/scanController.service';
import { ScanSummaryControllerService } from '../../api-client/api/scanSummaryController.service';
import { AccessibilityIssueResponse } from '../../api-client/model/accessibilityIssueResponse';
import { ScanResponse } from '../../api-client/model/scanResponse';
import { ScanSummaryResponse } from '../../api-client/model/scanSummaryResponse';

export type KnownScanStatus = 'RUNNING' | 'COMPLETED' | 'FAILED';

export interface ScanDetailData {
  scan: ScanResponse;
  summary: ScanSummaryResponse | null;
  issues: AccessibilityIssueResponse[];
}

@Injectable({ providedIn: 'root' })
export class ScanService {
  private readonly scanController = inject(ScanControllerService);
  private readonly scanSummaryController = inject(ScanSummaryControllerService);
  private readonly accessibilityIssueController = inject(AccessibilityIssueControllerService);

  getScanById(scanId: number): Observable<ScanResponse> {
    return this.scanController.findById(scanId);
  }

  getSummaryByScanId(scanId: number): Observable<ScanSummaryResponse | null> {
    return this.scanSummaryController.getSummary(scanId).pipe(catchError(() => of(null)));
  }

  getIssuesByScanId(scanId: number): Observable<AccessibilityIssueResponse[]> {
    return this.accessibilityIssueController.findByScanId(scanId).pipe(catchError(() => of([])));
  }

  getScanDetailData(scanId: number): Observable<ScanDetailData> {
    return forkJoin({
      scan: this.getScanById(scanId),
      summary: this.getSummaryByScanId(scanId),
      issues: this.getIssuesByScanId(scanId),
    });
  }

  isRunning(status: ScanResponse['status']): boolean {
    return status === 'RUNNING';
  }

  isTerminal(status: ScanResponse['status']): boolean {
    return status === 'COMPLETED' || status === 'FAILED';
  }
}
