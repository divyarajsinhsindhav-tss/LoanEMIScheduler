-- 1. DROP THE TRIGGER THAT BLOCKS HARD DELETES
DROP TRIGGER IF EXISTS no_delete_users ON users;

-- 2. FIX AUDIT LOGS (Allow deletion of logs tied to ghost users)
ALTER TABLE audit_logs DROP CONSTRAINT IF EXISTS fk_audit_officer;
ALTER TABLE audit_logs DROP CONSTRAINT IF EXISTS fk_audit_actor; -- Just in case

ALTER TABLE audit_logs
    ADD CONSTRAINT fk_audit_actor
    FOREIGN KEY (actor_id) REFERENCES users(user_id) ON DELETE CASCADE;

-- 3. FIX NOTIFICATIONS (Allow deletion of OTP email logs tied to ghost users)
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS fk_notif_user;

ALTER TABLE notifications
    ADD CONSTRAINT fk_notif_user
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;