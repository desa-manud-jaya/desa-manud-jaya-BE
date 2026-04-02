# AGENTS.md — Desa Manud Jaya Backend Guide

This document defines implementation conventions for contributors and AI/code agents.

## 1) Architecture Overview
- **Framework:** Spring Boot 3, Java 17, MongoDB.
- **Layers:**
  - `controller/` → HTTP API layer
  - `service/` → business logic
  - `repository/` → Mongo access
  - `model/entity/` → persisted document models
  - `model/inbound/request|response/` → API DTOs
  - `configuration/` → security, Swagger, infra wiring

## 2) Security Rules (Critical)
1. **Never trust client-provided vendor identity** (header/path/body) for ownership.
2. Vendor identity must come from JWT auth principal (`Authentication.getName()`).
3. For vendor-owned resources:
   - resolve current user from username,
   - resolve target business/resource,
   - enforce `resource.vendorId == authenticatedVendor.id`.
4. Keep admin endpoints under `/admin/**` and vendor endpoints under `/vendor/**`.
5. Swagger endpoints are public:
   - `/v3/api-docs/**`
   - `/swagger-ui/**`
   - `/swagger-ui.html`

### Temporary exception (project decision)
- `GET /vendor/{vendorId}/business` is currently kept as-is for compatibility with existing frontend behavior.
- This endpoint is an explicit exception to rule #1/#2 above and must be treated as technical debt to be revisited later.

## 3) Domain Conventions
### Auth model scope (current)
- Roles remain `ADMIN`, `USER`, `VENDOR` only for now.
- No `CS` role implementation in current phase.
- Login identifier remains **username + password** for now.
- Registration identity must be globally unique across all roles:
  - `username` cannot be reused between USER/VENDOR/ADMIN accounts.
  - `email` cannot be reused between USER/VENDOR/ADMIN accounts.
  - Duplicate registration attempts must fail with HTTP `409 Conflict`.

### Vendor status lifecycle
- `PLEASE_FILL_PROFILE` → after vendor registration
- `PENDING` → after vendor completes profile form
- `APPROVED` / `REJECTED` → after admin review

### Business rules
- **One vendor = one business (1:1).**
- On admin vendor approval:
  - vendor becomes approved/active,
  - business is auto-created and marked approved.

### Package & destination moderation
- New package/destination starts as `PENDING`.
- Admin can transition to `APPROVED` or `REJECTED`.
- Package rejection reason is required and must be visible to vendor.

## 4) API Rules
1. Public APIs only expose approved data.
2. Vendor APIs may show own `PENDING/REJECTED` items.
3. Keep endpoint naming stable and REST-like.
4. For multipart package creation:
   - `data` (JSON request part, content-type must be `application/json`)
   - `requirementDocument` (file)
   - `photo` (file)

### Public approved-listing endpoints (current)
- `GET /destinations/approved` → returns all approved destinations.
- `GET /packages/approved` → returns all approved packages.
- `GET /packages/approved/with-destinations` → returns one payload containing both approved packages and approved destinations.
  - Supports optional pagination via `page` and `size`.
  - `page` and `size` must be provided together (if only one is sent, return HTTP `400`).
  - Response includes `page`, `size`, `totalPackages`, and `totalDestinations` metadata.

## 5) New Module Conventions (Implemented)
### Package lifecycle expansion
- Vendor package endpoints now support:
  - update package,
  - submit deletion request,
  - filtered/paginated listing.
- Admin package endpoints now support:
  - package detail for review,
  - deletion request queue,
  - deletion approve/reject actions.
- Package entity includes moderation metadata (`updatedAt`, deletion request status/review info, moderation notes).

### Vendor document verification
- `VendorDocument` is the canonical document verification entity.
- Document upload validation:
  - Allowed: PDF/JPG/JPEG/PNG
  - Compatibility: `application/octet-stream` is accepted for PDF when filename ends with `.pdf` (Swagger/client fallback)
  - Max size: 10 MB
- Vendor supports upload/list/progress APIs.
- Admin supports pending queue and approve/reject APIs.

### Analytics and approval center
- Dashboard analytics endpoints exist for `/vendor/dashboard/analytics` and `/admin/dashboard/analytics`.
- Impact analytics endpoint exists for `/vendor/impact-analytics`.
- Consolidated admin queue endpoint exists at `/admin/approval-center` for partner/package/deletion/document requests.

### Audit logging
- Moderation actions are persisted into `moderation_audit_logs`.
- Prefer logging all approve/reject/request-d-eletion/document-review operations.

### Booking transactions and revenue
- Canonical booking transaction collection: `transactions` (`BookingTransaction` entity).
- Transaction must keep relations to both `User` and `Business` (id fields + Mongo references).
- Booking payment lifecycle status is lowercase and follows:
  - `waiting_for_payment` → right after booking creation
  - `pending` → after user uploads payment proof
  - `approved` / `rejected` → after admin review decision
- User booking history endpoint is `/user/bookings/{userId}` and must enforce self-access only (JWT principal must match `userId`).
  - This endpoint is the source for user-side booking progress tracking (including unfinished payment states).
- User payment proof upload endpoint is `/user/bookings/{bookingId}/payment-proof`.
  - Allowed when status is `waiting_for_payment`, then status transitions to `pending`.
- Admin payment moderation endpoints:
  - `GET /admin/bookings/payment` (list, optional status filter)
  - `GET /admin/bookings/payment/{bookingId}` (detail)
  - `PATCH /admin/bookings/payment/{bookingId}/decision` (approve/reject)
- Vendor bookings endpoint `/vendor/bookings` must only expose `approved` booking transactions.
- Admin revenue endpoint is `/admin/revenue/summary` with optional `startDate` + `endDate` filter in `yyyy-MM-dd` format.

## 6) Swagger / OpenAPI Rules
1. Every new endpoint must include OpenAPI annotations:
   - `@Operation`
   - `@ApiResponse` / `@ApiResponses`
   - `@Tag`
2. Protected controllers should include `@SecurityRequirement(name = "bearer-jwt")`.
3. Swagger docs must reflect real behavior (status transitions, required fields, access constraints).
4. Canonical Swagger/OpenAPI URLs must use **HTTPS**:
   - **Dev Swagger UI:** `https://desa-manud-jaya-backend-dev.up.railway.app/swagger-ui/index.html`
   - **Dev OpenAPI docs:** `https://desa-manud-jaya-backend-dev.up.railway.app/v3/api-docs`
   - **Prod Swagger UI:** `https://desa-manud-jaya-backend.up.railway.app/swagger-ui/index.html`
   - **Prod OpenAPI docs:** `https://desa-manud-jaya-backend.up.railway.app/v3/api-docs`

## 7) Environment & Deployment Profiles
Use Spring profiles and environment-variable separation:

- `application-local.yml` → `LOCAL_*`
- `application-dev.yml` → `DEV_*`
- `application-prod.yml` → `PROD_*`

### Recommended CI/CD mapping
- **dev branch** deploy job:
  - `SPRING_PROFILES_ACTIVE=dev`
  - inject `DEV_*` secrets/vars
- **main branch** deploy job:
  - `SPRING_PROFILES_ACTIVE=prod`
  - inject `PROD_*` secrets/vars

### GitHub Actions implementation (current)
- Workflow file: `.github/workflows/ci-cd.yml`
- Branch/profile mapping is set in job `env`:
  - `SPRING_PROFILES_ACTIVE: ${{ github.ref_name == 'main' && 'prod' || 'dev' }}`
- Deployment environment is selected dynamically:
  - `environment: ${{ github.ref_name == 'main' && 'prod' || 'dev' }}`

### Railway target topology (verified)
- **Dev backend URL:** `https://desa-manud-jaya-backend-dev.up.railway.app`
- **Prod backend URL:** `https://desa-manud-jaya-backend.up.railway.app`

### Canonical resource mapping (verified)
- **Mongo cluster host (shared):** `cluster0.lkpsu9n.mongodb.net`
- **Dev Mongo database name:** `manud-jaya-dev`
- **Prod Mongo database name:** `manud-jaya`
- **Supabase project URL (shared):** `https://kvnsbatzpsbrocyinnpy.supabase.co`
- **S3 endpoint (shared):** `https://kvnsbatzpsbrocyinnpy.storage.supabase.co/storage/v1/s3`
- **Dev bucket name:** `Object-Image-Dev`
- **Prod bucket name:** `Object-Image`

### Mongo URI pattern (must include DB name by environment)
- **Dev:** `mongodb+srv://<user>:<pass>@cluster0.lkpsu9n.mongodb.net/manud-jaya-dev?retryWrites=true&w=majority&authSource=admin`
- **Prod:** `mongodb+srv://<user>:<pass>@cluster0.lkpsu9n.mongodb.net/manud-jaya?retryWrites=true&w=majority&authSource=admin`

### Where to put secrets in GitHub
1. Open repository **Settings** → **Environments**.
2. Create environment **dev** and add these secrets:
   - `DEV_JWT_SECRET`
   - `DEV_MONGODB_URI` (DB name must be `manud-jaya-dev`)
   - `DEV_SUPABASE_URL` (same Supabase URL as prod)
   - `DEV_SUPABASE_API_KEY` (can be same key if intentionally shared)
   - `DEV_SUPABASE_BUCKET` = `Object-Image-Dev`
   - `DEV_S3_ENDPOINT` (same endpoint as prod)
   - `DEV_S3_ACCESS_KEY` (can be same key if intentionally shared)
   - `DEV_S3_SECRET_KEY` (can be same key if intentionally shared)
3. Create environment **prod** and add these secrets:
   - `PROD_JWT_SECRET`
   - `PROD_MONGODB_URI` (DB name must be `manud-jaya`)
   - `PROD_SUPABASE_URL`
   - `PROD_SUPABASE_API_KEY`
   - `PROD_SUPABASE_BUCKET` = `Object-Image`
   - `PROD_S3_ENDPOINT`
   - `PROD_S3_ACCESS_KEY`
   - `PROD_S3_SECRET_KEY`

### Railway runtime variables
If Railway performs the actual runtime/deploy, mirror the same variable names in Railway service variables for each environment (dev/prod) and set:
- `SPRING_PROFILES_ACTIVE=dev` in dev service
- `SPRING_PROFILES_ACTIVE=prod` in prod service

Do not commit real secrets into repository files.

## 8) Testing Rules
1. Keep tests aligned with current service/controller signatures.
2. Prefer focused unit tests for service logic and ownership checks.
3. Any change to moderation/status flows must include test coverage.
4. `mvn test` must pass before merge.

## 9) Coding Style Notes
- Use descriptive runtime errors or custom exceptions for domain errors.
- Keep string literals for status centralized via enums where practical.
- Avoid dead imports/unused dependencies in controllers/services.

## 10) Git Operation Policy
1. Do **not** push to remote repositories (including GitHub) unless the user explicitly asks for a push.
2. If code changes are requested, prepare them locally first and wait for explicit approval before any `git push`.

## 11) Implementation Status Snapshot (Apr 2, 2026)
### Implemented in current backend
- Package lifecycle expansion:
  - Vendor update package
  - Vendor deletion request submission
  - Admin deletion request moderation (approve/reject)
  - Package detail support for moderation
- Vendor document verification domain:
  - `VendorDocument` entity + repository + services
  - Vendor upload/list/progress APIs
  - Admin pending queue + approve/reject APIs
- Dashboard and operations APIs:
  - Vendor dashboard analytics + impact analytics
  - Admin dashboard analytics
  - Vendor/Admin bookings and reviews listing APIs
- Unified admin approval center endpoint:
  - partner requests
  - package requests
  - package deletion requests
  - document verification requests
- Moderation audit logging persistence (`moderation_audit_logs`)
- Booking transaction and revenue features (PB-37 to PB-41 + payment moderation extension):
  - `transactions` collection with user/business relations and payment moderation metadata
  - `POST /user/bookings` for booking creation with automatic `waiting_for_payment` status
  - `POST /user/bookings/{bookingId}/payment-proof` to upload proof and move status to `pending`
  - `GET /user/bookings/{userId}` for self booking history
  - `GET /admin/bookings/payment` + `GET /admin/bookings/payment/{bookingId}` for admin visibility
  - `PATCH /admin/bookings/payment/{bookingId}/decision` for approve/reject flow
  - `GET /vendor/bookings` returns only `approved` booking transactions
  - `GET /admin/revenue/summary` for total revenue
  - Date-range filtering via `startDate` + `endDate` with format validation (`yyyy-MM-dd`)
  - Unit tests for booking transaction and vendor operations services
- Public approved listing endpoints:
  - `GET /destinations/approved`
  - `GET /packages/approved`
  - `GET /packages/approved/with-destinations` (supports pagination via `page` and `size`, with totals in response)
- Auth registration duplicate protection:
  - `/auth/register/user` and `/auth/register/vendor` reject duplicated username/email across all roles.
  - Duplicate identity errors are surfaced as HTTP `409 Conflict`.

### Intentionally not implemented in current phase
- CS role introduction
- Login identity migration (still username/password)
- Refactor of `GET /vendor/{vendorId}/business` (explicitly deferred)

### Validation baseline
- `mvn -q -DskipTests compile` passes
- `mvn test -q` passes
