# Accessibility Copilot - Frontend

Frontend de **auditoría de accesibilidad web** construido con Angular 21. Permite crear proyectos, lanzar escaneos, revisar histórico y analizar issues de accesibilidad con guía de remediación.

## Qué hace

- Lista y crea proyectos de auditoría.
- Lanza escaneos desde el frontend contra el backend.
- Visualiza detalle de escaneo, resumen por severidad e issues detectados.
- Incluye guía WCAG para contextualizar hallazgos.
- Soporta internacionalización (`es` / `en`) con carga runtime de traducciones.

## Stack y enfoque técnico

- Angular 21 (standalone components + lazy routes + OnPush en páginas).
- TypeScript estricto + RxJS para orquestación de flujos.
- Cliente API **generado desde OpenAPI** (fuente de verdad en `api/openapi.json`).
- Arquitectura por features (`projects`, `scans`, `issues`) y servicios de dominio en `src/app/core/services`.
- Estilos con SCSS y layout responsive básico.

## Estructura principal

- `src/app/features/projects`: listado de proyectos y detalle.
- `src/app/features/scans`: histórico y detalle de escaneos.
- `src/app/core/services`: integración con cliente generado.
- `src/app/api-client`: SDK generado de OpenAPI (no editar manualmente).
- `public/i18n`: traducciones runtime.

## Requisitos

- Node.js 20+
- npm 10+
- Backend en ejecución (por defecto `http://localhost:8080`)

## Ejecutar en local

```bash
npm install
npm run start
```

Abre `http://localhost:4200`.

## Configuración de API

- Entorno desarrollo: `src/environments/environment.ts`
- Entorno producción: `src/environments/environment.production.ts`

La URL base se configura en `apiBaseUrl`.

## Contrato OpenAPI (flujo recomendado)

Este frontend **no inventa endpoints**: consume el contrato del backend.

```bash
npm run api:pull
npm run api:generate
```

O en un solo paso:

```bash
npm run api:sync
```

Cuando cambie el backend, actualiza contrato + cliente y commitea ambos cambios.

## Calidad

```bash
npm run test
npm run build
```

## Limitaciones actuales

- Sin autenticación/autorización (MVP).
- Sin e2e automatizado en este repositorio.
- La robustez de validación de URLs depende del backend.

## Roadmap sugerido

- Añadir autenticación y vistas multiusuario.
- Incorporar e2e (Playwright) para flujos críticos.
- Añadir métricas históricas de accesibilidad por proyecto.
- Publicar despliegue demo y capturas/gif para portfolio.
