-- Add profile tracking fields to User entity
ALTER TABLE users
ADD COLUMN latest_profile_id UUID,
ADD COLUMN latest_organization_id UUID;

-- Create indexes for better query performance
CREATE INDEX idx_user_profile ON users(latest_profile_id);
CREATE INDEX idx_user_organization ON users(latest_organization_id);

-- Add comment for documentation
COMMENT ON COLUMN users.latest_profile_id IS 'ID of the user''s latest profile in an organization, used for JWT generation';
COMMENT ON COLUMN users.latest_organization_id IS 'ID of the user''s latest organization, used for JWT generation';
