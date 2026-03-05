-- =============================================================
--  OAuth2 support patch
--  Run this once in pgAdmin / DBeaver against the "mywebsite" DB
--
--  Adds two columns to users:
--    oauth_provider  — e.g. 'google', 'github'  (NULL = local account)
--    oauth_id        — the provider's unique subject ID
--
--  password_hash becomes nullable so OAuth-only users have no
--  password stored in our DB at all.
-- =============================================================

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS oauth_provider VARCHAR(50)  DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS oauth_id       VARCHAR(255) DEFAULT NULL,
    ALTER COLUMN password_hash DROP NOT NULL;

-- Ensure a user can't accidentally have two OAuth accounts
-- with the same provider+id combination
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_oauth
    ON users (oauth_provider, oauth_id)
    WHERE oauth_provider IS NOT NULL;

