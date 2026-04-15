-- ==============================================================
-- CoreCompass — Fitness Schema V3 (FINAL)
-- Matches Java entity field names exactly
-- ==============================================================
CREATE SCHEMA IF NOT EXISTS fitness_schema;

-- Auto-update trigger function
CREATE OR REPLACE FUNCTION fitness_schema.update_updated_at()
RETURNS TRIGGER AS $$ BEGIN NEW.updated_at = NOW(); RETURN NEW; END; $$ LANGUAGE plpgsql;

-- CARDIO LOGS
CREATE TABLE IF NOT EXISTS fitness_schema.cardio_logs (
    id               UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id          UUID         NOT NULL,
    cardio_type      VARCHAR(60)  NOT NULL,
    duration_minutes INT          NOT NULL,
    distance_km      NUMERIC(8,2),
    calories_burned  NUMERIC(8,2),
    avg_heart_rate   INT,
    max_heart_rate   INT,
    logged_date      DATE         NOT NULL,
    notes            TEXT,
    is_deleted       BOOLEAN      NOT NULL DEFAULT false,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_cardio_duration CHECK (duration_minutes > 0)
);
CREATE INDEX IF NOT EXISTS idx_cardio_user_date ON fitness_schema.cardio_logs(user_id, logged_date) WHERE is_deleted=false;
CREATE TRIGGER trg_cardio_updated_at BEFORE UPDATE ON fitness_schema.cardio_logs
    FOR EACH ROW EXECUTE FUNCTION fitness_schema.update_updated_at();

-- WORKOUT SESSIONS (workout_name NOT workout_type)
CREATE TABLE IF NOT EXISTS fitness_schema.workout_sessions (
    id               UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id          UUID         NOT NULL,
    workout_name     VARCHAR(120) NOT NULL,
    session_date     DATE         NOT NULL,
    duration_minutes INT,
    total_volume_kg  NUMERIC(10,2),
    notes            TEXT,
    is_deleted       BOOLEAN      NOT NULL DEFAULT false,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_workout_user_date ON fitness_schema.workout_sessions(user_id, session_date) WHERE is_deleted=false;
CREATE TRIGGER trg_workout_updated_at BEFORE UPDATE ON fitness_schema.workout_sessions
    FOR EACH ROW EXECUTE FUNCTION fitness_schema.update_updated_at();

-- EXERCISE SETS (exercise_sets NOT exercise_logs; has set_number)
CREATE TABLE IF NOT EXISTS fitness_schema.exercise_sets (
    id               UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    session_id       UUID         NOT NULL REFERENCES fitness_schema.workout_sessions(id) ON DELETE CASCADE,
    exercise_name    VARCHAR(100) NOT NULL,
    set_number       INT          NOT NULL,
    reps             INT,
    weight_kg        NUMERIC(6,2),
    duration_seconds INT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_exercise_session ON fitness_schema.exercise_sets(session_id);

-- MEAL LOGS
CREATE TABLE IF NOT EXISTS fitness_schema.meal_logs (
    id             UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id        UUID         NOT NULL,
    meal_type      VARCHAR(30)  NOT NULL CHECK (meal_type IN ('BREAKFAST','LUNCH','DINNER','SNACK')),
    meal_date      DATE         NOT NULL,
    meal_time      TIME,
    total_calories NUMERIC(8,2),
    protein_g      NUMERIC(6,2),
    carbs_g        NUMERIC(6,2),
    fat_g          NUMERIC(6,2),
    meal_name      VARCHAR(200),
    notes          TEXT,
    is_deleted     BOOLEAN      NOT NULL DEFAULT false,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_meal_user_date ON fitness_schema.meal_logs(user_id, meal_date) WHERE is_deleted=false;

-- BODY METRICS
CREATE TABLE IF NOT EXISTS fitness_schema.body_metrics (
    id          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id     UUID         NOT NULL,
    metric_type VARCHAR(30)  NOT NULL,
    value       NUMERIC(8,2) NOT NULL,
    logged_date DATE         NOT NULL,
    notes       TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_metrics_user_type_date ON fitness_schema.body_metrics(user_id, metric_type, logged_date);

-- SLEEP LOGS
CREATE TABLE IF NOT EXISTS fitness_schema.sleep_logs (
    id               UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id          UUID        NOT NULL,
    sleep_date       DATE        NOT NULL,
    bed_time         TIME,
    wake_time        TIME,
    duration_minutes INT,
    quality_rating   INT         CHECK (quality_rating BETWEEN 1 AND 5),
    notes            TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, sleep_date)
);
CREATE INDEX IF NOT EXISTS idx_sleep_user_date ON fitness_schema.sleep_logs(user_id, sleep_date);

-- MOOD LOGS
CREATE TABLE IF NOT EXISTS fitness_schema.mood_logs (
    id           UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id      UUID        NOT NULL,
    log_date     DATE        NOT NULL,
    energy_level INT         CHECK (energy_level BETWEEN 1 AND 10),
    mood_score   INT         CHECK (mood_score BETWEEN 1 AND 10),
    mood         VARCHAR(20) CHECK (mood IN ('GREAT','GOOD','NEUTRAL','TIRED','SICK')),
    notes        TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, log_date)
);
CREATE INDEX IF NOT EXISTS idx_mood_user_date ON fitness_schema.mood_logs(user_id, log_date);

-- HYDRATION LOGS
CREATE TABLE IF NOT EXISTS fitness_schema.hydration_logs (
    id         UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id    UUID        NOT NULL,
    log_date   DATE        NOT NULL,
    amount_ml  INT         NOT NULL CHECK (amount_ml > 0),
    target_ml  INT         NOT NULL DEFAULT 2500,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_hydration_user_date ON fitness_schema.hydration_logs(user_id, log_date);

-- SEED: System cardio types (Type Registry)
CREATE TABLE IF NOT EXISTS fitness_schema.cardio_types (
    id        UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name      VARCHAR(60) NOT NULL,
    icon      VARCHAR(10),
    color     VARCHAR(7),
    is_system BOOLEAN     NOT NULL DEFAULT false,
    created_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
INSERT INTO fitness_schema.cardio_types (name, icon, is_system) VALUES
    ('Running','🏃',true),('Cycling','🚲',true),('Swimming','🏊',true),
    ('Walking','🚶',true),('Hiking','⛰️',true),('HIIT','⚡',true),
    ('Yoga','🧘',true),('Boxing','🥊',true),('Jump Rope','🪢',true),
    ('Rowing','🚣',true),('Elliptical','🔄',true),('Stair Climbing','🪜',true)
ON CONFLICT DO NOTHING;
