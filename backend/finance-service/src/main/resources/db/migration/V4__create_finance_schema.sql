-- ==============================================================
-- CoreCompass — Finance Schema V4 (FINAL)
-- expenses, incomes, budgets, savings_goals, debts, investments
-- payment_methods, expense_categories (Type Registry)
-- ==============================================================
CREATE SCHEMA IF NOT EXISTS finance_schema;

CREATE OR REPLACE FUNCTION finance_schema.update_updated_at()
RETURNS TRIGGER AS $$ BEGIN NEW.updated_at = NOW(); RETURN NEW; END; $$ LANGUAGE plpgsql;

-- EXPENSE CATEGORIES (Type Registry - system + user-extensible)
CREATE TABLE IF NOT EXISTS finance_schema.expense_categories (
    id          UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name        VARCHAR(60) NOT NULL,
    icon        VARCHAR(10),
    color       VARCHAR(7),
    parent_id   UUID        REFERENCES finance_schema.expense_categories(id),
    is_system   BOOLEAN     NOT NULL DEFAULT false,
    is_public   BOOLEAN     NOT NULL DEFAULT false,
    created_by  UUID,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
INSERT INTO finance_schema.expense_categories (name,icon,color,is_system) VALUES
    ('Food & Dining','🍽️','#FF6B35',true),
    ('Transport','🚗','#3498DB',true),
    ('Housing','🏠','#2ECC71',true),
    ('Healthcare','💊','#E74C3C',true),
    ('Entertainment','🎬','#9B59B6',true),
    ('Shopping','🛍️','#E67E22',true),
    ('Education','📚','#F39C12',true),
    ('Bills & Utilities','💡','#1ABC9C',true),
    ('Personal Care','💅','#E91E63',true),
    ('Travel','✈️','#00BCD4',true),
    ('Subscriptions','📱','#607D8B',true),
    ('EMI / Loan Repayment','🏦','#795548',true),
    ('Investments','📈','#4CAF50',true),
    ('Gifts & Donations','🎁','#FF9800',true),
    ('Miscellaneous','📦','#9E9E9E',true)
ON CONFLICT DO NOTHING;

-- PAYMENT METHODS (Type Registry)
CREATE TABLE IF NOT EXISTS finance_schema.payment_methods (
    id          UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name        VARCHAR(60) NOT NULL,
    icon        VARCHAR(10),
    is_system   BOOLEAN     NOT NULL DEFAULT false,
    created_by  UUID,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
INSERT INTO finance_schema.payment_methods (name,icon,is_system) VALUES
    ('Cash','💵',true),('UPI — GPay','📱',true),('UPI — PhonePe','📱',true),
    ('Credit Card','💳',true),('Debit Card','💳',true),('Net Banking','🏦',true),
    ('Wallet — Paytm','👛',true),('Cheque','📄',true),('NEFT / IMPS','🔄',true)
ON CONFLICT DO NOTHING;

-- EXPENSES
CREATE TABLE IF NOT EXISTS finance_schema.expenses (
    id                UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id           UUID         NOT NULL,
    amount            NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    category_id       UUID         NOT NULL REFERENCES finance_schema.expense_categories(id),
    sub_category_id   UUID         REFERENCES finance_schema.expense_categories(id),
    payment_method_id UUID         REFERENCES finance_schema.payment_methods(id),
    expense_date      DATE         NOT NULL,
    merchant          VARCHAR(100),
    note              VARCHAR(200),
    tags              JSONB,
    is_recurring      BOOLEAN      NOT NULL DEFAULT false,
    is_deleted        BOOLEAN      NOT NULL DEFAULT false,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_expenses_user_date     ON finance_schema.expenses(user_id, expense_date) WHERE is_deleted=false;
CREATE INDEX IF NOT EXISTS idx_expenses_user_category ON finance_schema.expenses(user_id, category_id)  WHERE is_deleted=false;
CREATE TRIGGER trg_expenses_updated_at BEFORE UPDATE ON finance_schema.expenses
    FOR EACH ROW EXECUTE FUNCTION finance_schema.update_updated_at();

-- INCOMES
CREATE TABLE IF NOT EXISTS finance_schema.incomes (
    id          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id     UUID         NOT NULL,
    amount      NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    source_type VARCHAR(60)  NOT NULL,
    income_date DATE         NOT NULL,
    note        VARCHAR(200),
    is_recurring BOOLEAN     NOT NULL DEFAULT false,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_income_user_date ON finance_schema.incomes(user_id, income_date) WHERE is_deleted=false;

-- BUDGETS (per category per month)
CREATE TABLE IF NOT EXISTS finance_schema.budgets (
    id           UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id      UUID          NOT NULL,
    category_id  UUID          NOT NULL REFERENCES finance_schema.expense_categories(id),
    budget_month VARCHAR(7)    NOT NULL,  -- YYYY-MM
    amount_limit NUMERIC(12,2) NOT NULL CHECK (amount_limit > 0),
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, category_id, budget_month)
);
CREATE TRIGGER trg_budgets_updated_at BEFORE UPDATE ON finance_schema.budgets
    FOR EACH ROW EXECUTE FUNCTION finance_schema.update_updated_at();

-- SAVINGS GOALS
CREATE TABLE IF NOT EXISTS finance_schema.savings_goals (
    id             UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id        UUID          NOT NULL,
    title          VARCHAR(120)  NOT NULL,
    target_amount  NUMERIC(12,2) NOT NULL CHECK (target_amount > 0),
    current_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    target_date    DATE,
    is_deleted     BOOLEAN       NOT NULL DEFAULT false,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
CREATE TRIGGER trg_savings_updated_at BEFORE UPDATE ON finance_schema.savings_goals
    FOR EACH ROW EXECUTE FUNCTION finance_schema.update_updated_at();

-- DEBTS
CREATE TABLE IF NOT EXISTS finance_schema.debts (
    id                 UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id            UUID          NOT NULL,
    name               VARCHAR(120)  NOT NULL,
    debt_type          VARCHAR(50),
    principal_amount   NUMERIC(12,2) NOT NULL CHECK (principal_amount > 0),
    current_balance    NUMERIC(12,2) NOT NULL CHECK (current_balance >= 0),
    interest_rate      NUMERIC(5,2)  CHECK (interest_rate >= 0),
    min_payment        NUMERIC(10,2),
    is_deleted         BOOLEAN       NOT NULL DEFAULT false,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
CREATE TRIGGER trg_debts_updated_at BEFORE UPDATE ON finance_schema.debts
    FOR EACH ROW EXECUTE FUNCTION finance_schema.update_updated_at();

-- INVESTMENT TYPES (Type Registry)
CREATE TABLE IF NOT EXISTS finance_schema.investment_types (
    id          UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name        VARCHAR(80) NOT NULL,
    icon        VARCHAR(10),
    color       VARCHAR(7),
    is_system   BOOLEAN     NOT NULL DEFAULT false,
    created_by  UUID,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
INSERT INTO finance_schema.investment_types (name,icon,is_system) VALUES
    ('Mutual Fund — SIP','📊',true),('Mutual Fund — Lump Sum','📊',true),
    ('Fixed Deposit','🏦',true),('PPF','🏛️',true),('NPS','🏛️',true),
    ('Stocks','📈',true),('Gold / Silver','🥇',true),
    ('Real Estate','🏘️',true),('ELSS','📋',true),('Crypto','₿',true),
    ('Recurring Deposit','🔄',true),('Bonds','📜',true)
ON CONFLICT DO NOTHING;

-- INVESTMENTS
CREATE TABLE IF NOT EXISTS finance_schema.investments (
    id                UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id           UUID          NOT NULL,
    investment_type_id UUID         NOT NULL REFERENCES finance_schema.investment_types(id),
    name              VARCHAR(120)  NOT NULL,
    invested_amount   NUMERIC(14,2) NOT NULL CHECK (invested_amount > 0),
    current_value     NUMERIC(14,2),
    purchase_date     DATE          NOT NULL,
    maturity_date     DATE,
    notes             TEXT,
    is_deleted        BOOLEAN       NOT NULL DEFAULT false,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_investments_user ON finance_schema.investments(user_id) WHERE is_deleted=false;
CREATE TRIGGER trg_investments_updated_at BEFORE UPDATE ON finance_schema.investments
    FOR EACH ROW EXECUTE FUNCTION finance_schema.update_updated_at();
