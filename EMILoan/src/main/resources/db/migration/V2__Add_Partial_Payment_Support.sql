
ALTER TABLE emi_schedule
    ADD COLUMN amount_paid NUMERIC(12,2) DEFAULT 0.00 CHECK (amount_paid >= 0);

ALTER TABLE emi_schedule DROP CONSTRAINT IF EXISTS emi_schedule_status_check;

ALTER TABLE emi_schedule ADD CONSTRAINT emi_schedule_status_check
    CHECK (status IN ('PENDING', 'PARTIALLY_PAID', 'PAID', 'OVERDUE'));


ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_status_check;

ALTER TABLE notifications ADD CONSTRAINT notifications_status_check
    CHECK (status IN ('PENDING', 'SENT', 'FAILED'));


ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_payment_mode_check;

ALTER TABLE payments ADD CONSTRAINT payments_payment_mode_check
    CHECK (payment_mode IN ('UPI', 'CARD', 'NET_BANKING'));


ALTER TABLE payments
    ADD COLUMN payment_metadata JSONB;