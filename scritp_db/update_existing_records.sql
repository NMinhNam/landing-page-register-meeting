-- Update existing records with calculated values based on current data
-- This requires a stored procedure or batch update since we need to calculate based on the complex logic

-- First, let's update records with the year
UPDATE visit_registration 
SET visit_year = YEAR(created_at) 
WHERE visit_year IS NULL;

-- For visit_week_month_display, we'll need to update records in batches using application logic
-- since the calculation is complex and depends on the calculateWeekMonthDisplay function
-- This will be handled by a data migration in the application