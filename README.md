# Noshop

Noshop is a Spring Boot microservices-based e-commerce backend for an online grocery/retail platform.

> **Interview learning note:** Detailed design-decision explanations and interview preparation are intentionally postponed until the project is finished. This README is a practical project reference only.

---

## 1. Services

| Service | Port | Responsibility |
|---|---:|---|
| API Gateway | 8080 | External API entry point and routing |
| Auth Service | 8081 | Authentication and JWT issuance |
| Product Service | 8082 | Product catalog, categories, brands, variants and images |
| Inventory Service | 8083 | Warehouse inventory, stock and reservations |
| Admin Server | 9090 | Spring Boot Admin monitoring |
| Discovery Server | 8761 | Eureka service discovery |

Infrastructure:

- MySQL — service-owned databases
- Redis — product caching
- Kafka — asynchronous product/inventory events
- S3 — product image storage
- Eureka — service discovery
- Spring Boot Actuator — health/metrics endpoints
- Spring Boot Admin — application monitoring

---

## 2. Local URLs

### Infrastructure

- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Spring Boot Admin: http://localhost:9090
- Redis: localhost:6379
- Kafka: localhost:9092

### Backend services

- Auth Service: http://localhost:8081
- Product Service: http://localhost:8082
- Inventory Service: http://localhost:8083

---

## 3. Swagger / OpenAPI

Swagger UI is available for the MVC services that include Springdoc.

### Product Service

- Swagger UI: http://localhost:8082/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8082/v3/api-docs

### Inventory Service

- Swagger UI: http://localhost:8083/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8083/v3/api-docs

### Gateway API usage

The gateway routes client API calls to the services using Eureka service discovery.

- Product API base: http://localhost:8080/api/v1/product
- Inventory API base: http://localhost:8080/api/v1/inventory
- Auth API base: http://localhost:8080/api/v1/auth

The gateway currently routes Product and Inventory paths to `PRODUCT-SERVICE` and `INVENTORY-SERVICE` respectively.

---

# 4. Product Service

**Port:** `8082`

**Base URL:** `http://localhost:8082/api/v1/product`

**Gateway URL:** `http://localhost:8080/api/v1/product`

## Product APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/products` | Create product |
| GET | `/products` | List products with pagination/filtering |
| GET | `/products/{id}` | Get product by ID |
| PUT | `/products/{id}` | Update product |
| DELETE | `/products/{id}` | Delete product |
| GET | `/products/search?query={query}` | Search products |
| PATCH | `/products/{id}/status` | Change product lifecycle status |

Example:

```text
GET http://localhost:8080/api/v1/product/products?page=0&size=20
GET http://localhost:8080/api/v1/product/products?categoryId=1&status=ACTIVE&page=0&size=20
GET http://localhost:8080/api/v1/product/products/search?query=milk&page=0&size=20
GET http://localhost:8080/api/v1/product/products/1
```

## Brand APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/brands` | Create brand |
| GET | `/brands` | List brands |
| GET | `/brands/{id}` | Get brand |
| PUT | `/brands/{id}` | Update brand |
| DELETE | `/brands/{id}` | Delete brand |

## Category APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/categories` | Create category |
| GET | `/categories` | List categories |
| GET | `/categories/{id}` | Get category |
| PUT | `/categories/{id}` | Update category |
| DELETE | `/categories/{id}` | Delete category |

## Subcategory APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/subcategories` | Create subcategory |
| GET | `/subcategories` | List subcategories |
| GET | `/subcategories/{id}` | Get subcategory |
| PUT | `/subcategories/{id}` | Update subcategory |
| DELETE | `/subcategories/{id}` | Delete subcategory |

## Product Variant APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/variants/product/{productId}` | Create variant for product |
| GET | `/variants/product/{productId}` | List variants for product |
| GET | `/variants/{variantId}` | Get variant |
| PUT | `/variants/{variantId}` | Update variant |
| DELETE | `/variants/{variantId}` | Delete variant |

## Product Image APIs

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/product-images/product/{productId}` | List product images |
| GET | `/product-images/product/{productId}/primary` | Get primary image |
| POST | `/product-images/product/{productId}/upload-url` | Generate image upload URL |
| POST | `/product-images/product/{productId}/confirm` | Confirm image upload |
| DELETE | `/product-images/{imageId}` | Delete image |

---

## 5. Product Service Flow

### Product creation

```text
Client
  |
  v
API Gateway :8080
  |
  v
Product Service :8082
  |
  +--> Validate request
  |
  +--> Product Service business logic
  |
  +--> Product DB
  |
  +--> Create product-created event/outbox data
  |
  v
Response
```

### Product image flow

```text
Client
  |
  +--> Product Service asks S3 for upload URL
  |
  +--> Client uploads image to S3
  |
  +--> Client confirms upload with Product Service
  |
  +--> Product image metadata stored in Product DB
```

### Product search/filter flow

```text
Client
  |
  v
GET /api/v1/product/products
  |
  +--> optional category filter
  +--> optional subcategory filter
  +--> optional product status filter
  +--> pagination
  +--> sorting
  |
  v
Product Service
  |
  v
Product DB
```

Search uses the Product Service search API:

```text
GET /api/v1/product/products/search?query=milk
```

### Product caching

Product reads can use Redis caching to reduce repeated database access.

```text
Client
  |
  v
Product Service
  |
  +--> Redis cache hit ----> Response
  |
  +--> cache miss
          |
          v
       Product DB
          |
          v
       Redis cache
          |
          v
       Response
```

---

# 6. Inventory Service

**Port:** `8083`

**Base URL:** `http://localhost:8083/api/v1/inventory`

**Gateway URL:** `http://localhost:8080/api/v1/inventory`

Inventory belongs to a **product variant + warehouse** combination.

```text
Inventory
├── id
├── variantId
├── warehouseId
├── quantity
├── reservedQuantity
├── version
├── createdAt
└── updatedAt
```

The database enforces uniqueness for `(variantId, warehouseId)`.

## Inventory APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/` | Create inventory record |
| GET | `/?variantId={variantId}&warehouseId={warehouseId}` | Get inventory for variant + warehouse |
| GET | `/variant/{variantId}` | Get inventory across warehouses |
| GET | `/warehouse/{warehouseId}` | Get inventory in warehouse |
| PUT | `/{id}` | Update physical quantity |
| DELETE | `/{id}` | Delete inventory record |
| POST | `/stock/add` | Add stock |
| POST | `/stock/reduce` | Reduce available stock |
| POST | `/stock/reserve` | Reserve stock |
| POST | `/stock/release` | Release reserved stock |

Example:

```text
GET http://localhost:8080/api/v1/inventory?variantId=10&warehouseId=1
GET http://localhost:8080/api/v1/inventory/variant/10
GET http://localhost:8080/api/v1/inventory/warehouse/1
POST http://localhost:8080/api/v1/inventory/stock/add?variantId=10&warehouseId=1&quantity=20
POST http://localhost:8080/api/v1/inventory/stock/reduce?variantId=10&warehouseId=1&quantity=2
POST http://localhost:8080/api/v1/inventory/stock/reserve?variantId=10&warehouseId=1&quantity=3
POST http://localhost:8080/api/v1/inventory/stock/release?variantId=10&warehouseId=1&quantity=3
```

## Warehouse APIs

**Base:** `/api/v1/inventory/warehouses`

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/warehouses` | Create warehouse |
| GET | `/warehouses/{id}` | Get warehouse |
| GET | `/warehouses` | List warehouses |
| PATCH | `/warehouses/{id}/activate` | Activate warehouse |
| PATCH | `/warehouses/{id}/deactivate` | Deactivate warehouse |

---

## 7. Inventory Stock Rules

### Available stock

```text
availableQuantity = quantity - reservedQuantity
```

### Status

```text
availableQuantity == 0       -> OUT_OF_STOCK
availableQuantity <= 5       -> LOW_STOCK
availableQuantity > 5        -> IN_STOCK
```

### Add stock

```text
quantity = quantity + requestedQuantity
```

### Reduce stock

Stock can only be reduced when enough **available** stock exists.

```text
availableQuantity >= requestedQuantity
```

### Reserve stock

Reservation increases `reservedQuantity` without reducing physical `quantity`.

```text
reservedQuantity = reservedQuantity + requestedQuantity
```

### Release stock

Release decreases `reservedQuantity` and returns the quantity to the available pool.

```text
reservedQuantity = reservedQuantity - requestedQuantity
```

### Update/delete safeguards

- Physical quantity cannot become lower than the currently reserved quantity.
- Inventory with reserved stock cannot be deleted.
- Inactive warehouses cannot receive newly created inventory records.

---

# 8. Product → Inventory Event Flow

Product and Inventory are separate services with separate databases.

```text
                  Product Service
                       |
                       v
                  Product DB
                       |
                       v
                product-created
                    event
                       |
                     Kafka
                       |
                       v
              Inventory Service
                       |
                       v
                Inventory DB
```

When a product-created event contains variants, Inventory initializes an inventory record for each variant and each active warehouse where that combination does not already exist.

Current event contract:

```text
ProductCreatedEvent
├── productId
├── name
├── status
└── variants[]
      ├── variantId
      ├── sku
      ├── packSize
      ├── unit
      └── price
```

This keeps Product responsible for the product catalog and Inventory responsible for availability/stock.

---

# 9. Inventory Security Flow

Inventory APIs use JWT authentication.

```text
Client
  |
  | Authorization: Bearer <JWT>
  v
API Gateway
  |
  v
Inventory Service
  |
  +--> JWT validation
  |
  +--> Spring Security
  |
  v
Inventory Controller
```

Inventory validates the JWT locally rather than calling Auth Service for every request.

Read and write operations are protected through Spring Security.

---

# 10. Error Handling

Inventory uses a global exception handler and project-specific domain exceptions.

Common responses:

| Situation | HTTP status |
|---|---:|
| Inventory not found | 404 |
| Warehouse not found | 404 |
| Duplicate inventory | 409 |
| Duplicate warehouse | 409 |
| Insufficient stock | 409 |
| Inactive warehouse | 409 |
| Invalid inventory operation | 409 |
| Invalid request/validation | 400 |

---

# 11. Concurrency

Inventory uses JPA optimistic locking through the `version` field.

```text
@Version
private Long version;
```

This protects inventory updates from silently overwriting concurrent changes.

---

# 12. Database Ownership

Each service owns its own database/schema.

```text
Product Service   -> noshop_product_db
Auth Service      -> noshop_auth_db
Inventory Service -> noshop_inventory_db
```

Services should communicate through APIs/events rather than directly querying another service's database.

---

# 13. Docker Infrastructure

The project uses Docker Compose for infrastructure dependencies.

Current infrastructure includes:

- Redis
- Kafka

Typical command:

```bash
docker compose up -d
```

Check containers:

```bash
docker ps
```

---

# 14. Build

From the repository root:

```bash
mvn clean install
```

To build without tests when needed during development:

```bash
mvn clean install -DskipTests
```

---

# 15. Javadocs

Public service contracts and REST controllers are documented with JavaDoc comments.

Examples include:

- Product service operations
- Inventory service operations
- Inventory REST endpoints
- Product REST endpoints

The documentation is intended to make the codebase easier to understand and maintain without replacing the detailed interview walkthrough planned after project completion.

---

# 16. Development Order

Current high-level development sequence:

```text
Product Service
      |
      v
Inventory Service
      |
      v
Product ↔ Inventory integration
      |
      v
Backend end-to-end testing
      |
      v
Angular customer/admin UI
      |
      v
Docker/containerization
      |
      v
AWS architecture/deployment
```

Order Service and Payment Service are intentionally not implemented in this project. Their architecture and integration will be covered later as interview knowledge rather than as project services.

---

# 17. Project Status

### Product Service

- Product domain and CRUD: completed
- Pagination: completed
- Filtering and sorting: completed
- Product search: completed
- Product status management: completed
- Product variants/pack sizes: completed
- Product images/S3 integration: implemented; CDN work pending
- Redis caching: implemented; end-to-end testing pending
- Kafka product events/outbox: implemented; end-to-end testing pending

### Inventory Service

- Inventory domain: completed
- Warehouse domain: completed
- Inventory CRUD: completed
- Stock add/reduce: completed
- Stock reservation/release: completed
- Stock status calculation: completed
- Validation/error handling: completed
- JWT security: implemented
- Kafka product-created consumer: implemented
- Warehouse seed data: implemented
- End-to-end testing: pending for the project-wide testing phase

---

## 18. Important URLs at a Glance

```text
Eureka
http://localhost:8761

Admin
http://localhost:9090

Gateway
http://localhost:8080

Product Swagger
http://localhost:8082/swagger-ui/index.html

Product OpenAPI
http://localhost:8082/v3/api-docs

Inventory Swagger
http://localhost:8083/swagger-ui/index.html

Inventory OpenAPI
http://localhost:8083/v3/api-docs
```
