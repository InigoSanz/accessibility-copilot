# Accessibility Copilot

Plataforma full stack para auditar accesibilidad web sobre sitios publicos, lanzar escaneos automatizados y revisar incidencias WCAG desde una interfaz pensada para portfolio.

El proyecto combina una API en Spring Boot, un frontend en Angular y un motor de escaneo basado en Playwright + axe-core. Los resultados se persisten en PostgreSQL para mantener historico de proyectos, ejecuciones e incidencias detectadas.

Actualmente es un MVP funcional centrado en el flujo principal de auditoria: crear proyecto, lanzar scan, persistir resultados y revisar incidencias desde UI.

## Que resuelve

- Permite registrar proyectos con una URL raiz.
- Ejecuta escaneos asincronos de accesibilidad sobre paginas publicas.
- Detecta incidencias automaticas con axe-core.
- Guarda resultados, resumen por severidad y detalle tecnico de cada issue.
- Expone una UI donde revisar historico, estado del scan y guia de remediacion.
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
- Internacionalizacion runtime en espanol e ingles.

### Backend

- Enfoque Ports and Adapters / Hexagonal.
- Casos de uso en application, dominio desacoplado en domain e infraestructura en adapters.
- Persistencia relacional con JPA + Flyway.
- Escaneo asincrono para no bloquear la peticion HTTP que inicia el scan.

### Flujo principal

1. El usuario crea un proyecto desde el frontend.
2. El frontend llama al backend para lanzar un scan sobre la URL raiz del proyecto.
3. El backend ejecuta Playwright en segundo plano, inyecta axe-core y recoge incidencias.
4. Los resultados se almacenan en PostgreSQL.
5. El frontend consulta detalle del scan, resumen por severidad e issues detectados.

## Funcionalidades actuales

- Alta y listado de proyectos.
- Ejecucion de scans asincronos.
- Historico de scans por proyecto.
- Vista global de historial con filtros por proyecto y estado.
- Detalle de scan con polling mientras la ejecucion sigue en curso.
- Visualizacion de issues con severidad, criterio WCAG, selector, snippet HTML y recomendacion.
- Guia WCAG integrada en frontend.
- Soporte i18n es/en.
- Documentacion OpenAPI y Swagger UI en backend.
- Politica basica de seguridad para evitar destinos inseguros en el escaneo.

## Estructura del workspace

```text
.
|-- backend/      API REST, persistencia, seguridad de escaneo y motor Playwright + axe-core
|-- frontend/     Aplicacion Angular para gestionar proyectos, scans e incidencias
|-- docs/         Documentacion general del workspace
|-- infra/        Infraestructura adicional y experimentacion
`-- scanner-worker/ soporte futuro / trabajo auxiliar del scanner
```

## Puesta en marcha

### Requisitos

- JDK 21
- Node.js 20+
- npm 10+
- Docker y Docker Compose

### Opcion recomendada: levantar PostgreSQL y backend con Docker

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

La aplicacion queda disponible en http://localhost:4200.

### Opcion alternativa: backend local fuera de Docker

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

## Calidad y validacion

### Backend

Desde [backend](backend):

```bash
./mvnw -B verify
```

Incluye:

- Tests.
- Checkstyle.
- SpotBugs.
- JaCoCo con umbral minimo de cobertura.

### Frontend

Desde [frontend](frontend):

```bash
npm run test -- --watch=false
npm run build
```

## Seguridad de escaneo

El backend incorpora una politica minima para reducir riesgo SSRF en el escaneo:

- Solo permite URLs http/https.
- Puede forzar allowlist de dominios.
- Bloquea localhost, loopback y redes privadas cuando la proteccion esta activa.

Variables relevantes:

- SCANNER_SECURITY_ENFORCE_WHITELIST
- SCANNER_SECURITY_ALLOWED_DOMAINS
- SCANNER_SECURITY_BLOCK_PRIVATE_NETWORKS

## Puntos tecnicos destacables para publicacion

- Proyecto full stack con separacion clara entre UI, API, persistencia y motor de escaneo.
- Integracion real con PostgreSQL y migraciones versionadas por Flyway.
- Cliente Angular generado desde OpenAPI para evitar desalineacion entre frontend y backend.
- Enfoque pragmatico de accesibilidad: no solo detecta issues, tambien ofrece contexto y guia de remediacion.
- Base preparada para evolucionar a autenticacion, multiusuario, colas de trabajo y analitica historica.

## Demo y material para LinkedIn

En [backend/docs/evidence/README.md](backend/docs/evidence/README.md) tienes un guion corto para grabar una demo mostrando:

1. Creacion de proyecto.
2. Lanzamiento de scan.
3. Cambio de estado RUNNING a COMPLETED o FAILED.
4. Visualizacion de issues y resumen por severidad.
5. Cierre rapido con Swagger UI o logs.

## Documentacion adicional

- [backend/README.md](backend/README.md): detalle tecnico del backend.
- [frontend/README.md](frontend/README.md): detalle tecnico del frontend.
- [frontend/docs/design-system.md](frontend/docs/design-system.md): sistema visual del frontend.

## Limitaciones actuales

- No hay autenticacion ni control de acceso.
- El escaneo esta orientado a MVP y puede ampliarse con colas, retry y politicas mas avanzadas.
- No hay e2e automatizado en este repo.
- El despliegue productivo no esta documentado todavia.

## Evolucion futura

Las siguientes lineas no estan implementadas todavia; son mejoras naturales para una siguiente iteracion del proyecto.

- Autenticacion y multiusuario.
- Dashboard con metricas historicas por proyecto.
- Cola de trabajos y control de concurrencia para scans.
- E2E con Playwright para flujos criticos.
- Demo desplegada y capturas/GIF embebidas en el README.
