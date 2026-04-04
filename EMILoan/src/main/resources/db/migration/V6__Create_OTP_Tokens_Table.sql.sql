CREATE TABLE otp_tokens (
                            id UUID PRIMARY KEY,
                            email VARCHAR(255) NOT NULL,
                            otp_code VARCHAR(6) NOT NULL,
                            purpose VARCHAR(50) NOT NULL,
                            expiry_time TIMESTAMP NOT NULL
);

CREATE INDEX idx_otp_email_purpose ON otp_tokens(email, purpose);

CREATE INDEX idx_otp_expiry_time ON otp_tokens(expiry_time);
COMMENT ON TABLE otp_tokens IS 'Stores temporary 6-digit codes for registration and login verification';