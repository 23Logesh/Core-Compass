-- =============================================================
-- CoreCompass — Fitness Schema Migration V13
-- Creates: workout_types, metric_types, meal_types tables
-- (cardio_types already exists in V3)
-- =============================================================

-- ── Workout types ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS fitness_schema.workout_types (
    id          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name        VARCHAR(60)  NOT NULL,
    icon        VARCHAR(10),
    color       VARCHAR(7),
    is_system   BOOLEAN      NOT NULL DEFAULT false,
    created_by  UUID,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

INSERT INTO fitness_schema.workout_types (name, icon, is_system) VALUES
    ('Push Day',       '💪', true),
    ('Pull Day',       '🔙', true),
    ('Leg Day',        '🦵', true),
    ('Full Body',      '🏋️', true),
    ('Upper Body',     '👆', true),
    ('Lower Body',     '👇', true),
    ('Core',           '🎯', true),
    ('Cardio',         '🏃', true),
    ('Mobility',       '🤸', true),
    ('Sport Specific', '⚽', true)
ON CONFLICT DO NOTHING;

-- ── Metric types ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS fitness_schema.metric_types (
    id          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name        VARCHAR(60)  NOT NULL,
    unit        VARCHAR(20),
    icon        VARCHAR(10),
    is_system   BOOLEAN      NOT NULL DEFAULT false,
    created_by  UUID,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

INSERT INTO fitness_schema.metric_types (name, unit, icon, is_system) VALUES
    ('Weight',          'kg',   '⚖️',  true),
    ('Height',          'cm',   '📏',  true),
    ('Body Fat',        '%',    '📊',  true),
    ('Muscle Mass',     'kg',   '💪',  true),
    ('Waist',           'cm',   '📐',  true),
    ('Chest',           'cm',   '📐',  true),
    ('Hips',            'cm',   '📐',  true),
    ('Thigh',           'cm',   '📐',  true),
    ('Bicep',           'cm',   '📐',  true),
    ('Neck',            'cm',   '📐',  true),
    ('Resting HR',      'bpm',  '❤️',  true),
    ('Blood Pressure',  'mmHg', '🩺',  true),
    ('VO2 Max',         'ml/kg/min', '🫁', true)
ON CONFLICT DO NOTHING;

-- ── Meal types ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS fitness_schema.meal_types (
    id          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name        VARCHAR(60)  NOT NULL,
    icon        VARCHAR(10),
    is_system   BOOLEAN      NOT NULL DEFAULT false,
    created_by  UUID,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

INSERT INTO fitness_schema.meal_types (name, icon, is_system) VALUES
    ('Breakfast',   '🌅', true),
    ('Lunch',       '🌞', true),
    ('Dinner',      '🌙', true),
    ('Snack',       '🍎', true),
    ('Pre-Workout', '⚡', true),
    ('Post-Workout','🥛', true),
    ('Cheat Meal',  '🍔', true)
ON CONFLICT DO NOTHING;