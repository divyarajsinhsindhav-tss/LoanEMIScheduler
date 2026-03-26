
CREATE TYPE emi_status_enum AS ENUM ('PENDING', 'PAID', 'OVERDUE');
CREATE TYPE payment_mode_enum AS ENUM ('UPI', 'CARD', 'NETBANKING');
CREATE TYPE payment_status_enum AS ENUM ('SUCCESS', 'FAILED');

CREATE TABLE emi_schedule (
                              emi_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              loan_id UUID NOT NULL,

                              installment_no INT NOT NULL CHECK (installment_no > 0),
                              due_date DATE NOT NULL,

                              principal_component NUMERIC(12,2) NOT NULL CHECK (principal_component >= 0),
                              interest_component NUMERIC(12,2) NOT NULL CHECK (interest_component >= 0),
                              total_emi NUMERIC(12,2) NOT NULL CHECK (total_emi > 0),

                              remaining_balance NUMERIC(12,2) CHECK (remaining_balance >= 0),

                              status emi_status_enum DEFAULT 'PENDING',
                              paid_date DATE,

                              CONSTRAINT fk_emi_loan FOREIGN KEY (loan_id) REFERENCES loans(loan_id) ON DELETE CASCADE
);

CREATE TABLE payments (
                          payment_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          emi_id UUID NOT NULL,
                          loan_id UUID NOT NULL,

                          amount_paid NUMERIC(12,2) NOT NULL CHECK (amount_paid > 0),
                          payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                          payment_mode payment_mode_enum,
                          status payment_status_enum,

                          CONSTRAINT fk_payment_emi FOREIGN KEY (emi_id) REFERENCES emi_schedule(emi_id) ON DELETE CASCADE,
                          CONSTRAINT fk_payment_loan FOREIGN KEY (loan_id) REFERENCES loans(loan_id) ON DELETE CASCADE
);

CREATE INDEX idx_emi_loan ON emi_schedule(loan_id);
CREATE INDEX idx_emi_due_date ON emi_schedule(due_date);
CREATE INDEX idx_payment_loan ON payments(loan_id);
CREATE INDEX idx_payment_emi ON payments(emi_id);