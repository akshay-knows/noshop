# NoShop — Enterprise E-Commerce Microservices Architecture

## Core Stack (baseline for every service)

| Component | Version | Notes |
|---|---|---|
| Java | **17 (LTS)** | Minimum for Spring Boot 3.x and 4.x |
| Spring Boot | **3.5.16** | Latest patch on the 3.5 line — matches your current stack (3.5.5), least breaking-change risk. *(Note: Spring Boot 3.5 reached open-source EOL on June 30, 2026. Spring Boot 4.1.0 is now the officially recommended line for new greenfield projects — Java 17 still the minimum — but 4.x has breaking changes: Jakarta EE 11, Spring Framework 7. Most enterprises still run 3.4/3.5 in production while migrating, so 3.5.16 is the pragmatic "widely used in companies right now" choice; use 4.1.0 if you want to build forward-looking.)* |
| Spring Cloud | **2025.0.6** (release train matching Boot 3.5.x) | Keep this and Boot in lockstep — never mix trains |
| Build tool | Maven | Matches your existing multi-module setup |
| Container runtime | Docker + Docker Compose (local), Kubernetes (prod) | Standard for microservices deployment |

---

## Service Inventory

| # | Service | Purpose | Key Dependencies (artifact) | Version |
|---|---|---|---|---|
| 1 | **discovery-server** | Service registry (Eureka) | `spring-cloud-starter-netflix-eureka-server` | managed by Spring Cloud BOM |
| 2 | **config-server** | Centralized externalized config for all services | `spring-cloud-config-server` | managed by Spring Cloud BOM |
| | | | `spring-cloud-starter-netflix-eureka-client` | managed by Spring Cloud BOM |
| 3 | **api-gateway** | Single entry point, routing, auth pass-through, rate limiting | `spring-cloud-starter-gateway` | managed by Spring Cloud BOM |
| | | | `spring-cloud-starter-netflix-eureka-client` | managed by Spring Cloud BOM |
| | | | `spring-boot-starter-security` | managed by Boot BOM |
| | | | `resilience4j-spring-boot3` | **2.3.0** |
| 4 | **auth-service** | Registration, login, JWT issuance, role management | `spring-boot-starter-security` | managed by Boot BOM |
| | | | `spring-boot-starter-data-jpa` | managed by Boot BOM |
| | | | `io.jsonwebtoken:jjwt-api` / `jjwt-impl` / `jjwt-jackson` | **0.12.6** |
| | | | `mysql-connector-j` | managed by Boot BOM |
| 5 | **user-service** | Customer profile, addresses, preferences | `spring-boot-starter-data-jpa` | managed by Boot BOM |
| | | | `spring-cloud-starter-openfeign` | managed by Spring Cloud BOM |
| 6 | **product-catalog-service** | Product listings, categories, pricing | `spring-boot-starter-data-jpa` | managed by Boot BOM |
| | | | `spring-boot-starter-cache` + `spring-boot-starter-data-redis` | managed by Boot BOM |
| 7 | **inventory-service** | Stock levels, reservations, warehouse sync | `spring-boot-starter-data-jpa` | managed by Boot BOM |
| | | | `spring-kafka` | managed by Boot BOM |
| 8 | **cart-service** | Shopping cart state, session handling | `spring-boot-starter-data-redis` | managed by Boot BOM |
| 9 | **order-service** | Order lifecycle, order history, saga orchestration | `spring-boot-starter-data-jpa` | managed by Boot BOM |
| | | | `spring-kafka` | managed by Boot BOM |
| | | | `resilience4j-spring-boot3` | **2.3.0** |
| 10 | **payment-service** | Payment processing, gateway integration (Stripe/Razorpay), refunds | `spring-boot-starter-data-jpa` | managed by Boot BOM |
| | | | `spring-kafka` | managed by Boot BOM |
| | | | `resilience4j-spring-boot3` | **2.3.0** |
| 11 | **notification-service** | Email/SMS/push notifications on order/auth events | `spring-boot-starter-mail` | managed by Boot BOM |
| | | | `spring-kafka` | managed by Boot BOM |
| 12 | **search-service** | Full-text product search, filters, facets | `spring-boot-starter-data-elasticsearch` | managed by Boot BOM |
| 13 | **review-service** | Product reviews and ratings | `spring-boot-starter-data-jpa` | managed by Boot BOM |
| 14 | **shipping-service** | Shipment tracking, courier integration | `spring-cloud-starter-openfeign` | managed by Spring Cloud BOM |
| 15 | **admin-server** | Spring Boot Admin — monitors all services | `spring-boot-admin-starter-server` | **3.5.10** |
| | | | `spring-boot-starter-security` | managed by Boot BOM |

---

## Cross-Cutting Dependencies (add to relevant/all services)

| Purpose | Dependency | Version | Notes |
|---|---|---|---|
| Common shared DTOs/exceptions | `common-library` (internal) | 1.0.0 | Your existing shared module |
| Object mapping | `org.mapstruct:mapstruct` + `mapstruct-processor` | **1.6.3** | Already in your parent POM properties |
| Boilerplate reduction | `org.projectlombok:lombok` | managed by Boot BOM | Keep `optional=true` |
| API documentation | `org.springdoc:springdoc-openapi-starter-webmvc-ui` | **2.8.17** | Boot 3.x line — do **not** use springdoc 3.x (that targets Boot 4/Jakarta 11) |
| Circuit breaker / retry / rate limiter | `io.github.resilience4j:resilience4j-spring-boot3` | **2.3.0** | Requires Java 17; do not use resilience4j 3.x (needs Java 21) |
| Distributed tracing | `io.micrometer:micrometer-tracing-bridge-brave` + `io.zipkin.reporter2:zipkin-reporter-brave` | managed by Boot BOM | Pair with a Zipkin server for trace visualization |
| Metrics | `io.micrometer:micrometer-registry-prometheus` | managed by Boot BOM | Scrape via Prometheus + Grafana |
| Messaging | `spring-kafka` | managed by Boot BOM | Use for order/inventory/payment/notification event flow |
| Caching / session store | `spring-boot-starter-data-redis` | managed by Boot BOM | Cart, rate limiting, token blacklists |
| DB migrations | `org.flywaydb:flyway-mysql` | managed by Boot BOM | Prefer over `ddl-auto=update` in real environments |
| Testing | `spring-boot-starter-test` + `spring-security-test` | managed by Boot BOM | Already in your auth-service |
| Testing (integration) | `org.testcontainers:junit-jupiter`, `mysql`, `kafka` | managed by Boot BOM (via testcontainers-bom) | Spin up real MySQL/Kafka in tests |

---

## Why "managed by Boot/Cloud BOM" instead of pinned versions

Any dependency published under `org.springframework.boot` or included in the `spring-cloud-dependencies` BOM should **not** get an explicit `<version>` in your POMs — your parent already imports these BOMs, so Maven resolves the correct, tested-together version automatically. Only pin a version explicitly when the library is **outside** those BOMs (jjwt, mapstruct, springdoc, resilience4j) — these are the ones listed with explicit numbers above, and they're the ones you should double check against the compatibility matrix whenever you bump Spring Boot.

## Compatibility rule of thumb

- Spring Boot and Spring Cloud versions must always come from the **same release train** — check `https://spring.io/projects/spring-cloud` before changing either.
- springdoc-openapi 2.x ↔ Spring Boot 3.x. springdoc-openapi 3.x ↔ Spring Boot 4.x. Never mix.
- resilience4j 2.x ↔ Java 17. resilience4j 3.x ↔ Java 21 (don't use it here).
