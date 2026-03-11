# AccessibilityCopilotWeb

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 21.2.1.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## OpenAPI contract sync

The backend API contract is defined in `api/openapi.json` and is the source of truth for:

- endpoints
- HTTP methods
- payloads and parameters
- response schemas and field names

This frontend uses the generated client in `src/app/api-client`.

Available scripts:

```bash
npm run api:pull
```

Pulls the latest contract from `http://localhost:8080/v3/api-docs` into `api/openapi.json`.

```bash
npm run api:generate
```

Regenerates the Angular API client from `api/openapi.json` into `src/app/api-client`.

```bash
npm run api:sync
```

Runs both commands in sequence (`api:pull` + `api:generate`).

Recommended workflow:

1. Start backend locally.
2. Run `npm run api:sync` after backend contract changes.
3. Run `npm run build` (or `npm test`) to validate integration.
4. Commit both `api/openapi.json` and generated client updates.

Minimal CI recommendation:

- Run `npm run api:generate` and fail if it creates uncommitted diffs.
- This ensures the committed client is always aligned with `api/openapi.json`.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
