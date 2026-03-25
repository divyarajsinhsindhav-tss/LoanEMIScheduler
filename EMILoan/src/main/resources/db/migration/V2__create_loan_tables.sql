-- =========================================
-- V2: Loan Applications & Loans Tables
-- =========================================

-- =========================================
-- ENUM TYPES
-- =========================================

-- Application Status Enum
CREATE TYPE application_status_enum AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED'
);

-- Loan Status Enum
CREATE TYPE loan_status_enum AS ENUM (
    'ACTIVE',
    'CLOSED',
    'DEFAULTED'
);

CREATE TYPE loan_strategy_enum AS ENUM (
    'FLAT_RATE',
    'REDUCING_BALANCE',
    'STEP_UP'
);

CREATE TABLE loan_applications (
                                   application_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                                   user_id UUID NOT NULL,

                                   requested_amount NUMERIC(12,2) NOT NULL,
                                   interest_rate NUMERIC(5,2),
                                   tenure_months INT NOT NULL,

                                   existing_emi NUMERIC(12,2),
                                   dti_ratio NUMERIC(5,2),

                                   suggested_strategy loan_strategy_enum,
                                   officer_strategy loan_strategy_enum,

                                   status application_status_enum DEFAULT 'PENDING',

                                   applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                   reviewed_by UUID,
                                   reviewed_at TIMESTAMP,

                                   CONSTRAINT fk_application_user
                                       FOREIGN KEY (user_id)
                                           REFERENCES users(user_id)
                                           ON DELETE CASCADE,

                                   CONSTRAINT fk_application_officer
                                       FOREIGN KEY (reviewed_by)
                                           REFERENCES users(user_id)
);

CREATE TABLE loans (
                       loan_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                       application_id UUID NOT NULL,
                       user_id UUID NOT NULL,

                       principal_amount NUMERIC(12,2) NOT NULL,
                       interest_rate NUMERIC(5,2) NOT NULL,
                       tenure_months INT NOT NULL,

                       strategy loan_strategy_enum,

                       emi_amount NUMERIC(12,2),

                       start_date DATE,
                       end_date DATE,

                       loan_status loan_status_enum DEFAULT 'ACTIVE',

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_loan_application
                           FOREIGN KEY (application_id)
                               REFERENCES loan_applications(application_id)
                               ON DELETE CASCADE,

                       CONSTRAINT fk_loan_user
                           FOREIGN KEY (user_id)
                               REFERENCES users(user_id)
                               ON DELETE CASCADE
);

CREATE INDEX idx_loan_user ON loans(user_id);

CREATE INDEX idx_application_status ON loan_applications(status);

CREATE INDEX idx_loan_status ON loans(loan_status);