import { inject, Injectable, signal } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';

export type AppLanguage = 'es' | 'en';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private static readonly STORAGE_KEY = 'ac.language';

  private readonly translocoService = inject(TranslocoService);

  readonly availableLanguages: AppLanguage[] = ['es', 'en'];
  readonly activeLanguage = signal<AppLanguage>('es');

  initializeLanguage(): void {
    const initialLanguage = this.resolveInitialLanguage();
    this.setLanguage(initialLanguage);
  }

  setLanguage(language: AppLanguage): void {
    this.activeLanguage.set(language);
    this.translocoService.setActiveLang(language);
    localStorage.setItem(LanguageService.STORAGE_KEY, language);
  }

  private resolveInitialLanguage(): AppLanguage {
    const storedLanguage = localStorage.getItem(LanguageService.STORAGE_KEY);
    if (this.isSupportedLanguage(storedLanguage)) {
      return storedLanguage;
    }

    const browserLanguage = navigator.language.toLowerCase().startsWith('en') ? 'en' : 'es';
    return browserLanguage;
  }

  private isSupportedLanguage(value: string | null): value is AppLanguage {
    return value === 'es' || value === 'en';
  }
}
