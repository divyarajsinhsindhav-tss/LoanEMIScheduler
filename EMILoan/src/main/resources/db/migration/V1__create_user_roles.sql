
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE TABLE roles (
                       role_id SERIAL PRIMARY KEY,
                       role_name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE users (
                       user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50),
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       phone VARCHAR(15),
                       monthly_income NUMERIC(12,2),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       enabled BOOLEAN DEFAULT TRUE
);

CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id INT NOT NULL,

                            PRIMARY KEY (user_id, role_id),

                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(user_id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id)
                                    REFERENCES roles(role_id)
                                    ON DELETE CASCADE
);

INSERT INTO roles (role_name) VALUES
                                  ('ADMIN'),
                                  ('BORROWER'),
                                  ('LOAN_OFFICER');