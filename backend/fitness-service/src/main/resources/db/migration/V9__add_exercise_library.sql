-- =============================================================
-- CoreCompass — Fitness Schema Migration V9
-- Creates: exercises table
-- System exercises (is_system=true) are read-only for users.
-- Users can create custom exercises (is_system=false).
-- =============================================================

CREATE TABLE IF NOT EXISTS fitness_schema.exercises (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    name                VARCHAR(80)     NOT NULL,
    -- CHEST | BACK | SHOULDERS | BICEPS | TRICEPS | LEGS | GLUTES | CORE | FULL_BODY | CARDIO | OTHER
    muscle_group        VARCHAR(30)     NOT NULL,
    -- BARBELL | DUMBBELL | MACHINE | CABLE | BODYWEIGHT | RESISTANCE_BAND | KETTLEBELL | NONE | CUSTOM
    equipment           VARCHAR(30)     NOT NULL DEFAULT 'NONE',
    -- BEGINNER | INTERMEDIATE | ADVANCED
    difficulty          VARCHAR(20)     NOT NULL DEFAULT 'BEGINNER',
    instructions        TEXT,
    video_url           VARCHAR(500),
    is_system           BOOLEAN         NOT NULL DEFAULT false,
    -- created_by is NULL for system exercises
    created_by          UUID,
    is_deleted          BOOLEAN         NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_exercises PRIMARY KEY (id),
    CONSTRAINT chk_exercise_equipment
        CHECK (equipment IN ('BARBELL','DUMBBELL','MACHINE','CABLE',
                             'BODYWEIGHT','RESISTANCE_BAND','KETTLEBELL','NONE','CUSTOM')),
    CONSTRAINT chk_exercise_difficulty
        CHECK (difficulty IN ('BEGINNER','INTERMEDIATE','ADVANCED'))
);

CREATE INDEX IF NOT EXISTS idx_exercises_muscle
    ON fitness_schema.exercises (muscle_group)
    WHERE is_deleted = false;

CREATE INDEX IF NOT EXISTS idx_exercises_created_by
    ON fitness_schema.exercises (created_by)
    WHERE is_deleted = false;

-- ── SEED: System exercises ────────────────────────────────────
INSERT INTO fitness_schema.exercises
    (name, muscle_group, equipment, difficulty, instructions, is_system) VALUES
('Barbell Bench Press',  'CHEST',      'BARBELL',    'INTERMEDIATE', 'Lie flat on bench. Grip bar slightly wider than shoulders. Lower to chest, press up.', true),
('Push-up',              'CHEST',      'BODYWEIGHT',  'BEGINNER',    'Start in plank. Lower chest to floor, push back up. Keep core tight.', true),
('Pull-up',              'BACK',       'BODYWEIGHT',  'INTERMEDIATE', 'Hang from bar. Pull until chin clears bar. Lower with control.', true),
('Barbell Deadlift',     'BACK',       'BARBELL',    'ADVANCED',    'Feet hip-width. Hinge at hips, grip bar. Drive hips forward to stand.', true),
('Overhead Press',       'SHOULDERS',  'BARBELL',    'INTERMEDIATE', 'Hold bar at shoulder height. Press overhead until arms locked. Lower controlled.', true),
('Dumbbell Lateral Raise','SHOULDERS', 'DUMBBELL',   'BEGINNER',    'Stand, dumbbells at sides. Raise arms out to shoulder height. Lower slowly.', true),
('Barbell Curl',         'BICEPS',     'BARBELL',    'BEGINNER',    'Hold bar with underhand grip. Curl up keeping elbows fixed. Lower slowly.', true),
('Tricep Pushdown',      'TRICEPS',    'CABLE',      'BEGINNER',    'Grip cable bar. Keep elbows at sides. Push bar down until arms extended.', true),
('Barbell Squat',        'LEGS',       'BARBELL',    'INTERMEDIATE', 'Bar on upper back. Feet shoulder-width. Squat until thighs parallel. Drive up.', true),
('Romanian Deadlift',    'LEGS',       'BARBELL',    'INTERMEDIATE', 'Hip hinge with soft knees. Lower bar along legs. Feel hamstring stretch. Drive up.', true),
('Hip Thrust',           'GLUTES',     'BARBELL',    'INTERMEDIATE', 'Shoulder on bench, bar on hips. Drive hips up to full extension. Squeeze glutes.', true),
('Glute Bridge',         'GLUTES',     'BODYWEIGHT',  'BEGINNER',    'Lie on back, feet flat. Drive hips up, squeeze glutes at top. Lower controlled.', true),
('Plank',                'CORE',       'BODYWEIGHT',  'BEGINNER',    'Forearms on floor, body straight. Hold position. Breathe steadily.', true),
('Cable Crunch',         'CORE',       'CABLE',      'BEGINNER',    'Kneel at cable. Crunch down pulling weight. Contract abs at bottom.', true),
('Burpee',               'FULL_BODY',  'BODYWEIGHT',  'INTERMEDIATE', 'Squat down, kick feet back, push-up, jump feet in, jump up. Repeat.', true),
('Running',              'CARDIO',     'NONE',       'BEGINNER',    'Maintain steady pace. Land midfoot. Keep arms relaxed at 90 degrees.', true),
('Jump Rope',            'CARDIO',     'NONE',       'BEGINNER',    'Light grip on handles. Jump on balls of feet. Keep jumps small and controlled.', true)
ON CONFLICT DO NOTHING;