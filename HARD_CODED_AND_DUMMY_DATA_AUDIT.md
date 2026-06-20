# HARD_CODED_AND_DUMMY_DATA_AUDIT

## Scope

This audit covers the Android application source under `app/` and flags places where business logic, UI content, navigation, configuration, or data are still hardcoded or driven by dummy/mock values.

## Summary

- Total findings: 19
- Highest-risk areas:
  - Placeholder Supabase configuration and fallback URLs
  - Dummy category and lost-item data on the home screen
  - Mock GPS values and string-packed report payloads
  - Fixed category IDs and status mappings used as business rules
  - TODO left in production backup configuration

---

## 1. Placeholder Supabase URL and API key in Gradle config

1. File name: `app/build.gradle.kts`
2. Class name: Gradle module script
3. Function name: `android.defaultConfig`
4. Exact code snippet:

```kotlin
val supabaseUrl = props.getProperty("supabase.url") ?: "https://your-project.supabase.co"
val supabaseKey = props.getProperty("supabase.key") ?: "your-anon-key"

buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
```

5. Why it is considered hardcoded or dummy:
   The module silently falls back to template values instead of failing fast when real environment configuration is missing.
6. Risk level: High
7. Recommended production-ready solution:
   Fail the build if required properties are missing, inject values per build type or CI secret store, and never ship placeholder defaults into `BuildConfig`.

---

## 2. Temporary fallback base URL inside Retrofit initialization

1. File name: `app/src/main/java/pember/qq/petugasunramhub/data/network/RetrofitClient.kt`
2. Class name: `RetrofitClient`
3. Function name: `instance`
4. Exact code snippet:

```kotlin
val baseUrl = when {
    rawUrl.isNullOrBlank() || rawUrl == "null" -> "https://placeholder.supabase.co/"
    rawUrl.endsWith("/") -> rawUrl
    else -> "$rawUrl/"
}
```

5. Why it is considered hardcoded or dummy:
   The client replaces missing config with a fake URL, which hides misconfiguration until runtime.
6. Risk level: High
7. Recommended production-ready solution:
   Reject startup when the base URL is missing or invalid, and validate configuration once during app bootstrap.

---

## 3. Hardcoded API contract strings and default query fields

1. File name: `app/src/main/java/pember/qq/petugasunramhub/data/network/SupabaseAPI.kt`
2. Class name: `SupabaseApi`
3. Function name: interface method declarations
4. Exact code snippet:

```kotlin
@Query("is_active") isActive: String = "eq.true",
@Query("select") select: String = "id,nim_nip,name,email,role,is_active"

@Query("select") select: String = "id,title,description,status,is_anonymous,latitude,longitude,created_at,categories(name),users!reports_user_id_fkey(name,nim_nip),report_media(id,file_path,file_type),task_logs(id,old_status,new_status,notes,created_at)"
```

5. Why it is considered hardcoded or dummy:
   The API contract is embedded as string literals in the client. Field selection and filters are not centralized, typed, or versioned.
6. Risk level: Medium
7. Recommended production-ready solution:
   Centralize query constants or move contract shaping to a backend service. Prefer typed DTOs and repository-level abstractions over repeating raw query strings.

---

## 4. Direct password comparison query in login flow

1. File name: `app/src/main/java/pember/qq/petugasunramhub/data/repository/AuthRepository.kt`
2. Class name: `AuthRepository`
3. Function name: `login`
4. Exact code snippet:

```kotlin
val users = api.login(
    nimNip = "eq.$nimNip",
    password = "eq.$password"
)
```

5. Why it is considered hardcoded or dummy:
   The login behavior is tied to a fixed PostgREST query shape and assumes direct credential matching on the `users` table.
6. Risk level: High
7. Recommended production-ready solution:
   Replace this with a real authentication flow using Supabase Auth or a backend-issued session token. Remove direct password querying from the Android client.

---

## 5. Hardcoded home-screen placeholder actions via Toasts

1. File name: `app/src/main/java/pember/qq/petugasunramhub/ui/home/CivitasHomeActivity.kt`
2. Class name: `CivitasHomeActivity`
3. Function name: `setupListeners`
4. Exact code snippet:

```kotlin
binding.btnCivitasPanic.setOnClickListener {
    Toast.makeText(this, "🚨 Panic Button Aktif! Mengirim koordinat darurat...", Toast.LENGTH_LONG).show()
}

binding.btnCivitasLaporBanner.setOnClickListener {
    Toast.makeText(this, "Mengarahkan ke Formulir Pelaporan PPKS", Toast.LENGTH_SHORT).show()
}

binding.tvCivitasLihatSemuaLostItems.setOnClickListener {
    Toast.makeText(this, "Membuka halaman seluruh info barang hilang & temuan", Toast.LENGTH_SHORT).show()
}
```

5. Why it is considered hardcoded or dummy:
   These are placeholder interactions. They present intent to the user but do not execute the actual feature.
6. Risk level: High
7. Recommended production-ready solution:
   Wire each action to a real flow: panic service/event submission, banner deep link or form entry, and a lost-items list screen backed by server data.

---

## 6. Hardcoded category list and system icon mapping

1. File name: `app/src/main/java/pember/qq/petugasunramhub/ui/home/CivitasHomeActivity.kt`
2. Class name: `CivitasHomeActivity`
3. Function name: `setupCategoriesRecyclerView`
4. Exact code snippet:

```kotlin
val dummyCategories = listOf(
    CivitasCategory(1, "Kekerasan/\nPelecehan", android.R.drawable.ic_menu_agenda),
    CivitasCategory(2, "Kerusakan\nFasilitas", android.R.drawable.ic_menu_manage),
    CivitasCategory(3, "Bencana/\nDarurat", android.R.drawable.ic_dialog_alert),
    CivitasCategory(4, "Barang Hilang\n/Temuan", android.R.drawable.ic_menu_search),
    CivitasCategory(5, "Lainnya", android.R.drawable.ic_menu_more)
)
```

5. Why it is considered hardcoded or dummy:
   Category IDs, labels, ordering, and icons are embedded in code rather than sourced from a domain model or backend.
6. Risk level: High
7. Recommended production-ready solution:
   Load category metadata from a repository or config endpoint. Store server IDs, names, and icon tokens, then map tokens to app resources in one place.

---

## 7. Fixed ID and index rules for category/reporting behavior

1. File name: `app/src/main/java/pember/qq/petugasunramhub/ui/home/CivitasHomeActivity.kt`
2. Class name: `CivitasHomeActivity`
3. Function name: `showReportingTypeDialog`
4. Exact code snippet:

```kotlin
val options = arrayOf("Laporkan sebagai Anonim", "Laporkan sebagai User Biasa")

builder.setItems(options) { dialog, which ->
    val intentKeForm = Intent(this, pember.qq.petugasunramhub.ui.form.FormLaporanActivity::class.java).apply {
        putExtra("CATEGORY_ID", category.id)
        putExtra("CATEGORY_NAME", cleanCategoryName)

        // index 0 = Anonim (true), index 1 = User Biasa (false)
        putExtra("EXTRA_IS_ANONYMOUS", which == 0)
    }
```

5. Why it is considered hardcoded or dummy:
   Anonymous behavior depends on a fixed dialog ordering and string labels. Navigation keys are raw string constants spread across activities.
6. Risk level: Medium
7. Recommended production-ready solution:
   Use typed navigation args or shared constants, and model reporting mode as an enum or sealed type rather than `which == 0`.

---

## 8. Dummy lost-item RecyclerView data and launcher-image placeholders

1. File name: `app/src/main/java/pember/qq/petugasunramhub/ui/home/CivitasHomeActivity.kt`
2. Class name: `CivitasHomeActivity`
3. Function name: `setupLostItemsRecyclerView`
4. Exact code snippet:

```kotlin
val dummyLostItems = listOf(
    CivitasLostItem(
        id = 1,
        title = "Telah Hilang Kunci Motor Honda Vario",
        date = "21/04/2026",
        timeAgo = "Dilaporkan 2 jam lalu",
        imageResId = R.mipmap.ic_launcher
    ),
    CivitasLostItem(
        id = 2,
        title = "Telah Hilang Kunci Motor Honda Vario",
        date = "21/04/2026",
        timeAgo = "Dilaporkan 2 jam lalu",
        imageResId = R.mipmap.ic_launcher
    ),
    CivitasLostItem(
        id = 3,
        title = "Ditemukan Dompet Hitam di Parkiran",
        date = "21/04/2026",
        timeAgo = "Dilaporkan 5 jam lalu",
        imageResId = R.mipmap.ic_launcher
    )
)
```

5. Why it is considered hardcoded or dummy:
   The data is explicitly marked dummy and uses the launcher icon instead of item-specific imagery.
6. Risk level: High
7. Recommended production-ready solution:
   Back the list with a lost-items API, store canonical timestamps, and load real image URLs with an image loader such as Coil or Glide.

---

## 9. Lost-item detail action is still a Toast placeholder

1. File name: `app/src/main/java/pember/qq/petugasunramhub/ui/home/CivitasHomeActivity.kt`
2. Class name: `CivitasHomeActivity`
3. Function name: `setupLostItemsRecyclerView`
4. Exact code snippet:

```kotlin
binding.rvCivitasLostItems.adapter = CivitasLostItemAdapter(dummyLostItems) { item ->
    Toast.makeText(this, "Melihat detail barang: ${item.title}", Toast.LENGTH_SHORT).show()
}
```

5. Why it is considered hardcoded or dummy:
   The click path does not navigate to a detail screen or fetch real content.
6. Risk level: Medium
7. Recommended production-ready solution:
   Navigate to a lost-item detail route with a stable item ID and load the record from the repository.

---

## 10. Spinner options are hardcoded in the report form

1. File name: `app/src/main/java/pember/qq/petugasunramhub/ui/form/CivitasFormLaporan.kt`
2. Class name: `FormLaporanActivity`
3. Function name: `setupSpinners`
4. Exact code snippet:

```kotlin
val jenisPelapor = arrayOf("Silahkan Pilih Jenis Pelapor", "Mahasiswa", "Dosen", "Staff", "Lainnya")
binding.spinJenisPelapor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jenisPelapor)

binding.spinJenisKejadian.visibility = View.GONE
```

5. Why it is considered hardcoded or dummy:
   The options are fixed in the client, and one spinner is hidden entirely instead of being fed with real category-specific choices.
6. Risk level: Medium
7. Recommended production-ready solution:
   Provide these options from a domain config source and render only the fields declared by the selected report schema.

---

## 11. Category business rules are driven by fixed numeric IDs

1. File name: `app/src/main/java/pember/qq/petugasunramhub/ui/form/CivitasFormLaporan.kt`
2. Class name: `FormLaporanActivity`
3. Function name: `applyCategoryVisibility`
4. Exact code snippet:

```kotlin
// categoryId: 1 = Kekerasan/Pelecehan, 2 = Kerusakan Fasilitas, 3 = Bencana/Darurat, 4 = Barang Hilang/Temuan, 5 = Lainnya
when (categoryId) {
    1 -> {
        binding.spinJenisPelapor.visibility = View.VISIBLE
        binding.layoutWaktuTanggal.visibility = View.VISIBLE
        binding.etLokasi.visibility = View.VISIBLE
        binding.tvUploadBukti.visibility = View.VISIBLE
    }
    2 -> {
        binding.spinJenisPelapor.visibility = View.GONE
        binding.layoutWaktuTanggal.visibility = View.GONE
        binding.etLokasi.visibility = View.VISIBLE
        binding.tvUploadBukti.visibility = View.VISIBLE
    }
    3 -> {
        binding.spinJenisPelapor.visibility = View.GONE
        binding.layoutWaktuTanggal.visibility = View.GONE
        binding.etLokasi.visibility = View.VISIBLE
        binding.tvUploadBukti.visibility = View.GONE
    }
```

5. Why it is considered hardcoded or dummy:
   Form behavior depends on magic numbers and local assumptions about category semantics.
6. Risk level: High
7. Recommended production-ready solution:
   Introduce a typed category model with server-driven field requirements, or at minimum use enums/constants and a dedicated form schema mapper.

---

## 12. Mock GPS value is injected into the location field

1. File name: `app/src/main/java/pember/qq/petugasunramhub/ui/form/CivitasFormLaporan.kt`
2. Class name: `FormLaporanActivity`
3. Function name: `setupListeners`
4. Exact code snippet:

```kotlin
binding.etLokasi.setOnClickListener {
    // Simulasi deteksi GPS otomatis
    binding.etLokasi.setText("Gedung FT Unram (-8.5833, 116.0969)")
    Toast.makeText(this, "Lokasi terdeteksi otomatis via GPS", Toast.LENGTH_SHORT).show()
}
```

5. Why it is considered hardcoded or dummy:
   The feature claims GPS detection but always injects one static campus coordinate string.
6. Risk level: High
7. Recommended production-ready solution:
   Use the fused location provider, request runtime permission, capture latitude/longitude as typed fields, and render a human-readable location label separately.

---

## 13. Report payload is stored as a formatted prose string

1. File name: `app/src/main/java/pember/qq/petugasunramhub/ui/form/CivitasFormLaporan.kt`
2. Class name: `FormLaporanActivity`
3. Function name: `validateAndSubmit`
4. Exact code snippet:

```kotlin
val fullDescription = buildString {
    append("Kategori: $categoryName\n")
    if (binding.spinJenisPelapor.visibility == View.VISIBLE) {
        append("Jenis Pelapor: ${binding.spinJenisPelapor.selectedItem}\n")
    }
    if (binding.layoutWaktuTanggal.visibility == View.VISIBLE) {
        append("Waktu Kejadian: ${binding.etWaktu.text} ${binding.etTanggal.text}\n")
    }
    append("Lokasi: $lokasi\n")
    append("Kontak: $kontak\n\n")
    append("Deskripsi Kejadian:\n$deskripsi")
}
```

5. Why it is considered hardcoded or dummy:
   Structured report fields are flattened into one text blob and later reparsed by the detail screen.
6. Risk level: High
7. Recommended production-ready solution:
   Store each attribute in explicit columns or nested JSON fields. Keep `description` for free text only.

---

## 14. Coordinates are parsed back out of a display string

1. File name: `app/src/main/java/pember/qq/petugasunramhub/ui/form/CivitasFormLaporan.kt`
2. Class name: `FormLaporanActivity`
3. Function name: `validateAndSubmit`
4. Exact code snippet:

```kotlin
val regex = "\\(([-+]?\\d+\\.\\d+),\\s*([-+]?\\d+\\.\\d+)\\)".toRegex()
val matchResult = regex.find(lokasi)
if (matchResult != null) {
    lat = matchResult.groupValues[1].toDoubleOrNull()
    lon = matchResult.groupValues[2].toDoubleOrNull()
}
```

5. Why it is considered hardcoded or dummy:
   Latitude and longitude depend on one exact text format embedded in the UI string.
6. Risk level: High
7. Recommended production-ready solution:
   Hold coordinates in dedicated state variables from the start and display formatted location text independently of the stored numeric values.

---

## 15. Form UI state text is hardcoded in code instead of resource-driven

1. File name: `app/src/main/java/pember/qq/petugasunramhub/ui/form/CivitasFormLaporan.kt`
2. Class name: `FormLaporanActivity`
3. Function name: `pickImageLauncher`, `validateAndSubmit`
4. Exact code snippet:

```kotlin
binding.tvUploadBukti.text = "Bukti: Terpilih (Tap untuk ganti)"

binding.btnKirimLaporan.text = "MENGIRIM..."

binding.btnKirimLaporan.text = "KIRIM LAPORAN"
```

5. Why it is considered hardcoded or dummy:
   Dynamic UI labels are embedded in code, not localized, and tied to temporary UI copy.
6. Risk level: Low
7. Recommended production-ready solution:
   Move all stateful UI strings to `strings.xml`, use formatted resources, and keep view-state text changes resource-based.

---

## 16. Detail screen reparses structured values from `description`

1. File name: `app/src/main/java/pember/qq/petugasunramhub/ui/home/DetailLaporanActivity.kt`
2. Class name: `DetailLaporanActivity`
3. Function name: `loadReportDetail`
4. Exact code snippet:

```kotlin
binding.tvDetailLocation.text = report.description.substringAfter("Lokasi: ").substringBefore("\n")

val rawDescription = report.description
val userDesc = if (rawDescription.contains("Deskripsi Kejadian:\n")) {
    rawDescription.substringAfter("Deskripsi Kejadian:\n")
} else {
    rawDescription
}
```

5. Why it is considered hardcoded or dummy:
   The screen relies on string markers as a storage format rather than typed response fields.
6. Risk level: High
7. Recommended production-ready solution:
   Fetch location, contact, incident time, and narrative as separate fields from the backend and bind them directly.

---

## 17. Report status progress, labels, and colors are hardcoded as business rules

1. File name: `app/src/main/java/pember/qq/petugasunramhub/ui/home/ReportListAdapter.kt` and `app/src/main/java/pember/qq/petugasunramhub/ui/home/DetailLaporanActivity.kt`
2. Class name: `ReportListAdapter.ReportViewHolder`, `DetailLaporanActivity`
3. Function name: `getStatusProgress`, `getStatusLabel`, `getStatusColor`, `loadReportDetail`
4. Exact code snippet:

```kotlin
return when (status.lowercase()) {
    "pending" -> 25
    "assigned" -> 50
    "processing" -> 75
    "completed" -> 100
    else -> 10
}

return when (status.lowercase()) {
    "pending" -> "Dalam Antrean"
    "assigned" -> "Telah Diterima"
    "processing" -> "Sedang Diproses"
    "completed" -> "Selesai"
    else -> status.replaceFirstChar { it.uppercase() }
}

return when (status.lowercase()) {
    "pending" -> "#FFC107"
    "assigned" -> "#0D6EFD"
    "processing" -> "#17A2B8"
    "completed" -> "#28A745"
    else -> "#6C757D"
}
```

5. Why it is considered hardcoded or dummy:
   Status semantics, progress percentages, labels, and colors are duplicated in the UI layer and assume a fixed four-status workflow.
6. Risk level: Medium
7. Recommended production-ready solution:
   Centralize status metadata in one mapper or backend contract and expose typed UI state objects to all screens.

---

## 18. Manual storage URLs, bucket names, MIME type, and file type are hardcoded

1. File name: `app/src/main/java/pember/qq/petugasunramhub/data/repository/ReportRepository.kt` and `app/src/main/java/pember/qq/petugasunramhub/data/repository/StorageRepository.kt`
2. Class name: `ReportRepository`, `StorageRepository`
3. Function name: `createReport`, `uploadReportMedia`, `insertReportMedia`, `uploadFoto`
4. Exact code snippet:

```kotlin
status = "pending"

val url = "${BuildConfig.SUPABASE_URL}/storage/v1/object/report-media/${reportId}/${fileName}"
val publicUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/report-media/${reportId}/${fileName}"
val body = file.readBytes().toRequestBody("image/jpeg".toMediaType())

val body = mapOf(
    "report_id" to reportId.toString(),
    "file_path" to filePath,
    "file_type" to "image"
)

val fileName = "bukti_${reportId}_${System.currentTimeMillis()}.jpg"
val url = "${BuildConfig.SUPABASE_URL}/storage/v1/object/task-evidence/$fileName"
```

5. Why it is considered hardcoded or dummy:
   Business values such as default status, bucket names, upload paths, MIME type, and media type are fixed in client code.
6. Risk level: Medium
7. Recommended production-ready solution:
   Centralize storage policy and report defaults behind repository config or backend-issued upload descriptors. Derive MIME type from the file content and domain rules.

---

## 19. Layout files contain hardcoded UI copy, preview values, and placeholder visuals

1. File name:
   - `app/src/main/res/layout/civitas_home.xml`
   - `app/src/main/res/layout/civitas_form_laporan.xml`
   - `app/src/main/res/layout/activity_login.xml`
   - `app/src/main/res/layout/activity_list_laporan.xml`
   - `app/src/main/res/layout/activity_detail_laporan.xml`
   - `app/src/main/res/xml/data_extraction_rules.xml`
   - `app/src/main/java/pember/qq/petugasunramhub/utils/NotificationHelper.kt`
2. Class name:
   - XML layout/resource files
   - `NotificationHelper`
3. Function name:
   - Layout declarations
   - `showNotification`
4. Exact code snippet:

```xml
android:text="Lalu Rifqi Ramadhan"
android:text="F1D02310071"
android:text="Kampus Aman,\nRuang Aman Bersama"
android:text="🚨 PANIC BUTTON"
android:text="Hak Cipta © 2026 UnramHUB. Seluruh hak dilindungi."
```

```xml
android:hint="Deskripsi Kejadian\nCeritakan kejadian secara singkat dan jelas"
android:text="KIRIM LAPORAN"
```

```xml
android:text="Unram Hub"
android:text="Single Sign On"
android:hint="Nama Akun"
android:hint="Kata Sandi"
```

```xml
android:text="Belum Ada Laporan"
android:text="Seluruh laporan yang Anda kirim akan muncul di sini untuk dipantau perkembangannya."
```

```xml
android:text="Pending"
android:text="Judul Laporan"
android:text="Kategori: Fasilitas"
android:text="20/06/2026 12:00"
android:text="Gedung FT Unram"
android:text="User Biasa (NIM)"
android:text="Isi deskripsi lengkap di sini..."
```

```xml
<!-- TODO: Use <include> and <exclude> to control what is backed up. -->
```

```kotlin
.setSmallIcon(android.R.drawable.ic_dialog_info) // Ganti pake icon aplikasi kamu nnti
private const val CHANNEL_NAME = "Disposisi Tugas Baru"
description = "Notifikasi ketika admin memberikan tugas perbaikan baru"
```

5. Why it is considered hardcoded or dummy:
   UI copy is embedded directly in layouts instead of `strings.xml`, sample profile/detail values are used as real defaults, the backup policy still contains TODO scaffolding, and notification visuals still use placeholder/system resources.
6. Risk level: Medium
7. Recommended production-ready solution:
   Move all user-facing copy into `strings.xml`, remove sample content from runtime layouts, define real empty/loading placeholders via preview-only `tools:` attributes, finish the backup policy, and replace placeholder notification assets with app-owned resources.

---

## Notes

- `tools:listitem` and `tools:visibility` usages were not flagged as runtime dummy data because they are preview-only and do not ship behavior.
- Hardcoded colors were only called out where they directly encode workflow/business state or placeholder UI behavior. Pure styling constants were not exhaustively listed.
