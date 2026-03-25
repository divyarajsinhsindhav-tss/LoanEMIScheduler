CREATE TABLE notifications (
                               notification_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                               user_id UUID NOT NULL,
                               loan_id UUID,
                               emi_id UUID,

                               email VARCHAR(100) NOT NULL,
                               subject VARCHAR(255),
                               message TEXT,

                               sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               status VARCHAR(20), -- SENT, FAILED

                               CONSTRAINT fk_notification_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(user_id)
                                       ON DELETE CASCADE
);

CREATE TABLE audit_logs (
                            audit_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                            officer_id UUID NOT NULL,

                            action VARCHAR(50) NOT NULL,       -- APPROVED, REJECTED, STRATEGY_OVERRIDE
                            entity_type VARCHAR(50) NOT NULL,  -- LOAN, APPLICATION
                            entity_id UUID NOT NULL,

                            action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_audit_officer
                                FOREIGN KEY (officer_id)
                                    REFERENCES users(user_id)
);

CREATE TABLE strategy_audit (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                                application_id UUID NOT NULL,

                                system_strategy VARCHAR(50),
                                officer_strategy VARCHAR(50),

                                overridden BOOLEAN DEFAULT FALSE,

                                changed_by UUID,
                                changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_strategy_application
                                    FOREIGN KEY (application_id)
                                        REFERENCES loan_applications(application_id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_strategy_changed_by
                                    FOREIGN KEY (changed_by)
                                        REFERENCES users(user_id)
);