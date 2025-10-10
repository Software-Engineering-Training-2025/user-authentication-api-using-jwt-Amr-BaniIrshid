# JWT Auth Server (Spring Boot + PostgreSQL)

A modular authentication service built with Spring Boot that issues stateless **JWT access tokens** and **httpOnly refresh tokens (cookies)**, with support for email verification and PostgreSQL persistence. Designed to be embedded in a microservices ecosystem or used as a standalone auth backend. 

## Contents Covered

* Overview & features
* Architecture (modules & data)
* Local development (PostgreSQL, Maven, Docker)
* Configuration (env vars)
* Run, test, common tasks
* Security model (JWT + refresh cookie)
* Troubleshooting & tips
* Roadmap / TODO

---

## Features

* **JWT access tokens** for APIs; **refresh token** stored as secure, `HttpOnly`, `SameSite=Strict` cookie with 7-day lifetime.
* **Email verification workflow** (templated emails + verification link).
* **PostgreSQL 16** compatible schema and configs (multiple DBs discovered locally by IDE integrations).
* Ready for **containerization** and CI use (normalized line endings, IDE ignores, Maven wrapper). 

---

## Architecture

### Tech Stack

* **Java** (Spring Boot), **Maven** build
* **PostgreSQL**
* **JWT** (access) + **httpOnly cookies** (refresh)
* Email sender (SMTP)

> IntelliJ config shows module name `jwtAuthServer` and annotation processing enabled for the project. 

### High-Level Flow

1. **Register** → create user (unverified) → send **verification email** with signed link.
2. **Verify** → mark user verified → allow login.
3. **Login** → return short-lived **access JWT** + set `refresh_token` cookie (`HttpOnly`, `Secure`, `SameSite=Strict`, path `/`, max-age 7d).
4. **Refresh** → read `refresh_token` cookie → mint new access token (and typically rotate refresh).
5. **Logout** → clear/expire `refresh_token` cookie (and/or revoke server-side if you maintain a token store/denylist).

> The cookie attributes and a 7-day max age are part of the current implementation. 

---

## Local Development

### Prerequisites

* **JDK 17+**
* **Maven 3.9+**
* **PostgreSQL 16** running locally

### Quick Start (Postgres local)

```bash
# 1) Create a database (example)
createdb authserver

# 2) Set env vars (see Configuration)
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/authserver
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres

# 3) Run the service
./mvnw spring-boot:run
```


> The project metadata includes multiple local Postgres databases (e.g., `authserver`, `jwt_security`, etc.) discovered via IDE data source inspection; you can use `authserver` for this service. 

---

## Configuration

Set these via environment variables or `application.yml`:

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
    username: 
    password: 

    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true


jwt:
  access-secret: 
  refresh-secret: 
  expiration:     
  refresh-expiration: 



```

**Required env vars (suggested):**

* `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
* `JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET`
* `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`

---

## Build, Run, Test

```bash
# Build (skip tests if needed)
./mvnw -DskipTests package

# Run
./mvnw spring-boot:run

# Unit tests
./mvnw test
```

**IDE Notes**

* `.idea`, `.iml`, `/target`, etc., are ignored; Maven wrapper and LF/CRLF settings are normalized via `.gitattributes`. 

---

## HTTP API (baseline)

> Endpoints vary by controller package. Use this as a reference template and adjust to actual paths:

```
POST /auth/register        # body: email, password, ... → sends verification email
GET  /auth/verify?token=   # email verification callback
POST /auth/login           # sets refresh cookie + returns access JWT
POST /auth/refresh         # uses refresh cookie → returns new access JWT
POST /auth/logout          # clears refresh cookie
GET  /test/public                  # protected (requires Bearer access JWT)
GET  /test/user                  # protected (requires Bearer access JWT )
GET  /test/admin                  # protected (requires Bearer access JWT && ADMIN ROLE)

```

**Auth headers & cookies**

* Access: `Authorization: Bearer <access.jwt>`
* Refresh: `refresh_token` cookie (HttpOnly, Secure, SameSite=Strict, Path=/, MaxAge=7d). 

---

## Security Model (JWT + Refresh Cookie)

* **Access JWT**: short-lived, carried in `Authorization` header.
* **Refresh cookie**: **never** exposed to JS; only sent over HTTPS due to `Secure` + `HttpOnly`.
* **Rotation** : issue a new refresh token on every refresh and **revoke** the old one (server-side store or token versioning).
* **Email verification gate**: block login until `email_verified = true` to reduce account fraud.

---

## Troubleshooting

* **Formatter/templating exceptions** (email templates): check template placeholders and `String.format` specifiers in your template loader and email service.
* **`SameSite=Strict` cookie not sent** on cross-site flows: during local front-end testing, consider `SameSite=Lax` or same-site domain mapping, but revert to `Strict`/`Lax` in production as needed.
* **Missing schema**: ensure the DB exists and credentials are correct; use `ddl-auto=update` for initial dev bootstrapping, then migrate to Flyway/Liquibase.

---

## Contributing

* Use feature branches and conventional commits.
* Add tests for business logic (token rotation, email verification).
* Keep secrets out of the repo; use env vars or a secrets manager.

---

## Roadmap / TODO

* [ ] Flyway/Liquibase migrations
* [ ] Refresh-token denylist/versioning
* [ ] Rate limiting for auth endpoints
* [ ] Password reset flow
* [ ] OpenAPI (Swagger) docs
* [ ] Dockerfile & docker-compose for one-command dev

---

