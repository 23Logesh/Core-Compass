-- =============================================================
-- CoreCompass — Fitness Schema Migration V10
-- Creates: workout_plans, workout_plan_exercises tables
-- =============================================================

-- ── Workout plan templates ────────────────────────────────────
CREATE TABLE IF NOT EXISTS fitness_schema.workout_plans (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id          UUID         NOT NULL,
    name             VARCHAR(120) NOT NULL,
    description      VARCHAR(300),
    -- BEGINNER | INTERMEDIATE | ADVANCED
    difficulty       VARCHAR(20)  NOT NULL DEFAULT 'BEGINNER',
    -- days per week this plan targets
    days_per_week    INT          NOT NULL DEFAULT 3,
    -- only one plan can be active at a time per user
    is_active        BOOLEAN      NOT NULL DEFAULT false,
    is_deleted       BOOLEAN      NOT NULL DEFAULT false,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_workout_plans PRIMARY KEY (id),
    CONSTRAINT chk_plan_difficulty
        CHECK (difficulty IN ('BEGINNER','INTERMEDIATE','ADVANCED')),
    CONSTRAINT chk_days_per_week
        CHECK (days_per_week BETWEEN 1 AND 7)
);

CREATE INDEX IF NOT EXISTS idx_workout_plans_user
    ON fitness_schema.workout_plans (user_id, is_active)
    WHERE is_deleted = false;

CREATE TRIGGER trg_workout_plans_updated_at
    BEFORE UPDATE ON fitness_schema.workout_plans
    FOR EACH ROW EXECUTE FUNCTION fitness_schema.update_updated_at();

-- ── Exercises inside a plan (template, not actual logs) ───────
CREATE TABLE IF NOT EXISTS fitness_schema.workout_plan_exercises (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    plan_id          UUID         NOT NULL
        REFERENCES fitness_schema.workout_plans(id) ON DELETE CASCADE,
    -- which day of the week this exercise belongs to (1=Mon … 7=Sun)
    day_number       INT          NOT NULL DEFAULT 1,
    -- free-text name (or matches exercises.name for library link)
    exercise_name    VARCHAR(100) NOT NULL,
    -- optional FK to exercise library — null is fine (custom name)
    exercise_id      UUID,
    sets             INT          NOT NULL DEFAULT 3,
    target_reps      INT,
    target_weight_kg NUMERIC(6,2),
    notes            VARCHAR(200),
    sort_order       INT          NOT NULL DEFAULT 0,

    CONSTRAINT pk_workout_plan_exercises PRIMARY KEY (id),
    CONSTRAINT chk_day_number CHECK (day_number BETWEEN 1 AND 7)
);

CREATE INDEX IF NOT EXISTS idx_plan_exercises_plan
    ON fitness_schema.workout_plan_exercises (plan_id, day_number);