# Multitenant Starter

This project is a simplified starter built from the tenancy pattern used in the main ARTrail codebase.

## Modules

- `common-utils`: shared tenant context, routing datasource, filters, constants, and tenant property files
- `gateway`: Spring Cloud Gateway with JWT validation, tenant forwarding, UI testing, and proxy routing
- `auth-service`: tenant-aware authentication service that stores users and validates username/password
- `microservice-1`: sample business service backed by per-tenant datasources

## Tenancy model

- Tenant is accepted from `X-TenantID` header or cookie.
- Tenant is stored in `TenantContext`.
- Datasource routing uses `AbstractRoutingDataSource`.
- Tenant properties are loaded from `classpath*:tenants/*.properties`.
- Gateway authentication is intentionally simple: `Authorization: Bearer <jwt>`

## Default sample tenants

- `tenant_alpha`
- `tenant_beta`

## Run order

1. Start `auth-service`
2. Start `microservice-1`
3. Start `gateway`
4. Register or login through the gateway UI, then call gateway routes with `X-TenantID` and `Authorization: Bearer <jwt>`

## Ports

- `gateway`: `8007`
- `auth-service`: `8006`
- `microservice-1`: `8008`

## Environment

- `JWT_SECRET`: secret key used by the gateway to sign and validate JWTs

## Example

```http
GET http://localhost:8007/microservice-1/api/messages/current
X-TenantID: dGVuYW50X2FscGhh
Authorization: Bearer <your-jwt>
```

The header value is Base64 for `tenant_alpha`.
