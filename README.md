# Accessibility Copilot

Plataforma full stack para auditar accesibilidad web sobre sitios públicos, lanzar escaneos automatizados y revisar incidencias WCAG desde una interfaz pensada para portfolio.

El proyecto combina una API en Spring Boot, un frontend en Angular y un motor de escaneo basado en Playwright + axe-core. Los resultados se persisten en PostgreSQL para mantener histórico de proyectos, ejecuciones e incidencias detectadas.

Actualmente es un MVP funcional centrado en el flujo principal de auditoría: crear proyecto, lanzar scan, persistir resultados y revisar incidencias desde UI.

## Qué resuelve

- Permite registrar proyectos con una URL raíz.
- Ejecuta escaneos asíncronos de accesibilidad sobre páginas públicas.
- Detecta incidencias automáticas con axe-core.
- Guarda resultados, resumen por severidad y detalle técnico de cada issue.
- Expone una UI donde revisar histórico, estado del scan y guía de remediación.
- Mantiene contrato API documentado con OpenAPI para sincronizar frontend y backend.

## Stack

- Frontend: Angular 21, TypeScript, RxJS, Transloco, SCSS.
- Backend: Java 21, Spring Boot, Spring MVC, Spring Data JPA, Flyway, MapStruct.
- Scanner: Playwright + axe-core ejecutado desde el backend.
- Base de datos: PostgreSQL.
- Infra local: Docker Compose para backend + PostgreSQL.
- Calidad: GitHub Actions, tests unitarios, Checkstyle, SpotBugs y JaCoCo en backend.

## Arquitectura

### Frontend

- Arquitectura por features: projects, scans e issues.
- Rutas lazy y componentes standalone.
- Cliente API generado desde OpenAPI en [frontend/src/app/api-client](frontend/src/app/api-client).
- Internacionalización runtime en español e inglés.

### Backend

- Enfoque Ports and Adapters / Hexagonal.
- Casos de uso en application, dominio desacoplado en domain e infraestructura en adapters.
- Persistencia relacional con JPA + Flyway.
- Escaneo asíncrono para no bloquear la petición HTTP que inicia el scan.

### Flujo principal

1. El usuario crea un proyecto desde el frontend.
2. El frontend llama al backend para lanzar un scan sobre la URL raíz del proyecto.
3. El backend ejecuta Playwright en segundo plano, inyecta axe-core y recoge incidencias.
4. Los resultados se almacenan en PostgreSQL.
5. El frontend consulta detalle del scan, resumen por severidad e issues detectados.

## Funcionalidades actuales

- Alta y listado de proyectos.
- Ejecución de scans asíncronos.
- Histórico de scans por proyecto.
- Vista global de historial con filtros por proyecto y estado.
- Detalle de scan con polling mientras la ejecución sigue en curso.
- Visualización de issues con severidad, criterio WCAG, selector, snippet HTML y recomendación.
- Guía WCAG integrada en frontend.
- Soporte i18n es/en.
- Documentación OpenAPI y Swagger UI en backend.
- Política básica de seguridad para evitar destinos inseguros en el escaneo.

## Estructura del workspace

```text
.
|-- backend/      API REST, persistencia, seguridad de escaneo y motor Playwright + axe-core
|-- frontend/     Aplicación Angular para gestionar proyectos, scans e incidencias
|-- docs/         Documentación general del workspace
|-- infra/        Infraestructura adicional y experimentación
`-- scanner-worker/ soporte futuro / trabajo auxiliar del scanner
```

## Puesta en marcha

### Requisitos

- JDK 21
- Node.js 20+
- npm 10+
- Docker y Docker Compose

### Opción recomendada: levantar PostgreSQL y backend con Docker

Desde [backend](backend):

```bash
cp .env.example .env
docker compose up --build
```

Esto arranca:

- PostgreSQL 17 en el puerto 5433 del host.
- Backend Spring Boot en http://localhost:8080.

### Frontend en local

Desde [frontend](frontend):

```bash
npm install
npm run start
```

La aplicación queda disponible en http://localhost:4200.

### Opción alternativa: backend local fuera de Docker

Si prefieres ejecutar la API localmente, el backend espera PostgreSQL y usa por defecto:

- Base de datos: accessibility_copilot
- Usuario: accessibility
- Password: accessibility
- Puerto: 5433

Ejemplo en PowerShell desde [backend](backend):

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/accessibility_copilot"
$env:SPRING_DATASOURCE_USERNAME="accessibility"
$env:SPRING_DATASOURCE_PASSWORD="accessibility"
./mvnw.cmd spring-boot:run
```

## Contrato API

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

El frontend consume el contrato del backend y puede regenerar el cliente con:

```bash
npm run api:pull
npm run api:generate
```

O en un solo paso:

```bash
npm run api:sync
```

## Calidad y validación

### Backend

Desde [backend](backend):

```bash
./mvnw -B verify
```

Incluye:

- Tests.
- Checkstyle.
- SpotBugs.
- JaCoCo con umbral mínimo de cobertura.

### Frontend

Desde [frontend](frontend):

```bash
npm run test -- --watch=false
npm run build
```

## Seguridad de escaneo

El backend incorpora una política mínima para reducir riesgo SSRF en el escaneo:

- Solo permite URLs http/https.
- Puede forzar allowlist de dominios.
- Bloquea localhost, loopback y redes privadas cuando la protección está activa.

Variables relevantes:

- SCANNER_SECURITY_ENFORCE_WHITELIST
- SCANNER_SECURITY_ALLOWED_DOMAINS
- SCANNER_SECURITY_BLOCK_PRIVATE_NETWORKS

## Puntos técnicos destacables para publicación

- Proyecto full stack con separación clara entre UI, API, persistencia y motor de escaneo.
- Integración real con PostgreSQL y migraciones versionadas por Flyway.
- Cliente Angular generado desde OpenAPI para evitar desalineación entre frontend y backend.
- Enfoque pragmático de accesibilidad: no solo detecta issues, también ofrece contexto y guía de remediación.
- Base preparada para evolucionar a autenticación, multiusuario, colas de trabajo y analítica histórica.

## Demo y material para LinkedIn

En [backend/docs/evidence/README.md](backend/docs/evidence/README.md) tienes un guion corto para grabar una demo mostrando:

1. Creación de proyecto.
2. Lanzamiento de scan.
3. Cambio de estado RUNNING a COMPLETED o FAILED.
4. Visualización de issues y resumen por severidad.
5. Cierre rápido con Swagger UI o logs.

## Documentación adicional

- [backend/README.md](backend/README.md): detalle técnico del backend.
- [frontend/README.md](frontend/README.md): detalle técnico del frontend.
- [frontend/docs/design-system.md](frontend/docs/design-system.md): sistema visual del frontend.

## Limitaciones actuales

- No hay autenticación ni control de acceso.
- El escaneo está orientado a MVP y puede ampliarse con colas, retry y políticas más avanzadas.
- No hay e2e automatizado en este repo.
- El despliegue productivo no está documentado todavía.

## Evolución futura

Las siguientes líneas no están implementadas todavía; son mejoras naturales para una siguiente iteración del proyecto.

- Autenticación y multiusuario.
- Dashboard con métricas históricas por proyecto.
- Cola de trabajos y control de concurrencia para scans.
- E2E con Playwright para flujos críticos.
- Demo desplegada y capturas/GIF embebidas en el README.
