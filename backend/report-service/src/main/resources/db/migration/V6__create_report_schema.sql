-- ==============================================================
-- CoreCompass — Report Schema V6 (FINAL)
-- weekly_reports, monthly_reports
-- ==============================================================
CREATE SCHEMA IF NOT EXISTS report_schema;

-- WEEKLY REPORTS
CREATE TABLE IF NOT EXISTS report_schema.weekly_reports (
    id               UUID             NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id          UUID             NOT NULL,
    week_start       DATE             NOT NULL,
    week_end         DATE             NOT NULL,
    -- Core stats
    active_goals     INT              NOT NULL DEFAULT 0,
    avg_goal_progress DOUBLE PRECISION NOT NULL DEFAULT 0,
    todos_completed  INT              NOT NULL DEFAULT 0,
    -- Fitness stats
    workouts_count   INT              NOT NULL DEFAULT 0,
    cardio_count     INT              NOT NULL DEFAULT 0,
    calories_burned  DOUBLE PRECISION NOT NULL DEFAULT 0,
    avg_sleep_hours  DOUBLE PRECISION NOT NULL DEFAULT 0,
    avg_mood_score   DOUBLE PRECISION NOT NULL DEFAULT 0,
    water_goal_days  INT              NOT NULL DEFAULT 0,
    -- Finance stats
    monthly_income   DOUBLE PRECISION NOT NULL DEFAULT 0,
    monthly_expenses DOUBLE PRECISION NOT NULL DEFAULT 0,
    net_savings      DOUBLE PRECISION NOT NULL DEFAULT 0,
    finance_health_score INT          NOT NULL DEFAULT 0,
    -- Habits stats
    habit_score      INT              NOT NULL DEFAULT 0,
    habits_checked_in INT             NOT NULL DEFAULT 0,
    -- AI-style insights (JSON array of strings)
    insights         JSONB,
    created_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, week_start)
);
CREATE INDEX IF NOT EXISTS idx_reports_user_week ON report_schema.weekly_reports(user_id, week_start);

-- MONTHLY REPORTS (FR-RPT-05)
CREATE TABLE IF NOT EXISTS report_schema.monthly_reports (
    id                    UUID             NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id               UUID             NOT NULL,
    month                 VARCHAR(7)       NOT NULL,  -- YYYY-MM
    -- Aggregated monthly stats
    total_goals_completed INT              NOT NULL DEFAULT 0,
    avg_goal_progress     DOUBLE PRECISION NOT NULL DEFAULT 0,
    total_workouts        INT              NOT NULL DEFAULT 0,
    total_cardio_sessions INT              NOT NULL DEFAULT 0,
    avg_sleep_hours       DOUBLE PRECISION NOT NULL DEFAULT 0,
    total_income          DOUBLE PRECISION NOT NULL DEFAULT 0,
    total_expenses        DOUBLE PRECISION NOT NULL DEFAULT 0,
    net_savings           DOUBLE PRECISION NOT NULL DEFAULT 0,
    avg_habit_score       DOUBLE PRECISION NOT NULL DEFAULT 0,
    -- Trends (JSON for chart data)
    weekly_trend          JSONB,   -- weekly breakdown for charts
    insights              JSONB,
    created_at            TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, month)
);
CREATE INDEX IF NOT EXISTS idx_monthly_reports_user ON report_schema.monthly_reports(user_id, month);
