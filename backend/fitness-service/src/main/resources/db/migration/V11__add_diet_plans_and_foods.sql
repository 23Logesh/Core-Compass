-- =============================================================
-- CoreCompass — Fitness Schema Migration V11
-- Creates: foods, diet_plans, diet_plan_meals tables
-- =============================================================

-- ── Food library ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS fitness_schema.foods (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    name                VARCHAR(120)    NOT NULL,
    brand               VARCHAR(100),
    -- macros are per 100g
    calories_per_100g   NUMERIC(8, 2)   NOT NULL DEFAULT 0,
    protein_per_100g    NUMERIC(6, 2)   NOT NULL DEFAULT 0,
    carbs_per_100g      NUMERIC(6, 2)   NOT NULL DEFAULT 0,
    fat_per_100g        NUMERIC(6, 2)   NOT NULL DEFAULT 0,
    -- common serving size in grams (e.g. 30 for a scoop of protein)
    serving_size_g      NUMERIC(6, 2),
    -- SOLID | LIQUID
    food_type           VARCHAR(10)     NOT NULL DEFAULT 'SOLID',
    is_system           BOOLEAN         NOT NULL DEFAULT false,
    created_by          UUID,
    is_deleted          BOOLEAN         NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_foods PRIMARY KEY (id),
    CONSTRAINT chk_food_type CHECK (food_type IN ('SOLID','LIQUID'))
);

CREATE INDEX IF NOT EXISTS idx_foods_created_by
    ON fitness_schema.foods (created_by)
    WHERE is_deleted = false;

CREATE TRIGGER trg_foods_updated_at
    BEFORE UPDATE ON fitness_schema.foods
    FOR EACH ROW EXECUTE FUNCTION fitness_schema.update_updated_at();

-- ── Seed: common system foods ────────────────────────────────
INSERT INTO fitness_schema.foods
    (name, brand, calories_per_100g, protein_per_100g, carbs_per_100g, fat_per_100g, serving_size_g, is_system) VALUES
('Chicken Breast (cooked)',  NULL,  165, 31.0,  0.0,  3.6,  100, true),
('Brown Rice (cooked)',      NULL,  123,  2.6, 25.6,  0.9,  150, true),
('Whole Egg',                NULL,  143, 12.6,  0.7, 10.0,   60, true),
('Banana',                   NULL,   89,  1.1, 22.8,  0.3,  120, true),
('Oats (dry)',               NULL,  389, 16.9, 66.3,  6.9,   50, true),
('Paneer',                   NULL,  265, 18.3,  1.2, 20.8,  100, true),
('Greek Yogurt (plain)',     NULL,   59,  10.0, 3.6,  0.4,  150, true),
('Almonds',                  NULL,  579, 21.2, 21.6, 49.9,   30, true),
('Sweet Potato (cooked)',    NULL,   86,  1.6, 20.1,  0.1,  150, true),
('Salmon (cooked)',          NULL,  208, 20.4,  0.0, 13.4,  150, true),
('Whole Milk',               NULL,   61,  3.2,  4.8,  3.3,  250, true),
('Whey Protein (powder)',    NULL,  400, 80.0, 10.0,  5.0,   30, true)
ON CONFLICT DO NOTHING;

-- ── Diet plan templates ────────────────────────────────────────
CREATE TABLE IF NOT EXISTS fitness_schema.diet_plans (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    user_id                 UUID            NOT NULL,
    name                    VARCHAR(120)    NOT NULL,
    description             VARCHAR(300),
    -- WEIGHT_LOSS | MUSCLE_GAIN | MAINTENANCE | CUSTOM
    goal                    VARCHAR(20)     NOT NULL DEFAULT 'MAINTENANCE',
    daily_calorie_target    NUMERIC(8, 2),
    daily_protein_g         NUMERIC(6, 2),
    daily_carbs_g           NUMERIC(6, 2),
    daily_fat_g             NUMERIC(6, 2),
    is_active               BOOLEAN         NOT NULL DEFAULT false,
    is_deleted              BOOLEAN         NOT NULL DEFAULT false,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_diet_plans PRIMARY KEY (id),
    CONSTRAINT chk_diet_goal
        CHECK (goal IN ('WEIGHT_LOSS','MUSCLE_GAIN','MAINTENANCE','CUSTOM'))
);

CREATE INDEX IF NOT EXISTS idx_diet_plans_user
    ON fitness_schema.diet_plans (user_id, is_active)
    WHERE is_deleted = false;

CREATE TRIGGER trg_diet_plans_updated_at
    BEFORE UPDATE ON fitness_schema.diet_plans
    FOR EACH ROW EXECUTE FUNCTION fitness_schema.update_updated_at();

-- ── Meals inside a diet plan (template, not actual logs) ──────
CREATE TABLE IF NOT EXISTS fitness_schema.diet_plan_meals (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    plan_id         UUID            NOT NULL
        REFERENCES fitness_schema.diet_plans(id) ON DELETE CASCADE,
    day_number      INT             NOT NULL DEFAULT 1,
    -- BREAKFAST | LUNCH | DINNER | SNACK
    meal_type       VARCHAR(20)     NOT NULL,
    -- optional FK to food library
    food_id         UUID,
    -- display name (use food.name if food_id set, else this)
    food_name       VARCHAR(120)    NOT NULL,
    quantity_g      NUMERIC(6, 2)   NOT NULL DEFAULT 100,
    -- pre-calculated macros for this entry (quantity_g / 100 * per_100g)
    calories        NUMERIC(8, 2),
    protein_g       NUMERIC(6, 2),
    carbs_g         NUMERIC(6, 2),
    fat_g           NUMERIC(6, 2),
    sort_order      INT             NOT NULL DEFAULT 0,

    CONSTRAINT pk_diet_plan_meals PRIMARY KEY (id),
    CONSTRAINT chk_diet_meal_type
        CHECK (meal_type IN ('BREAKFAST','LUNCH','DINNER','SNACK')),
    CONSTRAINT chk_diet_day_number CHECK (day_number BETWEEN 1 AND 7)
);

CREATE INDEX IF NOT EXISTS idx_diet_plan_meals_plan
    ON fitness_schema.diet_plan_meals (plan_id, day_number);