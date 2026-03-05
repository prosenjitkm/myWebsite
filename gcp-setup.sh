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
PROJECT_ID="your-gcp-project-id"          # e.g. mywebsite-123456
REGION="us-central1"
GITHUB_USER="your-github-username"         # e.g. JohnDoe
GITHUB_REPO="myWebsite"                    # your repo name
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
  cloudresourcemanager.googleapis.com \
  vpcaccess.googleapis.com \
  servicenetworking.googleapis.com

# ─────────────────────────────────────────────────────────────────────────────
# 2. Artifact Registry (Docker image repository)
# ─────────────────────────────────────────────────────────────────────────────
echo "==> Creating Artifact Registry repository..."
gcloud artifacts repositories create mywebsite \
  --repository-format=docker \
  --location="$REGION" \
  --description="Docker images for myWebsite"

REGISTRY="${REGION}-docker.pkg.dev/${PROJECT_ID}/mywebsite"
echo "Registry: $REGISTRY"

# ─────────────────────────────────────────────────────────────────────────────
# 3. Cloud SQL (PostgreSQL) — db-f1-micro is free-tier eligible
# ─────────────────────────────────────────────────────────────────────────────
echo "==> Creating Cloud SQL instance (this takes ~5 minutes)..."
gcloud sql instances create mywebsite-db \
  --database-version=POSTGRES_15 \
  --tier=db-f1-micro \
  --region="$REGION" \
  --no-assign-ip \
  --network=default \
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

DB_PRIVATE_IP=$(gcloud sql instances describe mywebsite-db --format='value(ipAddresses[0].ipAddress)')
echo "==> Cloud SQL private IP: $DB_PRIVATE_IP"
echo "==> SAVE this DB password: $DB_APP_PASSWORD"

# ─────────────────────────────────────────────────────────────────────────────
# 4. VPC Serverless Connector (Cloud Run → Cloud SQL private IP)
# ─────────────────────────────────────────────────────────────────────────────
echo "==> Creating VPC Serverless Connector..."
gcloud compute networks vpc-access connectors create mywebsite-connector \
  --network=default \
  --region="$REGION" \
  --range=10.8.0.0/28

# ─────────────────────────────────────────────────────────────────────────────
# 5. Service Account for GitHub Actions
# ─────────────────────────────────────────────────────────────────────────────
echo "==> Creating service account..."
SA_EMAIL="github-actions-sa@${PROJECT_ID}.iam.gserviceaccount.com"

gcloud iam service-accounts create github-actions-sa \
  --display-name="GitHub Actions Deploy SA" \
  --description="Used by GitHub Actions to build/deploy myWebsite"

# Grant least-privilege IAM roles
for ROLE in \
  roles/run.admin \
  roles/artifactregistry.writer \
  roles/cloudsql.client \
  roles/iam.serviceAccountUser \
  roles/secretmanager.secretAccessor; do
  gcloud projects add-iam-policy-binding "$PROJECT_ID" \
    --member="serviceAccount:${SA_EMAIL}" \
    --role="$ROLE"
done

# ─────────────────────────────────────────────────────────────────────────────
# 6. Workload Identity Federation (keyless auth for GitHub Actions)
# ─────────────────────────────────────────────────────────────────────────────
echo "==> Setting up Workload Identity Federation..."
gcloud iam workload-identity-pools create github-pool \
  --location=global \
  --display-name="GitHub Actions Pool" \
  --description="WIF pool for GitHub Actions"

gcloud iam workload-identity-pools providers create-oidc github-provider \
  --workload-identity-pool=github-pool \
  --location=global \
  --issuer-uri="https://token.actions.githubusercontent.com" \
  --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository,attribute.actor=assertion.actor" \
  --attribute-condition="assertion.repository=='${GITHUB_USER}/${GITHUB_REPO}'"

# Bind the GitHub repo to the service account
POOL_RESOURCE="projects/${PROJECT_NUMBER}/locations/global/workloadIdentityPools/github-pool"
gcloud iam service-accounts add-iam-policy-binding "${SA_EMAIL}" \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/${POOL_RESOURCE}/attribute.repository/${GITHUB_USER}/${GITHUB_REPO}"

WIF_PROVIDER="${POOL_RESOURCE}/providers/github-provider"
echo "==> WIF Provider: $WIF_PROVIDER"
echo "==> Service Account: $SA_EMAIL"

# ─────────────────────────────────────────────────────────────────────────────
# 7. Secrets in Secret Manager
# ─────────────────────────────────────────────────────────────────────────────
echo "==> Creating secrets in Secret Manager..."
echo "    (You'll be prompted to enter each secret value)"

# DB password — use the auto-generated one from step 3
printf '%s' "$DB_APP_PASSWORD" | \
  gcloud secrets create DB_PASSWORD --data-file=- --replication-policy=automatic

# JWT Secret — must be >= 32 chars
JWT_SECRET_VAL="$(openssl rand -base64 48)"
printf '%s' "$JWT_SECRET_VAL" | \
  gcloud secrets create JWT_SECRET --data-file=- --replication-policy=automatic
echo "==> Generated JWT_SECRET: $JWT_SECRET_VAL"

# Google OAuth2 — enter manually (from Google Cloud Console)
echo "==> Enter GOOGLE_CLIENT_ID (from GCP Console → APIs & Services → Credentials):"
read -r GOOGLE_CLIENT_ID_VAL
printf '%s' "$GOOGLE_CLIENT_ID_VAL" | \
  gcloud secrets create GOOGLE_CLIENT_ID --data-file=- --replication-policy=automatic

echo "==> Enter GOOGLE_CLIENT_SECRET:"
read -r -s GOOGLE_CLIENT_SECRET_VAL
printf '%s' "$GOOGLE_CLIENT_SECRET_VAL" | \
  gcloud secrets create GOOGLE_CLIENT_SECRET --data-file=- --replication-policy=automatic

# ─────────────────────────────────────────────────────────────────────────────
# 8. Schema: run schema.sql against Cloud SQL
# ─────────────────────────────────────────────────────────────────────────────
echo "==> To run schema.sql, use Cloud SQL Auth Proxy locally:"
echo "    cloud-sql-proxy ${PROJECT_ID}:${REGION}:mywebsite-db &"
echo "    psql -h 127.0.0.1 -U appuser -d mywebsite -f database/schema.sql"

# ─────────────────────────────────────────────────────────────────────────────
# SUMMARY — Copy these into GitHub Secrets
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
echo "GCP_VPC_CONNECTOR             = projects/${PROJECT_ID}/locations/${REGION}/connectors/mywebsite-connector"
echo "DB_HOST                       = $DB_PRIVATE_IP"
echo "DB_NAME                       = mywebsite"
echo "DB_USER                       = appuser"
echo "FRONTEND_CLOUD_RUN_DOMAIN     = (set after first deploy — Cloud Run auto-assigns)"
echo "BACKEND_CLOUD_RUN_DOMAIN      = (set after first deploy — Cloud Run auto-assigns)"
echo ""
echo "NOTE: DB_PASSWORD, JWT_SECRET, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET"
echo "      are stored in Secret Manager — NOT needed as GitHub Secrets."
echo "============================================================"


