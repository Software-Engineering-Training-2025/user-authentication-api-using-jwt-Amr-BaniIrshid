#  jwtAuthServer — Spring Boot JWT Authentication Service

A secure, production-ready **authentication microservice** built with **Spring Boot 3 + Spring Security 6**. 
It implements **JWT access tokens**, **hashed refresh tokens with revoke-on-use rotation**, and an **email verification flow** enforced at the **filter chain** level.

---

## Features

- **Stateless JWT auth** with short-lived access tokens and long-lived refresh tokens
- **Refresh token rotation (revoke-on-use)** — prevents replay of old refresh tokens
- **Multiple concurrent sessions** per user (Chrome, Firefox, Mobile, etc.)
- **Hashed refresh tokens stored in DB** (no plaintext at rest)
- **Email Verification** enforced by `EmailVerificationFilter` before controller access
- **Centralized error handling** via domain-specific exceptions (e.g., `InvalidTokenException`, `VerificationTokenExpiredException`)
- **Clean layering** (Controller → Facade → Services → Repositories)
- **Cookie utilities** for secure refresh token delivery (HttpOnly, Secure, SameSite=Strict)

---

## Project Structure (actual)

```
├── com/
├── securityProject/
  ├── jwtAuthServer/
    └── JwtAuthServerApplication.java
    ├── config/
      └── ApplicationConfiguration.java
      └── SecurityConfiguration.java
    ├── controller/
      └── AuthController.java
    ├── dto/
      ├── login/
        └── LoginRequest.java
        └── LoginResponse.java
      ├── refresh/
        └── RefreshResponse.java
      ├── register/
        └── RegisterRequest.java
        └── RegisterResponse.java
    ├── entity/
      └── EmailVerificationToken.java
      └── RefreshToken.java
      └── User.java
    ├── enums/
      └── Role.java
      └── TokenType.java
    ├── exception/
      └── ErrorResponse.java
      └── GlobalExceptionHandler.java
      ├── api/
        └── DuplicateEmailException.java
        └── EmailNotVerifiedException.java
        └── InternalServerException.java
        └── InvalidCredentialsException.java
        └── InvalidTokenException.java
        └── InvalidVerificationTokenException.java
        └── MissingTokenException.java
        └── RefreshTokenRevokedException.java
        └── TokenExpiredException.java
        └── UserNotFoundException.java
        └── VerificationTokenExpiredException.java
        ├── base/
          └── ApiException.java
    ├── filter/
      └── EmailVerificationFilter.java
      └── JwtAuthFilter.java
      └── LogoutFilter.java
    ├── repository/
      └── EmailVerificationTokenRepository.java
      └── RefreshTokenRepository.java
      └── UserRepository.java
    ├── service/
      ├── auth/
        └── AuthFacade.java
      ├── email/
        └── EmailSenderService.java
        └── EmailTemplateLoader.java
        └── EmailVerificationService.java
      ├── jwt/
        ├── core/
          └── JwtKeyProvider.java
          └── JwtService.java
          └── TokenFactory.java
        ├── strategy/
          └── AccessTokenStrategy.java
          └── RefreshTokenStrategy.java
          └── TokenStrategy.java
      ├── login/
        └── LoginService.java
      ├── logout/
        └── LogoutService.java
      ├── refreshToken/
        └── RefreshTokenRepoService.java
        └── RefreshTokenService.java
      ├── register/
        └── RegistrationService.java
    ├── test/
      └── TestController.java
    ├── util/
      └── CookieUtil.java
      └── TokenExtractor.java
      └── TokenHashUtil.java
```

> Root package: `com.securityProject.jwtAuthServer`

**Notable packages**

- `config/` → `SecurityConfiguration`, `ApplicationConfiguration`
- `controller/` → `AuthController`
- `filter/` → `JwtAuthFilter`, `EmailVerificationFilter`, `LogoutFilter`
- `service/auth/` → `AuthFacade` (orchestration of register/login/refresh/verify)
- `service/register/` → `RegistrationService`
- `service/login/` → `LoginService`
- `service/refreshToken/` → `RefreshTokenService`, `RefreshTokenRepoService`
- `service/email/` → `EmailVerificationService`, `EmailSenderService`, `EmailTemplateLoader`
- `service/jwt/core|strategy` → `JwtService`, strategies for access/refresh
- `repository/` → `UserRepository`, `RefreshTokenRepository`, `EmailVerificationTokenRepository`
- `dto/` → feature-scoped DTOs (`login/`, `register/`, `refresh/`)
- `entity/` → `User`, `RefreshToken`, `EmailVerificationToken`
- `exception/api` → rich domain exceptions and base API exception types
- `util/` → `CookieUtil`, `TokenExtractor`, `TokenHashUtil`
- `test/` → `TestController` for quick role-based checks

---

## Security & Filter Chain

Implemented in `config/SecurityConfiguration.java`:

- `SessionCreationPolicy.STATELESS`
- Request authorization rules:
  - Permit: `/auth/**`, `/verify/**`
  - `ROLE_ADMIN` → `/admin/**`
  - `ROLE_USER` or `ROLE_ADMIN` → `/user/**`
- Filters ordering:
  1. `JwtAuthFilter` (extract/validate token, set SecurityContext)
  2. `EmailVerificationFilter` (deny unverified users early)
  3. `LogoutFilter` (custom logout handling)

> Enforcing **email verification** as a filter makes it an invariant — no controller can accidentally bypass it.

---

## Token Lifecycle

### 1) Login → Issue Tokens
- Valid credentials → returns **access_token** (short TTL) and **refresh_token** (long TTL).
- Optionally, the refresh token can be returned via a **secure HttpOnly cookie** using `CookieUtil.addRefreshToCookie()`.

### 2) Refresh → Rotate & Revoke
- Client sends **refresh token** to `/auth/refresh`.
- Server validates the token (claims + signature), loads the user, and checks DB state.
- **Old refresh token is revoked immediately** (revoke-on-use).
- New **access + refresh** pair is issued.
- If a revoked/expired/missing token is presented → mapped to a specific API exception.

### 3) Email Verification
- After registration, a verification email is sent with a token.
- A dedicated verification endpoint (under `/verify/**`) calls `EmailVerificationService` to mark the user as verified.
- `EmailVerificationFilter` blocks requests from unverified accounts before reaching controllers.

---

##  API (discovered from code)

> Base path: `/auth` (see `AuthController`)

| Endpoint | Method | Description |
|---|---|---|
| `/auth/register` | `POST` | Register new user → sends verification email |
| `/auth/login` | `POST` | Login with email/password → returns token pair |
| `/auth/refresh` | `POST` | Refresh access token (revoke-on-use rotation) |
| `/verify?token=...` | `GET` | Verify email token (permitted route) |

> Additionally, a `TestController` exists under `/test/**` with role-protected endpoints (e.g., `/test/admin`).

**DTOs**
- `dto/login` → `LoginRequest`, `LoginResponse`
- `dto/register` → `RegisterRequest`, `RegisterResponse`
- `dto/refresh` → `RefreshResponse`

---

## Configuration (excerpt)

> `src/main/resources/application.yml` (present in your repo)

```yaml
spring:

  datasource:
    url: jdbc:postgresql://localhost:5432/authserver
    username: postgres
    password: 1234
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        format_sql: true
    database: postgresql
    database-platform: org.hibernate.dialect.PostgreSQLDialect

  mail:
    host: smtp.gmail.com
    port: 587
    username: yourEmail@gmail.com
    password: appPassowrd

    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true


jwt:
  access-secret: 9m2a3m8a4m0alk218i12kf95anf82a4d1lsb4e3d2jsf3a586a7b5e82kj91h2m5
  refresh-secret: 7b5a3d244a3e646f7a556b584e327a4d6c5b4e3d445f3a586a7b5e326e756e74
  expiration: 900000        # 15 minutes
  refresh-expiration: 604800000 # 7 days
```

> **Production tip:** externalize secrets via environment variables (`JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET`, DB creds). Disable `ddl-auto: create-drop` in prod.

---

## Tech Stack

- **Spring Boot 3**, **Spring Security 6**
- **PostgreSQL** + Spring Data JPA
- **JJWT** strategies for access/refresh tokens
- **JavaMailSender** for emails
- **Lombok** for boilerplate reduction

---

## Quick Test Endpoints

- `GET /test/admin` (requires `ROLE_ADMIN`) — verifies the full JWT + verification filter chain.
- `GET /test/user` (if present) — for `ROLE_USER`/`ROLE_ADMIN` checks.

---

## Local Development

1. Start PostgreSQL and create DB `authserver` (or adjust URL envs).
2. Export secrets:
   ```bash
   export JWT_ACCESS_SECRET=...
   export JWT_REFRESH_SECRET=...
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/authserver
   export SPRING_DATASOURCE_USERNAME=postgres
   export SPRING_DATASOURCE_PASSWORD=yourpass
   ```
3. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Hit the endpoints:
   - `POST /auth/register`
   - `POST /auth/login`
   - `POST /auth/refresh`
   - `GET  /verify?token=...`

---

## Design Principles

- **Statelessness:** no HTTP session; rely on tokens
- **Defense-in-depth:** security invariants as filters
- **Feature-scoped DTOs and services:** clearer ownership
- **Explicit exceptions:** consistent error envelopes
- **Separation of concerns:** auth domain isolated (ready to sit behind a real API gateway)

---

## Roadmap Ideas

- Audit log for **revoked-token reuse** attempts (security analytics)
- `/sessions` endpoint to list/revoke active refresh tokens
- OpenAPI/Swagger documentation
- Profile-based mail configs and prod `ddl-auto` policy
- Optionally, Redis cache for token blacklists (if needed)

---

## Mini Glossary

- **Revoke-on-use rotation:** Invalidate a refresh token immediately after it’s used to get a new pair, blocking replay.
- **EmailVerificationFilter:** A filter that denies access for unverified accounts before controllers are reached.
- **Facade Pattern (AuthFacade):** Encapsulates multi-step flows like register → send mail → persist → issue tokens.
- **JWT Access vs Refresh:** Access is short-lived for requests; Refresh is long-lived to obtain new access tokens.

---

## Summary & Key Takeaways

- This service implements a **modern, secure** JWT auth flow with **hashed, revocable refresh tokens**.
- **Email verification** is enforced at the **filter chain**, not just in controllers.
- Architecture is **clean and extendable**: controllers are thin, business logic lives in services/facade.
- It supports **multiple concurrent sessions** per user while remaining secure against token replay.
- Ready to be placed **behind a dedicated API Gateway** in a microservices setup.

---

**Author:** Amr Bani Irshid  
Fourth-year CS student • Backend & Security Enthusiast  
Focus: scalable Spring Boot architectures and practical security.
