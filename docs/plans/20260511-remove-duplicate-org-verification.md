# Удалить дублированную проверку организации из ProfileService

## Overview
ProfileService.createProfile() выполняет ненужную проверку существования организации через вызов к OrganizationService. Это вызывает 404-ошибку, потому что:
1. Когда вызывается из OrganizationService.create(), организация еще не закоммичена в БД
2. Проверка находится внутри активной транзакции, которая еще не завершена

Решение: удалить эту проверку, так как:
- OrganizationService уже создал организацию перед вызовом profileService
- Если profileService вызывается напрямую, вызывающий код должен проверить организацию
- Дополнительная валидация на уровне профиля не нужна

## Context
- **Files**: 
  - `UserService/src/main/java/com/slavacom/userservice/service/ProfileService.java` (строка 33)
  - `UserService/src/main/java/com/slavacom/userservice/client/OrganizationServiceClient.java`

- **Problem**: Транзакционная проблема - вложенный вызов сервиса во время создания еще не закоммиченной сущности

## Development Approach
- **testing approach**: Manual verification (no new tests required for deletion)
- Remove the unnecessary organization verification line
- Verify the flow works end-to-end in both scenarios:
  - Creating organization (from OrganizationService)
  - Creating profile directly (from REST API)

## Testing Strategy
- Manual testing: create organization request → verify it succeeds
- Verify profile is created in UserService database
- Test direct profile creation API if exists

## Solution Overview
Delete line 33 from ProfileService.createProfile():
```java
organizationServiceClient.getOrganizationById(request.getOrganizationId());
```

This verification is:
1. Redundant - OrganizationService already created the org
2. Problematic - org not yet committed when verification runs
3. Unnecessary - caller is responsible for providing valid org ID

## Implementation Steps

### Task 1: Remove organization verification from ProfileService

**Files:**
- Modify: `UserService/src/main/java/com/slavacom/userservice/service/ProfileService.java`
- Modify: `UserService/src/main/java/com/slavacom/userservice/client/OrganizationServiceClient.java` (potentially remove if not used elsewhere)

- [x] remove line 33: `organizationServiceClient.getOrganizationById(request.getOrganizationId());`
- [x] remove the log statement on line 34 if it becomes unused
- [x] verify ProfileService still compiles
- [x] remove unused OrganizationServiceClient import and injection
- [x] check if OrganizationServiceClient is used elsewhere (search for usages)

### Task 2: Verify the fix works

- [ ] start both UserService and OrganizationService
- [ ] create an organization → should succeed with profile creation
- [ ] verify organization and profile are both created in databases
- [ ] check logs show no 404 errors during organization creation

### Task 3: Clean up if needed

- [ ] if OrganizationServiceClient.getOrganizationById() is not used elsewhere, remove the unused method
- [ ] if OrganizationServiceClient is no longer needed, consider removing the injection from ProfileService

## Post-Completion
*Manual verification - no checkboxes*

**Testing scenarios:**
- Create organization via API → profile should be created without 404 errors
- Verify both organization and profile exist in their respective databases
- Check application logs for clean transaction flow without redundant calls
