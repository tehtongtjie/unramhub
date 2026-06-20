# Structured Report Data Migration Analysis

This document outlines the analysis, audit, database schemas, and migration steps for transitioning the report storage format from formatted text strings to structured database fields.

---

## 1. Current Parsing Audit & Impact Analysis

### Affected Files & Methods
1. **[FormLaporanViewModel.kt](file:///home/nantaaq/Android/unramhub/civitas-dev/app/src/main/java/pember/qq/petugasunramhub/ui/form/FormLaporanViewModel.kt)**
   * **Method**: `submitReport(...)`
   * **Role**: Assembles the description payload using a template structure:
     ```kotlin
     val fullDescription = buildString {
         append("Kategori: $categoryName\n")
         if (config.showReporterType) {
             append("Jenis Pelapor: ${input.reporterTypeSelectedValue}\n")
         }
         if (config.showDateTime) {
             append("Waktu Kejadian: ${input.eventTime} ${input.eventDate}\n")
         }
         append("Lokasi: ${input.location}\n")
         append("Kontak: ${input.contact}\n\n")
         append("Deskripsi Kejadian:\n${input.description}")
     }
     ```
2. **[DetailLaporanActivity.kt](file:///home/nantaaq/Android/unramhub/civitas-dev/app/src/main/java/pember/qq/petugasunramhub/ui/home/DetailLaporanActivity.kt)**
   * **Method**: `loadReportDetail(...)`
   * **Role**: Parses structural data from the description string to render components:
     * *Location Extraction*:
       ```kotlin
       binding.tvDetailLocation.text = report.description.substringAfter("Lokasi: ").substringBefore("\n")
       ```
     * *Description Extraction*:
       ```kotlin
       val userDesc = if (rawDescription.contains("Deskripsi Kejadian:\n")) {
           rawDescription.substringAfter("Deskripsi Kejadian:\n")
       } else {
           rawDescription
       }
       binding.tvDetailDescription.text = userDesc
       ```

### Risks & Vulnerabilities
* **Fragile String Parsing**: A simple formatting change during submission (e.g., adding whitespace, modifying labels, or localizing text) will cause silent failures in the detail page extraction, resulting in empty values or showing raw markup text.
* **Lack of Indexing & Filtering**: Because incident parameters (incident datetime, location name, and reporter role) are stored inside a text block, it is impossible for database engines to index or filter reports efficiently by location or time.
* **Integration Overhead**: Any third-party integration or notification systems must implement custom regex parsing engines to retrieve core incident details.

---

## 2. Database Design & Recommendations

We evaluated two architectural approaches for storing incident parameters in Supabase:

### Comparison Matrix

| Criteria | Option A: Flat columns on `reports` table | Option B: Metadata child table `report_metadata` |
| :--- | :--- | :--- |
| **Simplicity** | **High** (Flat data modeling) | **Low** (Relation modeling, FK constraints) |
| **Android Integration** | **Very Simple** (Plain GSON field mapping) | **Medium** (Complex DTO list conversion) |
| **Query Complexity** | **Low** (No JOINs required) | **High** (Requires JOINs or pivot operations) |
| **Database Performance** | **High** (Easy to index and search columns) | **Medium** (Index lookups traverse multiple rows) |
| **Supabase RLS Rules** | **Direct** (Evaluates column conditions) | **Indirect** (Requires lookup joins) |
| **Flexibility** | **Low** (Adding new attributes requires migration) | **High** (Dynamic key-value parameters) |

### Recommendation: **Option A (Flat Columns)**
For the current scope of reporting (where parameters like `incident_location`, `incident_datetime`, and `reporter_type` are highly standard and reused across almost all categories), **Option A** is strongly recommended. It avoids SQL JOIN operations, simplifies Android entity modeling, maximizes index efficiency, and matches the flat structure of existing coordinates columns (`latitude`, `longitude`).

### Migration SQL
```sql
-- Up Migration: Add structured columns to reports table
ALTER TABLE reports 
ADD COLUMN incident_location TEXT,
ADD COLUMN incident_datetime TIMESTAMPTZ,
ADD COLUMN reporter_type TEXT;

-- Indexing for performance optimization
CREATE INDEX idx_reports_incident_location ON reports(incident_location);
CREATE INDEX idx_reports_incident_datetime ON reports(incident_datetime);
```

### Rollback SQL
```sql
-- Down Migration: Drop added columns
ALTER TABLE reports 
DROP COLUMN IF EXISTS incident_location,
DROP COLUMN IF EXISTS incident_datetime,
DROP COLUMN IF EXISTS reporter_type;
```

### Sample Data Insertion Payload (JSON)
```json
{
  "user_id": 4,
  "category_id": 1,
  "title": "Kekerasan Verbal di Selasar FT",
  "description": "Terjadi perselisihan antar kelompok mahasiswa.",
  "latitude": -8.5833,
  "longitude": 116.0969,
  "incident_location": "Selasar Fakultas Teknik",
  "incident_datetime": "2026-06-20T17:30:00+08:00",
  "reporter_type": "Mahasiswa",
  "is_anonymous": false,
  "status": "pending"
}
```

---

## 3. Android Refactor Plan

```
[FormInput] -> [FormLaporanViewModel] -> [ReportRequest] -> [Supabase Api] -> [DetailLaporanActivity]
  (Flat)             (Flat)                (DTO Flat)        (Table columns)     (Observes Flat fields)
```

1. **Model Adjustments**:
   * Update DTOs in `Report.kt`: Add nullable fields `incidentLocation`, `incidentDatetime`, and `reporterType` to both `Report` and `ReportRequest` models (serialized to their snake_case database equivalents).
2. **ViewModel Adjustment**:
   * Update [FormLaporanViewModel.kt](file:///home/nantaaq/Android/unramhub/civitas-dev/app/src/main/java/pember/qq/petugasunramhub/ui/form/FormLaporanViewModel.kt): Pass inputs (`input.location`, `input.eventTime`, `input.reporterTypeSelectedValue`) directly as structured parameters to `reportRepository.createReport` instead of generating a formatted description string.
   * `description` will now store only the raw description text typed by the user (`input.description`).
3. **Repository Refactor**:
   * Adjust `createReport` signature to accept these structured fields and map them to `ReportRequest`.
4. **View Activity Refactor**:
   * Refactor [DetailLaporanActivity.kt](file:///home/nantaaq/Android/unramhub/civitas-dev/app/src/main/java/pember/qq/petugasunramhub/ui/home/DetailLaporanActivity.kt): Bind `tvDetailLocation` directly to `report.incidentLocation` (falling back to a default label if null), and `tvDetailDescription` directly to `report.description`. Completely remove `substringAfter`/`substringBefore` parsing.

---

## 4. Recommended Implementation Order

1. **Step 1**: Run database schema migration on Supabase console (Adding the columns).
2. **Step 2**: Update Android `Report` and `ReportRequest` data classes to support the new JSON fields.
3. **Step 3**: Update `ReportRepository.createReport()` parameters to accept the new structured fields.
4. **Step 4**: Refactor `FormLaporanViewModel` to send values to the updated repository method.
5. **Step 5**: Refactor `DetailLaporanActivity` to read structured values from the backend directly, removing the parsing conventions.
