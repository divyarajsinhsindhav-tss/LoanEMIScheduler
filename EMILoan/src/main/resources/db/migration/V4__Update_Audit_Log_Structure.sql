ALTER TABLE audit_logs RENAME COLUMN officer_id TO actor_id;

ALTER TABLE audit_logs ADD COLUMN description VARCHAR(500);


ALTER TABLE audit_logs ADD COLUMN old_value TEXT;
ALTER TABLE audit_logs ADD COLUMN new_value TEXT;