# secure-api

Lightweight REST API built with Spring Boot and MongoDB that demonstrates user and product management secured with JWT authentication (cookies) and role-based authorization.

## What this repository contains

- A Spring Boot application (`secure-api`) that exposes REST endpoints for users and products.
- JWT-based authentication where tokens are stored in a secure HttpOnly cookie named `jwt`.
- Role-based access control (USER vs ADMIN) and ownership checks for product updates/deletes.
- Basic rate limiting via Bucket4J (configurable in `application.properties`).
- HTTPS configured using a bundled `keystore.p12` (development self-signed certificate).

## Quick facts

- Java: configured for Java 24 in the POM (`<java.version>24</java.version>`).
- Build: Maven (Spring Boot Maven Plugin).
- Database: MongoDB (default URI is `mongodb://localhost:27017/user_product_db`).

## Prerequisites

- JDK 24 (matches the project POM). If you cannot use 24, adjust the `pom.xml` and `maven-compiler-plugin` settings accordingly.
- Apache Maven 3.8.x+
- MongoDB running locally (default port 27017) or update `spring.data.mongodb.uri` in `src/main/resources/application.properties`.
- Optional: Postman or curl for testing requests.

## Configuration notes

Open `src/main/resources/application.properties`. Important properties include:

- `spring.data.mongodb.uri` — MongoDB connection string (default: `mongodb://localhost:27017/user_product_db`).
- `jwt.secret` — JWT signing secret shown in the file. NOTE: the `JwtTokenProvider` currently reads `app.jwt.secret` (property name mismatch). For correct runtime configuration either:
    - set `app.jwt.secret` in `application.properties` to match the provider, or
    - change `JwtTokenProvider` to read `jwt.secret`.
- HTTPS is enabled by default in this project. The app uses `server.ssl.key-store=classpath:keystore.p12` with a default password in the example properties. Since cookies are set with `cookie.setSecure(true)`, the `jwt` cookie will be sent only over HTTPS.

Bucket4J rate limiting is controlled via `bucket4j.enabled` and filter entries in the properties file.

## Build and run

From the project root:

```bash
mvn clean package
```

Run the generated JAR (artifactId: `secure-api`, version `0.0.1-SNAPSHOT`):

```bash
java -jar target/secure-api-0.0.1-SNAPSHOT.jar
```

If you keep the bundled keystore and default SSL settings, the server will start on `https://localhost:8080`.

For local testing (self-signed cert):

- Use Postman and accept the untrusted certificate, or
- Use curl with `-k` to ignore certificate validation (not recommended for production).

## Important endpoints

Authentication (cookie-based JWT)

- POST /api/auth/register
    - Register a new user.
    - Body: { "name": "Name", "email": "user@example.com", "password": "secret" }
    - Returns: 201 Created (plain message)

- POST /api/auth/login
    - Login with credentials. On success the server sets an HttpOnly, Secure cookie named `jwt`.
    - Body: { "email": "user@example.com", "password": "secret" }
    - Returns: 200 OK and cookie in response.

- POST /api/auth/logout
    - Clears the JWT cookie.

User endpoints

- GET /api/users               (ADMIN only)
- GET /api/users/me            (authenticated user)
- PUT /api/users/{id}          (ADMIN can update user info)
- PUT /api/users/me            (authenticated user updates own info)
- DELETE /api/users/me         (authenticated user deletes their account)

Product endpoints

- GET /api/products            (public)
- GET /api/products/my-products (authenticated; returns products owned by current user)
- POST /api/products           (authenticated; creates product with current user's id)
- PUT /api/products/{id}       (ADMIN or owner only — enforced via @PreAuthorize)
- DELETE /api/products/{id}    (ADMIN or owner only)

Notes on authorization:

- Method-level security uses `@PreAuthorize` in controllers. Some checks use Spring EL to compare the authenticated user's id to the product's owner id.

## How authentication works (summary)

- The app authenticates users via username/password (email is used as username).
- On successful authentication the `JwtTokenProvider` creates a signed JWT and `JwtAuthenticationFilter` reads the token from a cookie named `jwt` (fallback: Authorization header).
- `JwtAuthenticationFilter` validates the token, loads the user via `CustomUserDetailsService`, and sets the Spring Security context.
- Cookies are HttpOnly and Secure; the Secure flag requires HTTPS for the browser to include the cookie.

## Testing with curl (examples)

Register:

```bash
curl -k -X POST https://localhost:8080/api/auth/register \
    -H "Content-Type: application/json" \
    -d '{"name":"Alice","email":"alice@example.com","password":"secret"}'
```

Login (this returns a cookie; curl can store it in a jar file):

```bash
curl -k -c cookies.txt -X POST https://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"alice@example.com","password":"secret"}'

# Use the stored cookie for authenticated requests
curl -k -b cookies.txt https://localhost:8080/api/products/my-products
```

## Troubleshooting & tips

- If cookies are not being sent from your browser, ensure you are using HTTPS (cookie Secure flag) and that the domain/port match.
- If authentication fails after changing email/password, the controllers attempt to re-authenticate and issue a fresh cookie; check logs for authentication errors.
- To disable HTTPS in development quickly, remove or comment out the `server.ssl.*` properties in `application.properties` and adjust `SecurityConfig` (it currently enforces `requiresSecure()`).
- Fix the JWT property name mismatch by setting `app.jwt.secret` in `application.properties` or updating `JwtTokenProvider` to read `jwt.secret`.

## License & attribution

This project is for learning/demo purposes. Review dependencies and hard-coded secrets before using in production.

---

If you'd like, I can also:

- update `application.properties` to add `app.jwt.secret` (quick fix), or
- change `JwtTokenProvider` to read the `jwt.secret` property instead.

Which would you prefer?