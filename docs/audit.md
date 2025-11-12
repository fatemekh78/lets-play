# secure-api — Audit flow, checklist, and evidence guidance

This document summarizes the program flows, critical control points, recommended audit checks, and sample evidence to collect for the `secure-api` project. Use it as the basis for a security / architecture audit or compliance checklist.

## 1) High-level flows

- User registration (POST /api/auth/register)
  - Controller: `AuthController.register`
  - Validates input (controller-level checks + JSR-380 annotations on DTOs/entities).
  - Passwords are hashed using a `PasswordEncoder` (BCrypt) before saving to `UserRepository`.

- Login (POST /api/auth/login)
  - Controller: `AuthController.authenticateUser`
  - Spring `AuthenticationManager` authenticates username(email)/password using `CustomUserDetailsService` and the underlying `UserRepository`.
  - On success `JwtTokenProvider.generateToken()` signs a JWT and `generateJwtCookie()` issues an HttpOnly, Secure cookie named `jwt`.

- Authenticated requests
  - Filter: `JwtAuthenticationFilter` (OncePerRequestFilter)
  - Reads cookie `jwt` via `WebUtils.getCookie(request, "jwt")` (fallback: Authorization header), validates via `JwtTokenProvider`, then loads user via `CustomUserDetailsService` and sets the `SecurityContext`.

- Product CRUD
  - Controller: `ProductController`
  - GET /api/products — public
  - POST /api/products — requires authentication; server sets product.userId = authenticatedUser.id
  - PUT/DELETE /api/products/{id} — `@PreAuthorize` allows ADMIN or owner (Spring EL comparing repository values)

- User management
  - `UserController` exposes `GET /api/users` (ADMIN only), `GET /api/users/me`, `PUT /api/users/me`, `DELETE /api/users/me`.
  - When email/password are changed, controller re-authenticates to refresh cookie.


## 2) Mermaid diagrams (reference)

- See `docs/flow.mmd` for a flowchart (Mermaid) that illustrates the same flows visually.


## 3) Audit checklist (what to verify)

Authentication & session management

- [ ] JWT signing key
  - Confirm the active property used at runtime: code expects `app.jwt.secret` but `application.properties` defines `jwt.secret`. Verify which is present in the environment and that the secret is long, random, and kept out of source control.
  - Evidence: `application.properties`, environment variables, CI/CD secrets store.

- [ ] Cookie settings
  - Verify `Set-Cookie` flags: HttpOnly=true, Secure=true (code sets these). Confirm SameSite attribute is set (currently not set in code). Cookies used for authentication should include SameSite=Lax or Strict to reduce CSRF risk.
  - Evidence: capture response headers from login request.

- [ ] Transport security
  - `SecurityConfig` sets `requiresSecure()`. Confirm HTTPS is enabled in production and `server.ssl.*` is configured securely (certs, not self-signed). For testing, the repo includes `keystore.p12` (self-signed) — note that browsers will warn.
  - Evidence: `application.properties` and server startup logs showing https port.

Authorization & access control

- [ ] Role checks & ownership checks
  - Confirm `@PreAuthorize` expressions in `ProductController` and `UserController` enforce intended logic. Verify edge cases for missing product or user (repository .get() used in SPEL could throw NoSuchElementException if not present).
  - Evidence: code snippets and sample requests proving enforcement.

Input validation & data flow

- [ ] DTO vs Entity usage
  - Verify controllers do not return entities with sensitive fields (password). `UserController.getAllUsers()` currently returns `List<User>` — verify and change to `UserResponse` to avoid leaking hashed passwords.
  - Evidence: API responses, controllers code.

- [ ] Validation on updates
  - Update DTOs should permit partial updates. Check `UserUpdate` currently has `@NotBlank` on password which forces password to be provided during updates; this is likely a bug.
  - Evidence: DTO source and sample update requests.

Rate limiting & DoS

- [ ] Bucket4J config
  - `application.properties` contains bucket4j rules. Confirm `bucket4j.enabled` is set appropriately in prod. Test limits (login attempts, per-IP/per-user) and look for global cache config to prevent evasion.
  - Evidence: property values and a test showing bucket exhaustion behavior.

Error handling & logging

- [ ] GlobalExceptionHandler
  - Confirm global handler masks internal details and returns structured `ErrorResponse` objects. Ensure exceptions are logged with safe detail levels.
  - Evidence: sample exception responses and logs.

Build & infrastructure

- [ ] Java / build config
  - POM shows `<java.version>24</java.version>` and compiler args include `--enable-preview`. Ensure CI uses the same JDK and security policy allows preview features.
  - Evidence: CI pipeline config, build logs.

- [ ] Secrets & configuration management
  - Any secrets (JWT secret, keystore password) must not be stored in repo. The repo currently has `server.ssl.key-store-password=password` and `keystore.p12` bundled: acceptable only for local development. For production, store secrets in a secret manager and ensure `server.ssl.key-store` references a production keystore.
  - Evidence: repo files and secret vault configuration.

Security recommendations / quick wins

- Add SameSite cookie attribute (Lax or Strict) when generating the JWT cookie in `JwtTokenProvider.generateJwtCookie()`.
- Fix the JWT property-name mismatch (set `app.jwt.secret` or change provider to use `jwt.secret`).
- Stop returning entity objects from controllers (replace `List<User>` with mapped `UserResponse` DTOs). Audit existing endpoints for any direct entity returns.
- Make `UserUpdate` fields optional to allow partial updates.
- Review SPEL `@PreAuthorize` expressions that call repository `.get()`; replace with safe checks or method-based authorization to avoid NoSuchElementException.
- Consider including token revocation (JWT blacklist) if immediate logout/invalidation is required.


## 4) Evidence to collect for the audit

- `application.properties` (or runtime env) to show property values
- Startup logs showing application bound ports and SSL enabled
- Sample HTTP request/response captures for:
  - Register (payload + 201 response)
  - Login (response headers with Set-Cookie; confirm flags)
  - Authenticated request using cookie (server accepts cookie)
  - Attempted cross-site request (CSRF) example if applicable
- Code snippets: `JwtTokenProvider`, `JwtAuthenticationFilter`, `SecurityConfig`, `ProductController`, `UserController`, `AuthController`


## 5) Acceptance criteria for a clean audit

- No credentials or secrets are present in source control.
- JWT secret is configured in a secrets manager or environment variable; repo does not contain production keys.
- All user-facing endpoints never return the `password` field.
- Cookies used for authentication are HttpOnly, Secure, and include a SameSite directive.
- HTTPS is enforced in production and Certificate management is in place.
- Input validation prevents malformed input; partial updates work correctly without forcing unnecessary fields.


---

If you want, I can:

- implement suggested small fixes (SameSite cookie, fix property mismatch, change `UserResponse.id` to String and stop returning `User` from controllers),
- produce an exported PNG of the Mermaid diagram (requires a Mermaid renderer), or
- run an automated smoke test (mvn package) and collect logs to attach to the audit.

Tell me which follow-up you prefer and I will proceed.
