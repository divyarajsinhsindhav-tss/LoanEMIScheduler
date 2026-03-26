
CREATE TYPE notification_status_enum AS ENUM ('SENT', 'FAILED');
CREATE TYPE audit_action_enum AS ENUM ('APPROVED', 'REJECTED', 'STRATEGY_OVERRIDE');
CREATE TYPE audit_entity_enum AS ENUM ('LOAN', 'APPLICATION');


CREATE TABLE notifications (
                               notification_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               user_id UUID NOT NULL,
                               loan_id UUID,
                               emi_id UUID,

                               email VARCHAR(100) NOT NULL,
                               subject VARCHAR(255),
                               message TEXT,

                               sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               status notification_status_enum,


                               is_deleted BOOLEAN DEFAULT FALSE,
                               deleted_at TIMESTAMP,
                               deleted_by UUID,

                               CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                               CONSTRAINT fk_notif_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(user_id)
);
CREATE TABLE audit_logs (
                            audit_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            officer_id UUID NOT NULL,

                            action audit_action_enum NOT NULL,
                            entity_type audit_entity_enum NOT NULL,
                            entity_id UUID NOT NULL,

                            action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_audit_officer FOREIGN KEY (officer_id) REFERENCES users(user_id)
);

CREATE TABLE strategy_audit (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                application_id UUID NOT NULL,

                                system_strategy loan_strategy_enum,
                                officer_strategy loan_strategy_enum,

                                overridden BOOLEAN DEFAULT FALSE,

                                changed_by UUID,
                                changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_strategy_application FOREIGN KEY (application_id) REFERENCES loan_applications(application_id) ON DELETE CASCADE,
                                CONSTRAINT fk_strategy_changed_by FOREIGN KEY (changed_by) REFERENCES users(user_id)
);


CREATE TRIGGER no_delete_notifications
    BEFORE DELETE ON notifications
    FOR EACH ROW EXECUTE FUNCTION prevent_delete();

CREATE INDEX idx_notif_user ON notifications(user_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_id);
CREATE INDEX idx_strategy_app ON strategy_audit(application_id);