CREATE TABLE IF NOT EXISTS fitness_schema.fitness_targets (
    id                        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                   UUID         NOT NULL UNIQUE,
    weekly_workout_target     INT,
    daily_calorie_target      NUMERIC(8,2),
    daily_protein_target_g    NUMERIC(6,2),
    daily_hydration_target_ml INT,
    daily_calorie_burn_target NUMERIC(8,2),
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT now()
);