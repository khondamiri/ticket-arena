ALTER TABLE venues
ADD COLUMN public_id UUID UNIQUE NOT NULL default gen_random_uuid()