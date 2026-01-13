-- Add column to store the calculated month separately
ALTER TABLE visit_registration 
ADD COLUMN visit_month INT NULL COMMENT 'Month of the visit registration (based on display logic)';