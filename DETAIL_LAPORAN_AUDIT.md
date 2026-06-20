# DetailLaporan MVVM Audit Report

This report outlines the audit findings of the `DetailLaporanActivity` prior to its refactoring in Phase 6.5, focusing on direct repository bindings, coroutine lifecycles, parsing patterns, and error standardizations.

---

## 1. Audit Findings

### 1. Repository & Coroutine Usage
* **Current Behavior**:
  * Directly instantiated repository: `reportRepository = ReportRepository()` in `onCreate()`.
  * Launched asynchronous actions directly on the view layer scope: `lifecycleScope.launch` in `loadReportDetail(...)` and `loadImageFromUrl(...)`.
* **Weakness**: 
  * **Configuration Changes**: Rotating the device cancels the network coroutines and triggers the API queries again upon recreation.
  * **Coupled Testing**: The API calls and data bindings are tied to Android platform classes (`Activity`), preventing lightweight JUnit testing.

### 2. Error Handling & Data Flow
* **Current Behavior**:
  * Inline exception catching: `result.fold(..., onFailure = { error -> Toast.makeText(..., error.localizedMessage, ...).show() })`.
* **Weakness**: Leaks database and server-specific exception messages to the user interface directly. Lacks integration with the centralized `AppError` architecture.

### 3. Media Loading Flow
* **Current Behavior**: 
  * Asynchronously downloads images via standard URL streams inside a coroutine: `URL(imageUrl).openStream()`.
  * Silently catches URL failures and prints logs: `android.util.Log.e(...)`.
* **Weakness**: Failed image downloads are hidden from the user, leaving a blank view space without warning.

### 4. Legacy Report Compatibility (LEGACY_REPORT_SUPPORT)
* **Current Behavior**:
  * Evaluated if `report.incidentLocation` is null directly in the Activity to decide whether to parse location details from the raw `description` string.
* **Weakness**: Violates Separation of Concerns by managing layout configuration formats and string slicing inside the view controllers.

---

## 2. Recommended Refactoring Order
1. Extract all string extraction logic, status parser lookups, and date-time formatting to a presentation mapping function in the ViewModel.
2. Formulate `DetailUiState` representing data load success, error, and empty outcomes.
3. Integrate the `AppError` mapping flow to translate network throwables.
4. Clean `DetailLaporanActivity` to observe state updates and render elements dynamically.
