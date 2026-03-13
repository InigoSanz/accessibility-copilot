import { Component } from '@angular/core';
import { IsActiveMatchOptions, Router, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly matchOptions: IsActiveMatchOptions = {
    paths: 'subset',
    queryParams: 'ignored',
    matrixParams: 'ignored',
    fragment: 'ignored',
  };

  constructor(private readonly router: Router) {}

  isProjectsRouteActive(): boolean {
    return this.router.isActive('/projects', this.matchOptions);
  }

  isScansRouteActive(): boolean {
    return this.router.isActive('/scans', this.matchOptions);
  }
}