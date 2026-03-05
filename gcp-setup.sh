#!/usr/bin/env bash
# =============================================================================
# GCP One-Time Setup Script for myWebsite
#
# Run these commands ONCE to set up the entire GCP infrastructure.
# Replace the placeholder values at the top before running.
#
# Prerequisites:
#   - gcloud CLI installed and authenticated: gcloud auth login
#   - A GCP project already created
# =============================================================================

set -euo pipefail

# ── REPLACE THESE ─────────────────────────────────────────────────────────────
PROJECT_ID="mywebsite-489221"
REGION="us-east1"
GITHUB_USER="prosenjitkm"
GITHUB_REPO="myWebsite"
DB_APP_PASSWORD="$(openssl rand -base64 24)"  # auto-generated secure password
# ──────────────────────────────────────────────────────────────────────────────

echo "==> Using project: $PROJECT_ID"
gcloud config set project "$PROJECT_ID"
PROJECT_NUMBER=$(gcloud projects describe "$PROJECT_ID" --format='value(projectNumber)')

# ─────────────────────────────────────────────────────────────────────────────
# 1. Enable Required APIs
# ─────────────────────────────────────────────────────────────────────────────
echo "==> Enabling APIs..."
gcloud services enable \
  run.googleapis.com \
  sqladmin.googleapis.com \
  artifactregistry.googleapis.com \
  secretmanager.googleapis.com \
  iam.googleapis.com \
  iamcredentials.googleapis.com \
  cloudresourcemanager.googleapis.com

# ─────────────────────────────────────────────────────────────────────────────
# 2. Artifact Registry (Docker image repository)
# ─────────────────────────────────────────────────────────────────────────────
echo "==> Creating Artifact Registry repository..."
gcloud artifacts repositories create mywebsite \
  --repository-format=docker \
  --location="$REGION" \
  --description="Docker images for myWebsite" 2>/dev/null || \
  echo "    (already exists — skipping)"

REGISTRY="${REGION}-docker.pkg.dev/${PROJECT_ID}/mywebsite"
echo "Registry: $REGISTRY"

# ─────────────────────────────────────────────────────────────────────────────
# 3. Cloud SQL (PostgreSQL) — public IP + Cloud SQL connector (no VPC peering needed)
# ─────────────────────────────────────────────────────────────────────────────
echo "==> Creating Cloud SQL instance (this takes ~5 minutes)..."
gcloud sql instances create mywebsite-db \
  --database-version=POSTGRES_15 \
  --tier=db-f1-micro \
  --region="$REGION" \
  --storage-type=SSD \
  --storage-size=10GB \
  --backup-start-time=04:00 \
  --maintenance-window-day=SUN \
  --maintenance-window-hour=5

echo "==> Creating database and user..."
gcloud sql databases create mywebsite --instance=mywebsite-db
gcloud sql users create appuser \
  --instance=mywebsite-db \
  --password="$DB_APP_PASSWORD"

# Cloud Run connects via Cloud SQL connector (instance connection name), not IP
INSTANCE_CONNECTION_NAME="${PROJECT_ID}:${REGION}:mywebsite-db"
echo "==> Instance connection name: $INSTANCE_CONNECTION_NAME"
echo "==> SAVE this DB password: $DB_APP_PASSWORD"

# ─────────────────────────────────────────────────────────────────────────────
# 4. Service Account for GitHub Actions
# ─────────────────────────────────────────────────────────────────────────────
echo "==> Creating service account..."
SA_EMAIL="github-actions-sa@${PROJECT_ID}.iam.gserviceaccount.com"

gcloud iam service-accounts create github-actions-sa \
  --display-name="GitHub Actions Deploy SA" \
  --description="Used by GitHub Actions to build/deploy myWebsite" 2>/dev/null || \
  echo "    (already exists — skipping)"

# Grant least-privilege IAM roles
for ROLE in \
  roles/run.admin \
  roles/artifactregistry.writer \
  roles/cloudsql.client \
  roles/iam.serviceAccountUser \
  roles/secretmanager.secretAccessor; do
  gcloud projects add-iam-policy-binding "$PROJECT_ID" \
    --member="serviceAccount:${SA_EMAIL}" \
    --role="$ROLE" --condition=None
done

# ─────────────────────────────────────────────────────────────────────────────
# 5. Workload Identity Federation
# ─────────────────────────────────────────────────────────────────────────────
echo "==> Setting up Workload Identity Federation..."
gcloud iam workload-identity-pools create github-pool \
  --location=global \
  --display-name="GitHub Actions Pool" 2>/dev/null || \
  echo "    (already exists — skipping)"

gcloud iam workload-identity-pools providers create-oidc github-provider \
  --workload-identity-pool=github-pool \
  --location=global \
  --issuer-uri="https://token.actions.githubusercontent.com" \
  --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository,attribute.actor=assertion.actor" \
  --attribute-condition="assertion.repository=='${GITHUB_USER}/${GITHUB_REPO}'" 2>/dev/null || \
  echo "    (already exists — skipping)"

# Bind the GitHub repo to the service account
POOL_RESOURCE="projects/${PROJECT_NUMBER}/locations/global/workloadIdentityPools/github-pool"
gcloud iam service-accounts add-iam-policy-binding "${SA_EMAIL}" \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/${POOL_RESOURCE}/attribute.repository/${GITHUB_USER}/${GITHUB_REPO}"

WIF_PROVIDER="${POOL_RESOURCE}/providers/github-provider"
echo "==> WIF Provider: $WIF_PROVIDER"
echo "==> Service Account: $SA_EMAIL"

# ─────────────────────────────────────────────────────────────────────────────
# 6. Secrets in Secret Manager
# ─────────────────────────────────────────────────────────────────────────────
echo "==> Creating secrets in Secret Manager..."

printf '%s' "$DB_APP_PASSWORD" | \
  gcloud secrets create DB_PASSWORD --data-file=- --replication-policy=automatic 2>/dev/null || \
  printf '%s' "$DB_APP_PASSWORD" | gcloud secrets versions add DB_PASSWORD --data-file=-

JWT_SECRET_VAL="$(openssl rand -base64 48)"
printf '%s' "$JWT_SECRET_VAL" | \
  gcloud secrets create JWT_SECRET --data-file=- --replication-policy=automatic 2>/dev/null || \
  printf '%s' "$JWT_SECRET_VAL" | gcloud secrets versions add JWT_SECRET --data-file=-
echo "==> Generated JWT_SECRET: $JWT_SECRET_VAL"

echo "==> Enter GOOGLE_CLIENT_ID (from GCP Console → APIs & Services → Credentials):"
read -r GOOGLE_CLIENT_ID_VAL
printf '%s' "$GOOGLE_CLIENT_ID_VAL" | \
  gcloud secrets create GOOGLE_CLIENT_ID --data-file=- --replication-policy=automatic 2>/dev/null || \
  printf '%s' "$GOOGLE_CLIENT_ID_VAL" | gcloud secrets versions add GOOGLE_CLIENT_ID --data-file=-

echo "==> Enter GOOGLE_CLIENT_SECRET:"
read -r -s GOOGLE_CLIENT_SECRET_VAL
printf '%s' "$GOOGLE_CLIENT_SECRET_VAL" | \
  gcloud secrets create GOOGLE_CLIENT_SECRET --data-file=- --replication-policy=automatic 2>/dev/null || \
  printf '%s' "$GOOGLE_CLIENT_SECRET_VAL" | gcloud secrets versions add GOOGLE_CLIENT_SECRET --data-file=-

# ─────────────────────────────────────────────────────────────────────────────
# 7. Schema instructions
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo "==> To load the database schema, run in Cloud Shell:"
echo "    cloud-sql-proxy ${INSTANCE_CONNECTION_NAME} &"
echo "    sleep 5"
echo "    PGPASSWORD='${DB_APP_PASSWORD}' psql -h 127.0.0.1 -U appuser -d mywebsite -f database/schema.sql"

# ─────────────────────────────────────────────────────────────────────────────
# SUMMARY
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo "============================================================"
echo " GITHUB SECRETS — add these at:"
echo " https://github.com/${GITHUB_USER}/${GITHUB_REPO}/settings/secrets/actions"
echo "============================================================"
echo "GCP_PROJECT_ID                = $PROJECT_ID"
echo "GCP_REGION                    = $REGION"
echo "GCP_WORKLOAD_IDENTITY_PROVIDER= $WIF_PROVIDER"
echo "GCP_SERVICE_ACCOUNT           = $SA_EMAIL"
echo "GCP_ARTIFACT_REGISTRY         = $REGISTRY"
echo "GCP_REGISTRY_HOSTNAME         = ${REGION}-docker.pkg.dev"
echo "GCP_CLOUDSQL_INSTANCE         = $INSTANCE_CONNECTION_NAME"
echo "DB_NAME                       = mywebsite"
echo "DB_USER                       = appuser"
echo "FRONTEND_CLOUD_RUN_DOMAIN     = (set after first deploy)"
echo "BACKEND_CLOUD_RUN_DOMAIN      = (set after first deploy)"
echo ""
echo "NOTE: DB_PASSWORD, JWT_SECRET, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET"
echo "      are stored in Secret Manager — NOT needed as GitHub Secrets."
echo "============================================================"

