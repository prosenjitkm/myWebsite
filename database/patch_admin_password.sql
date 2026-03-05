-- Run this ONCE in pgAdmin 4 Query Tool (connected to the mywebsite database)
-- This sets the real bcrypt hash for the admin account.
-- (Do NOT store the plaintext password in this file.)

UPDATE users
SET password_hash = '$2a$12$/0ku453/2rPKtfxun1utw.2/aO0TQe0NvYADHW0PuSEfWuMNqrBl2'
WHERE email = 'admin@mywebsite.com';

-- Verify it updated
SELECT id, email, username, role_id, is_active FROM users WHERE email = 'admin@mywebsite.com';

