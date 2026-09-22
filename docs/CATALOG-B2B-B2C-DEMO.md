# Product Catalog B2B/B2C Demo

This branch adds two contained Product Catalog capabilities:

1. Role-aware catalog visibility
2. Product substitute recommendations

## Local seed data

Run Product Service with the local Spring profile to seed 30 grocery products, including products tagged as B2C, B2B, and BOTH.

Run Auth Service with the local profile to create the demo B2B account:

Email: b2b.demo@noshop.local
Password: ChangeMe123!

Normal registration continues to create ROLE_USER accounts, which are treated as B2C for catalog reads.

## Role-aware catalog

Public or ROLE_USER reads return products whose audience is B2C or BOTH.

A ROLE_B2B JWT returns B2B or BOTH.

A ROLE_ADMIN JWT can see all audiences.

The audience is resolved from the authenticated JWT role; there is no user-controlled role query parameter.

Examples:

GET /api/v1/product/products?page=0&size=20

GET /api/v1/product/products/2

## Substitute recommendations

GET /api/v1/product/products/{productId}/substitutes?limit=3

The service:

- verifies that the requested product is visible to the current customer segment
- looks for active products in the same category
- keeps candidates visible to the same segment
- reads the minimum active variant price for each candidate
- ranks candidates by absolute price difference
- returns the top N results

Inventory is intentionally not part of this feature.

## Example B2B flow

1. Log in with the seeded B2B account and copy the JWT.
2. Call the product listing API with Authorization: Bearer <token>.
3. Compare results with an unauthenticated or ROLE_USER request.
4. Pick a visible product and call its substitutes endpoint.

The product cache key includes the resolved catalog audience so a B2B response cannot be reused for a B2C request.
