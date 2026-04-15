-- ==============================================================
-- CoreCompass — Habits Schema V5 (FINAL)
-- habits, habit_checkins, habit_stacks, routine_groups
-- Atomic Habits: cue → routine → reward
-- ==============================================================
CREATE SCHEMA IF NOT EXISTS habits_schema;

CREATE OR REPLACE FUNCTION habits_schema.update_updated_at()
RETURNS TRIGGER AS $$ BEGIN NEW.updated_at = NOW(); RETURN NEW; END; $$ LANGUAGE plpgsql;

-- HABIT CATEGORY TYPES (Type Registry)
CREATE TABLE IF NOT EXISTS habits_schema.habit_category_types (
    id          UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name        VARCHAR(60) NOT NULL,
    icon        VARCHAR(10),
    color       VARCHAR(7),
    is_system   BOOLEAN     NOT NULL DEFAULT false,
    is_public   BOOLEAN     NOT NULL DEFAULT false,
    created_by  UUID,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
INSERT INTO habits_schema.habit_category_types (name,icon,color,is_system) VALUES
    ('Health & Fitness','💪','#FF6B35',true),
    ('Finance & Money','💰','#27AE60',true),
    ('Learning & Growth','📚','#3498DB',true),
    ('Mental Wellness','🧘','#9B59B6',true),
    ('Productivity','⚡','#F39C12',true),
    ('Social & Relationships','👥','#1ABC9C',true),
    ('Spiritual / Mindfulness','🙏','#E91E63',true),
    ('Environmental','🌱','#4CAF50',true),
    ('Creative','🎨','#FF5722',true),
    ('Professional','💼','#607D8B',true)
ON CONFLICT DO NOTHING;

-- HABITS
CREATE TABLE IF NOT EXISTS habits_schema.habits (
    id               UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id          UUID        NOT NULL,
    title            VARCHAR(120) NOT NULL,
    description      VARCHAR(300),
    category_type_id UUID        REFERENCES habits_schema.habit_category_types(id),
    -- BINARY | QUANTITY | DURATION | RATING | CHECKLIST
    tracking_type    VARCHAR(20) NOT NULL DEFAULT 'BINARY',
    -- DAILY | SPECIFIC_DAYS | X_TIMES_PER_WEEK | X_TIMES_PER_MONTH | EVERY_N_DAYS | WEEKDAYS_ONLY | WEEKENDS_ONLY
    frequency_pattern VARCHAR(30) NOT NULL DEFAULT 'DAILY',
    frequency_config  JSONB,         -- {"daysOfWeek":["MON","WED","FRI"]} or {"timesPerWeek":4}
    target_value     DOUBLE PRECISION,
    target_unit      VARCHAR(30),
    checklist_steps  JSONB,           -- ["Step 1","Step 2"] for CHECKLIST type
    -- Atomic Habits framework
    cue              VARCHAR(200),
    reward           VARCHAR(200),
    reminder_time    TIME,
    reminder_days    JSONB,           -- ["MON","TUE","WED"]
    color            VARCHAR(7),
    icon             VARCHAR(10),
    start_date       DATE        NOT NULL DEFAULT CURRENT_DATE,
    -- Streak tracking
    current_streak   INT         NOT NULL DEFAULT 0,
    best_streak      INT         NOT NULL DEFAULT 0,
    total_checkins   INT         NOT NULL DEFAULT 0,
    -- ACTIVE | PAUSED | ARCHIVED
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_deleted       BOOLEAN     NOT NULL DEFAULT false,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_tracking  CHECK (tracking_type   IN ('BINARY','QUANTITY','DURATION','RATING','CHECKLIST')),
    CONSTRAINT chk_frequency CHECK (frequency_pattern IN ('DAILY','SPECIFIC_DAYS','X_TIMES_PER_WEEK','X_TIMES_PER_MONTH','EVERY_N_DAYS','WEEKDAYS_ONLY','WEEKENDS_ONLY')),
    CONSTRAINT chk_status    CHECK (status           IN ('ACTIVE','PAUSED','ARCHIVED'))
);
CREATE INDEX IF NOT EXISTS idx_habits_user_id     ON habits_schema.habits(user_id) WHERE is_deleted=false;
CREATE INDEX IF NOT EXISTS idx_habits_user_status ON habits_schema.habits(user_id, status) WHERE is_deleted=false;
CREATE TRIGGER trg_habits_updated_at BEFORE UPDATE ON habits_schema.habits
    FOR EACH ROW EXECUTE FUNCTION habits_schema.update_updated_at();

-- HABIT CHECK-INS
CREATE TABLE IF NOT EXISTS habits_schema.habit_checkins (
    id               UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    habit_id         UUID        NOT NULL REFERENCES habits_schema.habits(id) ON DELETE CASCADE,
    user_id          UUID        NOT NULL,
    checkin_date     DATE        NOT NULL,
    value            DOUBLE PRECISION,           -- for QUANTITY/DURATION/RATING
    steps_completed  JSONB,                       -- [0,1,3] for CHECKLIST
    mood             VARCHAR(20) CHECK (mood IN ('GREAT','GOOD','NEUTRAL','TIRED','SICK')),
    note             VARCHAR(300),
    is_skip          BOOLEAN     NOT NULL DEFAULT false,
    skip_reason      VARCHAR(200),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (habit_id, checkin_date)
);
CREATE INDEX IF NOT EXISTS idx_checkins_habit_date ON habits_schema.habit_checkins(habit_id, checkin_date);
CREATE INDEX IF NOT EXISTS idx_checkins_user_date  ON habits_schema.habit_checkins(user_id,  checkin_date);

-- HABIT STACKS (linked habit sequences — FR-HAB-06)
CREATE TABLE IF NOT EXISTS habits_schema.habit_stacks (
    id          UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id     UUID        NOT NULL,
    name        VARCHAR(80) NOT NULL,
    description VARCHAR(200),
    habit_ids   JSONB       NOT NULL DEFAULT '[]',  -- ordered UUID array
    is_deleted  BOOLEAN     NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_habit_stacks_user ON habits_schema.habit_stacks(user_id) WHERE is_deleted=false;
CREATE TRIGGER trg_stacks_updated_at BEFORE UPDATE ON habits_schema.habit_stacks
    FOR EACH ROW EXECUTE FUNCTION habits_schema.update_updated_at();

-- ROUTINE GROUPS (Morning / Evening / Pre-workout groupings — FR-HAB-06)
CREATE TABLE IF NOT EXISTS habits_schema.routine_groups (
    id          UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id     UUID        NOT NULL,
    name        VARCHAR(80) NOT NULL,
    description VARCHAR(200),
    time_of_day VARCHAR(20),  -- MORNING | AFTERNOON | EVENING | NIGHT
    habit_ids   JSONB       NOT NULL DEFAULT '[]',
    is_deleted  BOOLEAN     NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_routines_user ON habits_schema.routine_groups(user_id) WHERE is_deleted=false;
CREATE TRIGGER trg_routines_updated_at BEFORE UPDATE ON habits_schema.routine_groups
    FOR EACH ROW EXECUTE FUNCTION habits_schema.update_updated_at();
