import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'app-wcag-guide-page',
  imports: [RouterLink, TranslocoPipe],
  templateUrl: './wcag-guide-page.html',
  styleUrl: './wcag-guide-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WcagGuidePage implements OnInit {
  private readonly route = inject(ActivatedRoute);

  returnPath: string | null = null;
  returnFragment: string | null = null;

  get canReturnToIssue(): boolean {
    return !!this.returnPath;
  }

  ngOnInit(): void {
    const queryParams = this.route.snapshot.queryParamMap;
    const returnPathParam = queryParams.get('returnPath');
    const returnFragmentParam = queryParams.get('returnFragment');

    if (this.isValidReturnPath(returnPathParam)) {
      this.returnPath = returnPathParam;
      this.returnFragment = returnFragmentParam;
    }
  }

  private isValidReturnPath(value: string | null): value is string {
    if (!value || !value.startsWith('/')) {
      return false;
    }

    return value.startsWith('/scans/');
  }
}
