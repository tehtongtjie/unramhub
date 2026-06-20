# Authorization Security Audit

This document analyzes authorization flow, exposes privilege escalation threats, and outlines recommendations for access control hardening.

---

## 1. Access Control Findings

### 1. Client-Side Access Enforcement
* **Behavior**: UI elements are hidden or shown based on `sessionManager.getRole()`.
* **Weakness**: Completely bypassable. An attacker can write scripts using the shared Supabase Anon key to query any database endpoints directly.

### 2. Lack of Report Ownership Checks (IDOR)
* **Behavior**: Fetching report details (`GET /reports?id=eq.X`) and listings (`GET /reports?user_id=eq.Y`) does not verify if the requesting user owns or has access permissions to the targets.
* **Weakness**: Any authenticated client can fetch, read, or modify reports belonging to *any* other user simply by modifying query parameter values.
* **Severity**: **CRITICAL**

---

## 2. Attack Scenarios

### Scenario #1: Report Data Scraping (IDOR)
1. An attacker logs in to the app and extracts the shared `SUPABASE_KEY` from the APK.
2. The attacker writes a script calling `GET https://your-supabase.co/rest/v1/reports?select=*` using the Anon key.
3. Because there are no row ownership validations on the server, the database returns all records, exposing locations, description texts, contact details, and identities of every reporter in the system.

### Scenario #2: Report Hijacking
1. An attacker crafts a `PATCH /reports?id=eq.[target_id]` query.
2. They modify the report status to `"completed"` or edit the description text of another user's active report.
3. Supabase processes the request because the shared Anon key has write permissions and no RLS constraints verify request ownership.

---

## 3. Recommended Future Direction

1. **Enable Row Level Security (RLS) on Supabase**:
   * Run `ALTER TABLE reports ENABLE ROW LEVEL SECURITY;`.
2. **Implement Ownership Policies**:
   * Configure RLS policies checking the authenticated user's JWT ID (`auth.uid()`):
     ```sql
     -- Example Policy for selecting reports
     CREATE POLICY "Users can only read their own reports" ON reports
     FOR SELECT USING (auth.uid() = user_id);
     ```
3. **Establish Role-Based Backend Policies**:
   * Officers should be assigned database roles or custom JWT metadata claims (e.g. `role = 'officer'`).
   * Define RLS policies allowing officers to read all reports while restricting standard users (`role = 'mahasiswa'`) to their own entries:
     ```sql
     CREATE POLICY "Officers can read all reports" ON reports
     FOR SELECT USING (
       (auth.jwt() ->> 'role') = 'officer' OR auth.uid() = user_id
     );
     ```
