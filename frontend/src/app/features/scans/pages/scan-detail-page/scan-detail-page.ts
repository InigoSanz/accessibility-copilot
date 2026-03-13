import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EMPTY, timer } from 'rxjs';
import { catchError, switchMap, takeWhile, tap } from 'rxjs/operators';

import { AccessibilityIssueResponse } from '../../../../api-client/model/accessibilityIssueResponse';
import { ScanResponse } from '../../../../api-client/model/scanResponse';
import { ScanSummaryResponse } from '../../../../api-client/model/scanSummaryResponse';
import { ScanService } from '../../../../core/services/scan.service';

@Component({
  selector: 'app-scan-detail-page',
  imports: [CommonModule, DatePipe, RouterLink],
  templateUrl: './scan-detail-page.html',
  styleUrl: './scan-detail-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ScanDetailPage implements OnInit {
  private static readonly POLLING_INTERVAL_MS = 4000;

  private readonly route = inject(ActivatedRoute);
  private readonly scanService = inject(ScanService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);

  loading = true;
  error: string | null = null;
  isPolling = false;
  scan: ScanResponse | null = null;
  summary: ScanSummaryResponse | null = null;
  issues: AccessibilityIssueResponse[] = [];

  get durationText(): string | null {
    if (!this.scan?.startedAt || !this.scan?.finishedAt) {
      return null;
    }

    const startedAt = new Date(this.scan.startedAt).getTime();
    const finishedAt = new Date(this.scan.finishedAt).getTime();

    if (!Number.isFinite(startedAt) || !Number.isFinite(finishedAt) || finishedAt < startedAt) {
      return null;
    }

    const totalSeconds = Math.round((finishedAt - startedAt) / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    if (minutes === 0) {
      return `${seconds}s`;
    }

    return `${minutes}m ${seconds}s`;
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const scanId = Number(idParam);

    if (!idParam || !Number.isInteger(scanId) || scanId <= 0) {
      this.loading = false;
      this.error = 'Invalid scan id.';
      this.changeDetectorRef.markForCheck();
      return;
    }

    this.loading = true;
    this.error = null;
    this.loadScanWithPolling(scanId);
  }

  get isRunning(): boolean {
    return this.scanService.isRunning(this.scan?.status);
  }

  get isCompleted(): boolean {
    return this.scan?.status === 'COMPLETED';
  }

  get isFailed(): boolean {
    return this.scan?.status === 'FAILED';
  }

  get showNoIssuesMessage(): boolean {
    return !this.isRunning && this.issues.length === 0;
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

  private loadScanWithPolling(scanId: number): void {
    timer(0, ScanDetailPage.POLLING_INTERVAL_MS)
      .pipe(
        switchMap(() => this.scanService.getScanDetailData(scanId)),
        tap((scanDetail) => {
          this.scan = scanDetail.scan;
          this.summary = scanDetail.summary;
          this.issues = scanDetail.issues;
          this.loading = false;
          this.error = null;
          this.isPolling = this.scanService.isRunning(scanDetail.scan.status);
          this.changeDetectorRef.markForCheck();
        }),
        takeWhile((scanDetail) => this.scanService.isRunning(scanDetail.scan.status), true),
        catchError(() => {
          this.error = 'Could not load scan details. Please try again.';
          this.loading = false;
          this.isPolling = false;
          this.changeDetectorRef.markForCheck();
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        complete: () => {
          this.isPolling = false;
          this.changeDetectorRef.markForCheck();
        },
      });
  }
}
