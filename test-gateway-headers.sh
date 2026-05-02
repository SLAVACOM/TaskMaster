#!/bin/bash

# Gateway JWT Header Propagation Test Script
# This script verifies that JWT claims are correctly propagated as headers

set -e

GATEWAY_URL="http://localhost:8080"
EMAIL="testuser@example.com"
PASSWORD="Test123!Pass"
USERNAME="testuser"

echo "=== Gateway JWT Header Propagation Test ==="
echo ""

# Step 1: Check gateway is up
echo "Step 1: Checking if Gateway is running..."
if curl -s -f "$GATEWAY_URL/api/auth/register" -X OPTIONS > /dev/null 2>&1; then
    echo "✓ Gateway is accessible"
else
    echo "✗ Gateway is not accessible at $GATEWAY_URL"
    echo "  Please ensure the gateway is running:"
    echo "  cd All-Compose && docker compose ... up -d"
    exit 1
fi

# Step 2: Register a new user
echo ""
echo "Step 2: Registering test user..."
REGISTER_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$EMAIL\",
    \"password\": \"$PASSWORD\",
    \"username\": \"$USERNAME\"
  }")

echo "Register response: $REGISTER_RESPONSE"

# Check for success
if echo "$REGISTER_RESPONSE" | grep -q "error\|Error"; then
    echo "⚠ Registration may have failed or user already exists"
else
    echo "✓ User registration successful"
fi

# Step 3: Login to get JWT token
echo ""
echo "Step 3: Logging in to get JWT token..."
LOGIN_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$EMAIL\",
    \"password\": \"$PASSWORD\"
  }")

echo "Login response: $LOGIN_RESPONSE"

# Extract token
TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "✗ Failed to extract JWT token"
    echo "  Response: $LOGIN_RESPONSE"
    exit 1
fi

echo "✓ JWT token obtained (first 50 chars): ${TOKEN:0:50}..."

# Step 4: Decode JWT to see claims
echo ""
echo "Step 4: JWT Token Claims (decoded):"
# Simple JWT decode (only works for well-formed JWTs)
PAYLOAD=$(echo "$TOKEN" | cut -d'.' -f2)
# Add padding if needed
PADDING=$((4 - ${#PAYLOAD} % 4))
if [ $PADDING -ne 4 ]; then
    PAYLOAD="$PAYLOAD$(printf '%*s' $PADDING | tr ' ' '=')"
fi
echo "$PAYLOAD" | base64 -d 2>/dev/null | jq . || echo "  (Could not decode JWT payload)"

# Step 5: Make authenticated request to protected endpoint
echo ""
echo "Step 5: Making authenticated request to protected endpoint..."
echo "  Endpoint: GET /api/users/profile"
echo "  With Authorization: Bearer $TOKEN"

PROTECTED_RESPONSE=$(curl -s -X GET "$GATEWAY_URL/api/users/profile" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -v 2>&1)

echo "Response: "
echo "$PROTECTED_RESPONSE" | tail -20

# Step 6: Check for success
echo ""
echo "Step 6: Verification..."

if echo "$PROTECTED_RESPONSE" | grep -q "401\|Unauthorized"; then
    echo "✗ Received 401 Unauthorized"
    echo "  Gateway may not be accepting the token"
    echo "  Check gateway logs for JWT validation errors"
elif echo "$PROTECTED_RESPONSE" | grep -q "404\|Not Found"; then
    echo "⚠ Received 404 Not Found"
    echo "  Gateway accepted token, but endpoint doesn't exist or routed incorrectly"
elif echo "$PROTECTED_RESPONSE" | grep -q "200"; then
    echo "✓ Received 200 OK"
    echo "  Request was successful!"
else
    echo "? Unexpected response"
    echo "  Check response above for details"
fi

echo ""
echo "=== Next Steps ==="
echo ""
echo "To verify headers reached downstream services:"
echo "1. Check Gateway logs: docker logs <gateway-container>"
echo "   Look for: 'JWT validated' and 'Added header X-User-Id'"
echo ""
echo "2. Check UserService logs: docker logs <userservice-container>"
echo "   Look for incoming request headers with X-User-Id"
echo ""
echo "3. Check for errors:"
echo "   Look for 'UnsupportedOperationException' or 'ReadOnlyHttpHeaders'"
echo "   (These would indicate the immutability issue is NOT fixed)"
