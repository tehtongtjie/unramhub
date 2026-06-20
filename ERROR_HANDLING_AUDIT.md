# Global Error Handling Audit Report

This document reviews the error handling mechanisms in the UnramHub Android application, details their weaknesses, and provides the recommended standardization pattern implemented in Phase 6.

---

## 1. Findings & Weakness Audit

| File | Method | Current Behavior | Weakness | Recommended Replacement |
| :--- | :--- | :--- | :--- | :--- |
| **AuthRepository.kt** | `login` | Catches `Throwable` and returns `Result.failure(Exception(msg))` with raw strings. | Returns raw exceptions; obscures standard authentication error codes. | Standardize to catch `Throwable` and return `Result.failure(AppException(AppError.AuthenticationError))` or map via `ErrorMapper`. |
| **LoginViewModel.kt** | `login` | Catches exceptions in repository fold and posts raw string error messages to `LoginState.Error`. | Couples UI with string messages; UI cannot dynamically distinguish network timeouts from bad credentials. | Catch failures, convert to `AppError` using `ErrorMapper`, and propagate `LoginState.Error(AppError)` to UI. |
| **LoginActivity.kt** | `observeLoginState` | Sets `binding.tilPassword.error = state.message` directly. | Relies on raw string validation; no programmatic parsing capabilities. | Extract message from domain-focused error structure (`state.error.message`). |
| **CategoryRepository.kt** | `getCategories` | Returns `Result.failure(e)` directly containing raw network exceptions. | Leaks network layer exceptions (Retrofit/OkHttp) directly to the domain layer. | Wrap caught exception in `AppException(ErrorMapper.map(e))`. |
| **HomeViewModel.kt** | `refresh` | Wraps API fails in `CategoryUiState.Error(error.message)` containing raw messages. | Leaks raw API connection errors directly to UI states. | Wrap failures in `CategoryUiState.Error(AppError)` mapped via `ErrorMapper`. |
| **CivitasHomeActivity.kt**| `observeViewModel` | Shows `tvCategoryStatus.text = message` directly. | Inspects raw messages in UI. | Bind display fields directly to `state.error.message`. |
| **LostItemRepository.kt** | `getLostItems` | Returns raw `Result.failure(e)`. | Leaks platform network and database exception details. | Standardize exception wrapping to `AppException(ErrorMapper.map(e))`. |
| **LostItemsViewModel.kt** | `loadLostItems` | Directly forwards `error.message` to `LostItemsUiState.Error`. | Decoupled states rely on string messages. | Map to `LostItemsUiState.Error(AppError)`. |
| **LostItemsActivity.kt** | `observeViewModel` | Binds `tvStatus.text = state.message`. | View layer tightly coupled with exception descriptions. | Bind directly to `state.error.message`. |
| **GPSLocationProvider.kt**| `getCurrentLocation`| Returns `Result.failure(Exception(msg))` for location errors. | Does not distinguish system settings issues from GPS connection errors. | Return `Result.failure(AppException(AppError.LocationError(msg)))`. |
| **PanicViewModel.kt** | `requestLocation` | Directly sets raw string in `PanicUiState.Error`. | Restricting UI capability to inspect specific location failures. | Map failures to `PanicUiState.Error(AppError)`. |
| **PanicActivity.kt** | `observeViewModel` | Sets status values via `showError(state.message)`. | Relies on hardcoded string formatting in ViewModel. | Bind display message directly to `state.error.message`. |
| **ReportRepository.kt** | `createReport`, etc. | Propagates generic `Result.failure(Exception)` or raw network `Throwable`. | Leaks JSON serialization/PostgREST exceptions to ViewModel. | Map exceptions and custom data failures to domain-focused `AppException`. |
| **FormLaporanViewModel.kt**| `submitReport`, etc. | Propagates UI state strings like `ValidationError(message)`. | Limits dynamic validation actions (e.g. focusing specific views). | Use `FormSubmissionState.ValidationError(AppError)`. |
| **CivitasFormLaporan.kt** | `observeViewModel` | Toasts raw messages `state.message`. | View displays unparsed exception messages. | Read `state.error.message`. |

---

## 2. Standardized Error Handling Architecture

The refactored error model introduces structured domain errors and standardizes exception flow across all layers of the application:

```
[OkHttp / Supabase API] 
        ↓ throws raw Exceptions (IOException, SocketTimeoutException)
[Repositories]
        ↓ catches and translates using ErrorMapper
        ↓ returns Result.failure(AppException(AppError))
[ViewModels]
        ↓ consumes Result and maps to UI State classes containing AppError (ValidationError, NetworkError, etc.)
[Activities]
        ↓ observes UI State and binds state.error.message directly to text views/TILs without business error logic.
```
