# Gateway JWT Header Propagation - Manual Testing Guide

## Prerequisites
- Docker and docker-compose installed
- All services configured (.env files set up)
- Access to command line/terminal

## Test Steps

### 1. Start the Infrastructure Stack

```bash
cd All-Compose

# Start core services (Postgres, Redis, Kafka)
docker compose -f docker-compose.yaml -f docker-compose.db.yml -f docker-compose.kafka.yml up -d

# Then start the application services (including Gateway)
docker compose -f docker-compose.yaml -f docker-compose.db.yml -f docker-compose.kafka.yml -f docker-compose.services.yml up -d

# Verify containers are running
docker compose ps
```

**Expected output:**
```
NAME                                  STATUS
taskmaster-gateway-1                  Up
taskmaster-auth-service-1             Up
taskmaster-user-service-1             Up
taskmaster-postgres-auth-1            Up
taskmaster-postgres-user-1            Up
taskmaster-kafka-1                    Up
```

### 2. Wait for Services to Start

Gateway may take 10-15 seconds to fully initialize. Check logs:

```bash
docker logs taskmaster-gateway-1 -f
```

Look for:
```
Started GatewayApplication in X.XXX seconds
```

Once you see this, services are ready.

### 3. Run the Automated Test Script

```bash
# From repo root
bash test-gateway-headers.sh
```

Or run the steps manually:

### 4a. Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "username": "testuser"
  }'
```

**Expected response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "test@example.com",
  "username": "testuser",
  "enabled": true,
  "createdAt": "2026-05-02T10:30:00Z"
}
```

### 4b. Login to Get JWT Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }' | jq .
```

**Expected response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Save the `accessToken` value** — you'll need it for the next steps.

```bash
# Store token in a variable for easier testing
TOKEN="eyJhbGciOiJIUzI1NiJ9..."  # Replace with actual token
```

### 5. Make Authenticated Request Through Gateway

```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -v
```

**Expected response:** 200 OK with user profile data

**What to look for in the response:**
- Response code: 200 (not 401 or 404)
- User profile data returned
- Headers in response should be standard HTTP headers

### 6. Check Gateway Logs for JWT Processing

```bash
docker logs taskmaster-gateway-1 | grep -E "JWT|RequestHeaderFilter|Exchange"
```

**Expected output (look for these patterns):**

✅ **JWT validation success:**
```
INFO [...] Incoming request: GET /api/users/profile
INFO [...] JWT validated: path=/api/users/profile userId=550e8400-e29b-41d4-a716-446655440000 role=USER
DEBUG [...] JWT attributes stored in exchange: X-User-Id=550e8400-e29b-41d4-a716-446655440000, X-User-Role=USER, X-Profile-Id=..., X-Organization-Id=...
```

✅ **Header injection:**
```
DEBUG [...] RequestHeaderFilter: Retrieved attributes - userId=550e8400-e29b-41d4-a716-446655440000, role=USER, profileId=..., organizationId=...
DEBUG [...] RequestHeaderFilter: Added header X-User-Id=550e8400-e29b-41d4-a716-446655440000
DEBUG [...] RequestHeaderFilter: Added header X-User-Role=USER
DEBUG [...] RequestHeaderFilter: Mutated request headers: [x-user-id, x-user-role, x-profile-id, x-organization-id, ...]
```

✅ **No errors:**
```
(Should NOT see any of these):
UnsupportedOperationException
ReadOnlyHttpHeaders
Failed to add header
Invalid header
```

### 7. Check Downstream Service Logs

```bash
docker logs taskmaster-user-service-1 | grep -E "X-User-Id|X-User-Role|headers|request"
```

**Expected output:**
```
INFO [...] Incoming request from Gateway: GET /api/users/profile
DEBUG [...] Request headers: X-User-Id=550e8400-e29b-41d4-a716-446655440000, X-User-Role=USER, ...
INFO [...] Processing profile request for user: 550e8400-e29b-41d4-a716-446655440000
```

### 8. Test Public Endpoint (No Custom Headers)

Public paths under `/api/auth/**` should NOT have custom headers:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }' \
  -v
```

**Expected in Gateway logs:**
```
INFO [...] Public path accessed: /api/auth/login
INFO [...] chain.filter(exchange)  // JWT validation skipped
```

Request should succeed without Authorization header.

### 9. Test with Invalid Token

```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer invalid.token.here" \
  -H "Content-Type: application/json"
```

**Expected response:** 401 Unauthorized

**Expected in Gateway logs:**
```
ERROR [...] JWT validation failed: path=/api/users/profile error=SignatureException
```

### 10. Test Without Token

```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Content-Type: application/json"
```

**Expected response:** 401 Unauthorized

**Expected in Gateway logs:**
```
WARN [...] Missing or invalid Authorization header for path /api/users/profile: null
```

## Verification Checklist

Use this checklist to verify the fix is working:

### Gateway Processing
- [ ] JWT tokens are validated successfully
- [ ] JWT claims are extracted (userId, role, profileId, organizationId)
- [ ] Claims are stored in exchange attributes
- [ ] No "ReadOnlyHttpHeaders" errors
- [ ] No "UnsupportedOperationException" errors
- [ ] Headers are logged as "Added header X-User-Id=..."

### Request Propagation
- [ ] RequestHeaderFilter processes the request
- [ ] Custom headers are added to the request
- [ ] Request is forwarded to downstream service with headers
- [ ] Logs show "Forwarding request with mutated headers to chain"

### Downstream Service
- [ ] Downstream service receives the request
- [ ] Logs show X-User-Id header in incoming request
- [ ] Service can identify the user from the header
- [ ] User-related operations work correctly

### Error Handling
- [ ] Public paths work without tokens
- [ ] Invalid tokens return 401
- [ ] Missing tokens return 401
- [ ] Valid tokens grant access

## If Tests Fail

### Headers Not Appearing in Logs

1. **Check gateway logs are verbose enough:**
   ```bash
   docker exec taskmaster-gateway-1 env | grep -i log
   ```
   
   You should see a logging level setting. If it's not DEBUG or TRACE, logs won't show header details.

2. **Enable debug logging:**
   Edit `Gateway-service/src/main/resources/application-docker.yml`:
   ```yaml
   logging:
     level:
       com.slavacom.gateway: DEBUG
       org.springframework.cloud.gateway: DEBUG
   ```

3. **Rebuild and restart:**
   ```bash
   cd Gateway-service
   ./gradlew build
   docker compose -f ../All-Compose/docker-compose.yaml \
               -f ../All-Compose/docker-compose.db.yml \
               -f ../All-Compose/docker-compose.kafka.yml \
               -f ../All-Compose/docker-compose.services.yml up -d --force-recreate taskmaster-gateway-1
   ```

### 401 Errors on Protected Endpoints

1. **Check JWT_SECRET matches:**
   ```bash
   # Should be identical across services
   docker exec taskmaster-gateway-1 env | grep JWT
   docker exec taskmaster-auth-service-1 env | grep JWT
   docker exec taskmaster-user-service-1 env | grep JWT
   ```

2. **Check service ports are correct:**
   ```bash
   docker exec taskmaster-gateway-1 env | grep SERVICE_URL
   ```

3. **Check tokens are not expired:**
   ```bash
   # Tokens expire in 900 seconds (15 minutes)
   # If too much time passed, login again
   ```

### 500 Errors or Service Unavailable

1. **Check all services are running:**
   ```bash
   docker compose ps
   ```

2. **Check service logs for startup errors:**
   ```bash
   docker logs taskmaster-user-service-1
   docker logs taskmaster-auth-service-1
   ```

3. **Check database connections:**
   ```bash
   docker logs taskmaster-postgres-user-1
   docker logs taskmaster-postgres-auth-1
   ```

### ReadOnlyHttpHeaders Exception

**This indicates the fix is NOT applied correctly.**

1. Check if you're on the latest code:
   ```bash
   git log --oneline -1
   ```
   Should show: `fix(gateway): resolve ReadOnlyHttpHeaders mutation issue`

2. Rebuild the gateway:
   ```bash
   cd Gateway-service && ./gradlew clean build
   docker compose up -d --build taskmaster-gateway-1
   ```

## Success Criteria

✅ **All of the following must be true:**

1. Registration endpoint returns 200 with user data
2. Login endpoint returns 200 with JWT token
3. Protected endpoint with valid token returns 200
4. Protected endpoint with invalid token returns 401
5. Gateway logs show "JWT validated" messages
6. Gateway logs show "Added header X-User-Id" messages
7. No "UnsupportedOperationException" or "ReadOnlyHttpHeaders" errors
8. Downstream service receives request with custom headers

Once all criteria are met, the immutability fix is **verified and working**.

## Next Steps

After successful verification:

1. **Document findings:**
   - Update `docs/plans/20260502-gateway-verification.md` with test results
   - Add any configuration notes discovered during testing

2. **Commit verification:**
   ```bash
   git add docs/plans/20260502-gateway-verification.md
   git commit -m "docs: verify gateway JWT header propagation fix is working"
   ```

3. **Consider additional features:**
   - Add request ID tracing header for debugging
   - Add custom header validation in downstream services
   - Implement header logging middleware in other services

## Troubleshooting Reference

| Issue | Cause | Solution |
|-------|-------|----------|
| 401 Unauthorized | JWT validation failed | Check JWT_SECRET matches, token not expired |
| 404 Not Found | Route not found in gateway | Check application.yml route configuration |
| 500 Internal Server Error | Downstream service error | Check downstream service logs |
| No custom headers in logs | Logging level too low | Enable DEBUG logging, restart gateway |
| ReadOnlyHttpHeaders exception | Old code using .header() method | Rebuild with latest code, restart container |
| Services not starting | Port already in use | `docker compose down` and start fresh |
