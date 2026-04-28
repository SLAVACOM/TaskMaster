-- Auth Service Database Schema

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('USER', 'ADMIN', 'MANAGER')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index for faster user_id lookups
CREATE INDEX IF NOT EXISTS idx_user_id ON users(user_id);

-- Add comments
COMMENT ON TABLE users IS 'Stores user credentials and roles for authentication';
COMMENT ON COLUMN users.id IS 'Primary key';
COMMENT ON COLUMN users.user_id IS 'Reference to user in User Service';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password';
COMMENT ON COLUMN users.role IS 'User role: USER, ADMIN, or MANAGER';
COMMENT ON COLUMN users.created_at IS 'Timestamp when credentials were created';
COMMENT ON COLUMN users.updated_at IS 'Timestamp when credentials were last updated';

