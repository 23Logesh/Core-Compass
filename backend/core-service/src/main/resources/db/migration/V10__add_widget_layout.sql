-- =============================================================
-- CoreCompass — Core Schema Migration V9
-- Adds widget_layout column to user_preferences
-- Stores user-configurable dashboard widget order + visibility
-- =============================================================

ALTER TABLE core_schema.user_preferences
    ADD COLUMN IF NOT EXISTS widget_layout JSONB;

-- Default layout (all widgets enabled, standard order)
COMMENT ON COLUMN core_schema.user_preferences.widget_layout IS
    'JSON array of {widgetId, position, visible}. NULL = use default layout.';