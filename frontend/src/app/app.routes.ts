import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'projects',
    pathMatch: 'full',
  },
  {
    path: 'scans/:id',
    loadComponent: () =>
      import('./features/scans/pages/scan-detail-page/scan-detail-page').then(
        (m) => m.ScanDetailPage,
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
];