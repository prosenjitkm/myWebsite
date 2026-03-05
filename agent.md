# Agent Context — myWebsite

> Read this file first to understand the project before making changes.
> Updated: 2026-03-05

---

## Project at a glance

Personal portfolio website for **Prosenjit Kumar Mandal**.
Full-stack monorepo: Spring Boot 4 backend + Angular 21 frontend + PostgreSQL 18 DB.
Deployed to **Google Cloud Run** via **GitHub Actions** CI/CD.

```
myWebsite/
├── backend/        Spring Boot 4, Java 21, Maven
├── frontend/       Angular 21, TypeScript, standalone components
├── database/       schema.sql, patch scripts, README
├── .github/
│   └── workflows/
│       ├── ci.yml      — PR builds (no deploy)
│       └── deploy.yml  — push to main → Artifact Registry → Cloud Run
├── gcp-setup.sh        — one-time GCP infra provisioning
├── README.md
└── agent.md            ← YOU ARE HERE
```

---

## Key Technologies

| Layer | Tech |
|---|---|
| Backend | Spring Boot 4.0.3, Spring Security 7, Spring Data JPA, Hibernate 7.2, JJWT 0.12.6 |
| Frontend | Angular 21, TypeScript, standalone components, RxJS, Angular Signals |
| Database | PostgreSQL 18 (local), PostgreSQL 15 (Cloud SQL) |
| Auth | JWT (HMAC-SHA256), Google OAuth2 (Spring Security OAuth2 client) |
| Infra | GCP Cloud Run, Cloud SQL, Artifact Registry, Secret Manager, WIF |
| CI/CD | GitHub Actions (Workload Identity Federation — no JSON key) |

---

## Commands that work

### Backend
```powershell
cd backend

# Run locally (uses application-local.yaml + profile=local)
mvn spring-boot:run
# OR
.\mvnw.cmd spring-boot:run

# Build & test
mvn clean install
.\mvnw.cmd clean install

# Skip tests
mvn clean install -DskipTests

# Run with explicit profile
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
Backend starts at **http://localhost:8081**

### Frontend
```powershell
cd frontend

npm install          # first time only
npm start            # ng serve → http://localhost:4200
npm run build        # dev build
npm run build -- --configuration production   # prod build
```

### Database (PostgreSQL local)
```sql
-- Run schema (in pgAdmin4 / psql)
\i database/schema.sql

-- Run patches after schema changes
\i database/patch_admin_password.sql
\i database/patch_oauth2_columns.sql
```

---

## Spring Profiles

| Profile | How activated | Config loaded |
|---|---|---|
| `local` | Default (set in `application.yaml`) | `application-local.yaml` (gitignored!) + `application.yaml` |
| `test` | Set in `application-test.yaml` | `application-test.yaml` |
| `prod` | `SPRING_PROFILES_ACTIVE=prod` in Cloud Run | `application-prod.yaml` |

> `application-local.yaml` is **gitignored** — it holds real local secrets.
> The file must exist at `backend/src/main/resources/application-local.yaml`.
> Example structure: see `.env.example` at the repo root.

---

## application.yaml duplicate-key pitfall

`application.yaml` uses `spring.profiles.active: local` at the top level AND previously had a duplicate `spring:` key — this caused a SnakeYAML error:
```
found duplicate key profiles
```
Fix: merge all `spring:` keys into a single block. Never repeat a top-level YAML key.

---

## URL Constants

All API paths are in `UrlConstants.java`:
```
/api/auth/**     — login, register, set-password, me
/api/posts/**    — blog posts + comments
/api/resume/**   — resume sections (GET public, PUT/POST/DELETE admin-only)
/oauth2/**       — Spring Security OAuth2 (handled automatically)
/login/oauth2/code/google  — OAuth2 callback (Spring Security)
```

---

## Security rules summary

```
Public (no token):
  GET  /api/resume/**
  GET  /api/posts/**
  POST /api/auth/register
  POST /api/auth/login
  GET  /api/auth/me
  GET  /oauth2/authorization/**
  GET  /login/oauth2/code/**

Authenticated (any valid JWT):
  POST /api/posts/{postId}/comments

Admin only:
  POST   /api/resume
  PUT    /api/resume/{id}
  DELETE /api/resume/{id}
  PATCH  /api/resume/reorder
  GET    /api/resume/all      ← returns ALL sections incl. hidden
  POST   /api/posts
  PUT    /api/posts/{id}
  DELETE /api/posts/{id}
```

---

## Frontend structure

```
src/app/
├── app.ts / app.html / app.css / app.routes.ts / app.config.ts
├── core/
│   ├── guards/      auth.guard.ts
│   ├── interceptors/ auth.interceptor.ts, logging.interceptor.ts
│   ├── models/      auth.model.ts, post.model.ts, resume.model.ts
│   └── services/    api.service.ts, auth.service.ts
├── pages/
│   ├── blog/        blog.component.*
│   ├── home/        home.component.*
│   ├── login/       login.component.*
│   ├── oauth2-callback/ oauth2-callback.component.ts
│   ├── post/        post.component.*
│   ├── register/    register.component.*
│   └── resume/      resume.component.*
└── shared/
    └── components/
        ├── navbar/
        └── footer/
```

### Environment files
```
src/environments/environment.ts       → apiUrl: 'http://localhost:8081/api'  (dev)
src/environments/environment.prod.ts  → apiUrl: '__BACKEND_URL__'  (placeholder)
```
In production builds (Docker), the Dockerfile does:
```dockerfile
ARG BACKEND_URL=http://localhost:8081/api
RUN sed -i "s|__BACKEND_URL__|${BACKEND_URL}|g" src/environments/environment.prod.ts
```
The CI pipeline passes the real Cloud Run URL as the `BACKEND_URL` build-arg.

---

## Database schema (key tables)

```sql
users          — id (UUID), email, password_hash, username, first_name, last_name,
                 oauth_provider, oauth_id, role_id, is_active, created_at, updated_at
roles          — id, name (e.g. 'ADMIN', 'USER')
posts          — id, title, slug (unique), content (TEXT), author_id, is_published, ...
tags           — id, name (unique)
post_tags      — post_id, tag_id  (join table)
comments       — id, post_id, author_id, content, created_at
resume_sections— id, section (enum: SUMMARY,SKILLS,EXPERIENCE,EDUCATION,CERTIFICATIONS,OTHER),
                 title, subtitle, location, start_date, end_date, description (TEXT),
                 sort_order, is_visible, created_at, updated_at
```

> `description` is `TEXT` (unlimited length) in PostgreSQL — long resume entries are fine.

---

## GCP Deployment — how it works

```
push to main → GitHub Actions
  1. test       — mvn verify (profile=test) + ng build (stub URL)
  2. build-push-backend   — docker build ./backend → Artifact Registry
  3. deploy-backend       — Cloud Run deploy → outputs HTTPS URL
  4. build-push-frontend  — docker build ./frontend --build-arg BACKEND_URL=<real URL>/api
  5. deploy-frontend      — Cloud Run deploy
```

### GitHub Secrets needed

| Secret | Notes |
|---|---|
| `GCP_PROJECT_ID` | |
| `GCP_REGION` | e.g. `us-central1` |
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | WIF provider resource name |
| `GCP_SERVICE_ACCOUNT` | `github-actions-sa@<project>.iam.gserviceaccount.com` |
| `GCP_ARTIFACT_REGISTRY` | e.g. `us-central1-docker.pkg.dev/<project>/mywebsite` |
| `GCP_REGISTRY_HOSTNAME` | e.g. `us-central1-docker.pkg.dev` |
| `GCP_VPC_CONNECTOR` | Full VPC connector resource path |
| `DB_HOST` | Cloud SQL private IP |
| `DB_NAME` | `mywebsite` |
| `DB_USER` | `appuser` |
| `FRONTEND_CLOUD_RUN_DOMAIN` | Without `https://` |
| `BACKEND_CLOUD_RUN_DOMAIN` | Without `https://` — used for OAuth2 redirect URI |

> `DB_PASSWORD`, `JWT_SECRET`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` are in **Secret Manager**, NOT GitHub Secrets.

### After first deploy — update Google OAuth2 Console
1. Go to GCP Console → APIs & Services → OAuth 2.0 Credentials
2. Add to **Authorised redirect URIs**:
   `https://<backend-cloud-run-url>/login/oauth2/code/google`
3. Add to **Authorised JavaScript origins**:
   `https://<frontend-cloud-run-url>`

---

## Known issues / gotchas

| Issue | Fix |
|---|---|
| `duplicate key profiles` in YAML | Merge all `spring:` keys into one block |
| Backend port conflict on 8080 | We use port 8081 locally (`application.yaml: server.port: 8081`) |
| `GET /api/resume/all` returns 403 | This endpoint is admin-only; use `GET /api/resume` for public data |
| OAuth2 returns 401 `invalid_client` | Real Google Client ID/Secret needed; dummy values only work for local non-OAuth flows |
| Frontend editing not visible | Only ADMIN role sees edit UI; check JWT payload has `role: ADMIN` |
| Save fails on resume section | Backend logs will show if it's auth (403) or validation (400) |
| `application-local.yaml` missing | Create it with DB and JWT config; see `.env.example` |

---

## Resume section endpoint differences

| Endpoint | Auth | Returns |
|---|---|---|
| `GET /api/resume` | Public | Only `is_visible=true` sections |
| `GET /api/resume/all` | **ADMIN** | All sections incl. hidden |
| `POST /api/resume` | **ADMIN** | Created section |
| `PUT /api/resume/{id}` | **ADMIN** | Updated section |
| `DELETE /api/resume/{id}` | **ADMIN** | 204 |
| `PATCH /api/resume/reorder` | **ADMIN** | 200 |

