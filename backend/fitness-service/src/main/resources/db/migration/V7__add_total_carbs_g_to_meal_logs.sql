ALTER TABLE fitness_schema.meal_logs
ADD COLUMN total_carbs_g numeric;  -- or integer/float depending on your entity type

-- V8__add_total_fat_g_to_meal_logs.sql
ALTER TABLE fitness_schema.meal_logs
ADD COLUMN total_fat_g numeric; -- or integer depending on your entity


