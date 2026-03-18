# myWebsite

Personal website monorepo for Prosenjit Kumar Mandal.

This repository is now local-first. Managed GCP deployment automation has been removed from the repo so pushes to `main` do not deploy to Google Cloud.

## Stack

- Backend: Spring Boot 4, Java 21, Maven
- Frontend: Angular 21, TypeScript
- Database: PostgreSQL 18
- Auth: JWT and Google OAuth2
- CI: GitHub Actions

## Features

- Public blog reads
- Authenticated comments
- Interactive resume
- Admin resume editing and reordering
- Email/password auth plus Google OAuth account linking

## Repo Layout

```text
myWebsite/
|-- backend/
|-- frontend/
|-- database/
|-- .env
|-- .env.example
|-- docker-compose.yml
`-- AGENTS.md
```

## Local Development

### Prerequisites

- Java 21+
- Node.js 20+
- PostgreSQL 18

### Database

Create a local `mywebsite` database, then run:

```powershell
$env:PGPASSWORD = 'admin'
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U admin -d mywebsite -f "database/schema.sql"
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U admin -d mywebsite -f "database/seed_resume.sql"
```

### Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Backend URL: `http://localhost:8081`

### Frontend

```powershell
cd frontend
npm install
npm start
```

Frontend URL: `http://localhost:4200`

## Environment Variables

Use `.env.example` as the template for local secrets.

| Variable | Description |
| --- | --- |
| `DB_HOST` | PostgreSQL host, usually `localhost` |
| `DB_PORT` | PostgreSQL port, usually `5432` |
| `DB_NAME` | Database name, usually `mywebsite` |
| `DB_USER` | Database user |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | HMAC secret for JWT signing |
| `JWT_EXPIRATION_MS` | JWT lifetime in milliseconds |
| `GOOGLE_CLIENT_ID` | Google OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth client secret |
| `FRONTEND_URL` | Allowed frontend origin for CORS |
| `BACKEND_URL` | Backend base URL used for OAuth redirect generation |

## Google OAuth Local Setup

Create a Google OAuth client and add:

- Authorized redirect URI: `http://localhost:8081/login/oauth2/code/google`
- Authorized JavaScript origins: `http://localhost:8081` and `http://localhost:4200`

## CI

GitHub Actions still runs the test workflow in [.github/workflows/ci.yml](/C:/Users/prose/IdeaProjects/myWebsite/.github/workflows/ci.yml).

## Deployment Note

This repo no longer contains managed GCP deployment automation.

If you previously deployed this app to Google Cloud, deleting files in this repo does not delete the live resources. You must delete the GCP resources separately to stop charges.
