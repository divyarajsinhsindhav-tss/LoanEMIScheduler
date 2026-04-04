ALTER TABLE audit_logs ALTER COLUMN old_value TYPE jsonb USING old_value::jsonb;
ALTER TABLE audit_logs ALTER COLUMN new_value TYPE jsonb USING new_value::jsonb;