-- ==========================================================
-- 1. EXTENSIONS & SEQUENCES
-- ==========================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SEQUENCE IF NOT EXISTS person_code_seq   START 1;
CREATE SEQUENCE IF NOT EXISTS user_code_seq     START 1;
CREATE SEQUENCE IF NOT EXISTS borrower_code_seq START 1;
CREATE SEQUENCE IF NOT EXISTS employee_code_seq START 1;
CREATE SEQUENCE IF NOT EXISTS app_code_seq      START 1;
CREATE SEQUENCE IF NOT EXISTS loan_code_seq     START 1;

-- ==========================================================
-- 2. TABLES
-- ==========================================================

CREATE TABLE roles (
                       role_id     UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
                       role_name   VARCHAR(50) UNIQUE NOT NULL,
                       description VARCHAR(200),
                       created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO roles (role_name, description) VALUES
                                               ('ADMIN',        'System administrator'),
                                               ('BORROWER',     'Loan applicant / borrower'),
                                               ('LOAN_OFFICER', 'Reviews and approves loan applications');

CREATE TABLE person_identity (
                                 person_id   UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
                                 person_code VARCHAR(20) UNIQUE,
                                 pan_hash    VARCHAR(64) UNIQUE NOT NULL,
                                 pan_first3  VARCHAR(3)  NOT NULL,
                                 pan_last2   VARCHAR(2)  NOT NULL,
                                 created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                 updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT chk_pan_first3 CHECK (pan_first3 ~ '^[A-Z]{3}$'),
    CONSTRAINT chk_pan_last2  CHECK (pan_last2  ~ '^[0-9][A-Z]$')
);

CREATE TABLE users (
                       user_id     UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
                       user_code   VARCHAR(20) UNIQUE,
                       person_id   UUID        NOT NULL,
                       role_id     UUID        NOT NULL,
                       first_name  VARCHAR(50) NOT NULL,
                       last_name   VARCHAR(50),
                       email       VARCHAR(100) UNIQUE NOT NULL,
                       phone       VARCHAR(15),
                       password    VARCHAR(255) NOT NULL,
                       is_active   BOOLEAN DEFAULT TRUE,
                       is_deleted  BOOLEAN DEFAULT FALSE,
                       deleted_at  TIMESTAMP WITH TIME ZONE,
                       deleted_by  UUID,
                       created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT fk_user_person FOREIGN KEY (person_id) REFERENCES person_identity(person_id),
                       CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

CREATE TABLE borrower_profile (
                                  borrower_id         UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  borrower_code       VARCHAR(20) UNIQUE,
                                  user_id             UUID        UNIQUE NOT NULL,
                                  monthly_income      NUMERIC(12,2) CHECK (monthly_income >= 0),
                                  existing_loan_count INT DEFAULT 0 CHECK (existing_loan_count >= 0),
                                  created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                  updated_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_borrower_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE employee_profile (
                                  employee_id   UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  employee_code VARCHAR(20) UNIQUE,
                                  user_id       UUID        UNIQUE NOT NULL,
                                  joining_date  DATE        NOT NULL,
                                  salary        NUMERIC(12,2) CHECK (salary >= 0),
                                  is_active     BOOLEAN DEFAULT TRUE,
                                  created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                  updated_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_employee_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE loan_applications (
                                   application_id     UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   application_code   VARCHAR(20) UNIQUE,
                                   user_id            UUID        NOT NULL,
                                   requested_amount   NUMERIC(12,2) CHECK (requested_amount > 0),
                                   interest_rate      NUMERIC(5,2)  CHECK (interest_rate > 0),
                                   tenure_months      INT           CHECK (tenure_months > 0),
                                   existing_emi       NUMERIC(12,2) DEFAULT 0 CHECK (existing_emi >= 0),
                                   dti_ratio          NUMERIC(5,2)  CHECK (dti_ratio >= 0),
                                   suggested_strategy VARCHAR(50),
                                   officer_strategy   VARCHAR(50),
                                   status             VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED')),
                                   applied_at         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                   reviewed_by        UUID,
                                   reviewed_at        TIMESTAMP WITH TIME ZONE,
                                   is_deleted         BOOLEAN DEFAULT FALSE,
                                   deleted_at         TIMESTAMP WITH TIME ZONE,
                                   deleted_by         UUID,
                                   CONSTRAINT fk_app_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE loans (
                       loan_id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
                       loan_code        VARCHAR(20) UNIQUE,
                       application_id   UUID        UNIQUE NOT NULL,
                       user_id          UUID        NOT NULL,
                       principal_amount NUMERIC(12,2) CHECK (principal_amount > 0),
                       interest_rate    NUMERIC(5,2)  CHECK (interest_rate > 0),
                       tenure_months    INT           CHECK (tenure_months > 0),
                       strategy         VARCHAR(50),
                       emi_amount       NUMERIC(12,2) CHECK (emi_amount > 0),
                       start_date       DATE NOT NULL,
                       end_date         DATE NOT NULL,
                       loan_status      VARCHAR(20) DEFAULT 'ACTIVE' CHECK (loan_status IN ('ACTIVE','CLOSED','DEFAULTED')),
                       created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT fk_loan_application FOREIGN KEY (application_id) REFERENCES loan_applications(application_id),
                       CONSTRAINT fk_loan_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE emi_schedule (
                              emi_id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              loan_id             UUID NOT NULL,
                              installment_no      INT  NOT NULL CHECK (installment_no > 0),
                              due_date            DATE NOT NULL,
                              principal_component NUMERIC(12,2) CHECK (principal_component >= 0),
                              interest_component  NUMERIC(12,2) CHECK (interest_component >= 0),
                              total_emi           NUMERIC(12,2) CHECK (total_emi > 0),
                              remaining_balance   NUMERIC(12,2) CHECK (remaining_balance >= 0),
                              status              VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','PAID','OVERDUE')),
                              paid_date           DATE,
                              CONSTRAINT fk_emi_loan FOREIGN KEY (loan_id) REFERENCES loans(loan_id) ON DELETE CASCADE,
                              CONSTRAINT uq_emi_installment UNIQUE (loan_id, installment_no)
);

CREATE TABLE payments (
                          payment_id   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          emi_id       UUID NOT NULL,
                          loan_id      UUID NOT NULL,
                          amount_paid  NUMERIC(12,2) CHECK (amount_paid > 0),
                          payment_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          payment_mode VARCHAR(20) CHECK (payment_mode IN ('UPI','CARD','NETBANKING')),
                          status       VARCHAR(20) CHECK (status IN ('SUCCESS','FAILED')),
                          CONSTRAINT fk_payment_emi FOREIGN KEY (emi_id) REFERENCES emi_schedule(emi_id),
                          CONSTRAINT fk_payment_loan FOREIGN KEY (loan_id) REFERENCES loans(loan_id)
);

CREATE TABLE notifications (
                               notification_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               user_id         UUID,
                               loan_id         UUID,
                               emi_id          UUID,
                               email           VARCHAR(100),
                               subject         VARCHAR(255),
                               message         TEXT,
                               sent_at         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               status          VARCHAR(20) CHECK (status IN ('SENT','FAILED')),
                               is_deleted      BOOLEAN DEFAULT FALSE,
                               deleted_at      TIMESTAMP WITH TIME ZONE, -- ADDED THIS
                               deleted_by      UUID,                      -- ADDED THIS
                               CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE audit_logs (
                            audit_id    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            officer_id  UUID,
                            action      VARCHAR(50),
                            entity_type VARCHAR(50),
                            entity_id   UUID,
                            action_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_audit_officer FOREIGN KEY (officer_id) REFERENCES users(user_id)
);

CREATE TABLE strategy_audit (
                                id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                application_id   UUID NOT NULL,
                                system_strategy  VARCHAR(50),
                                officer_strategy VARCHAR(50),
                                overridden       BOOLEAN DEFAULT FALSE,
                                changed_by       UUID,
                                changed_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT fk_strategy_application FOREIGN KEY (application_id) REFERENCES loan_applications(application_id)
);

-- ==========================================================
-- 3. FUNCTIONS & TRIGGERS
-- ==========================================================

CREATE OR REPLACE FUNCTION generate_person_code() RETURNS TEXT AS $$
BEGIN RETURN 'PID' || LPAD(nextval('person_code_seq')::TEXT, 6, '0'); END; $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION generate_user_code() RETURNS TEXT AS $$
BEGIN RETURN 'USR' || LPAD(nextval('user_code_seq')::TEXT, 6, '0'); END; $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION generate_borrower_code() RETURNS TEXT AS $$
BEGIN RETURN 'BRW' || LPAD(nextval('borrower_code_seq')::TEXT, 6, '0'); END; $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION generate_employee_code() RETURNS TEXT AS $$
BEGIN RETURN 'EMP' || LPAD(nextval('employee_code_seq')::TEXT, 6, '0'); END; $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION generate_app_code() RETURNS TEXT AS $$
BEGIN RETURN 'APP' || LPAD(nextval('app_code_seq')::TEXT, 6, '0'); END; $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION generate_loan_code() RETURNS TEXT AS $$
BEGIN RETURN 'LAN' || LPAD(nextval('loan_code_seq')::TEXT, 6, '0'); END; $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_person_code() RETURNS TRIGGER AS $$
BEGIN IF NEW.person_code IS NULL THEN NEW.person_code := generate_person_code(); END IF; RETURN NEW; END; $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_user_code() RETURNS TRIGGER AS $$
BEGIN IF NEW.user_code IS NULL THEN NEW.user_code := generate_user_code(); END IF; RETURN NEW; END; $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_borrower_code() RETURNS TRIGGER AS $$
BEGIN IF NEW.borrower_code IS NULL THEN NEW.borrower_code := generate_borrower_code(); END IF; RETURN NEW; END; $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_employee_code() RETURNS TRIGGER AS $$
BEGIN IF NEW.employee_code IS NULL THEN NEW.employee_code := generate_employee_code(); END IF; RETURN NEW; END; $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_app_code() RETURNS TRIGGER AS $$
BEGIN IF NEW.application_code IS NULL THEN NEW.application_code := generate_app_code(); END IF; RETURN NEW; END; $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_loan_code() RETURNS TRIGGER AS $$
BEGIN IF NEW.loan_code IS NULL THEN NEW.loan_code := generate_loan_code(); END IF; RETURN NEW; END; $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION prevent_delete() RETURNS TRIGGER AS $$
BEGIN RAISE EXCEPTION 'Hard delete not allowed on table "%". Use soft delete.', TG_TABLE_NAME; END; $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION touch_updated_at() RETURNS TRIGGER AS $$
BEGIN NEW.updated_at := CURRENT_TIMESTAMP; RETURN NEW; END; $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_person_code   BEFORE INSERT ON person_identity FOR EACH ROW EXECUTE FUNCTION set_person_code();
CREATE TRIGGER trg_user_code     BEFORE INSERT ON users           FOR EACH ROW EXECUTE FUNCTION set_user_code();
CREATE TRIGGER trg_borrower_code BEFORE INSERT ON borrower_profile FOR EACH ROW EXECUTE FUNCTION set_borrower_code();
CREATE TRIGGER trg_employee_code BEFORE INSERT ON employee_profile FOR EACH ROW EXECUTE FUNCTION set_employee_code();
CREATE TRIGGER trg_app_code      BEFORE INSERT ON loan_applications FOR EACH ROW EXECUTE FUNCTION set_app_code();
CREATE TRIGGER trg_loan_code     BEFORE INSERT ON loans           FOR EACH ROW EXECUTE FUNCTION set_loan_code();

CREATE TRIGGER no_delete_users        BEFORE DELETE ON users             FOR EACH ROW EXECUTE FUNCTION prevent_delete();
CREATE TRIGGER no_delete_applications BEFORE DELETE ON loan_applications FOR EACH ROW EXECUTE FUNCTION prevent_delete();
CREATE TRIGGER no_delete_notifications BEFORE DELETE ON notifications     FOR EACH ROW EXECUTE FUNCTION prevent_delete();

CREATE TRIGGER trg_person_updated_at   BEFORE UPDATE ON person_identity FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_user_updated_at     BEFORE UPDATE ON users           FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_borrower_updated_at BEFORE UPDATE ON borrower_profile FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_employee_updated_at BEFORE UPDATE ON employee_profile FOR EACH ROW EXECUTE FUNCTION touch_updated_at();