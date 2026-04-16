-- =============================================================
-- CoreCompass — Auth Schema Migration V2
-- Creates: password_reset_tokens table
-- =============================================================

CREATE TABLE IF NOT EXISTS auth_schema.password_reset_tokens (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    email            VARCHAR(255) NOT NULL,
    otp_hash         VARCHAR(64)  NOT NULL,   -- SHA-256 of 6-digit OTP
    reset_token_hash VARCHAR(64),             -- SHA-256 of UUID; set after OTP verified
    expires_at       TIMESTAMPTZ  NOT NULL,   -- OTP expires in 15 min
    verified         BOOLEAN      NOT NULL DEFAULT false,
    used             BOOLEAN      NOT NULL DEFAULT false,
    attempts         INTEGER      NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_prt_email
    ON auth_schema.password_reset_tokens (email, expires_at)
    WHERE used = false;

CREATE INDEX IF NOT EXISTS idx_prt_reset_token
    ON auth_schema.password_reset_tokens (reset_token_hash)
    WHERE reset_token_hash IS NOT NULL AND used = false;