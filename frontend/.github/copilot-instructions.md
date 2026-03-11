This frontend uses the backend contract defined in `/api/openapi.json`.

Rules:
- Never invent endpoints, payloads, or field names.
- Prefer using the generated API client in `/src/app/api-client`.
- If backend contracts change, assume `api/openapi.json` and generated client are the source of truth.
- Do not create duplicate request/response models if an equivalent generated model already exists.
- Use Angular HttpClient only through the generated client unless explicitly requested otherwise.
- For UI code, create view models only when they are clearly separate from API models.