# 🌐 Prosenjit Kumar Mandal — Personal Website

A production-grade full-stack personal website featuring a blog, interactive resume, OAuth2 authentication, and a comment system. Built with **Spring Boot 4 + Angular 21**, deployed to **Google Cloud Platform**.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=java)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-21-red?logo=angular)](https://angular.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue?logo=postgresql)](https://www.postgresql.org/)
[![GCP](https://img.shields.io/badge/GCP-Cloud%20Run-4285F4?logo=googlecloud)](https://cloud.google.com/)

---

## 📋 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [Tech Stack](#-tech-stack)
- [Features](#-features)
- [Project Structure](#-project-structure)
- [Database Design](#-database-design)
- [API Reference](#-api-reference)
- [Security & Authentication](#-security--authentication)
- [Local Development Setup](#-local-development-setup)
- [Environment Variables](#-environment-variables)
- [Running the App](#-running-the-app)
- [Deployment](#-deployment-gcp)
- [Roadmap](#-roadmap)

---

## 🏗 Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                         │
│              Angular 21 SPA  (localhost:4200)               │
│   ┌──────────┐ ┌────────┐ ┌────────┐ ┌──────────────────┐  │
│   │  Blog    │ │Resume  │ │  Home  │ │ Login / Register │  │
│   └──────────┘ └────────┘ └────────┘ └──────────────────┘  │
│         JWT stored in localStorage  |  Auth Interceptor     │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTPS / REST + JSON
┌──────────────────────▼──────────────────────────────────────┐
│                       API LAYER                             │
│          Spring Boot 4  (localhost:8081 / Cloud Run)        │
│  ┌─────────────┐  ┌─────────────┐  ┌────────────────────┐  │
│  │AuthController│  │PostController│  │ResumeController    │  │
│  └──────┬──────┘  └──────┬──────┘  └────────┬───────────┘  │
│         │                │                   │              │
│  ┌──────▼──────────────────────────────────▼──────────────┐ │
│  │              Spring Security Filter Chain               │ │
│  │   JwtAuthFilter → OAuth2LoginSuccessHandler             │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────┬──────────────────────────────────────┘
                       │ JDBC / JPA
┌──────────────────────▼──────────────────────────────────────┐
│                     DATA LAYER                              │
│         PostgreSQL 18  (localhost:5432 / Cloud SQL)         │
│   users │ roles │ posts │ tags │ comments │ resume_sections  │
└─────────────────────────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                  EXTERNAL SERVICES                          │
│            Google OAuth2  (accounts.google.com)             │
└─────────────────────────────────────────────────────────────┘
```

### Request Flow (authenticated)
```
Browser → Angular → AuthInterceptor (attaches JWT)
       → Spring Boot → JwtAuthFilter (validates JWT)
       → Controller → Service → Repository → PostgreSQL
       → JSON Response → Angular → Rendered UI
```

### OAuth2 Flow
```
User clicks "Continue with Google"
  → GET /oauth2/authorization/google  (Spring redirects to Google)
  → Google login + consent
  → GET /login/oauth2/code/google  (Spring receives code)
  → OAuth2LoginSuccessHandler: find/create user, issue JWT
  → Redirect to Angular /oauth2/callback?token=<jwt>
  → Angular stores JWT, user is logged in
```

---

## 🛠 Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Core language |
| Spring Boot | 4.0.3 | Application framework |
| Spring Security | 7.0.3 | Auth, JWT, OAuth2 |
| Spring Data JPA | 4.0.3 | Database ORM |
| Hibernate | 7.2.4 | JPA implementation |
| PostgreSQL Driver | 42.7.10 | DB connectivity |
| JJWT | 0.12.6 | JWT creation/validation |
| Lombok | 1.18.42 | Boilerplate reduction |
| HikariCP | 7.0.2 | Connection pooling |
| Maven | 3.x | Build tool |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| Angular | 21 | SPA framework |
| TypeScript | 5.x | Language |
| RxJS | 7.x | Reactive streams |
| Angular Signals | 21 | Reactive state management |
| HTML5 Drag & Drop API | native | Resume reordering |

### Infrastructure
| Technology | Purpose |
|---|---|
| PostgreSQL 18 | Primary database |
| Google Cloud Run | Backend hosting |
| Google Cloud SQL | Managed PostgreSQL |
| Google OAuth2 | Social login |
| Docker | Containerization |

---

## ✨ Features

### Public
- 📝 **Blog** — Read posts written by admin; paginated list view + full post view
- 💼 **Resume** — Interactive resume with download button (DOCX)
- 🏠 **Home** — Landing page
- 🔐 **Register** — Create account with email + password + username + name
- 🔑 **Login** — Email/password OR Google OAuth2

### Authenticated Users
- 💬 **Comments** — Post comments on blog entries (max ~1000 words)
- ⚙️ **Settings** — Set/change password; OAuth users can add a password to enable dual login

### Admin Only
- ✏️ **Resume Editing** — Every field inline-editable directly on the page (LinkedIn-style)
- ➕ **Add Resume Entry** — Modal to add new sections/entries
- 🔃 **Drag & Drop Reorder** — Drag resume cards to reorder; persists to database
- 👁 **Show/Hide Entries** — Toggle visibility without deleting
- 🗑 **Delete Entries** — Remove resume sections
- ✍️ **Blog Post Management** — Create, edit, publish/unpublish posts

### Account Linking
- Users who sign up with email can later use Google OAuth (same account, linked by email)
- OAuth users can add a password in Settings to enable dual login

---

## 📁 Project Structure

```
myWebsite/
├── .env                          # Local secrets (NEVER committed)
├── .env.example                  # Template — copy to .env
├── .gitignore
├── docker-compose.yml
├── README.md
├── AGENTS.md                     # AI agent context file
│
├── backend/                      # Spring Boot application
│   ├── pom.xml
│   ├── src/main/java/com/pro/backend/
│   │   ├── BackendApplication.java
│   │   ├── constants/
│   │   │   ├── FieldConstants.java       # All field length limits
│   │   │   ├── ServiceConstants.java     # Business logic constants
│   │   │   └── UrlConstants.java         # All API URL paths
│   │   ├── controller/
│   │   │   ├── AuthController.java       # /api/auth/**
│   │   │   ├── PostController.java       # /api/posts/**
│   │   │   └── ResumeController.java     # /api/resume/**
│   │   ├── dto/
│   │   │   ├── AuthResponse.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── SetPasswordRequest.java
│   │   │   ├── UserResponse.java
│   │   │   ├── PostResponse.java
│   │   │   ├── CommentRequest.java
│   │   │   └── CommentResponse.java
│   │   ├── entity/
│   │   │   ├── User.java                 # Implements UserDetails
│   │   │   ├── Role.java                 # ADMIN | USER
│   │   │   ├── Post.java
│   │   │   ├── Comment.java
│   │   │   ├── Tag.java
│   │   │   └── ResumeSection.java
│   │   ├── repository/                   # Spring Data JPA interfaces
│   │   ├── security/
│   │   │   ├── JwtAuthFilter.java        # Validates JWT on every request
│   │   │   ├── JwtService.java           # JWT create/validate, embeds role claim
│   │   │   ├── OAuth2LoginSuccessHandler.java  # Links OAuth → existing account
│   │   │   └── SecurityConfig.java       # CORS, filter chain, public routes
│   │   └── service/
│   │       ├── AuthService.java          # register, login, setPassword, getMe
│   │       ├── PostService.java
│   │       ├── CommentService.java
│   │       └── ResumeService.java
│   └── src/main/resources/
│       ├── application.yaml              # Base config (committed, safe defaults)
│       ├── application-local.yaml        # Local secrets (gitignored)
│       └── static/assets/resume.docx    # Downloadable resume file
│
├── frontend/                     # Angular 21 SPA
│   ├── angular.json
│   ├── package.json
│   └── src/app/
│       ├── app.routes.ts                 # Lazy-loaded routes
│       ├── core/
│       │   ├── guards/auth.guard.ts      # Protects authenticated routes
│       │   ├── interceptors/auth.interceptor.ts  # Attaches JWT to requests
│       │   ├── models/                   # TypeScript interfaces
│       │   └── services/
│       │       ├── auth.service.ts       # Login, register, OAuth, JWT decode
│       │       └── api.service.ts        # All HTTP calls to backend
│       ├── pages/
│       │   ├── home/
│       │   ├── blog/
│       │   ├── post/
│       │   ├── resume/                   # Editable resume (admin drag & drop)
│       │   ├── login/                    # Email + Google OAuth button
│       │   ├── register/
│       │   ├── settings/                 # Set/change password
│       │   └── oauth2-callback/          # Handles Google redirect
│       └── shared/components/
│           ├── navbar/
│           └── footer/
│
└── database/
    ├── schema.sql                # Full DB schema — run once
    ├── seed_resume.sql           # Resume data seed — run once
    ├── patch_admin_password.sql  # Update admin bcrypt hash
    ├── patch_oauth2_columns.sql  # Add OAuth columns migration
    └── create_db_user.sql        # Create DB user script
```

---

## 🗄 Database Design

### Entity Relationship

```
roles (1) ──────────── (M) users
                              │
                    ┌─────────┴──────────┐
                    │                    │
               (M) posts           (M) comments
                    │                    │
               (M) post_tags       references posts
                    │
               (M) tags

resume_sections  (standalone — no FK to users)
```

### Tables

#### `roles`
| Column | Type | Notes |
|---|---|---|
| id | SMALLINT PK | 1=ADMIN, 2=USER |
| name | VARCHAR(20) | 'ADMIN' or 'USER' |

#### `users`
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | Auto-generated |
| email | VARCHAR(255) UNIQUE | Login identifier |
| password_hash | VARCHAR(255) NULL | NULL for OAuth-only accounts |
| username | VARCHAR(50) UNIQUE | Display name |
| first_name | VARCHAR(100) | |
| last_name | VARCHAR(100) | |
| oauth_provider | VARCHAR(50) NULL | e.g. 'google' |
| oauth_id | VARCHAR(255) NULL | Provider's subject ID |
| role_id | SMALLINT FK | → roles.id |
| is_active | BOOLEAN | Soft disable |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

#### `posts`
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| author_id | UUID FK | → users.id |
| title | VARCHAR(255) | |
| slug | VARCHAR(255) UNIQUE | URL-friendly e.g. `my-first-post` |
| summary | TEXT | Short teaser for list view |
| body | TEXT | Full content (Markdown/HTML) |
| cover_image | VARCHAR(500) | URL to image |
| is_published | BOOLEAN | Draft vs published |
| published_at | TIMESTAMPTZ | |

#### `comments`
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| post_id | UUID FK | → posts.id CASCADE |
| author_id | UUID FK | → users.id CASCADE |
| parent_id | UUID FK NULL | → comments.id (nested replies) |
| body | VARCHAR(6000) | ~1000 words |
| is_approved | BOOLEAN | For moderation |

#### `resume_sections`
| Column | Type | Notes |
|---|---|---|
| id | SERIAL PK | |
| section | VARCHAR(50) | SUMMARY / SKILLS / EXPERIENCE / EDUCATION / CERTIFICATIONS / OTHER |
| sort_order | SMALLINT | Order within section group |
| title | VARCHAR(255) | Job title / degree / skill group |
| subtitle | VARCHAR(255) NULL | Company / institution |
| location | VARCHAR(255) NULL | |
| start_date | DATE NULL | |
| end_date | DATE NULL | NULL = Present |
| description | TEXT NULL | Bullet points / summary |
| is_visible | BOOLEAN | Hide without deleting |

---

## 🔌 API Reference

### Auth — `/api/auth`
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/register` | Public | Register with email + password |
| POST | `/login` | Public | Login, returns JWT |
| GET | `/me` | 🔒 Any | Get current user profile |
| POST | `/set-password` | 🔒 Any | Set/change password (OAuth users) |

### Posts — `/api/posts`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/` | Public | Paginated post list |
| GET | `/{slug}` | Public | Single post |
| POST | `/` | 🔒 ADMIN | Create post |
| PUT | `/{id}` | 🔒 ADMIN | Update post |
| DELETE | `/{id}` | 🔒 ADMIN | Delete post |
| GET | `/{postId}/comments` | Public | Post comments |
| POST | `/{postId}/comments` | 🔒 Any | Add comment |

### Resume — `/api/resume`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/` | Public | All visible sections |
| GET | `/all` | 🔒 ADMIN | All sections incl. hidden |
| POST | `/` | 🔒 ADMIN | Add section |
| PUT | `/{id}` | 🔒 ADMIN | Update section |
| DELETE | `/{id}` | 🔒 ADMIN | Delete section |
| PATCH | `/reorder` | 🔒 ADMIN | Bulk reorder |

### JWT Payload
```json
{
  "sub": "user@example.com",
  "role": "ADMIN",
  "iat": 1234567890,
  "exp": 1234654290
}
```

---

## 🔐 Security & Authentication

### JWT
- Signed with HMAC-SHA256 using a 256-bit secret from `JWT_SECRET` env var
- Expiry: 24 hours (`JWT_EXPIRATION_MS=86400000`)
- Contains: `sub` (email), `role`, `iat`, `exp`
- Sent as `Authorization: Bearer <token>` header
- Validated in `JwtAuthFilter` on every request

### OAuth2 (Google)
- Spring Security handles the full OAuth2 code flow
- `OAuth2LoginSuccessHandler` implements smart account linking:
  - Lookup by `(provider, oauthId)` → existing OAuth account
  - Fallback: lookup by email → links OAuth to existing email/password account
  - Neither found → creates new account
- Redirects to Angular SPA with JWT as query param

### Password Security
- Passwords hashed with BCrypt (strength 12)
- `password_hash` column is `@JsonIgnore` — never serialized
- OAuth-only accounts have `password_hash = NULL`
- Users can set a password later via `POST /api/auth/set-password`

### Role-Based Access Control
- `@PreAuthorize("hasAuthority('ADMIN')")` on admin endpoints
- `@EnableMethodSecurity` in `SecurityConfig`
- Frontend `auth.guard.ts` protects `/settings` route
- Navbar conditionally shows admin features

---

## 🚀 Local Development Setup

### Prerequisites
- Java 21+ (JDK)
- Node.js 20+ & npm
- PostgreSQL 18 (running locally)
- Maven 3.x (or use `mvnw`)

### 1. Database Setup
Run in pgAdmin4 or DBeaver against a `mywebsite` database:
```sql
-- 1. Create the database (if not exists)
CREATE DATABASE mywebsite;

-- 2. Run full schema
\i database/schema.sql

-- 3. Seed resume data
\i database/seed_resume.sql
```

### 2. Environment Setup
```bash
# Copy the example file
cp .env.example .env

# Edit with your values
# DB_HOST=localhost
# DB_PORT=5432
# DB_NAME=mywebsite
# DB_USER=admin
# DB_PASSWORD=admin
# JWT_SECRET=<generate with: node -e "console.log(require('crypto').randomBytes(32).toString('hex'))">
# GOOGLE_CLIENT_ID=<from Google Cloud Console>
# GOOGLE_CLIENT_SECRET=<from Google Cloud Console>
```

### 3. Google OAuth2 Setup
1. Go to [console.cloud.google.com](https://console.cloud.google.com)
2. APIs & Services → Credentials → Create OAuth 2.0 Client ID
3. Add to **Authorised redirect URIs**: `http://localhost:8081/login/oauth2/code/google`
4. Add to **Authorised JavaScript origins**: `http://localhost:8081`, `http://localhost:4200`

### 4. Backend Local Profile
The app uses `application-local.yaml` (gitignored) for local secrets. It's auto-created at:
```
backend/src/main/resources/application-local.yaml
```
This file is loaded via `spring.profiles.active=local` and overrides the safe defaults in `application.yaml`.

---

## ⚙️ Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `DB_HOST` | No | `localhost` | PostgreSQL host |
| `DB_PORT` | No | `5432` | PostgreSQL port |
| `DB_NAME` | No | `mywebsite` | Database name |
| `DB_USER` | No | `admin` | DB username |
| `DB_PASSWORD` | No | `admin` | DB password |
| `JWT_SECRET` | **Yes** | fallback (insecure) | 256-bit HMAC secret |
| `JWT_EXPIRATION_MS` | No | `86400000` | Token TTL (24h) |
| `GOOGLE_CLIENT_ID` | **Yes** | dummy | Google OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | **Yes** | dummy | Google OAuth client secret |
| `FRONTEND_URL` | No | `http://localhost:4200` | CORS allowed origin |

---

## ▶️ Running the App

### Backend
```powershell
cd backend

# Run (loads application-local.yaml automatically)
.\mvnw.cmd spring-boot:run

# Or build JAR first, then run
.\mvnw.cmd clean install -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```
Backend starts at: **http://localhost:8081**

### Frontend
```powershell
cd frontend
npm install      # first time only
npm start        # ng serve
```
Frontend starts at: **http://localhost:4200**

### Verify it's working
```powershell
# Backend health
Invoke-WebRequest http://localhost:8081/api/resume

# Should return JSON array of resume sections
```

---

## ☁️ Deployment (GCP via GitHub Actions)

Deployment is fully automated through GitHub Actions on every push to `main`.

### Pipeline overview
```
push to main
  │
  ├─ test        (backend mvn verify + frontend ng build --stub URL)
  │
  ├─ build-push-backend  (Docker build + push to Artifact Registry)
  │
  ├─ deploy-backend      (Cloud Run deploy → outputs HTTPS URL)
  │
  ├─ build-push-frontend (Docker build with real backend URL injected as build-arg)
  │
  └─ deploy-frontend     (Cloud Run deploy)
```

### One-time GCP Setup
Run `gcp-setup.sh` **once** to provision all infrastructure:
```bash
# Edit the top of the file with your values first
bash gcp-setup.sh
```
This creates:
- Artifact Registry repository for Docker images
- Cloud SQL (PostgreSQL 15) instance with private IP
- VPC Serverless Connector (Cloud Run → Cloud SQL)
- Service Account for GitHub Actions
- Workload Identity Federation (keyless auth — no JSON key files)
- Secret Manager secrets (DB_PASSWORD, JWT_SECRET, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET)

### GitHub Secrets required
After running `gcp-setup.sh`, add these to your repo's Settings → Secrets → Actions:

| Secret | Description |
|---|---|
| `GCP_PROJECT_ID` | GCP project ID |
| `GCP_REGION` | e.g. `us-central1` |
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | Output from gcp-setup.sh |
| `GCP_SERVICE_ACCOUNT` | `github-actions-sa@<project>.iam.gserviceaccount.com` |
| `GCP_ARTIFACT_REGISTRY` | e.g. `us-central1-docker.pkg.dev/<project>/mywebsite` |
| `GCP_REGISTRY_HOSTNAME` | e.g. `us-central1-docker.pkg.dev` |
| `GCP_VPC_CONNECTOR` | Full connector resource path |
| `DB_HOST` | Cloud SQL private IP |
| `DB_NAME` | `mywebsite` |
| `DB_USER` | `appuser` |
| `FRONTEND_CLOUD_RUN_DOMAIN` | Cloud Run domain (without https://) — set after first deploy |
| `BACKEND_CLOUD_RUN_DOMAIN` | Cloud Run domain (without https://) — set after first deploy |

> **Note:** `DB_PASSWORD`, `JWT_SECRET`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` live in Secret Manager and are mounted directly into Cloud Run — they do **not** need to be GitHub Secrets.

### Google OAuth2 Production Setup
After first deploy, add to Google Cloud Console → APIs & Services → Credentials:
- **Authorised redirect URIs**: `https://<backend-cloud-run-url>/login/oauth2/code/google`
- **Authorised JavaScript origins**: `https://<frontend-cloud-run-url>`

### Architecture on GCP
```
Internet
  │
  ├── Cloud Run: mywebsite-frontend (nginx:alpine, port 8080)
  │       Serves Angular SPA; API calls go to the backend Cloud Run URL
  │
  └── Cloud Run: mywebsite-backend  (eclipse-temurin:21-jre, port 8081)
          │
          VPC Connector
          │
          Cloud SQL (PostgreSQL 15, private IP only)

Secrets: Secret Manager → mounted as env vars by Cloud Run
Images:  Artifact Registry → pulled by Cloud Run at deploy time
Auth:    Workload Identity Federation (no service account keys)
```

---

## 🗺 Roadmap

### ✅ Done
- [x] **CI/CD** — GitHub Actions → GCP Artifact Registry → Cloud Run
- [x] **JWT Auth** — Register, login, OAuth2 (Google), set-password for OAuth users
- [x] **Blog system** — Posts, tags, comments
- [x] **Resume editor** — Admin drag-and-drop editable resume sections
- [x] **Role-based access** — ADMIN vs USER controls via `@PreAuthorize`
- [x] **Workload Identity Federation** — Keyless GCP auth (no JSON key files)

### 🔜 Up Next
- [ ] **Custom domain** — Map `yourdomain.com` to Cloud Run frontend
- [ ] **Admin blog editor** — Rich text (Quill/TipTap) post creation UI
- [ ] **Comment moderation** — Admin approve/reject comments
- [ ] **Image uploads** — Cover images via GCS bucket
- [ ] **Email notifications** — SendGrid for comment alerts
- [ ] **Rate limiting** — Bucket4j on auth endpoints
- [ ] **Refresh tokens** — Sliding JWT expiry
- [ ] **GitHub OAuth** — Second social login provider

---

## 👤 Author

**Prosenjit Kumar Mandal**
- 📧 prosenjitkm91@gmail.com
- 🔗 [linkedin.com/in/prosenjitKm](https://www.linkedin.com/in/prosenjitKm)
- 📍 Paterson, NJ, USA

---

## 📄 License

This project is personal and not open for redistribution. All rights reserved.

