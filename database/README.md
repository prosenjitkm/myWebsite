# Database Schema – myWebsite

> This folder contains all raw SQL for the PostgreSQL database.
> Run scripts in pgAdmin 4 **Query Tool** against the `mywebsite` database.

---

## How to create the database from scratch

1. Open **pgAdmin 4** → connect to your local server (`localhost:5432`, user `admin`).
2. Right-click **Databases** → **Create** → **Database** → name it `mywebsite` → Save.
3. Open the **Query Tool** for the `mywebsite` database.
4. Open `schema.sql`, paste the whole file, and click **▶ Execute**.

---

## Table overview

| Table              | Purpose |
|--------------------|---------|
| `roles`            | Two rows: `ADMIN` (1) and `USER` (2). |
| `users`            | All accounts. Admin seeded here; regular users self-register. |
| `posts`            | Blog / thoughts entries written by admin. |
| `tags`             | Flat tag list for categorising posts. |
| `post_tags`        | Many-to-many join between posts and tags. |
| `comments`         | User comments on posts, max ~1 000 words (6 000 chars). Supports replies via `parent_id`. |
| `resume_sections`  | Structured resume rows driven by the API. |

---

## Entity-Relationship overview

```
roles ──< users ──< posts ──< post_tags >── tags
                        └──< comments (self-ref for replies)
users ──< comments
users ──< resume_sections  (admin-managed)
```

---

## Key design decisions

| Decision | Why |
|----------|-----|
| UUID primary keys | Safe to expose in URLs; no sequential guessing. |
| `password_hash` only | Passwords stored as bcrypt hashes by Spring Security. |
| `is_published` flag on posts | Write drafts, publish when ready. |
| `is_approved` flag on comments | You can switch to manual moderation any time. |
| `parent_id` on comments | One-level threading for replies with zero schema change. |
| `resume_sections.section` text field | Flexible — add any section type without a migration. |
| `set_updated_at()` trigger | `updated_at` is always accurate with no application code needed. |

---

## Planned improvements (do later, not now)

- **`post_likes`** — track which users liked which posts.
- **`sessions` / refresh tokens table** — for JWT revocation.
- **Email verification** — add `email_verified_at` column to `users`.
- **Password reset tokens** — separate table with expiry.
- **Full-text search index** — `tsvector` column on `posts.body` for fast search.
- **Flyway migrations** — replace `ddl-auto: update` with versioned SQL migrations for production safety.
- **Row-level security** — when moving to Cloud SQL on GCP.

---

## Security reminder

The `.env` file at the project root holds database credentials.
It is listed in `.gitignore` — **never commit it**.
See `.env.example` for the required keys.

