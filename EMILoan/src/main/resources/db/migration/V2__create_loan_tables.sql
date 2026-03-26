CREATE SEQUENCE app_code_seq START 1;
CREATE SEQUENCE loan_code_seq START 1;

CREATE TYPE application_status_enum AS ENUM ('PENDING', 'APPROVED', 'REJECTED');
CREATE TYPE loan_status_enum AS ENUM ('ACTIVE', 'CLOSED', 'DEFAULTED');
CREATE TYPE loan_strategy_enum AS ENUM ('FLAT_RATE', 'REDUCING_BALANCE', 'STEP_UP');


CREATE OR REPLACE FUNCTION generate_app_code()
RETURNS TEXT AS $$
BEGIN
RETURN 'APP' || LPAD(nextval('app_code_seq')::TEXT, 6, '0');
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION generate_loan_code()
RETURNS TEXT AS $$
BEGIN
RETURN 'LOAN' || LPAD(nextval('loan_code_seq')::TEXT, 6, '0');
END;
$$ LANGUAGE plpgsql;

CREATE TABLE loan_applications (
                                   application_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   application_code VARCHAR(20) UNIQUE,
                                   user_id UUID NOT NULL,

                                   requested_amount NUMERIC(12,2) NOT NULL CHECK (requested_amount > 0),
                                   interest_rate NUMERIC(5,2) CHECK (interest_rate > 0),
                                   tenure_months INT NOT NULL CHECK (tenure_months > 0),

                                   existing_emi NUMERIC(12,2) DEFAULT 0 CHECK (existing_emi >= 0),
                                   dti_ratio NUMERIC(5,2) CHECK (dti_ratio >= 0),

                                   suggested_strategy loan_strategy_enum,
                                   officer_strategy loan_strategy_enum,

                                   status application_status_enum DEFAULT 'PENDING',
                                   applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                   reviewed_by UUID,
                                   reviewed_at TIMESTAMP,

                                   is_deleted BOOLEAN DEFAULT FALSE,
                                   deleted_at TIMESTAMP,
                                   deleted_by UUID,

                                   CONSTRAINT fk_application_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                   CONSTRAINT fk_application_officer FOREIGN KEY (reviewed_by) REFERENCES users(user_id),
                                   CONSTRAINT fk_app_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(user_id),
                                   CONSTRAINT chk_officer_not_self CHECK (user_id IS DISTINCT FROM reviewed_by)
    );


CREATE TABLE loans (
                       loan_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       loan_code VARCHAR(20) UNIQUE, -- From Master Design
                       application_id UUID NOT NULL UNIQUE,
                       user_id UUID NOT NULL,

                       principal_amount NUMERIC(12,2) NOT NULL CHECK (principal_amount > 0),
                       interest_rate NUMERIC(5,2) NOT NULL CHECK (interest_rate > 0),
                       tenure_months INT NOT NULL CHECK (tenure_months > 0),

                       strategy loan_strategy_enum,
                       emi_amount NUMERIC(12,2) CHECK (emi_amount > 0),

                       start_date DATE NOT NULL,
                       end_date DATE NOT NULL,

                       loan_status loan_status_enum DEFAULT 'ACTIVE',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_loan_application FOREIGN KEY (application_id) REFERENCES loan_applications(application_id) ON DELETE CASCADE,
                       CONSTRAINT fk_loan_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION set_app_code()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.application_code IS NULL THEN
        NEW.application_code := generate_app_code();
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_app_code
    BEFORE INSERT ON loan_applications
    FOR EACH ROW EXECUTE FUNCTION set_app_code();

CREATE OR REPLACE FUNCTION set_loan_code()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.loan_code IS NULL THEN
        NEW.loan_code := generate_loan_code();
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_loan_code
    BEFORE INSERT ON loans
    FOR EACH ROW EXECUTE FUNCTION set_loan_code();

CREATE TRIGGER no_delete_applications
    BEFORE DELETE ON loan_applications
    FOR EACH ROW EXECUTE FUNCTION prevent_delete();

CREATE INDEX idx_loan_user ON loans(user_id);
CREATE INDEX idx_application_status ON loan_applications(status);
CREATE INDEX idx_loan_status ON loans(loan_status);