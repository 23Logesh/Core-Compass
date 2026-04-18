-- =============================================================
-- CoreCompass — Finance Schema Migration V5
-- Creates: recurring_expenses table (templates, not actual spend)
-- =============================================================

CREATE TABLE IF NOT EXISTS finance_schema.recurring_expenses (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    user_id             UUID            NOT NULL,
    amount              NUMERIC(12, 2)  NOT NULL,
    category_id         UUID            NOT NULL,
    sub_category_id     UUID,
    payment_method_id   UUID,
    merchant            VARCHAR(100),
    note                VARCHAR(200),
    -- DAILY | WEEKLY | MONTHLY | YEARLY
    frequency           VARCHAR(20)     NOT NULL DEFAULT 'MONTHLY',
    -- Day of month (1-31) for MONTHLY, day of week (1-7) for WEEKLY
    day_of_period       INTEGER,
    -- When this template is active from/until (null = forever)
    starts_on           DATE,
    ends_on             DATE,
    is_active           BOOLEAN         NOT NULL DEFAULT true,
    is_deleted          BOOLEAN         NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_recurring_expenses PRIMARY KEY (id),
    CONSTRAINT chk_recurring_frequency
        CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'))
);

CREATE INDEX IF NOT EXISTS idx_rec_exp_user
    ON finance_schema.recurring_expenses (user_id, is_active)
    WHERE is_deleted = false;