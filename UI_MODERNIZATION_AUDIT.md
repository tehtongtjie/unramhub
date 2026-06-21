# Unram Hub: UI Modernization Audit (UI Phase B1)

This audit documents typography, corner radius, border, elevation, icon, and spacing violations against the target modernization rules.

---

## 1. Typography Analysis

### Target Rules
*   **Caption**: Minimum `12sp`
*   **Secondary Text**: Minimum `14sp`
*   **Body Text**: Minimum `16sp`
*   **Section Title**: `18sp`
*   **Screen Title**: `20sp`

### Findings & Violations

| File Name | Component ID | Type / Role | Current Size | Proposed Size | Status / Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `civitas_home.xml` | `tvCivitasUserName` | Body / User Header | `16sp` | `16sp` | **Passes** |
| `civitas_home.xml` | `tvCivitasUserNim` | Caption / User Subtext | `12sp` | `12sp` | **Passes** |
| `civitas_home.xml` | (Campaign banner title) | Section Title | `18sp` | `18sp` | **Passes** |
| `civitas_home.xml` | (Campaign banner body) | Caption | `12sp` | `12sp` | **Passes** |
| `civitas_home.xml` | (Section title: Progress Laporan) | Section Title | `16sp` | `18sp` | **Violation**: Under-sized for section header |
| `civitas_home.xml` | (Section title: Info Kehilangan) | Section Title | `16sp` | `18sp` | **Violation**: Under-sized for section header |
| `civitas_home.xml` | (Copyright Footer text) | Caption | `10sp` | `12sp` | **Violation**: Below caption minimum of `12sp` |
| `civitas_form_laporan.xml` | `etDeskripsi` (hint) | Body Text (hint) | `14sp` | `16sp` | **Violation**: Hints must match standard body text size |
| `civitas_form_laporan.xml` | `etWaktu` / `etTanggal` (hint) | Body Text (hint) | `14sp` | `16sp` | **Violation**: Hints must match standard body text size |
| `civitas_form_laporan.xml` | `etLokasi` (hint) | Body Text (hint) | `14sp` | `16sp` | **Violation**: Hints must match standard body text size |
| `civitas_form_laporan.xml` | `etNamaPelapor` / `etNimPelapor` | Body Text (input) | `14sp` | `16sp` | **Violation**: Text inputs must match body minimum |
| `civitas_form_laporan.xml` | `etKontak` (input) | Body Text (input) | `14sp` | `16sp` | **Violation**: Text inputs must match body minimum |
| `civitas_form_laporan.xml` | `tvUploadBukti` (text) | Body Text / Trigger | `14sp` | `16sp` | **Violation**: Trigger must match body minimum |
| `civitas_form_laporan.xml` | (Copyright Footer text) | Caption | `10sp` | `12sp` | **Violation**: Below caption minimum |
| `activity_detail_laporan.xml` | (Toolbar Title) | Screen Title | `18sp` | `20sp` | **Violation**: Screen title should be `20sp` |
| `activity_detail_laporan.xml` | `tvDetailDescription` | Body Text | `14sp` | `16sp` | **Violation**: Core content text is below body minimum |
| `activity_detail_laporan.xml` | (Description header) | Section Title | `16sp` | `18sp` | **Violation**: Header is under-sized |
| `activity_detail_laporan.xml` | (Evidence header) | Section Title | `16sp` | `18sp` | **Violation**: Header is under-sized |
| `activity_list_laporan.xml` | (Toolbar Title) | Screen Title | `18sp` | `20sp` | **Violation**: Screen title should be `20sp` |
| `activity_lost_items.xml` | (Toolbar Title) | Screen Title | `18sp` | `20sp` | **Violation**: Screen title should be `20sp` |
| `activity_panic.xml` | (Toolbar Title) | Screen Title | `18sp` | `20sp` | **Violation**: Screen title should be `20sp` |
| `civitas_item_category.xml` | `tvCivitasCategoryLabel` | Caption | `10sp` | `12sp` | **Violation**: Category labels are too small |
| `civitas_item_lost_item.xml` | (Laporan Kehilangan tag) | Caption / Tag | `8sp` | `12sp` | **Violation**: Tag text is too small |
| `civitas_item_lost_item.xml` | `tvCivitasLostItemTitle` | Body Text | `14sp` | `16sp` | **Violation**: Item title should be body minimum |
| `civitas_item_lost_item.xml` | `tvCivitasLostItemTime` | Caption | `10sp` | `12sp` | **Violation**: Sub-label dates are too small |
| `civitas_item_report_progress.xml` | `tvReportTitle` | Body Text | `14sp` | `16sp` | **Violation**: Card title is below body minimum |
| `civitas_item_report_progress.xml` | `tvReportStatus` | Caption | `10sp` | `12sp` | **Violation**: Status label is too small |

---

## 2. Corner Radius Analysis

### Target Rules
*   **Inputs**: `16dp`
*   **Buttons**: `16dp`
*   **Cards**: `20dp`
*   **Dialogs**: `24dp`

### Findings & Violations

| File Name / Component | Component ID | Type | Current Radius | Proposed Radius | Status / Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `activity_login.xml` | `MaterialCardView` | Card | `16dp` | `20dp` | **Violation**: Increase card roundedness |
| `activity_login.xml` | `btnLogin` | Button | `12dp` | `16dp` | **Violation**: Increase button roundedness |
| `civitas_home.xml` | (Campaign banner) | Card | `16dp` | `20dp` | **Violation**: Increase card roundedness |
| `civitas_home.xml` | `btnCivitasPanic` | Button | `12dp` | `16dp` | **Violation**: Increase button roundedness |
| `civitas_form_laporan.xml` | `@drawable/bg_input_cyan` | Input Shape | `8dp` | `16dp` | **Violation**: Increase input roundedness |
| `civitas_form_laporan.xml` | `btnKirimLaporan` | Button | (Default: ~4dp) | `16dp` | **Violation**: Needs explicit button rounding style |
| `activity_detail_laporan.xml` | `cardStatus` | Card | `12dp` | `20dp` | **Violation**: Increase card roundedness |
| `activity_detail_laporan.xml` | `layoutEvidence` Card | Card | `8dp` | `20dp` | **Violation**: Increase card roundedness |
| `civitas_item_lost_item.xml` | CardView parent | Card | `12dp` | `20dp` | **Violation**: Increase card roundedness |
| `civitas_item_lost_item.xml` | `btnCivitasSelengkapnyaLost` | Button | `8dp` | `16dp` | **Violation**: Increase button roundedness |
| `civitas_item_report_progress.xml` | CardView parent | Card | `12dp` | `20dp` | **Violation**: Increase card roundedness |
| `civitas_item_report_progress.xml` | `btnSelengkapnya` | Button | (Default: ~4dp) | `16dp` | **Violation**: Needs explicit button rounding style |
| `CivitasHomeActivity.kt` | Alert Dialogs | Dialog | (Default M3: 28dp) | `24dp` | **Violation**: Override AlertDialog style for consistency |

---

## 3. Borders & Elevation Analysis

### Target Rules
*   **Borders**: Introduce `1.5dp` to `2dp` borders where appropriate (TextInputLayouts, Cards, Status containers) to replace raw shadow elevations or plain flat backgrounds.
*   **Elevation**: Reduce heavy drop shadows (e.g., `8dp`) in favor of subtle elevations (e.g., `1dp` or `2dp`) or border lines.

### Findings & Violations

*   **`activity_login.xml` Card Container**:
    *   *Current*: `app:cardElevation="8dp"` without borders. The shadow is heavy and inconsistent with flat M3.
    *   *Proposed*: Decrease to `app:cardElevation="2dp"` and add a `1.5dp` stroke/border (`app:strokeWidth="1.5dp"` with color `#E2E8F0` or `@color/selector_input_color` theme matches).
*   **`activity_login.xml` TextInputLayouts**:
    *   *Current*: Standard thin outlined border.
    *   *Proposed*: Keep standard box borders but verify outline states. Focused state border thickness can be emphasized at `2dp`.
*   **`civitas_form_laporan.xml` Inputs (`@drawable/bg_input_cyan`)**:
    *   *Current*: Stroke border is `1dp` width.
    *   *Proposed*: Modernize input outline to `1.5dp` stroke width to improve contrast and clarity of form elements.
*   **`civitas_item_lost_item.xml` & `civitas_item_report_progress.xml` Card Items**:
    *   *Current*: Standard gray background (`#F5F5F5`) with no borders and raw default shadow elevations.
    *   *Proposed*: Add `app:strokeWidth="1.5dp"` and `app:strokeColor="#E2E8F0"` (light grey), reduce background tint to pure white (`#FFFFFF`) or off-white, and remove heavy elevation.
*   **`activity_detail_laporan.xml` Status Card (`cardStatus`)**:
    *   *Current*: Flat card with no borders.
    *   *Proposed*: Add `app:strokeWidth="1.5dp"` and map borders programmatically to status colors, while keeping card elevation flat.

---

## 4. Icon Audit & Replacements

### Current `@android:drawable` and Legacy Assets
The codebase uses old Android system drawables (`@android:drawable/...`), legacy launcher backgrounds, or low-res placeholders.

| File Name | Component ID | Current Icon Source | Visual Description | Proposed Material Symbol (Rounded) |
| :--- | :--- | :--- | :--- | :--- |
| `civitas_home.xml` | `btnCivitasChat` | `@android:drawable/stat_notify_chat` | Legacy SMS-style dialog icon | `ic_rounded_chat` (Chat bubble) |
| `civitas_home.xml` | `btnCivitasNotification` | `@android:drawable/ic_popup_reminder` | Legacy pop-up alarm clock | `ic_rounded_notifications` (Bell) |
| `civitas_home.xml` | `imgCivitasProfile` | `@drawable/ic_launcher_background` | Default system grid image | `ic_rounded_account_circle` (User icon vector) |
| `civitas_form_laporan.xml` | `etLokasi` | `@android:drawable/ic_menu_mylocation` | Legacy concentric crosshair icon | `ic_rounded_my_location` (Target crosshair) |
| `activity_detail_laporan.xml` | `btnBack` | `@android:drawable/ic_menu_revert` | Legacy curved undo arrow | `ic_rounded_arrow_back` (Back arrow) |
| `activity_detail_laporan.xml` | `imgDetailEvidence` | `@android:drawable/ic_menu_gallery` | System picture folder frame | `ic_rounded_image` (Standard image placeholder) |
| `activity_list_laporan.xml` | `btnBack` | `@android:drawable/ic_menu_revert` | Legacy curved undo arrow | `ic_rounded_arrow_back` |
| `activity_list_laporan.xml` | Empty state icon | `@android:drawable/ic_menu_search` | Legacy system search lens | `ic_rounded_search_off` / Custom empty state graphic |
| `activity_lost_items.xml` | `btnBack` | `@android:drawable/ic_menu_revert` | Legacy curved undo arrow | `ic_rounded_arrow_back` |
| `activity_panic.xml` | `btnBack` | `@android:drawable/ic_menu_revert` | Legacy curved undo arrow | `ic_rounded_arrow_back` |
| `civitas_item_category.xml` | `viewCivitasCategoryIcon` | `@android:drawable/presence_online` | Standard green dot indicator | *Category-specific rounded icons* (shield, build, warning, etc.) |

---

## 5. Spacing Scale Analysis

### Unified Scale Standard
`4dp`, `8dp`, `12dp`, `16dp`, `24dp`, `32dp`

### Scale Violations
The layout XML files contain off-scale margins and paddings, primarily using `20dp`. These values should be normalized to the nearest scale standard (`16dp` or `24dp`).

1.  **`civitas_form_laporan.xml` Padding**:
    *   *Violator*: Parent `LinearLayout` padding is set to `20dp`.
    *   *Proposed*: Normalize to `24dp` to provide better page breathing room.
    *   *Violator*: Footer layout margin bottom is set to `20dp`.
    *   *Proposed*: Normalize to `24dp` or `16dp`.
2.  **`activity_detail_laporan.xml` Padding**:
    *   *Violator*: Detail content container padding is set to `20dp`.
    *   *Proposed*: Normalize to `16dp` or `24dp` based on scrolling boundaries.
    *   *Violator*: `cardStatus` bottom margin is set to `20dp`.
    *   *Proposed*: Normalize to `24dp`.
    *   *Violator*: Evidence image card bottom margin is set to `20dp`.
    *   *Proposed*: Normalize to `24dp`.
3.  **`activity_panic.xml` Spacing**:
    *   *Violator*: Content layout padding is set to `20dp`.
    *   *Proposed*: Normalize to `24dp` (matches standard page padding).
    *   *Violator*: ProgressBar top margin is set to `20dp`.
    *   *Proposed*: Normalize to `24dp`.
    *   *Violator*: Retry button top margin is set to `20dp`.
    *   *Proposed*: Normalize to `24dp`.
