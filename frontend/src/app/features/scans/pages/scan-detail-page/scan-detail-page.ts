import { ChangeDetectionStrategy, ChangeDetectorRef, Component, computed, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { ScanResponse } from '../../../../api-client/model/scanResponse';
import { ProjectService } from '../../../../core/services/project.service';

type Scan = ScanResponse;

@Component({
  selector: 'app-scan-detail-page',
  imports: [CommonModule, DatePipe, RouterLink],
  templateUrl: './scan-detail-page.html',
  styleUrl: './scan-detail-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ScanDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly projectService = inject(ProjectService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  loading = true;
  error: string | null = null;
  scan: Scan | null = null;

  readonly durationText = computed(() => {
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
  });

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

    this.projectService.getScanById(scanId).subscribe({
      next: (scan) => {
        this.scan = scan;
        this.loading = false;
        this.changeDetectorRef.markForCheck();
      },
      error: () => {
        this.error = 'Could not load scan details. Please try again.';
        this.loading = false;
        this.changeDetectorRef.markForCheck();
      },
    });
  }
}
