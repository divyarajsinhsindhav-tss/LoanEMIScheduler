-- Use singular or plural based on what is actually in your DB
ALTER TABLE borrower_profile DROP CONSTRAINT IF EXISTS fk_borrower_user;

ALTER TABLE borrower_profile
    ADD CONSTRAINT fk_borrower_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

-- Do the same for employee_profile if it exists
ALTER TABLE employee_profile DROP CONSTRAINT IF EXISTS fk_employee_user;

ALTER TABLE employee_profile
    ADD CONSTRAINT fk_employee_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;