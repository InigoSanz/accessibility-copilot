# Accessibility Copilot - Backend

Backend REST para auditar accesibilidad web con **Spring Boot**, **Playwright + axe-core**, persistencia en **PostgreSQL** y arquitectura hexagonal.

## 1) Que hace este proyecto

- Registra proyectos con una URL raiz.
- Lanza escaneos de accesibilidad asincronos.
- Analiza la web con `axe-core` (inyectado en navegador headless con Playwright).
- Guarda issues detectados y expone resumen por severidad.

## 2) Arquitectura

Se organiza con enfoque **Ports & Adapters (Hexagonal)**:

- `domain`: modelos y contratos (`port/in`, `port/out`).
- `application`: casos de uso (`service`).
- `infrastructure/adapter/in/web`: API REST, DTOs y mapeadores.
- `infrastructure/adapter/out/persistence`: JPA + mapeadores.
- `infrastructure/adapter/out/scanner`: adaptador Playwright/axe-core.

Flujo principal:
1. `POST /api/projects/{projectId}/scans` crea scan `RUNNING`.
2. `ScanExecutionService` ejecuta el escaneo async.
3. `PlaywrightWebAccessibilityScannerAdapter` valida URL (seguridad), escanea y transforma resultados.
4. Se guardan issues y el scan pasa a `COMPLETED` o `FAILED`.

## 3) Endpoints

Base URL local: `http://localhost:8080`

- `GET /api/health`
- `POST /api/projects`
- `GET /api/projects`
- `GET /api/projects/{id}`
- `POST /api/projects/{projectId}/scans`
- `GET /api/projects/{projectId}/scans`
- `GET /api/scans/{scanId}`
- `GET /api/scans/{scanId}/issues`
- `GET /api/scans/{scanId}/summary`

OpenAPI UI (si la app esta levantada): `http://localhost:8080/swagger-ui/index.html`

## 4) Como ejecutarlo (local)

Prerequisitos aproximados (segun `pom.xml`):
- JDK 21
- Maven Wrapper (`mvnw` / `mvnw.cmd`)
- PostgreSQL accesible

Variables clave:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_CORS_ALLOWED_ORIGINS`

Ejemplo en Windows PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/accessibility_copilot"
$env:SPRING_DATASOURCE_USERNAME="accessibility"
$env:SPRING_DATASOURCE_PASSWORD="accessibility"
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

## 5) Docker Compose (app + postgres)

Se incluye:
- `docker-compose.yml`
- `Dockerfile`
- `.env.example`

Pasos:

```powershell
Copy-Item .env.example .env
# Edita .env con tus secretos/valores

docker compose up --build
```

## 6) Calidad y CI

Pipeline en `.github/workflows/ci.yml` ejecuta:
- Build + tests (`mvn verify`)
- Checkstyle
- SpotBugs
- JaCoCo (reporte + umbral minimo de cobertura)

Comando local equivalente:

```powershell
.\mvnw.cmd -B clean verify
```

## 7) Seguridad minima de escaneo

Se anade una politica de seguridad para URLs de escaneo:

- Solo `http/https`.
- Bloqueo de redes internas/privadas (anti-SSRF basico): localhost, loopback, link-local, site-local, privadas IPv4/IPv6.
- Whitelist configurable de dominios.

Variables:
- `SCANNER_SECURITY_ENFORCE_WHITELIST` (`true/false`)
- `SCANNER_SECURITY_ALLOWED_DOMAINS` (coma separada, ejemplo: `example.com,midominio.com`)
- `SCANNER_SECURITY_BLOCK_PRIVATE_NETWORKS` (`true/false`)

## 8) Decisiones tecnicas destacables

- Arquitectura hexagonal para desacoplar casos de uso e infraestructura.
- Mapeo DTO <-> dominio con MapStruct.
- Migraciones SQL versionadas con Flyway.
- Escaneo asincrono para no bloquear peticiones HTTP.
- Persistencia relacional para trazabilidad historica de scans/issues.

## 9) Limitaciones actuales

- Un solo backend; falta autenticacion/autorizacion.
- Politica SSRF minima (correcta para MVP, mejorable con DNS pinning/allowlist estricta por tenant).
- No hay rate limiting ni cuotas por proyecto.
- Sin despliegue cloud documentado en este repo.

## 10) Roadmap sugerido

- Auth (JWT/OAuth2) y multiusuario.
- Rate limiting y control de concurrencia por proyecto.
- Retry/backoff para scans y cola de trabajos.
- Dashboard con tendencias historicas por severidad.
- Publicacion automatica de cobertura y badges en README.

## 11) Evidencia visual para LinkedIn

Guia paso a paso en `docs/evidence/README.md`.

## 12) Integracion con frontend (tu app en VS Code)

- Configura `APP_CORS_ALLOWED_ORIGINS` con el origen real de tu frontend.
- Ejemplo: `http://localhost:4200` (Angular) o `http://localhost:5173` (Vite/React).
- Si usas whitelist de escaneo, anade tambien el dominio que uses en las demos.


