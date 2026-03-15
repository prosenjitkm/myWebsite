# Deployment Runbook

This repository is configured for this production layout:

- `https://prosenjitkm.com` -> Angular frontend on Cloud Run
- `https://api.prosenjitkm.com` -> Spring Boot backend on Cloud Run

`www.prosenjitkm.com` is optional. If you expose it later, either redirect it to the apex domain or allow both origins in backend CORS.

## Current state

- GitHub Actions deploys both services from `main` or `master`
- The custom domain, managed TLS certificate, and external HTTPS load balancer are in place
- Cloud Run fallback URLs still exist and are useful for debugging
- Local PostgreSQL data is not migrated automatically to Cloud SQL

## 1. Prepare GCP

Run the base setup script once:

```bash
bash gcp-setup.sh
```

That script handles:

- Artifact Registry
- Cloud SQL
- Secret Manager
- GitHub Actions service account
- Workload Identity Federation
- Compute API enablement

## 2. Add GitHub Actions secrets

Set these in GitHub -> Settings -> Secrets and variables -> Actions:

- `GCP_REGION`
- `GCP_WORKLOAD_IDENTITY_PROVIDER`
- `GCP_SERVICE_ACCOUNT`
- `GCP_ARTIFACT_REGISTRY`
- `GCP_REGISTRY_HOSTNAME`
- `GCP_CLOUDSQL_INSTANCE`
- `DB_NAME`
- `DB_USER`
- `FRONTEND_CLOUD_RUN_DOMAIN=prosenjitkm.com`
- `BACKEND_CLOUD_RUN_DOMAIN=api.prosenjitkm.com`

Do not add these as GitHub secrets:

- `DB_PASSWORD`
- `JWT_SECRET`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`

Those belong in Secret Manager.

Public repo safety:

- keep real secrets out of committed files, issue comments, and screenshots
- do not store production dumps in the repo
- treat SQL exports containing user accounts or password hashes as sensitive

## 3. First deploy

Push to `main` or `master`, or run the deploy workflow manually.

Expected result:

- backend deploys to Cloud Run on port `8081`
- frontend deploys to Cloud Run on port `8080`
- frontend is built against `https://api.prosenjitkm.com/api`

At this stage, the custom domains do not have to be live yet.

## 4. Create the external HTTPS load balancer

Create one global external Application Load Balancer with:

- one serverless NEG for the frontend Cloud Run service
- one serverless NEG for the backend Cloud Run service
- one global IPv4 address
- one Google-managed certificate covering:
  - `prosenjitkm.com`
  - `api.prosenjitkm.com`
  - optional `www.prosenjitkm.com`

Configure host routing:

- `prosenjitkm.com` -> frontend backend service
- `api.prosenjitkm.com` -> backend backend service
- optional `www.prosenjitkm.com` -> frontend backend service

## 5. Update Squarespace DNS

In Squarespace Domains -> your domain -> DNS Settings -> Custom Records:

- add `A` record for `@` -> load balancer IPv4
- add `A` record for `api` -> load balancer IPv4
- optional: add `A` record for `www` -> load balancer IPv4

Important:

- remove conflicting old records for the same hosts
- if the domain uses custom nameservers, update DNS at that provider instead of Squarespace

## 6. Update Google OAuth

In Google Auth Platform:

- add `prosenjitkm.com` as an authorized domain
- set the exact redirect URI to:

```text
https://api.prosenjitkm.com/login/oauth2/code/google
```

If the OAuth app is still in testing mode, make sure the intended Google accounts are added as test users.

## 7. Load database schema and import data

The application uses `ddl-auto: validate`, so the database schema must exist before the backend starts cleanly.

Apply:

- `database/schema.sql`
- `database/patch_oauth2_columns.sql`
- `database/seed_resume.sql` if you want the starter resume content

Important:

- local PostgreSQL contents are not copied to Cloud SQL by the deploy workflow
- importing local data is a separate operation, typically via `pg_dump` / `psql` or a Cloud SQL import
- be careful when importing `users`, because password hashes, OAuth IDs, and foreign-key IDs must remain consistent

## 8. Final hardening

After the load balancer and certificate are working:

- restrict Cloud Run ingress to `internal-and-cloud-load-balancing`
- keep `BACKEND_URL=https://api.prosenjitkm.com`
- keep `FRONTEND_URL=https://prosenjitkm.com`
- if you serve both apex and `www`, configure a redirect or set `FRONTEND_URLS` with both origins

## 9. Smoke test checklist

- `https://prosenjitkm.com` loads
- blog and resume pages render
- API calls go to `https://api.prosenjitkm.com/api/...`
- Google login redirects through `https://api.prosenjitkm.com/login/oauth2/code/google`
- comments work when logged in
- resume DOCX download works
- HTTPS certificate is active for all configured hosts

## 10. Cost and cleanup

- The main fixed monthly costs in this setup are usually the external HTTPS load balancer and Cloud SQL.
- Cloud Run is usually a small part of the bill at low traffic because both services use `min-instances=0`.
- If you only need the deployment temporarily for demos or learning, the main cost-saving steps are:
  - delete the load balancer resources
  - stop or delete Cloud SQL
  - optionally delete Cloud Run services and Artifact Registry images for full cleanup
