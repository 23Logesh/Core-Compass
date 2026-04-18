-- =============================================================
-- CoreCompass — Core Schema Migration V9
-- Creates: achievement_definitions, user_achievements tables
-- =============================================================

-- ── Achievement definitions (system seeded, read-only for users) ──
CREATE TABLE IF NOT EXISTS core_schema.achievement_definitions (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    key              VARCHAR(50)  NOT NULL,  -- e.g. "7_DAY_WARRIOR"
    title            VARCHAR(100) NOT NULL,
    description      VARCHAR(300) NOT NULL,
    icon_emoji       VARCHAR(10)  NOT NULL DEFAULT '🏆',
    -- WORKOUT_STREAK | BUDGET_MONTHS | HYDRATION_STREAK | DEBT_PAID | WEIGHT_LOSS | TODOS_COMPLETED | GOAL_COMPLETED
    condition_type   VARCHAR(50)  NOT NULL,
    condition_value  INTEGER      NOT NULL, -- threshold to meet
    is_system        BOOLEAN      NOT NULL DEFAULT true,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_achievement_definitions PRIMARY KEY (id),
    CONSTRAINT uq_achievement_key         UNIQUE (key)
);

-- ── User earned achievements ──────────────────────────────────
CREATE TABLE IF NOT EXISTS core_schema.user_achievements (
    id                UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id           UUID        NOT NULL,
    achievement_id    UUID        NOT NULL,
    earned_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_user_achievements        PRIMARY KEY (id),
    CONSTRAINT uq_user_achievement         UNIQUE (user_id, achievement_id),
    CONSTRAINT fk_user_ach_definition      FOREIGN KEY (achievement_id)
        REFERENCES core_schema.achievement_definitions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_achievements_user
    ON core_schema.user_achievements (user_id);

-- ── Seed: system achievement definitions ─────────────────────
INSERT INTO core_schema.achievement_definitions
    (key, title, description, icon_emoji, condition_type, condition_value) VALUES
('7_DAY_WARRIOR',    '7-Day Warrior',      'Log workouts 7 days in a row',              '💪', 'WORKOUT_STREAK',    7),
('30_DAY_WARRIOR',   '30-Day Warrior',     'Log workouts 30 days in a row',             '🔥', 'WORKOUT_STREAK',    30),
('BUDGET_GUARDIAN',  'Budget Guardian',    'Stay under budget for 3 consecutive months','🛡️', 'BUDGET_MONTHS',     3),
('HYDRATION_HERO',   'Hydration Hero',     'Hit daily water goal for 14 days straight', '💧', 'HYDRATION_STREAK',  14),
('GOAL_CRUSHER',     'Goal Crusher',       'Complete your first goal',                  '🎯', 'GOAL_COMPLETED',    1),
('TODO_MACHINE',     'Todo Machine',       'Complete 100 todos total',                  '✅', 'TODOS_COMPLETED',   100),
('DEBT_SLAYER',      'Debt Slayer',        'Mark your first debt as fully paid off',    '⚔️', 'DEBT_PAID',         1),
('5KG_DOWN',         '5KG Down',          'Log a 5kg weight loss from your first entry','📉', 'WEIGHT_LOSS',        5)
ON CONFLICT (key) DO NOTHING;