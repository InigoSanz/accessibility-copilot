import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'projects',
    pathMatch: 'full',
  },
  {
    path: 'scans',
    pathMatch: 'full',
    loadComponent: () =>
      import('./features/scans/pages/scan-history-page/scan-history-page').then(
        (m) => m.ScanHistoryPage,
      ),
  },
  {
    path: 'scans/:id',
    loadComponent: () =>
      import('./features/scans/pages/scan-detail-page/scan-detail-page').then(
        (m) => m.ScanDetailPage,
      ),
  },
  {
    path: 'wcag-guide',
    pathMatch: 'full',
    loadComponent: () =>
      import('./layout/pages/wcag-guide-page/wcag-guide-page').then(
        (m) => m.WcagGuidePage,
      ),
  },
  {
    path: 'projects/:id',
    loadComponent: () =>
      import('./features/projects/pages/project-detail-page/project-detail-page').then(
        (m) => m.ProjectDetailPage,
      ),
  },
  {
    path: 'projects',
    pathMatch: 'full',
    loadComponent: () =>
      import('./features/projects/pages/project-list-page/project-list-page').then(
        (m) => m.ProjectListPage,
      ),
  },
  {
    path: '**',
    loadComponent: () =>
      import('./layout/pages/not-found-page/not-found-page').then(
        (m) => m.NotFoundPage,
      ),
  },
];