# AGENTS.md — AI Agent Context File

> This file is for AI coding assistants (GitHub Copilot, Claude, Gemini, Codex).
> Read this before touching any file to save tokens and avoid repeating past mistakes.

---

## 🧭 Project Identity

- **Name:** myWebsite — Prosenjit Kumar Mandal's personal website
- **Type:** Full-stack monorepo (Spring Boot backend + Angular frontend + PostgreSQL)
- **Owner:** Single admin user (`admin@mywebsite.com`), public readers, registered commenters
- **Repo visibility:** PUBLIC — never commit secrets, credentials, or real passwords

---

## 📁 Monorepo Layout

```
myWebsite/
├── backend/        Spring Boot 4, Java 21, Maven
├── frontend/       Angular 21, TypeScript, standalone components
├── database/       SQL scripts only (no ORM migrations)
├── .env            LOCAL secrets — gitignored, never commit
├── .env.example    Safe template — always keep updated
├── README.md       Public-facing docs
└── AGENTS.md       This file
```

---

## ⚡ Commands That Work

### Backend
```powershell
# From: myWebsite/backend/

# Run the app (uses application-local.yaml for secrets)
.\mvnw.cmd spring-boot:run

# Full build + test
mvn clean install

# Compile only (fast check)
.\mvnw.cmd compile -q

# Skip tests
.\mvnw.cmd clean install -DskipTests
```

### Frontend
```powershell
# From: myWebsite/frontend/

npm start           # dev server at localhost:4200
npx ng build        # production build → dist/frontend/
npx ng build 2>&1   # capture errors
```

### Database
```powershell
# Run SQL file against local postgres
$env:PGPASSWORD = 'admin'
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U admin -d mywebsite -f "database/schema.sql"
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U admin -d mywebsite -f "database/seed_resume.sql"

# Quick query
$env:PGPASSWORD = 'admin'
$result = & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U admin -d mywebsite -c "SELECT COUNT(*) FROM resume_sections;" 2>&1
Write-Host $result
```

---

## 🏛 Architecture Summary

```
Angular (4200) → JWT in Authorization header → Spring Boot (8081) → PostgreSQL (5432)
                                               ↕
                                       Google OAuth2
```

- **Auth:** JWT (HMAC-SHA256) + Google OAuth2 via Spring Security
- **JWT payload:** `{ sub: email, role: "ADMIN"|"USER", iat, exp }`
- **Token storage:** `localStorage` (key: `auth_token`, user: `auth_user`)
- **Profiles:** `local` (dev) loads `application-local.yaml`; `test` (CI) uses dummy values
- **CORS:** Allowed origins = `localhost:4200` + `FRONTEND_URL` env var

---

## 🗄 Database

- **Engine:** PostgreSQL 18
- **Database name:** `mywebsite`
- **Credentials (local):** `admin / admin`
- **Port:** 5432
- **Schema managed by:** `database/schema.sql` (manual, not Liquibase/Flyway)
- **DDL mode:** `validate` — Hibernate validates against schema, never modifies it

### Tables
| Table | Key columns |
|---|---|
| `roles` | id (1=ADMIN, 2=USER), name |
| `users` | id UUID, email, password_hash (nullable), username, oauth_provider, oauth_id, role_id |
| `posts` | id UUID, author_id, title, slug (unique), body, is_published |
| `comments` | id UUID, post_id, author_id, parent_id (nullable), body VARCHAR(6000) |
| `tags` + `post_tags` | Many-to-many posts↔tags |
| `resume_sections` | id SERIAL, section, sort_order, title, subtitle, location, start/end date, description, is_visible |

### Admin seed
- Email: `admin@mywebsite.com`
- Password: bcrypt hash in `schema.sql` (change via `patch_admin_password.sql`)
- Role: ADMIN (role_id=1)

---

## 🔑 Security Rules

| Rule | Detail |
|---|---|
| Public GET | `/api/auth/**`, `GET /api/posts/**`, `GET /api/resume/**` |
| Authenticated | Everything else |
| Admin only | `POST/PUT/DELETE /api/posts/**`, `POST/PUT/DELETE/PATCH /api/resume/**`, `GET /api/resume/all` |
| `@PreAuthorize` | Used on service/controller methods, NOT in SecurityConfig |
| JWT filter | `JwtAuthFilter` — silently ignores bad tokens (catch block), never blocks public routes |
| OAuth account linking | By `(provider, oauthId)` first, then by email — prevents duplicate accounts |

---

## 🧩 Key Classes

### Backend
| Class | Package | Role |
|---|---|---|
| `JwtService` | security | Generates + validates JWT; **embeds `role` claim** |
| `JwtAuthFilter` | security | Reads Bearer token per-request, sets SecurityContext |
| `OAuth2LoginSuccessHandler` | security | Links OAuth accounts, issues JWT, redirects to Angular |
| `SecurityConfig` | security | CORS, filter chain, public vs protected routes |
| `AuthService` | service | register, login, getMe, setPassword |
| `ResumeService` | service | CRUD + reorder for resume_sections |
| `UrlConstants` | constants | ALL API paths — change here, propagates everywhere |
| `FieldConstants` | constants | ALL field length limits — DB ↔ entity ↔ DTO in sync |
| `ServiceConstants` | constants | Role names, error messages, auth header keys |

### Frontend
| File | Role |
|---|---|
| `auth.service.ts` | Stores JWT, exposes `user` signal, `isLoggedIn`, `isAdmin` computed signals |
| `api.service.ts` | All HTTP calls — single source of truth for API URLs |
| `auth.interceptor.ts` | Attaches `Authorization: Bearer <token>` to every request |
| `auth.guard.ts` | Blocks unauthenticated access to `/settings` and other protected routes |
| `resume.component.ts` | Drag & drop reorder + inline edit + add modal (admin) |
| `settings.component.ts` | Set/change password for OAuth users |
| `oauth2-callback.component.ts` | Reads `?token=` from Google redirect, calls `auth.handleOAuthToken()` |

---

## 🐛 Known Issues & Fixes Applied

### ✅ FIXED: Resume cards all show as "hidden" + save fails
- **Cause:** `boolean isVisible` field — Lombok generates `isIsVisible()` getter → Jackson serializes as `"visible"` not `"isVisible"` → frontend model mismatch → always undefined/false → all cards look hidden. Same causes PUT save to fail.
- **Fix:** Renamed field to `visible`. Lombok now generates correct `isVisible()` + `setVisible()`. Added `@JsonProperty("isVisible")` so JSON key stays `"isVisible"`.
- **Rule:** Never prefix a `boolean` field with `is` when using Lombok. Use `visible`, `active`, `published` etc.

### ✅ FIXED: Resume page shows nothing after login
- **Cause:** Old JWT in localStorage had no `role` claim → `isAdmin()` returned false
  → called `getAllResumeSections()` which required auth → 403
- **Fix:** `JwtService.generateToken()` now embeds `role` claim
- **Resolution:** User must **log out and log back in** to get fresh token with role

### ✅ FIXED: Resume component shows "failed to load"
- **Cause:** Backend down, OR stale JWT causing admin endpoint to 403
- **Fix:** Resume component `ngOnInit` now falls back to public `getResume()` if admin call fails
- **Fix:** Backend default credentials changed from `postgres/postgres` → `admin/admin`

### ✅ FIXED: `mvn clean install` fails in CI/test
- **Cause:** Tests tried to load `.env` file which Maven doesn't read
- **Fix:** `src/test/resources/application.yaml` provides all needed values with test dummies

### ✅ FIXED: Port conflict on 8080
- **Resolution:** App moved to port 8081 (`server.port=8081` in `application.yaml`)

### ✅ FIXED: OAuth error 401 invalid_client
- **Cause:** `application.yaml` had `dummy-client-id` fallback; `application-local.yaml` was not loaded
- **Fix:** `spring.profiles.active=local` in `application.yaml` auto-loads `application-local.yaml`

### ✅ FIXED: Google OAuth callback wrong port
- **Resolution:** Update Google Cloud Console redirect URI to `http://localhost:8081/login/oauth2/code/google`

### ⚠️ KNOWN: `spring.jpa.open-in-view` warning
- Harmless warning; explicitly set to `false` in `application.yaml`

### ⚠️ KNOWN: Lombok `sun.misc.Unsafe` deprecation warning
- Harmless; will be resolved in future Lombok versions

---

## 🌐 Ports

| Service | Port | URL |
|---|---|---|
| Angular dev server | 4200 | http://localhost:4200 |
| Spring Boot API | 8081 | http://localhost:8081 |
| PostgreSQL | 5432 | jdbc:postgresql://localhost:5432/mywebsite |

---

## 📦 Secret Files (NEVER commit these)

| File | Why secret |
|---|---|
| `.env` | DB password, JWT secret, Google OAuth credentials |
| `backend/src/main/resources/application-local.yaml` | Same as .env but Spring profile format |

Both are in `.gitignore`. The `application-local.yaml` pattern is in both root and backend `.gitignore`.

---

## 🔄 Typical Development Workflow

```
1. Start PostgreSQL (Windows service or pgAdmin)
2. cd backend → mvn spring-boot:run      (stays running)
3. cd frontend → npm start               (stays running)
4. Edit code → hot reload on both sides
5. After backend changes → browser refresh
6. After entity changes → update schema.sql + run SQL manually
```

---

## 📐 Conventions

### Backend
- All API paths defined in `UrlConstants` — **never hardcode paths**
- All field lengths in `FieldConstants` — **never use magic numbers**
- DTOs are Java records
- Entities use Lombok `@Builder`, `@Getter`, `@Setter`
- Services are `@Transactional(readOnly = true)` by default; write methods override with `@Transactional`
- Admin-only endpoints use `@PreAuthorize("hasAuthority('ADMIN')")` — note: `ADMIN` not `ROLE_ADMIN`
  (the prefix stripping happens in `JwtService` and `User.getAuthorities()` uses `ROLE_` + roleName)

### Frontend
- All HTTP calls go through `ApiService` — never call `HttpClient` directly in components
- All backend URLs in `ApiService` only — currently hardcoded to `localhost:8081` (update for prod)
- Components use Angular Signals (`signal()`, `computed()`) for state — not `BehaviorSubject`
- All components are standalone (`standalone: true`)
- Lazy loading via `loadComponent` in `app.routes.ts`

### Database
- Schema changes go in `database/schema.sql` (full schema) AND a new `patch_*.sql` file
- `ddl-auto: validate` — Hibernate will crash on startup if entity ≠ DB schema
- UUIDs for user/post/comment IDs; SERIAL for resume_sections

---

## 🧪 Testing

```powershell
# Run all tests
cd backend; mvn test

# Test config lives at:
backend/src/test/resources/application.yaml
# Uses profile: test, connects to local postgres with admin/admin
# Has dummy OAuth2 and JWT values so Spring context loads without real secrets
```

---

## 🚢 Resume Data

- 17 rows seeded via `database/seed_resume.sql`
- Sections: SUMMARY, SKILLS (×5), EXPERIENCE (×4), EDUCATION (×3), CERTIFICATIONS (×2), OTHER (×2)
- Downloadable DOCX at: `GET http://localhost:8081/assets/resume.docx`
- Served from: `backend/src/main/resources/static/assets/resume.docx`

