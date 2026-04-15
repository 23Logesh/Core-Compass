-- =============================================================
-- CoreCompass — Auth Schema Migration V1
-- Creates: auth_schema, users table, refresh_tokens table
-- =============================================================

-- Create schema (isolated per service — per LLD Section 12)
CREATE SCHEMA IF NOT EXISTS auth_schema;

-- =============================================================
-- USERS
-- =============================================================
CREATE TABLE IF NOT EXISTS auth_schema.users (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255),                   -- NULL for OAuth-only users
    name            VARCHAR(255) NOT NULL,
    google_id       VARCHAR(255),                   -- NULL for email/password users
    role            VARCHAR(50)  NOT NULL DEFAULT 'USER',
    avatar_url      VARCHAR(500),
    is_active       BOOLEAN      NOT NULL DEFAULT true,
    is_deleted      BOOLEAN      NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email     UNIQUE (email),
    CONSTRAINT uq_users_google_id UNIQUE (google_id),
    CONSTRAINT chk_users_role     CHECK (role IN ('USER', 'ADMIN'))
);

-- Indexes for fast lookups
CREATE INDEX IF NOT EXISTS idx_users_email     ON auth_schema.users (email)     WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_users_google_id ON auth_schema.users (google_id) WHERE google_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_active    ON auth_schema.users (is_active, is_deleted);

-- Auto-update updated_at trigger
CREATE OR REPLACE FUNCTION auth_schema.update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON auth_schema.users
    FOR EACH ROW
    EXECUTE FUNCTION auth_schema.update_updated_at();

-- =============================================================
-- REFRESH TOKENS
-- BCrypt hash stored; raw UUID sent to client via HttpOnly cookie
-- =============================================================
CREATE TABLE IF NOT EXISTS auth_schema.refresh_tokens (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL,
    token_hash   VARCHAR(255) NOT NULL,
    expires_at   TIMESTAMPTZ  NOT NULL,
    revoked      BOOLEAN      NOT NULL DEFAULT false,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_refresh_tokens       PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token_hash   UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user  FOREIGN KEY (user_id)
        REFERENCES auth_schema.users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id  ON auth_schema.refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_hash     ON auth_schema.refresh_tokens (token_hash) WHERE revoked = false;
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires  ON auth_schema.refresh_tokens (expires_at) WHERE revoked = false;

-- =============================================================
-- SEED — Default admin user (change password immediately!)
-- Password: Admin@CoreCompass2026
-- =============================================================
-- INSERT INTO auth_schema.users (email, password_hash, name, role)
-- VALUES (
--   'admin@corecompass.app',
--   '$2a$12$...',   -- bcrypt hash of 'Admin@CoreCompass2026'
--   'System Admin',
--   'ADMIN'
-- ) ON CONFLICT DO NOTHING;
