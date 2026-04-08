-- 1. Create the Sequence
CREATE SEQUENCE IF NOT EXISTS emi_code_seq START 1;

-- 2. Add the Column to emi_schedule
ALTER TABLE emi_schedule
    ADD COLUMN emi_code VARCHAR(20) UNIQUE;

-- 3. Create the Function to format the code
CREATE OR REPLACE FUNCTION generate_emi_code() RETURNS TEXT AS $$
BEGIN
    RETURN 'EMI' || LPAD(nextval('emi_code_seq')::TEXT, 7, '0');
END;
$$ LANGUAGE plpgsql;

-- 4. Create the Trigger Function
CREATE OR REPLACE FUNCTION set_emi_code() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.emi_code IS NULL THEN
        NEW.emi_code := generate_emi_code();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 5. Attach the Trigger to the table
CREATE TRIGGER trg_emi_code
    BEFORE INSERT ON emi_schedule
    FOR EACH ROW
    EXECUTE FUNCTION set_emi_code();