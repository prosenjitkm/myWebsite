-- ================================================================
-- Run this in pgAdmin 4 Query Tool (connected to the mywebsite DB)
-- This creates the 'admin' DB user that the Spring Boot app uses.
-- ================================================================

-- Step 1: Create the database user (run connected to any DB)
CREATE USER admin WITH PASSWORD 'admin';

-- Step 2: Grant access to the mywebsite database
GRANT ALL PRIVILEGES ON DATABASE mywebsite TO admin;

-- Step 3: Grant schema and table access
GRANT ALL ON SCHEMA public TO admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO admin;

-- Step 4: Make sure future tables are also accessible
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL ON TABLES TO admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL ON SEQUENCES TO admin;

-- Verify
SELECT usename, usesuper, usecreatedb FROM pg_user WHERE usename = 'admin';

