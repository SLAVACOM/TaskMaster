# Автоматическое создание владельца-сотрудника при создании организации

## Overview

При создании организации система должна автоматически создавать сотрудника с ролью OWNER, который имеет полные права доступа. Владелец — это пользователь, создавший организацию. Таким образом обеспечивается, что каждая организация имеет как минимум одного администратора с момента создания.

**Проблема которую решает:** Без владельца организация остаётся в состоянии без администратора, что приводит к проблемам управления.

**Ключевые преимущества:**
- Автоматическое управление правами доступа
- Гарантированное наличие администратора
- Чистая, предсказуемая семантика создания организации

## Context (из исследования)

**Компоненты которые задействованы:**
- `com.slavacom.organizationservice.entity.Organization` — основная сущность организации
- `com.slavacom.organizationservice.entity.Employees` — сущность сотрудника
- `com.slavacom.organizationservice.service.OrganizationService` — бизнес-логика организаций
- `com.slavacom.organizationservice.employees.EmployeesService` — бизнес-логика сотрудников
- `com.slavacom.organizationservice.repository.OrganizationRepository` — репозиторий организаций
- `com.slavacom.organizationservice.employees.EmployeesRepository` — репозиторий сотрудников
- `com.slavacom.organizationservice.dto.CreateOrganizationRequest` — DTO для создания организации
- `com.slavacom.organizationservice.dto.OrganizationResponse` — DTO для ответа

**Найденные паттерны:**
- Spring Boot 4.0.5, Kotlin, JPA/Hibernate
- MapStruct маппера для Organization
- EmployeesService уже существует и может быть переиспользована

**Зависимости:**
- OrganizationService зависит от EmployeesService (будет добавлена транзакция)
- Необходимо извлечение userId из контекста аутентификации

## Development Approach

- **Подход к тестированию**: Manual via API — нет автоматических тестов
- Каждый шаг завершается полностью перед переходом к следующему
- Изменения должны быть сфокусированы и без излишних рефакторингов
- Все изменения будут коммитятся в отдельном коммите
- При каждом изменении проверять компиляцию

## Solution Overview

**Архитектурный подход:**

1. Создать Enum `EmployeeRole` с вариантами OWNER, ADMIN, MEMBER
2. Обновить Employees entity для использования Enum вместо String для role
3. Модифицировать `OrganizationService.createOrganization()`:
   - Создать Organization
   - Автоматически создать Employee с ролью OWNER
   - Обернуть в @Transactional для консистентности
4. Обновить EmployeesService.createEmployee() для поддержки создания с роль OWNER
5. Обновить DTOs если нужно

**Ключевые решения:**
- Enum для ролей — простая, типобезопасная реализация
- Transactional операция — гарантирует, что оба entity создаются или оба откатываются
- Извлечение userId из SecurityContext — следует существующим паттернам в микросервисах

## Technical Details

**Структура данных:**

```kotlin
enum class EmployeeRole {
    OWNER,      // полные права на организацию
    ADMIN,      // административные права
    MEMBER      // базовые права
}
```

**Изменения в сущности Employees:**
```kotlin
data class Employees(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val profileId: UUID,
    val organizationId: UUID,
    val role: EmployeeRole = EmployeeRole.MEMBER,  // изменить с String на EmployeeRole
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
```

**Поток создания организации:**
1. POST /organizations с CreateOrganizationRequest
2. OrganizationService.createOrganization():
   - Валидация входных данных
   - Создание Organization (сохранение)
   - Создание Employee с ролью OWNER для текущего пользователя
   - Обновление Organization.accountable = userId владельца
   - Возврат OrganizationResponse

## What Goes Where

**Implementation Steps** — задачи для реализации в коде  
**Post-Completion** — информационные пункты (без чек-боксов)

## Implementation Steps

### Task 1: Создание EmployeeRole Enum

**Files:**
- Create: `OrganizationService/src/main/kotlin/com/slavacom/organizationservice/entity/EmployeeRole.kt`

- [ ] создать файл EmployeeRole.kt с enum OWNER, ADMIN, MEMBER
- [ ] убедиться что файл компилируется

### Task 2: Обновление Employees entity для использования EmployeeRole

**Files:**
- Modify: `OrganizationService/src/main/kotlin/com/slavacom/organizationservice/entity/Employees.kt`

- [ ] изменить поле `role: String` на `role: EmployeeRole`
- [ ] установить default value EmployeeRole.MEMBER
- [ ] обновить конструктор если нужно
- [ ] убедиться что компилируется

### Task 3: Обновление EmployeesRepository и запросов если нужно

**Files:**
- Modify: `OrganizationService/src/main/kotlin/com/slavacom/organizationservice/employees/EmployeesRepository.kt`

- [ ] проверить, не нужны ли изменения в запросах из-за смены типа role
- [ ] откомпилировать и проверить отсутствие ошибок

### Task 4: Обновление EmployeesService для поддержки создания владельца

**Files:**
- Modify: `OrganizationService/src/main/kotlin/com/slavacom/organizationservice/employees/EmployeesService.kt`

- [ ] добавить метод createOwnerEmployee(userId: UUID, organizationId: UUID): Employees
- [ ] этот метод создаёт Employee с ролью OWNER
- [ ] убедиться что метод правильно сохраняет в БД

### Task 5: Модифицировать OrganizationService для создания владельца

**Files:**
- Modify: `OrganizationService/src/main/kotlin/com/slavacom/organizationservice/service/OrganizationService.kt`

- [ ] в методе createOrganization добавить логику вызова EmployeesService.createOwnerEmployee
- [ ] завернуть весь метод в @Transactional
- [ ] обновить Organization.accountable = userId владельца после создания сотрудника
- [ ] убедиться что логика правильная и компилируется

### Task 6: Обновление DTOs при необходимости

**Files:**
- Check: `OrganizationService/src/main/kotlin/com/slavacom/organizationservice/dto/`

- [ ] проверить, не нужны ли обновления CreateOrganizationRequest или OrganizationResponse
- [ ] если role теперь Enum, обновить маппер OrganizationMapper
- [ ] откомпилировать и проверить

### Task 7: DB Migration для обновления типа поля role

**Files:**
- Create: `OrganizationService/src/main/resources/db/migration/Vx__update_employee_role_to_enum.sql`

- [ ] создать миграцию для изменения типа поля role в таблице employees (varchar → другой enum тип или varchar с проверкой)
- [ ] миграция должна обновить существующие значения role в соответствии с маппингом (если нужно)

### Task 8: Ручное тестирование создания организации

**Manual Testing:**
- [ ] запустить OrganizationService: `cd OrganizationService && ./gradlew bootRun`
- [ ] убедиться что сервис стартует без ошибок
- [ ] создать организацию через POST /organizations с корректным userId
- [ ] проверить в БД что:
  - создана запись Organization
  - создана запись Employee с ролью OWNER
  - Employee имеет правильный userId и organizationId
- [ ] попробовать создать вторую организацию — убедиться что каждая получает своего владельца
- [ ] если есть ошибка, исправить и повторить тест

### Task 9: Компиляция и базовая проверка всех сервисов

- [ ] откомпилировать OrganizationService: `./gradlew build`
- [ ] убедиться что нет ошибок компиляции
- [ ] убедиться что другие микросервисы всё ещё компилируются (если нужно)

### Task 10: [Final] Обновление документации

- [ ] обновить CLAUDE.md в проекте если нужны новые паттерны/конвенции
- [ ] добавить краткую заметку о том, как работает создание владельца (если есть специальные детали)

## Post-Completion

**Ручная проверка в боевых сценариях:**
- Протестировать создание нескольких организаций и убедиться что каждая независима
- Проверить через UI (если есть) что владелец может управлять организацией
- Убедиться что permission система работает правильно с OWNER ролью
- Проверить backoff сценарии (что если создание Owner fails?)

**Возможные улучшения в будущем:**
- Добавить явные permissions field (List<String>) для более гибкой системы
- Добавить аудит логирование создания владельца
- Добавить отправку уведомления владельцу при создании организации
