# Supabase Integration Security Audit

This document reviews the security implications of integrating Supabase directly within the client application, evaluates risk exposures, and provides recommendations for database hardening.

---

## 1. Threat Model & Exposed Assets

### Exposed Assets in APK
* **SUPABASE_URL**: Compiled as a string in `BuildConfig`. Tells attackers exactly where the database endpoint is.
* **SUPABASE_KEY**: Compiled as a string in `BuildConfig`. This is the shared Anonymous key.
* **Severity of Exposure**: **MEDIUM** (Asset exposure is unavoidable for client-server direct database models, but it is critical that these keys carry minimal privileges).

### Threats
1. **APK Reverse Engineering**: An attacker can easily decompile the APK using toolkits like JADX-GUI or Apktool and retrieve both the database endpoint URL and the Anon API key in seconds.
2. **Bypass of API Constraints**: Once the Anon key is obtained, the attacker does not need to use the Android app. They can directly access Supabase database endpoints (`/rest/v1/`) via Postman or cUrl.
3. **RLS Absence (Database Compromise)**: Because the client queries tables directly using the Anon key and RLS is disabled, any person with the Anon key has read/write access to the database tables, completely bypassing application logic.
4. **Severity**: **CRITICAL**

---

## 2. Recommended Future Direction

1. **Enforce Row Level Security (RLS) Globally**:
   * Every database table in Supabase must have RLS active. 
   * Banned public access (`anon` role) on sensitive tables like `users` (except during sign-in verification if using direct queries, but Supabase Auth is preferred).
2. **Restrict Anon Key Privileges**:
   * The Anonymous API key must only be used to read public data (e.g., categories list) or submit new user reports (insert permissions).
   * It must have zero access to read other users' reports or modify metadata tables without a verified JWT session token.
3. **Use Supabase Edge Functions for Sensitive Logic**:
   * Business-critical workflows (such as authentication or system configurations) should be routed through secure backend Supabase Edge Functions or database triggers rather than executed directly by the client.
