-- Additive PostgreSQL migration for deployments using ddl-auto=validate.
-- Render uses ddl-auto=update; no table reset is needed.
ALTER TABLE registration_drafts ADD COLUMN IF NOT EXISTS password_hash varchar(100);
ALTER TABLE registration_drafts ADD COLUMN IF NOT EXISTS draft_login_phone varchar(20);
ALTER TABLE registration_drafts ADD COLUMN IF NOT EXISTS login_failures integer;
ALTER TABLE registration_drafts ADD COLUMN IF NOT EXISTS login_locked_until timestamp(6) with time zone;
CREATE UNIQUE INDEX IF NOT EXISTS draft_login_phone_unique ON registration_drafts(draft_login_phone);
