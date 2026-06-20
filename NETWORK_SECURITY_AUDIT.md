# Network & Transport Security Audit

This document reviews the network configuration, HTTP request management, and transport vulnerabilities of the application.

---

## 1. Network Architecture Findings

### 1. Retrofit & OkHttp Configuration
* **Logging Interceptor Leak**: 
  ```kotlin
  logging.level = HttpLoggingInterceptor.Level.BODY
  ```
  The logging interceptor is active and logs the entire HTTP request/response bodies (including passwords during login and auth details) directly to the system console log (Logcat). In production releases, Logcat can be read by other applications with read log permissions (on older versions) or cached on-device, exposing sensitive user information.
* **Lack of Request Signing**: Requests are sent without cryptographic signatures or HMAC headers. Attackers can intercept, modify, and replay payloads without detection.
* **No Certificate Pinning**: OkHttp does not enforce Certificate Pinning, exposing the app to Man-in-the-Middle (MitM) attacks.

### 2. Transport Security (HTTPS)
* **HTTPS Enforcement**: The app relies on `BuildConfig.SUPABASE_URL` using the `https://` prefix. However, there is no explicit Network Security Configuration (`network_security_config.xml`) enforcing cleartext traffic blocking at the OS level.
* **Production Readiness**: **NOT READY**. Plaintext logging of passwords and lack of MitM protections make the current network pipeline unsafe for public release.

---

## 2. Recommended Future Direction

1. **Disable Body Logging in Production Builds**:
   * Wrap the `HttpLoggingInterceptor` setup to check if the app is in debug mode:
     ```kotlin
     logging.level = if (BuildConfig.DEBUG) {
         HttpLoggingInterceptor.Level.BODY
     } else {
         HttpLoggingInterceptor.Level.NONE
     }
     ```
2. **Implement Certificate Pinning**:
   * Configure OkHttp to enforce SHA-256 certificate pinning against Supabase certificate authorities:
     ```kotlin
     val certificatePinner = CertificatePinner.Builder()
         .add("*.supabase.co", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
         .build()
     builder.certificatePinner(certificatePinner)
     ```
3. **Enforce HTTPS in Network Security Config**:
   * Create `res/xml/network_security_config.xml` to explicitly disable cleartext traffic:
     ```xml
     <?xml version="1.0" encoding="utf-8"?>
     <network-security-config>
         <base-config cleartextTrafficPermitted="false" />
     </network-security-config>
     ```
   * Register the config inside the `<application>` tag of `AndroidManifest.xml`.
