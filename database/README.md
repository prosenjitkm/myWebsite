# Database Schema

This folder contains the raw SQL for the local PostgreSQL database.

## Create the Database

1. Create a PostgreSQL database named `mywebsite`.
2. Run `schema.sql`.
3. Run `seed_resume.sql` if you want the starter resume data.

Example on Windows PowerShell:

```powershell
$env:PGPASSWORD = 'admin'
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U admin -d mywebsite -f "database/schema.sql"
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U admin -d mywebsite -f "database/seed_resume.sql"
```

## Tables

| Table | Purpose |
| --- | --- |
| `roles` | Role lookup table for `ADMIN` and `USER` |
| `users` | Registered users and OAuth-linked users |
| `posts` | Blog posts |
| `tags` | Tag definitions |
| `post_tags` | Post-to-tag join table |
| `comments` | Comments and replies |
| `resume_sections` | Resume rows managed through the app |

## Notes

- UUIDs are used for user, post, and comment IDs.
- `resume_sections` uses `SERIAL`.
- Schema changes belong in `schema.sql` plus a new `patch_*.sql` file when needed.
- Secrets do not belong in this folder or in committed SQL dumps.
