-- ==============================================================
-- V8 - Performance indexes per LLD Section 12.5
-- ==============================================================
-- Goals
CREATE INDEX IF NOT EXISTS idx_goals_user_status  ON core_schema.goals(user_id, status) WHERE is_deleted=false;
-- Todos
CREATE INDEX IF NOT EXISTS idx_todos_user_due     ON core_schema.todos(user_id, due_date) WHERE is_deleted=false;
CREATE INDEX IF NOT EXISTS idx_todos_goal_id      ON core_schema.todos(goal_id) WHERE is_deleted=false;
-- Expenses
CREATE INDEX IF NOT EXISTS idx_expenses_user_date ON finance_schema.expenses(user_id, expense_date) WHERE is_deleted=false;
-- Habit check-ins
CREATE INDEX IF NOT EXISTS idx_checkins_habit_date ON habits_schema.habit_checkins(habit_id, checkin_date);
-- Weekly reports
CREATE INDEX IF NOT EXISTS idx_reports_user_week  ON report_schema.weekly_reports(user_id, week_start);
