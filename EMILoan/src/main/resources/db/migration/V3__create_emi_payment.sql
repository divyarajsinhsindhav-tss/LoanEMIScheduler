
CREATE TABLE emi_schedule (
                              emi_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                              loan_id UUID NOT NULL,

                              installment_no INT NOT NULL,
                              due_date DATE NOT NULL,

                              principal_component NUMERIC(12,2) NOT NULL,
                              interest_component NUMERIC(12,2) NOT NULL,
                              total_emi NUMERIC(12,2) NOT NULL,

                              remaining_balance NUMERIC(12,2),

                              status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, PAID, OVERDUE
                              paid_date DATE,

                              CONSTRAINT fk_emi_loan
                                  FOREIGN KEY (loan_id)
                                      REFERENCES loans(loan_id)
                                      ON DELETE CASCADE
);
CREATE TABLE payments (
                          payment_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                          emi_id UUID NOT NULL,
                          loan_id UUID NOT NULL,

                          amount_paid NUMERIC(12,2) NOT NULL,

                          payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                          payment_mode VARCHAR(20), -- UPI, CARD, NETBANKING
                          status VARCHAR(20),       -- SUCCESS, FAILED

                          CONSTRAINT fk_payment_emi
                              FOREIGN KEY (emi_id)
                                  REFERENCES emi_schedule(emi_id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_payment_loan
                              FOREIGN KEY (loan_id)
                                  REFERENCES loans(loan_id)
                                  ON DELETE CASCADE
);

CREATE INDEX idx_emi_loan ON emi_schedule(loan_id);

CREATE INDEX idx_emi_due_date ON emi_schedule(due_date);