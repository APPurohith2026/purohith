-- Apply before deploying to PostgreSQL environments using ddl-auto=validate.
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_challenge varchar(36);
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_otp_hash varchar(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_token_hash varchar(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_expires_at timestamp with time zone;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_last_sent_at timestamp with time zone;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_window_start timestamp with time zone;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_send_count integer NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_attempts integer NOT NULL DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_users_password_reset_challenge ON users(password_reset_challenge);
