-- Update employee role column to use enum-like values (stored as varchar)
-- This migration ensures the role column in employees table supports EmployeeRole enum

-- Note: With ddl-auto: update in Hibernate, this migration documents the schema change
-- but the actual column type change may have already been applied by Hibernate on startup

-- Verify the column exists and is VARCHAR
-- The column will store values: 'OWNER', 'ADMIN', 'MEMBER'

-- Add comment for documentation
COMMENT ON COLUMN employees.role IS 'Employee role: OWNER (full permissions), ADMIN (admin permissions), MEMBER (basic permissions)';
