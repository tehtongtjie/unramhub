# Authentication Security Audit

This document reviews the current authentication architecture, exposes critical vulnerabilities, and outlines the recommended roadmap for authentication hardening.

---

## 1. Current Authentication Flow Diagram

```
[LoginActivity] 
       ↓ passes credentials in plaintext
[LoginViewModel] 
       ↓
[AuthRepository] 
       ↓ HTTP GET (password as query param)
[Retrofit / Supabase Api]
       ↓ SELECT * FROM users WHERE nim_nip = ? AND password = ?
[Supabase Database]
       ↓ Returns User list JSON payload
[LoginActivity] (Stores user details in SessionManager)
```

---

## 2. Identified Vulnerabilities

### Vulnerability #1: Plaintext Password Exposure in Transit
* **Description**: Passwords are sent as a plaintext query parameter (`password=eq.plaintext_password`) in the HTTP request. While HTTPS encrypts the URL path and queries during transit, query parameters are routinely logged in plaintext by API gateways, reverse proxies, and server-side connection logs.
* **Impact**: System administrators, proxy operators, or developers with log access can read user passwords in plain text.
* **Severity**: **CRITICAL**

### Vulnerability #2: Plaintext Password Storage in Database
* **Description**: Since the login query checks `password = "eq.plaintext"`, the database stores passwords in plaintext rather than cryptographically salted hashes (e.g., bcrypt, Argon2).
* **Impact**: A database breach instantly exposes all user passwords, leading to complete credential compromise.
* **Severity**: **CRITICAL**

### Vulnerability #3: Bypass of Supabase Auth Infrastructure
* **Description**: The app queries a standard database table `users` directly to verify passwords rather than using Supabase’s built-in Auth service (`GoTrue` engine).
* **Impact**: The server does not issue a JWT session token. The database cannot cryptographically verify requests, and standard Supabase session management is completely bypassed.
* **Severity**: **HIGH**

---

## 3. Recommended Future Direction

1. **Migrate to Supabase Auth**:
   * Integrate standard Supabase signup and sign-in REST endpoints (`POST /auth/v1/token?grant_type=password`).
   * Supabase Auth automatically handles password hashing (using bcrypt) and stores credentials securely in the protected `auth.users` schema, invisible to normal public queries.
2. **Enforce JWT Token Session Handling**:
   * Upon successful authentication, Supabase Auth returns a JWT `access_token`.
   * Cache this JWT token and append it as a `Bearer` token header to every database request, allowing Supabase to identify the logged-in user at the database level.
