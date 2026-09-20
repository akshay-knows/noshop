# NoShop Project Bug Backlog

Audit branch: dev
Purpose: interview preparation + controlled engineering fixes. Findings are classified as confirmed defects, reliability/security risks, or investigation items. Do not treat investigation items as confirmed bugs until reproduced.

## Confirmed / High-Priority Tickets

### BUG-001 — Product variant create does not normalize SKU before duplicate check
- Area: product-service / ProductVariantServiceImpl
- Severity: High
- Type: Functional bug / data consistency
- Evidence: createVariant() checks existsBySku(request.getSku()) but saves request.getSku().trim(). Update flow trims before duplicate checking.
- Reproduction: create SKU " SKU-001 " when "SKU-001" already exists.
- Expected: normalized SKU should be checked and stored consistently.
- Fix plan: normalize once at service boundary, validate duplicate on normalized value, persist normalized value; add regression tests.

### BUG-002 — Invalid variant unit can fail as an uncontrolled runtime error
- Area: product-service / ProductVariantServiceImpl
- Severity: Medium
- Type: Validation/error handling
- Evidence: Unit.valueOf(request.getUnit()) is called directly while request.unit is a String.
- Reproduction: send an unsupported or differently-cased unit such as "kg".
- Expected: controlled 400 validation/business error.
- Fix plan: explicit enum conversion/validation with safe error response; add tests.

### BUG-003 — Product image deletion couples external S3 deletion before DB deletion
- Area: product-service / ProductImageServiceImpl
- Severity: High
- Type: Reliability / distributed consistency
- Evidence: deleteImage() calls s3Service.deleteFile() before productImageRepository.delete().
- Risk: if DB deletion fails/rolls back after successful S3 deletion, DB metadata can point to a missing object.
- Fix plan: define failure-safe deletion strategy and test S3/DB failure scenarios. Do not blindly move calls without understanding transaction/outbox implications.

### BUG-004 — Product deletion has the same S3/DB consistency risk
- Area: product-service / ProductServiceImpl
- Severity: High
- Type: Reliability
- Evidence: deleteProduct() deletes S3 objects inside the DB transaction before deleting the product.
- Fix plan: make external-object cleanup failure-safe and idempotent; test partial failure.

### BUG-005 — Gateway currently permits every request
- Area: api-gateway / SecurityConfig
- Severity: High
- Type: Security / architecture
- Evidence: authorizeExchange(...).anyExchange().permitAll().
- Impact: gateway itself provides no authorization boundary; downstream services currently enforce JWT/RBAC.
- Fix plan: keep downstream authorization as defense in depth, but decide and document the intended gateway responsibility; protect management/internal routes and ensure direct-service exposure is controlled in deployment.

### BUG-006 — Production-sensitive configuration is hard-coded in application properties
- Area: all services / configuration
- Severity: Critical for production hygiene
- Type: Security / deployment
- Evidence: datasource username/password and JWT secret are committed as plain properties; examples use root/test and a shared JWT secret.
- Fix plan: externalize secrets/config using environment variables or deployment secret configuration; provide safe local defaults only where appropriate.

### BUG-007 — Actuator exposes all endpoints with full health details
- Area: all services / management configuration
- Severity: High
- Type: Security / observability
- Evidence: management.endpoints.web.exposure.include=* and health.show-details=always.
- Fix plan: expose only required endpoints, secure management endpoints, and avoid exposing sensitive health details publicly.

### BUG-008 — JPA schema auto-update and SQL logging are enabled by default
- Area: product/auth/inventory configuration
- Severity: High
- Type: Production configuration risk
- Evidence: spring.jpa.hibernate.ddl-auto=update and spring.jpa.show-sql=true.
- Fix plan: separate local/dev/prod configuration; use controlled migrations for deployed environments and disable SQL logging in production unless intentionally enabled at appropriate log levels.

## Investigation / Reliability Tickets

### BUG-009 — Outbox publisher blocks scheduled processing on Kafka send
- Area: product-service / OutboxPublisher
- Severity: Medium
- Type: Reliability/performance
- Evidence: KafkaTemplate.send(...).get() is executed synchronously for every event.
- Investigation: measure behavior with slow/unavailable Kafka and verify scheduler throughput/retry behavior.
- Fix only after reproduction/measurement.

### BUG-010 — Outbox FAILED events are retried without visible retry/backoff policy
- Area: product-service / OutboxPublisher
- Severity: Medium
- Type: Reliability
- Evidence: PENDING and FAILED events are selected every five seconds; FAILED state has no attempt/backoff metadata.
- Investigation: verify whether repeated failures can create hot retry loops and duplicate delivery scenarios.
- Fix plan: add bounded retry/backoff/attempt tracking if required.

### BUG-011 — Kafka consumer initializes inventory with repeated existence queries
- Area: inventory-service / ProductCreatedEventConsumer
- Severity: Medium
- Type: Performance
- Evidence: for each variant/warehouse pair it calls findByVariantIdAndWarehouseId().
- Investigation: benchmark for realistic variant × warehouse counts.
- Fix plan: batch lookup or rely on unique constraint + idempotent handling if measurement shows a problem.

### BUG-012 — Product search uses leading-wildcard LIKE
- Area: product-service / ProductRepository
- Severity: Medium
- Type: Performance
- Evidence: LIKE '%query%' on name/description.
- Investigation: inspect indexes, dataset size, query plan, and actual response time before changing.
- Fix plan: optimize only based on measured need.

### BUG-013 — Inventory optimistic locking exists but conflict behavior needs verification
- Area: inventory-service
- Severity: Medium
- Type: Concurrency/reliability
- Evidence: Inventory has @Version, but concurrent stock operations must be tested for ObjectOptimisticLockingFailureException behavior and API response.
- Fix plan: reproduce concurrent update; add regression test and controlled conflict response if needed.

### BUG-014 — Exception handling differs between common-library and inventory-service
- Area: exception handling
- Severity: Medium
- Type: API consistency
- Evidence: inventory service handles IllegalArgumentException and several service exceptions explicitly; common handler primarily handles BaseException and validation.
- Investigation: map which services actually register which advice and reproduce representative bad requests.
- Fix plan: standardize error contract without hiding useful service-specific errors.

### BUG-015 — Product image primary-order repair needs concurrency testing
- Area: product-service / ProductImageServiceImpl
- Severity: Medium
- Type: Data consistency
- Evidence: deleting displayOrder=1 then promoting the first remaining image can race with another image operation.
- Investigation: concurrent delete/confirm scenarios and unique constraint behavior.
- Fix plan: transactional locking/order strategy only if reproduced.

## Deferred / Not Yet a Bug
- Redis caching itself is not considered broken; cache eviction exists for product updates/status and image changes.
- Product list image loading already uses a batch query, so do not label it as N+1 without measurement.
- Inventory @Version is a positive control; it must be tested, not removed.
- Resilience4j is not claimed as implemented until verified/added.
- AWS EC2/RDS/CloudWatch/JUnit/Mockito hands-on claims remain pending until we actually configure/use them.

## Fixing Rule
For every ticket:
1. Reproduce or write a failing test.
2. Trace controller -> service -> repository/external dependency.
3. Identify root cause.
4. Make the smallest production-reasonable fix.
5. Add regression coverage.
6. Run verification.
7. Create a bugfix branch and PR to dev.
8. Perform code review and address review comments.
9. Record the interview explanation: symptom -> root cause -> fix -> validation -> result.
