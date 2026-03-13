import { Component, computed, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Title } from '@angular/platform-browser';
import { IsActiveMatchOptions, Router, RouterOutlet } from '@angular/router';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';

import { AppLanguage, LanguageService } from './core/i18n/language.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, TranslocoPipe],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private static readonly FALLBACK_TITLES: Record<AppLanguage, string> = {
    es: 'Accessibility Copilot | Espacio de escaneo',
    en: 'Accessibility Copilot | Scanning workspace',
  };

  private readonly router = inject(Router);
  private readonly titleService = inject(Title);
  private readonly translocoService = inject(TranslocoService);
  private readonly languageService = inject(LanguageService);
  private readonly destroyRef = inject(DestroyRef);

  private readonly matchOptions: IsActiveMatchOptions = {
    paths: 'subset',
    queryParams: 'ignored',
    matrixParams: 'ignored',
    fragment: 'ignored',
  };

  readonly availableLanguages = this.languageService.availableLanguages;
  readonly selectedLanguage = computed(() => this.languageService.activeLanguage());
  readonly selectedLanguageFlagPath = computed(() => this.flagPathByLanguage(this.selectedLanguage()));

  constructor() {
    this.languageService.initializeLanguage();
    this.updateDocumentLanguageMetadata();
    this.titleService.setTitle(App.FALLBACK_TITLES[this.selectedLanguage()]);

    this.translocoService
      .selectTranslate('app.meta.title')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((translatedTitle) => {
        this.titleService.setTitle(this.resolveDocumentTitle(translatedTitle, this.selectedLanguage()));
      });

    this.translocoService.langChanges$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.updateDocumentLanguageMetadata());
  }

  setLanguage(language: AppLanguage): void {
    this.languageService.setLanguage(language);
  }

  onLanguageChange(event: Event): void {
    const selectedValue = (event.target as HTMLSelectElement | null)?.value;
    if (selectedValue === 'es' || selectedValue === 'en') {
      this.setLanguage(selectedValue);
    }
  }

  navigateTo(path: '/projects' | '/scans', event: MouseEvent): void {
    event.preventDefault();
    this.router.navigateByUrl(path);
  }

  isProjectsRouteActive(): boolean {
    return this.router.isActive('/projects', this.matchOptions);
  }

  isScansRouteActive(): boolean {
    return this.router.isActive('/scans', this.matchOptions);
  }

  private updateDocumentLanguageMetadata(): void {
    const language = this.selectedLanguage();
    document.documentElement.lang = language;
    document.documentElement.dir = this.resolveDirection(language);
  }

  private resolveDocumentTitle(translatedTitle: string, language: AppLanguage): string {
    if (!translatedTitle || translatedTitle === 'app.meta.title') {
      return App.FALLBACK_TITLES[language];
    }

    return translatedTitle;
  }

  private resolveDirection(language: AppLanguage): 'ltr' | 'rtl' {
    if (language === 'es' || language === 'en') {
      return 'ltr';
    }

    return 'ltr';
  }

  private flagPathByLanguage(language: AppLanguage): string {
    if (language === 'es') {
      return 'images/flags/es.svg';
    }

    return 'images/flags/en.svg';
  }
}