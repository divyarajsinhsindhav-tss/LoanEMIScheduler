CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SEQUENCE user_code_seq START 1;

CREATE OR REPLACE FUNCTION generate_user_code()
RETURNS TEXT AS $$
BEGIN
RETURN 'USER' || LPAD(nextval('user_code_seq')::TEXT, 6, '0');
END;
$$ LANGUAGE plpgsql;


CREATE TABLE roles (
                       role_id SERIAL PRIMARY KEY,
                       role_name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE users (
                       user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       user_code VARCHAR(20) UNIQUE,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50),
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       phone VARCHAR(15) UNIQUE,
                       monthly_income NUMERIC(12,2) CHECK (monthly_income >= 0),

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       enabled BOOLEAN DEFAULT TRUE,

                       is_deleted BOOLEAN DEFAULT FALSE,
                       deleted_at TIMESTAMP,
                       deleted_by UUID,

                       CONSTRAINT fk_users_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(user_id)
);

CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id INT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                            CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);
CREATE OR REPLACE FUNCTION set_user_code()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.user_code IS NULL THEN
        NEW.user_code := generate_user_code();
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_user_code
    BEFORE INSERT ON users
    FOR EACH ROW EXECUTE FUNCTION set_user_code();

CREATE OR REPLACE FUNCTION prevent_delete()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Hard delete not allowed on this entity. Use soft delete (is_deleted = true).';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER no_delete_users
    BEFORE DELETE ON users
    FOR EACH ROW EXECUTE FUNCTION prevent_delete();

INSERT INTO roles (role_name) VALUES
                                  ('ADMIN'),
                                  ('BORROWER'),
                                  ('LOAN_OFFICER');