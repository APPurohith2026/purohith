-- Additive PostgreSQL migration for environments using ddl-auto=validate.
-- Render currently uses ddl-auto=update. Never drop/reset member tables.
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS payment_tracking_id varchar(40);
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS payment_reference varchar(64);
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS payment_status varchar(20);
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS payment_amount numeric(10,2);
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS payment_submitted_at timestamp(6) with time zone;
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS payment_verified_at timestamp(6) with time zone;
CREATE UNIQUE INDEX IF NOT EXISTS membership_payment_tracking_unique ON memberships(payment_tracking_id);
CREATE UNIQUE INDEX IF NOT EXISTS membership_payment_reference_unique ON memberships(payment_reference);
