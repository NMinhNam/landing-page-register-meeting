-- Add columns to store the calculated display values for week/month/year
ALTER TABLE visit_registration 
ADD COLUMN visit_week_month_display VARCHAR(20) NULL COMMENT 'Calculated display format: week/month (e.g., 1/03)',
ADD COLUMN visit_year INT NULL COMMENT 'Year of the visit registration';