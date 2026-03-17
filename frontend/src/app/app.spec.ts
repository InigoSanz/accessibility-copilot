import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { provideTransloco, TranslocoLoader, translocoConfig } from '@jsverse/transloco';

import { App } from './app';

class MockTranslocoLoader implements TranslocoLoader {
  getTranslation() {
    return of({
      app: {
        meta: { title: 'Accessibility Copilot' },
        brand: { name: 'Accessibility Copilot', tagline: 'A11y scanner' },
        navigation: {
          homeAria: 'Inicio',
          mainAria: 'Navegación principal',
          projects: 'Proyectos',
          wcagGuide: 'Guía WCAG',
          scans: 'Escaneos',
        },
        language: {
          label: 'Idioma',
          options: { es: 'Español', en: 'English' },
        },
        footer: {
          aria: 'Pie de página',
          brand: 'Accessibility Copilot',
          navigationAria: 'Navegación del pie',
        },
      },
    });
  }
}

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        provideTransloco({
          config: translocoConfig({
            availableLangs: ['es', 'en'],
            defaultLang: 'es',
            reRenderOnLangChange: true,
            prodMode: false,
          }),
          loader: MockTranslocoLoader,
        }),
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render main layout shell', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.app-shell')).toBeTruthy();
    expect(compiled.querySelector('.app-topbar')).toBeTruthy();
    expect(compiled.querySelector('.app-main')).toBeTruthy();
  });
});
