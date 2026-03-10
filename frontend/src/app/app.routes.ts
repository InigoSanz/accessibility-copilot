import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'projects',
    pathMatch: 'full',
  },
  {
    path: 'projects',
    loadComponent: () =>
      import('./features/projects/pages/project-list-page/project-list-page').then(
        (m) => m.ProjectListPage,
      ),
  },
];