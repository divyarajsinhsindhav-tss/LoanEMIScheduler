-- 1. Add Penalty Configuration to the Loan table
-- This allows different loans to have different penalty rules
ALTER TABLE loans
ADD COLUMN penalty_percentage_daily NUMERIC(5,3) DEFAULT 0.05, -- 0.05% per day
ADD COLUMN penalty_grace_period_days INT DEFAULT 3;           -- No charge if paid within 3 days

-- 2. Add Penalty Tracking to the EMI Schedule
-- This tracks the 'Debt' separately from the 'Principal'
ALTER TABLE emi_schedule
ADD COLUMN penalty_accrued NUMERIC(12,2) DEFAULT 0.00,
ADD COLUMN last_penalty_apply_date DATE;

-- 3. Industry Standard: Add a 'Penalty Paid' to the Payments table
-- This helps in accounting: knowing exactly how much of a payment went to fines
ALTER TABLE payments
ADD COLUMN penalty_paid_component NUMERIC(12,2) DEFAULT 0.00;