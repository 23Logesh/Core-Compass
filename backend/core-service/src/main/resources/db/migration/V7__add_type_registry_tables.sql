-- ==============================================================
-- V7 - Type Registry supplementary tables
-- notifications, achievements (core_schema)
-- ==============================================================

-- NOTIFICATIONS (core_schema)
CREATE TABLE IF NOT EXISTS core_schema.notifications (
    id          UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id     UUID        NOT NULL,
    type        VARCHAR(50) NOT NULL,  -- GOAL_DUE | HABIT_REMINDER | BUDGET_ALERT | WEEKLY_REPORT
    title       VARCHAR(200) NOT NULL,
    message     TEXT,
    is_read     BOOLEAN     NOT NULL DEFAULT false,
    metadata    JSONB,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_notifications_user    ON core_schema.notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created ON core_schema.notifications(user_id, created_at DESC);

-- USER PREFERENCES (core_schema)
CREATE TABLE IF NOT EXISTS core_schema.user_preferences (
    user_id         UUID        NOT NULL PRIMARY KEY,
    theme           VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',  -- LIGHT|DARK|SYSTEM
    currency        VARCHAR(10) NOT NULL DEFAULT 'INR',
    timezone        VARCHAR(50) NOT NULL DEFAULT 'Asia/Kolkata',
    units           VARCHAR(10) NOT NULL DEFAULT 'METRIC',  -- METRIC|IMPERIAL
    weekly_report   BOOLEAN     NOT NULL DEFAULT true,
    budget_alerts   BOOLEAN     NOT NULL DEFAULT true,
    habit_reminders BOOLEAN     NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
