-- =============================================================
-- CoreCompass — Core Schema Migration V2
-- Creates: goal_types, goals, todos, milestones, activities, activity_types
-- =============================================================

CREATE SCHEMA IF NOT EXISTS core_schema;

-- =============================================================
-- GOAL TYPES (Type Registry Pattern)
-- =============================================================
CREATE TABLE IF NOT EXISTS core_schema.goal_types (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(60) NOT NULL,
    icon        VARCHAR(10),
    color       VARCHAR(7),
    unit        VARCHAR(50),
    is_system   BOOLEAN     NOT NULL DEFAULT false,
    is_public   BOOLEAN     NOT NULL DEFAULT false,
    created_by  UUID,
    metadata    JSONB,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_goal_types PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_goal_types_created_by ON core_schema.goal_types(created_by);
CREATE INDEX IF NOT EXISTS idx_goal_types_system     ON core_schema.goal_types(is_system);

-- Seed system goal types (LLD Section 2.2)
INSERT INTO core_schema.goal_types (name, icon, color, is_system, is_public) VALUES
    ('FITNESS',          '🏃', '#FF6B35', true, true),
    ('FINANCE',          '💰', '#27AE60', true, true),
    ('CAREER',           '💼', '#2980B9', true, true),
    ('PERSONAL',         '🌱', '#8E44AD', true, true),
    ('HEALTH',           '❤️', '#E74C3C', true, true),
    ('LEARNING',         '📚', '#F39C12', true, true),
    ('RELATIONSHIPS',    '👥', '#1ABC9C', true, true),
    ('MENTAL_WELLNESS',  '🧘', '#3498DB', true, true),
    ('TRAVEL',           '✈️', '#E67E22', true, true),
    ('SPIRITUAL',        '🙏', '#9B59B6', true, true)
ON CONFLICT DO NOTHING;

-- =============================================================
-- GOALS
-- =============================================================
CREATE TABLE IF NOT EXISTS core_schema.goals (
    id               UUID           NOT NULL DEFAULT gen_random_uuid(),
    user_id          UUID           NOT NULL,
    title            VARCHAR(255)   NOT NULL,
    category_type_id UUID           NOT NULL,
    description      TEXT,
    target_date      DATE,
    progress_pct     NUMERIC(5,2)   NOT NULL DEFAULT 0.00,
    status           VARCHAR(50)    NOT NULL DEFAULT 'ACTIVE',
    color            VARCHAR(7),
    icon             VARCHAR(100),
    is_deleted       BOOLEAN        NOT NULL DEFAULT false,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    created_by       UUID           NOT NULL,

    CONSTRAINT pk_goals      PRIMARY KEY (id),
    CONSTRAINT chk_goal_status CHECK (status IN ('ACTIVE','COMPLETED','ARCHIVED')),
    CONSTRAINT chk_progress   CHECK (progress_pct BETWEEN 0 AND 100)
);

CREATE INDEX IF NOT EXISTS idx_goals_user_status  ON core_schema.goals(user_id, status) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_goals_user_id      ON core_schema.goals(user_id)         WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_goals_category     ON core_schema.goals(category_type_id);

-- Auto-update trigger
CREATE OR REPLACE FUNCTION core_schema.update_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_goals_updated_at
    BEFORE UPDATE ON core_schema.goals
    FOR EACH ROW EXECUTE FUNCTION core_schema.update_updated_at();

-- =============================================================
-- MILESTONES
-- =============================================================
CREATE TABLE IF NOT EXISTS core_schema.milestones (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    goal_id      UUID        NOT NULL,
    user_id      UUID        NOT NULL,
    title        VARCHAR(120) NOT NULL,
    target_date  DATE,
    completed    BOOLEAN     NOT NULL DEFAULT false,
    completed_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_milestones PRIMARY KEY (id),
    CONSTRAINT fk_milestones_goal FOREIGN KEY (goal_id)
        REFERENCES core_schema.goals(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_milestones_goal_id ON core_schema.milestones(goal_id);

-- =============================================================
-- TODOS
-- =============================================================
CREATE TABLE IF NOT EXISTS core_schema.todos (
    id                      UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id                 UUID        NOT NULL,
    goal_id                 UUID        NOT NULL,
    title                   VARCHAR(255) NOT NULL,
    description             TEXT,
    due_date                DATE,
    due_time                TIME,
    completed               BOOLEAN     NOT NULL DEFAULT false,
    completed_at            TIMESTAMPTZ,
    recurrence_rule         VARCHAR(20),
    calendar_event_id       VARCHAR(255),
    calendar_sync_attempts  INT         NOT NULL DEFAULT 0,
    is_deleted              BOOLEAN     NOT NULL DEFAULT false,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by              UUID        NOT NULL,

    CONSTRAINT pk_todos PRIMARY KEY (id),
    CONSTRAINT chk_recurrence CHECK (recurrence_rule IN ('DAILY','WEEKLY','MONTHLY') OR recurrence_rule IS NULL),
    CONSTRAINT fk_todos_goal  FOREIGN KEY (goal_id)
        REFERENCES core_schema.goals(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_todos_user_due  ON core_schema.todos(user_id, due_date)  WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_todos_goal_id   ON core_schema.todos(goal_id)             WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_todos_user_id   ON core_schema.todos(user_id)             WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_todos_pending_cal ON core_schema.todos(calendar_sync_attempts)
    WHERE calendar_event_id IS NULL AND due_date IS NOT NULL AND due_time IS NOT NULL;

CREATE OR REPLACE TRIGGER trg_todos_updated_at
    BEFORE UPDATE ON core_schema.todos
    FOR EACH ROW EXECUTE FUNCTION core_schema.update_updated_at();

-- =============================================================
-- ACTIVITY TYPES (Type Registry)
-- =============================================================
CREATE TABLE IF NOT EXISTS core_schema.activity_types (
    id         UUID        NOT NULL DEFAULT gen_random_uuid(),
    name       VARCHAR(60) NOT NULL,
    icon       VARCHAR(10),
    color      VARCHAR(7),
    is_system  BOOLEAN     NOT NULL DEFAULT false,
    created_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_activity_types PRIMARY KEY (id)
);

INSERT INTO core_schema.activity_types (name, icon, is_system) VALUES
    ('Note',       '📝', true),
    ('Meeting',    '🤝', true),
    ('Reading',    '📖', true),
    ('Exercise',   '🏋️', true),
    ('Research',   '🔍', true),
    ('Review',     '✅', true)
ON CONFLICT DO NOTHING;

-- =============================================================
-- ACTIVITIES
-- =============================================================
CREATE TABLE IF NOT EXISTS core_schema.activities (
    id               UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id          UUID        NOT NULL,
    goal_id          UUID        NOT NULL,
    activity_type_id UUID        NOT NULL,
    note             TEXT,
    value            NUMERIC(10,2),
    unit             VARCHAR(20),
    is_deleted       BOOLEAN     NOT NULL DEFAULT false,
    logged_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_activities PRIMARY KEY (id),
    CONSTRAINT fk_activities_goal FOREIGN KEY (goal_id)
        REFERENCES core_schema.goals(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_activities_goal_id ON core_schema.activities(goal_id) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_activities_user_id ON core_schema.activities(user_id) WHERE is_deleted = false;
