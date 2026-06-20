# Session Management Security Audit

This document reviews how the application stores user sessions on the device, evaluates threat vectors, and outlines recommendations for session hardening.

---

## 1. Stored Fields in Session

The [SessionManager](file:///home/nantaaq/Android/unramhub/civitas-dev/app/src/main/java/pember/qq/petugasunramhub/utils/SessionManager.kt) class utilizes standard Android SharedPreferences to store the session data:
* **Storage Target**: `unramhub_session.xml` (located under `/data/data/pember.qq.petugasunramhub/shared_prefs/`)
* **Fields stored in plaintext**:
  * `user_id` (Long)
  * `nim_nip` (String)
  * `name` (String)
  * `email` (String)
  * `role` (String)

---

## 2. Threat Analysis

### Threat #1: Plaintext Local Storage & Extraction
* **Description**: SharedPreferences are stored as unencrypted XML files. Furthermore, `AndroidManifest.xml` has `android:allowBackup="true"` active.
* **Attack Scenario**: An attacker can copy the XML preference file via ADB backup commands or read it directly on a rooted device.
* **Impact**: Extraction of email addresses, NIM/NIP, and user names, violating user privacy.
* **Severity**: **HIGH**

### Threat #2: Role Tampering & Client-Side Privilege Escalation
* **Description**: Authorization is determined by evaluating the `"role"` string read directly from the SharedPreferences file.
* **Attack Scenario**: A user on a rooted device can modify the `"role"` tag in `unramhub_session.xml` from `"mahasiswa"` to `"admin"` or `"officer"`.
* **Impact**: The app will render admin/officer features and layouts, leading to unauthorized actions if the backend relies on client-provided roles.
* **Severity**: **HIGH**

### Threat #3: Replay & Forgery Attacks
* **Description**: There is no session token signature or token validation against the server. The app assumes any user is logged in as long as `user_id != -1` is found locally.
* **Impact**: Session forgery is trivial; an attacker can construct a fake XML preference file with any random `user_id` to log in as another user.
* **Severity**: **HIGH**

---

## 3. Recommended Future Direction

1. **Adopt EncryptedSharedPreferences**:
   * Migrate `SessionManager` to use `EncryptedSharedPreferences` from the Android Jetpack Security library (`androidx.security:security-crypto`).
   * This encrypts both preference keys and values using AES-256 GCM, backed by Android Keystore.
2. **De-allow Android Backups**:
   * Change `android:allowBackup` to `false` in `AndroidManifest.xml` to block ADB backup session extraction.
3. **Transition to Token-Based Sessions**:
   * Store JWT `access_token` and `refresh_token` instead of plaintext identity strings.
   * Verify session token validity against Supabase Auth during app startup.
