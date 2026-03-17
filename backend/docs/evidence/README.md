# Evidencia visual (GIF) para LinkedIn

Objetivo: grabar un GIF corto (20-45s) mostrando el flujo completo con tu frontend (en VS Code) + backend.

## Guion recomendado

1. Levantas backend y frontend.
2. Creas un proyecto desde UI.
3. Lanzas un scan.
4. Abres detalle del scan:
   - estado (`RUNNING` -> `COMPLETED` o `FAILED`)
   - issues detectados
   - resumen por severidad
5. Cierre rapido mostrando API (`/swagger-ui/index.html`) o logs.

## Preparacion

- Usa una URL publica y estable para demo (evita dominios internos).
- Si activas whitelist, incluye el dominio demo en `SCANNER_SECURITY_ALLOWED_DOMAINS`.
- Ten una base de datos limpia para que el flujo salga en una sola toma.

## Herramientas utiles

- Windows: ScreenToGif, ShareX o Clipchamp.
- macOS: Kap o CleanShot.
- Linux: Peek.

## Buenas practicas

- Resolucion: 1280x720 o 1366x768.
- Duracion: 20-45 segundos.
- Peso: ideal < 15 MB.
- Destaca con zoom/mouse los 3 hitos: crear proyecto, lanzar scan, ver resumen.

## Nombres y ubicacion

Guarda en:
- `docs/evidence/demo-flow.gif`
- opcional: `docs/evidence/demo-flow.mp4`

## Checklist final antes de publicar

- [ ] GIF corto y legible.
- [ ] README actualizado con stack + arquitectura + CI + seguridad.
- [ ] Repo sin secretos (`.env` no versionado).
- [ ] Captura de pantalla adicional del pipeline en verde (opcional).

