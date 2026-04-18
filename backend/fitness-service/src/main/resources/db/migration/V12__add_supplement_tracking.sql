-- =============================================================
-- CoreCompass — Fitness Schema Migration V12
-- Creates: supplement_types, supplement_logs, supplement_schedules
-- =============================================================

-- ── Supplement types (system + user custom) ───────────────────
CREATE TABLE IF NOT EXISTS fitness_schema.supplement_types (
    id          UUID            NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(100)    NOT NULL,
    -- VITAMIN | MINERAL | PROTEIN | PREWORKOUT | RECOVERY | HERBAL | OTHER
    category    VARCHAR(20)     NOT NULL DEFAULT 'OTHER',
    description VARCHAR(300),
    is_system   BOOLEAN         NOT NULL DEFAULT false,
    created_by  UUID,

    CONSTRAINT pk_supplement_types PRIMARY KEY (id),
    CONSTRAINT chk_supplement_category
        CHECK (category IN ('VITAMIN','MINERAL','PROTEIN','PREWORKOUT','RECOVERY','HERBAL','OTHER'))
);

-- Seed: common system supplement types
INSERT INTO fitness_schema.supplement_types
    (name, category, description, is_system) VALUES
('Whey Protein',        'PROTEIN',    'Fast-digesting protein for muscle recovery',  true),
('Creatine Monohydrate','PREWORKOUT', 'Enhances strength and high-intensity output', true),
('Vitamin D3',          'VITAMIN',    'Bone health, immunity, mood support',         true),
('Omega-3 Fish Oil',    'MINERAL',    'Heart health and anti-inflammation',          true),
('Magnesium',           'MINERAL',    'Sleep quality, muscle relaxation',            true),
('Zinc',                'MINERAL',    'Immunity and testosterone support',           true),
('Caffeine',            'PREWORKOUT', 'Focus and energy boost',                      true),
('BCAA',                'RECOVERY',   'Branched-chain amino acids for recovery',     true),
('Ashwagandha',         'HERBAL',     'Stress reduction and cortisol management',    true),
('Multivitamin',        'VITAMIN',    'General daily micronutrient coverage',        true)
ON CONFLICT DO NOTHING;

-- ── Supplement logs (what user actually took) ─────────────────
CREATE TABLE IF NOT EXISTS fitness_schema.supplement_logs (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    user_id             UUID            NOT NULL,
    supplement_type_id  UUID            NOT NULL
        REFERENCES fitness_schema.supplement_types(id),
    -- dose amount e.g. 5 (grams), 1 (capsule), 500 (mg)
    dose_amount         NUMERIC(8, 2)   NOT NULL,
    -- GRAM | CAPSULE | ML | MG | TABLET | SCOOP
    dose_unit           VARCHAR(10)     NOT NULL DEFAULT 'GRAM',
    -- MORNING | AFTERNOON | EVENING | NIGHT | PRE_WORKOUT | POST_WORKOUT
    timing              VARCHAR(20),
    logged_date         DATE            NOT NULL DEFAULT CURRENT_DATE,
    notes               VARCHAR(200),
    is_deleted          BOOLEAN         NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_supplement_logs PRIMARY KEY (id),
    CONSTRAINT chk_dose_unit
        CHECK (dose_unit IN ('GRAM','CAPSULE','ML','MG','TABLET','SCOOP')),
    CONSTRAINT chk_supplement_timing
        CHECK (timing IN ('MORNING','AFTERNOON','EVENING','NIGHT',
                          'PRE_WORKOUT','POST_WORKOUT') OR timing IS NULL)
);

CREATE INDEX IF NOT EXISTS idx_supplement_logs_user_date
    ON fitness_schema.supplement_logs (user_id, logged_date)
    WHERE is_deleted = false;

-- ── Supplement schedules (recurring reminders) ────────────────
CREATE TABLE IF NOT EXISTS fitness_schema.supplement_schedules (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    user_id             UUID            NOT NULL,
    supplement_type_id  UUID            NOT NULL
        REFERENCES fitness_schema.supplement_types(id),
    dose_amount         NUMERIC(8, 2)   NOT NULL,
    dose_unit           VARCHAR(10)     NOT NULL DEFAULT 'GRAM',
    timing              VARCHAR(20)     NOT NULL,
    -- MON|TUE|WED|THU|FRI|SAT|SUN comma-separated, or DAILY
    frequency           VARCHAR(50)     NOT NULL DEFAULT 'DAILY',
    is_active           BOOLEAN         NOT NULL DEFAULT true,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_supplement_schedules PRIMARY KEY (id),
    CONSTRAINT chk_schedule_dose_unit
        CHECK (dose_unit IN ('GRAM','CAPSULE','ML','MG','TABLET','SCOOP')),
    CONSTRAINT chk_schedule_timing
        CHECK (timing IN ('MORNING','AFTERNOON','EVENING','NIGHT',
                          'PRE_WORKOUT','POST_WORKOUT'))
);

CREATE INDEX IF NOT EXISTS idx_supplement_schedules_user
    ON fitness_schema.supplement_schedules (user_id, is_active);

CREATE TRIGGER trg_supplement_schedules_updated_at
    BEFORE UPDATE ON fitness_schema.supplement_schedules
    FOR EACH ROW EXECUTE FUNCTION fitness_schema.update_updated_at();