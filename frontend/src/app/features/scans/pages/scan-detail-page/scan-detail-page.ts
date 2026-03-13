import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { EMPTY, timer } from 'rxjs';
import { catchError, switchMap, takeWhile, tap } from 'rxjs/operators';

import { AccessibilityIssueResponse } from '../../../../api-client/model/accessibilityIssueResponse';
import { ScanResponse } from '../../../../api-client/model/scanResponse';
import { ScanSummaryResponse } from '../../../../api-client/model/scanSummaryResponse';
import { ScanService } from '../../../../core/services/scan.service';

interface SummarySeverityItem {
  key: string;
  value: number;
}

interface IssueGuidanceContent {
  why: string;
  impact: string;
  fixSteps: string[];
}

type IssueImpactLevel = 'high' | 'medium' | 'low';

type SupportedGuidanceRule =
  | 'image-alt'
  | 'link-name'
  | 'heading-order'
  | 'landmark-one-main'
  | 'color-contrast'
  | 'button-name'
  | 'label'
  | 'aria-required-attr'
  | 'aria-valid-attr-value'
  | 'document-title'
  | 'html-has-lang'
  | 'bypass'
  | 'frame-title'
  | 'list'
  | 'listitem'
  | 'duplicate-id-aria'
  | 'aria-hidden-focus';

const ISSUE_RULE_ALIASES: Record<string, SupportedGuidanceRule> = {
  'image-alt': 'image-alt',
  'link-name': 'link-name',
  'heading-order': 'heading-order',
  'landmark-one-main': 'landmark-one-main',
  'color-contrast': 'color-contrast',
  'button-name': 'button-name',
  label: 'label',
  'form-field-multiple-labels': 'label',
  'aria-required-attr': 'aria-required-attr',
  'aria-valid-attr': 'aria-valid-attr-value',
  'aria-valid-attr-value': 'aria-valid-attr-value',
  'aria-allowed-attr': 'aria-valid-attr-value',
  'document-title': 'document-title',
  'html-has-lang': 'html-has-lang',
  'html-lang-valid': 'html-has-lang',
  bypass: 'bypass',
  'skip-link': 'bypass',
  'frame-title': 'frame-title',
  list: 'list',
  listitem: 'listitem',
  'duplicate-id-aria': 'duplicate-id-aria',
  'aria-hidden-focus': 'aria-hidden-focus',
};

const ISSUE_GUIDANCE_CATALOG: Record<'es' | 'en', Record<SupportedGuidanceRule, IssueGuidanceContent>> = {
  es: {
    'image-alt': {
      why: 'El elemento visual no tiene texto alternativo o es vacío, por eso el lector de pantalla no puede describirlo.',
      impact: 'Usuarios con baja o nula visión pierden contexto y pueden no entender acciones o contenido clave.',
      fixSteps: [
        'Si la imagen aporta información, añade un atributo alt descriptivo y breve.',
        'Si la imagen es decorativa, usa alt vacío (alt="") para que se ignore correctamente.',
        'Si la imagen funciona como botón/enlace, verifica también que el control tenga nombre accesible.',
      ],
    },
    'link-name': {
      why: 'El enlace no tiene nombre accesible (sin texto visible o sin etiqueta ARIA válida).',
      impact: 'Quien navega con lector de pantalla o por teclado no sabe a dónde lleva ese enlace.',
      fixSteps: [
        'Asegura texto visible claro dentro del enlace.',
        'Si usas solo icono, añade aria-label con un verbo/objetivo claro.',
        'Evita nombres genéricos como “click aquí” y describe el destino.',
      ],
    },
    'heading-order': {
      why: 'La jerarquía de encabezados salta niveles o está desordenada (por ejemplo, de h1 a h4).',
      impact: 'La estructura de la página se vuelve confusa para tecnologías de asistencia y navegación rápida.',
      fixSteps: [
        'Ordena encabezados en secuencia lógica (h1, h2, h3...).',
        'No uses encabezados solo para “hacer texto grande”; usa CSS para estilo visual.',
        'Mantén una sola estructura semántica coherente por sección.',
      ],
    },
    'landmark-one-main': {
      why: 'La página no define una región principal con <main> o role="main".',
      impact: 'Personas que usan atajos de navegación no pueden saltar rápido al contenido principal.',
      fixSteps: [
        'Envuelve el contenido principal en una etiqueta <main>.',
        'Asegúrate de que exista solo un main por página vista.',
        'Deja fuera de <main> navegación global, header y footer.',
      ],
    },
    'color-contrast': {
      why: 'El contraste entre texto y fondo no alcanza el mínimo recomendado WCAG.',
      impact: 'Texto difícil de leer para usuarios con baja visión, fatiga visual o en pantallas con brillo limitado.',
      fixSteps: [
        'Aumenta contraste entre color de texto y fondo.',
        'Para texto normal, apunta al menos a 4.5:1; para texto grande, 3:1.',
        'Revalida con herramienta de contraste antes de publicar.',
      ],
    },
    'button-name': {
      why: 'El botón no tiene nombre accesible claro.',
      impact: 'Usuarios de lector de pantalla oyen “botón” sin contexto y no saben qué acción ejecuta.',
      fixSteps: [
        'Asegura un texto visible dentro del botón o aria-label descriptivo.',
        'Si es un icono, incluye etiqueta accesible con la acción exacta.',
        'Evita labels ambiguas como “más” sin contexto.',
      ],
    },
    label: {
      why: 'Un campo de formulario no está correctamente asociado con su etiqueta.',
      impact: 'Usuarios de lector de pantalla o dictado por voz no identifican claramente qué dato deben introducir.',
      fixSteps: [
        'Asegura una etiqueta visible por campo usando <label for="id">.',
        'Verifica que el id del input coincida con el for del label.',
        'Si no hay label visible por diseño, añade aria-label o aria-labelledby válido.',
      ],
    },
    'aria-required-attr': {
      why: 'Se está usando un rol ARIA sin los atributos obligatorios para ese rol.',
      impact: 'Las tecnologías de asistencia pueden interpretar mal el componente y su estado.',
      fixSteps: [
        'Revisa el rol aplicado y sus atributos requeridos según especificación ARIA.',
        'Añade los atributos faltantes con valores válidos.',
        'Si no necesitas ARIA, prioriza HTML nativo semántico para reducir errores.',
      ],
    },
    'aria-valid-attr-value': {
      why: 'Hay atributos ARIA inválidos, mal escritos o con valores no permitidos.',
      impact: 'Lectores de pantalla pueden ignorar el atributo o anunciar información incorrecta.',
      fixSteps: [
        'Corrige nombres ARIA (por ejemplo aria-expanded, aria-controls).',
        'Usa solo valores permitidos (true/false, ids existentes, etc.).',
        'Elimina ARIA innecesario cuando HTML nativo ya cubre el caso.',
      ],
    },
    'document-title': {
      why: 'La página no define un título de documento útil o está vacío.',
      impact: 'Usuarios no distinguen la vista actual en pestañas, historial y lectores de pantalla.',
      fixSteps: [
        'Define un título único y descriptivo por página/vista.',
        'Incluye contexto de la sección y nombre del producto.',
        'Evita títulos genéricos repetidos como “Home” en todas las rutas.',
      ],
    },
    'html-has-lang': {
      why: 'El documento no especifica correctamente el idioma principal.',
      impact: 'La pronunciación en lectores de pantalla puede ser incorrecta y afectar comprensión.',
      fixSteps: [
        'Define lang en el elemento html (ej.: es, en).',
        'Mantén el valor sincronizado con el idioma visible de la interfaz.',
        'Si hay fragmentos en otro idioma, usa lang en esos elementos puntuales.',
      ],
    },
    bypass: {
      why: 'No existe un mecanismo claro para saltar bloques repetitivos (navegación, header, etc.).',
      impact: 'Usuarios de teclado deben recorrer demasiados elementos antes de llegar al contenido principal.',
      fixSteps: [
        'Incluye un enlace “Saltar al contenido principal” visible al enfocar.',
        'Asegura una región <main> única y navegable.',
        'Verifica que el enlace de salto funciona con teclado.',
      ],
    },
    'frame-title': {
      why: 'Un iframe no tiene título accesible.',
      impact: 'Usuarios no entienden qué contenido embebido están entrando a revisar.',
      fixSteps: [
        'Añade atributo title descriptivo al iframe.',
        'Usa una descripción breve del propósito del contenido embebido.',
        'Si el iframe es decorativo, considera eliminarlo o evitar foco innecesario.',
      ],
    },
    list: {
      why: 'La estructura de lista no está implementada semánticamente de forma correcta.',
      impact: 'Lectores de pantalla no anuncian bien número de elementos o jerarquía de la lista.',
      fixSteps: [
        'Usa <ul>/<ol> para colecciones y <li> para cada elemento.',
        'Evita simular listas solo con div y estilos visuales.',
        'No rompas la semántica insertando elementos no permitidos como hijos directos.',
      ],
    },
    listitem: {
      why: 'Existe un item de lista fuera de un contenedor de lista válido.',
      impact: 'La navegación estructural de listas se rompe para tecnologías de asistencia.',
      fixSteps: [
        'Asegura que cada <li> esté dentro de <ul> o <ol>.',
        'Evita usar role="listitem" sin un contenedor role="list" coherente.',
        'Reestructura el HTML para mantener jerarquía semántica correcta.',
      ],
    },
    'duplicate-id-aria': {
      why: 'Hay IDs duplicados referenciados por ARIA (aria-labelledby, aria-describedby, etc.).',
      impact: 'Los lectores de pantalla pueden enlazar etiquetas equivocadas o fallar al resolver referencias.',
      fixSteps: [
        'Haz únicos todos los id del documento.',
        'Revisa atributos ARIA que referencian ids y corrige apuntadores rotos/duplicados.',
        'En componentes repetidos, genera ids dinámicos únicos.',
      ],
    },
    'aria-hidden-focus': {
      why: 'Hay elementos enfocables dentro de contenedores marcados como aria-hidden.',
      impact: 'Usuarios de teclado pueden caer en elementos invisibles para lector de pantalla, creando una experiencia inconsistente.',
      fixSteps: [
        'Evita foco en contenido oculto con aria-hidden.',
        'Quita tabindex/enlaces/botones activos de regiones ocultas.',
        'Usa estrategias de ocultación/foco coherentes (por ejemplo, inert o gestión de focus trap).',
      ],
    },
  },
  en: {
    'image-alt': {
      why: 'The visual element has missing or empty alternative text, so screen readers cannot describe it.',
      impact: 'Users with low or no vision may miss context and key actions/content.',
      fixSteps: [
        'If the image is informative, add a short descriptive alt attribute.',
        'If the image is decorative, use empty alt (alt="") so it is ignored properly.',
        'If the image is a button/link, ensure the control also has an accessible name.',
      ],
    },
    'link-name': {
      why: 'The link has no accessible name (missing visible text or invalid ARIA label).',
      impact: 'Screen reader and keyboard users cannot understand the destination of the link.',
      fixSteps: [
        'Provide clear visible link text.',
        'For icon-only links, add a meaningful aria-label.',
        'Avoid generic names like “click here”; describe the destination.',
      ],
    },
    'heading-order': {
      why: 'Heading hierarchy skips levels or is out of order (for example h1 to h4).',
      impact: 'Page structure becomes confusing for assistive technology and quick heading navigation.',
      fixSteps: [
        'Use a logical heading sequence (h1, h2, h3...).',
        'Do not use headings only for visual styling; use CSS for size.',
        'Keep a consistent semantic structure per section.',
      ],
    },
    'landmark-one-main': {
      why: 'The page does not define a main landmark with <main> or role="main".',
      impact: 'Users relying on landmark navigation cannot quickly jump to primary content.',
      fixSteps: [
        'Wrap primary page content in a <main> element.',
        'Ensure only one main region exists per page view.',
        'Keep global header/nav/footer outside <main>.',
      ],
    },
    'color-contrast': {
      why: 'Text/background contrast does not meet minimum WCAG threshold.',
      impact: 'Text becomes hard to read for low-vision users or in adverse viewing conditions.',
      fixSteps: [
        'Increase contrast between text color and background.',
        'For normal text target at least 4.5:1; for large text 3:1.',
        'Re-check with a contrast tool before release.',
      ],
    },
    'button-name': {
      why: 'The button has no clear accessible name.',
      impact: 'Screen reader users hear “button” without knowing the action.',
      fixSteps: [
        'Provide visible button text or a meaningful aria-label.',
        'For icon-only buttons, include an explicit action label.',
        'Avoid ambiguous labels like “more” without context.',
      ],
    },
    label: {
      why: 'A form field is not properly associated with its label.',
      impact: 'Screen reader and voice-input users cannot clearly identify what data is expected.',
      fixSteps: [
        'Provide a visible label per field using <label for="id">.',
        'Ensure input id matches the label for attribute.',
        'If no visible label is possible, add a valid aria-label or aria-labelledby.',
      ],
    },
    'aria-required-attr': {
      why: 'An ARIA role is used without its required ARIA attributes.',
      impact: 'Assistive technologies may misinterpret the component and its state.',
      fixSteps: [
        'Review the applied role and required ARIA attributes.',
        'Add missing required attributes with valid values.',
        'Prefer native semantic HTML when possible to reduce ARIA complexity.',
      ],
    },
    'aria-valid-attr-value': {
      why: 'There are invalid ARIA attributes or invalid ARIA values.',
      impact: 'Screen readers may ignore attributes or announce incorrect information.',
      fixSteps: [
        'Fix ARIA attribute names (e.g. aria-expanded, aria-controls).',
        'Use only allowed values (true/false, existing ids, etc.).',
        'Remove unnecessary ARIA when native HTML already provides semantics.',
      ],
    },
    'document-title': {
      why: 'The page has no useful document title, or it is empty.',
      impact: 'Users cannot easily identify the current page in tabs, history, or assistive output.',
      fixSteps: [
        'Set a unique, descriptive title per page/view.',
        'Include section context plus product name.',
        'Avoid repeated generic titles like “Home” across all routes.',
      ],
    },
    'html-has-lang': {
      why: 'The document does not correctly declare the primary language.',
      impact: 'Screen readers may pronounce content incorrectly and reduce comprehension.',
      fixSteps: [
        'Set lang on the html element (e.g. es, en).',
        'Keep lang synchronized with the visible UI language.',
        'For mixed-language fragments, set lang on the specific element.',
      ],
    },
    bypass: {
      why: 'There is no reliable way to bypass repeated blocks (navigation/header/etc.).',
      impact: 'Keyboard users must tab through too many repeated controls before reaching main content.',
      fixSteps: [
        'Add a visible-on-focus “Skip to main content” link.',
        'Ensure a single navigable <main> region exists.',
        'Verify skip behavior using keyboard only.',
      ],
    },
    'frame-title': {
      why: 'An iframe has no accessible title.',
      impact: 'Users cannot understand the purpose of embedded content before entering it.',
      fixSteps: [
        'Add a descriptive title attribute on the iframe.',
        'Keep the description concise and purpose-oriented.',
        'If decorative, avoid unnecessary focusability.',
      ],
    },
    list: {
      why: 'List structure is not implemented with proper semantics.',
      impact: 'Screen readers may not announce item count or list hierarchy correctly.',
      fixSteps: [
        'Use <ul>/<ol> for collections and <li> for each item.',
        'Do not simulate lists only with divs and CSS.',
        'Avoid invalid child structure that breaks list semantics.',
      ],
    },
    listitem: {
      why: 'A list item is used outside a valid list container.',
      impact: 'Assistive list navigation and structure announcements become unreliable.',
      fixSteps: [
        'Ensure each <li> is inside <ul> or <ol>.',
        'Avoid role="listitem" without coherent role="list" container.',
        'Refactor HTML to keep semantic hierarchy consistent.',
      ],
    },
    'duplicate-id-aria': {
      why: 'Duplicate IDs are referenced by ARIA attributes (aria-labelledby, aria-describedby, etc.).',
      impact: 'Screen readers can resolve wrong labels/descriptions or fail references.',
      fixSteps: [
        'Make all document ids unique.',
        'Review ARIA id references and fix broken/duplicate targets.',
        'In repeated components, generate unique dynamic ids.',
      ],
    },
    'aria-hidden-focus': {
      why: 'Focusable elements exist inside containers marked as aria-hidden.',
      impact: 'Keyboard users can tab into controls hidden from assistive technologies, causing inconsistent UX.',
      fixSteps: [
        'Prevent focus on content hidden with aria-hidden.',
        'Remove tabindex/active controls from hidden regions.',
        'Use consistent hide/focus management (e.g., inert or proper focus trapping).',
      ],
    },
  },
};

@Component({
  selector: 'app-scan-detail-page',
  imports: [CommonModule, DatePipe, RouterLink, TranslocoPipe],
  templateUrl: './scan-detail-page.html',
  styleUrl: './scan-detail-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ScanDetailPage implements OnInit {
  private static readonly POLLING_INTERVAL_MS = 4000;
  private static readonly SUMMARY_SEVERITY_ORDER = ['critical', 'serious', 'moderate', 'minor'] as const;
  private static readonly ISSUE_HIGHLIGHT_DURATION_MS = 2600;

  private readonly route = inject(ActivatedRoute);
  private readonly scanService = inject(ScanService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);
  private readonly translocoService = inject(TranslocoService);

  loading = true;
  error: string | null = null;
  isPolling = false;
  scan: ScanResponse | null = null;
  summary: ScanSummaryResponse | null = null;
  issues: AccessibilityIssueResponse[] = [];
  private readonly expandedIssueKeys = new Set<string>();
  highlightedIssueKey: string | null = null;
  private highlightResetTimeoutId: number | null = null;

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
      return this.translocoService.translate('scanDetail.duration.secondsOnly', { seconds });
    }

    return this.translocoService.translate('scanDetail.duration.minutesAndSeconds', { minutes, seconds });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const scanId = Number(idParam);

    if (!idParam || !Number.isInteger(scanId) || scanId <= 0) {
      this.loading = false;
      this.error = this.translocoService.translate('scanDetail.messages.invalidScanId');
      this.changeDetectorRef.markForCheck();
      return;
    }

    this.loading = true;
    this.error = null;

    this.route.fragment.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((fragment) => {
      this.highlightIssueFromFragment(fragment);
    });

    this.destroyRef.onDestroy(() => {
      if (this.highlightResetTimeoutId !== null) {
        window.clearTimeout(this.highlightResetTimeoutId);
      }
    });

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

  get orderedSummarySeverities(): SummarySeverityItem[] {
    const bySeverity = this.summary?.bySeverity;
    if (!bySeverity) {
      return [];
    }

    const rankBySeverity = new Map<string, number>(
      ScanDetailPage.SUMMARY_SEVERITY_ORDER.map((severity, index) => [severity, index]),
    );

    return Object.entries(bySeverity)
      .map(([key, value]) => ({ key, value }))
      .sort((left, right) => {
        const leftRank = rankBySeverity.get(left.key.toLowerCase()) ?? Number.MAX_SAFE_INTEGER;
        const rightRank = rankBySeverity.get(right.key.toLowerCase()) ?? Number.MAX_SAFE_INTEGER;

        if (leftRank !== rightRank) {
          return leftRank - rightRank;
        }

        return left.key.localeCompare(right.key);
      });
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

  issueSeverityClass(severity: string | undefined): string {
    return `severity-badge ${this.severityModifier(severity)}`;
  }

  issueKey(issue: AccessibilityIssueResponse, index: number): string {
    if (typeof issue.id === 'number') {
      return `issue-${issue.id}`;
    }

    return `issue-${issue.ruleCode ?? 'unknown'}-${index}`;
  }

  isIssueExpanded(issue: AccessibilityIssueResponse, index: number): boolean {
    return this.expandedIssueKeys.has(this.issueKey(issue, index));
  }

  toggleIssueGuidance(issue: AccessibilityIssueResponse, index: number): void {
    const key = this.issueKey(issue, index);

    if (this.expandedIssueKeys.has(key)) {
      this.expandedIssueKeys.delete(key);
    } else {
      this.expandedIssueKeys.add(key);
    }
  }

  issueGuidance(issue: AccessibilityIssueResponse): IssueGuidanceContent {
    const language = this.activeGuidanceLanguage();
    const matchedRule = this.matchSupportedRule(issue.ruleCode);

    if (matchedRule) {
      return ISSUE_GUIDANCE_CATALOG[language][matchedRule];
    }

    return {
      why: this.translocoService.translate('scanDetail.issueGuide.fallback.why'),
      impact: this.translocoService.translate('scanDetail.issueGuide.fallback.impact'),
      fixSteps: [
        this.translocoService.translate('scanDetail.issueGuide.fallback.step1'),
        this.translocoService.translate('scanDetail.issueGuide.fallback.step2'),
        this.translocoService.translate('scanDetail.issueGuide.fallback.step3'),
      ],
    };
  }

  issueRecommendedFix(issue: AccessibilityIssueResponse): string | null {
    const recommendation = issue.recommendation?.trim();
    if (recommendation) {
      return recommendation;
    }

    return null;
  }

  issueImpactLevel(issue: AccessibilityIssueResponse): IssueImpactLevel {
    const normalizedSeverity = this.normalizeSeverity(issue.severity);

    if (normalizedSeverity === 'critical' || normalizedSeverity === 'serious' || normalizedSeverity === 'high') {
      return 'high';
    }

    if (normalizedSeverity === 'moderate' || normalizedSeverity === 'medium') {
      return 'medium';
    }

    if (normalizedSeverity === 'minor' || normalizedSeverity === 'low') {
      return 'low';
    }

    const matchedRule = this.matchSupportedRule(issue.ruleCode);
    if (
      matchedRule === 'image-alt' ||
      matchedRule === 'link-name' ||
      matchedRule === 'button-name' ||
      matchedRule === 'label' ||
      matchedRule === 'color-contrast' ||
      matchedRule === 'aria-hidden-focus'
    ) {
      return 'high';
    }

    if (matchedRule === 'document-title' || matchedRule === 'heading-order' || matchedRule === 'landmark-one-main') {
      return 'medium';
    }

    return 'medium';
  }

  issueImpactClass(issue: AccessibilityIssueResponse): string {
    const level = this.issueImpactLevel(issue);
    return `impact-badge impact-badge--${level}`;
  }

  isIssueHighlighted(issue: AccessibilityIssueResponse, index: number): boolean {
    return this.highlightedIssueKey === this.issueKey(issue, index);
  }

  summarySeverityClass(severity: string | undefined): string {
    return `severity-item ${this.summarySeverityModifier(severity)}`;
  }

  private summarySeverityModifier(severity: string | undefined): string {
    const normalized = this.normalizeSeverity(severity);

    if (normalized === 'critical') {
      return 'severity-item--critical';
    }

    if (normalized === 'serious' || normalized === 'high') {
      return 'severity-item--serious';
    }

    if (normalized === 'moderate' || normalized === 'medium') {
      return 'severity-item--moderate';
    }

    if (normalized === 'minor' || normalized === 'low') {
      return 'severity-item--minor';
    }

    return '';
  }

  private severityModifier(severity: string | undefined): string {
    const normalized = this.normalizeSeverity(severity);

    if (normalized === 'critical') {
      return 'severity-badge--critical';
    }

    if (normalized === 'serious' || normalized === 'high') {
      return 'severity-badge--serious';
    }

    if (normalized === 'moderate' || normalized === 'medium') {
      return 'severity-badge--moderate';
    }

    if (normalized === 'minor' || normalized === 'low') {
      return 'severity-badge--minor';
    }

    return '';
  }

  private normalizeSeverity(value: string | undefined): string {
    return (value ?? '').trim().toLowerCase();
  }

  private highlightIssueFromFragment(fragment: string | null): void {
    if (!fragment || !fragment.startsWith('issue-')) {
      return;
    }

    this.highlightedIssueKey = fragment;

    if (this.highlightResetTimeoutId !== null) {
      window.clearTimeout(this.highlightResetTimeoutId);
    }

    this.highlightResetTimeoutId = window.setTimeout(() => {
      this.highlightedIssueKey = null;
      this.highlightResetTimeoutId = null;
      this.changeDetectorRef.markForCheck();
    }, ScanDetailPage.ISSUE_HIGHLIGHT_DURATION_MS);

    this.changeDetectorRef.markForCheck();
  }

  private activeGuidanceLanguage(): 'es' | 'en' {
    return this.translocoService.getActiveLang().toLowerCase().startsWith('en') ? 'en' : 'es';
  }

  private matchSupportedRule(ruleCode: string | undefined): SupportedGuidanceRule | null {
    const normalizedRule = (ruleCode ?? '').trim().toLowerCase();

    const aliasMatch = ISSUE_RULE_ALIASES[normalizedRule];
    if (aliasMatch) {
      return aliasMatch;
    }

    if (normalizedRule.includes('color-contrast')) {
      return 'color-contrast';
    }

    if (normalizedRule.includes('landmark') && normalizedRule.includes('main')) {
      return 'landmark-one-main';
    }

    if (normalizedRule.includes('heading') && normalizedRule.includes('order')) {
      return 'heading-order';
    }

    return null;
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
          this.error = this.translocoService.translate('scanDetail.messages.loadError');
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
