# Unram Hub: Visual Modernization Plan (UI Phase B1)

This plan outlines the specific visual adjustments required to modernize the existing user interface. It focuses on typography normalization, corner radius consistency, border strokes, shadow/elevation reductions, icon replacements, and spacing scale normalization.

> [!IMPORTANT]
> **Design Constraint Check**: This plan maintains the current Figma color scheme, screen flow, navigation, features, and business logic intact. It only refines the layouts and resource drawables.

---

## 1. Plan Breakdown

### Phase B1.1: Typography Normalization
*   **Goal**: Increase readability and establish typographical hierarchy by updating font sizes.
*   **Affected Files & Changes**:
    1.  `app/src/main/res/layout/civitas_home.xml`:
        *   Change Section headers (titles above report progress list and lost items list) text size from `16sp` to `18sp`.
        *   Change copyright footer text size from `10sp` to `12sp`.
    2.  `app/src/main/res/layout/civitas_form_laporan.xml`:
        *   Change edit text sizes & hints for `etDeskripsi`, `etWaktu`, `etTanggal`, `etLokasi`, `etNamaPelapor`, `etNimPelapor`, `etKontak`, and evidence field trigger text `tvUploadBukti` from `14sp` to `16sp`.
        *   Change copyright footer text size from `10sp` to `12sp`.
    3.  `app/src/main/res/layout/activity_detail_laporan.xml`:
        *   Change Toolbar Title text size from `18sp` to `20sp`.
        *   Change description headers and evidence headers text size from `16sp` to `18sp`.
        *   Change description body `tvDetailDescription` text size from `14sp` to `16sp`.
    4.  `app/src/main/res/layout/activity_list_laporan.xml`, `activity_lost_items.xml`, `activity_panic.xml`:
        *   Change Toolbar Title text size from `18sp` to `20sp`.
    5.  `app/src/main/res/layout/civitas_item_category.xml`:
        *   Change category label text size `tvCivitasCategoryLabel` from `10sp` to `12sp`.
    6.  `app/src/main/res/layout/civitas_item_lost_item.xml`:
        *   Change report type tag label text size from `8sp` to `12sp`.
        *   Change lost item title `tvCivitasLostItemTitle` text size from `14sp` to `16sp`.
        *   Change sub-label date/time `tvCivitasLostItemTime` text size from `10sp` to `12sp`.
    7.  `app/src/main/res/layout/civitas_item_report_progress.xml`:
        *   Change progress card title `tvReportTitle` text size from `14sp` to `16sp`.
        *   Change progress status label `tvReportStatus` text size from `10sp` to `12sp`.
*   **Impact Level**: High (Immediate enhancement in text clarity and visual scale structure).
*   **Risk Level**: Very Low (Safe text size updates).

---

### Phase B1.2: Corner Radius Alignment
*   **Goal**: Create a consistent rounded-corner language that matches contemporary Material 3 shapes.
*   **Affected Files & Changes**:
    1.  `app/src/main/res/layout/activity_login.xml`:
        *   Change `MaterialCardView` card corner radius from `16dp` to `20dp`.
        *   Change login button `btnLogin` corner radius from `12dp` to `16dp`.
    2.  `app/src/main/res/layout/civitas_home.xml`:
        *   Change Campaign banner CardView corner radius from `16dp` to `20dp`.
        *   Change Panic button `btnCivitasPanic` corner radius from `12dp` to `16dp`.
    3.  `app/src/main/res/drawable/bg_input_cyan.xml`:
        *   Change solid shape corner radius from `8dp` to `16dp`.
    4.  `app/src/main/res/layout/civitas_form_laporan.xml`:
        *   Change submit button `btnKirimLaporan` corner radius to `16dp` by adding `app:cornerRadius="16dp"`.
    5.  `app/src/main/res/layout/activity_detail_laporan.xml`:
        *   Change status CardView `cardStatus` corner radius from `12dp` to `20dp`.
        *   Change evidence container CardView corner radius from `8dp` to `20dp`.
    6.  `app/src/main/res/layout/civitas_item_lost_item.xml`:
        *   Change CardView parent corner radius from `12dp` to `20dp`.
        *   Change button `btnCivitasSelengkapnyaLost` corner radius from `8dp` to `16dp`.
    7.  `app/src/main/res/layout/civitas_item_report_progress.xml`:
        *   Change CardView parent corner radius from `12dp` to `20dp`.
        *   Change button `btnSelengkapnya` corner radius to `16dp` by adding `app:cornerRadius="16dp"`.
    8.  `app/src/main/res/values/themes.xml` & `app/src/main/res/values-night/themes.xml`:
        *   Define dialog style shape overlay properties in themes to force AppCompat Alert Dialogs to default to a rounded corner radius of `24dp` (Dialog standard).
*   **Impact Level**: High (Direct improvements in component consistency).
*   **Risk Level**: Low (Requires verification of layout boundaries).

---

### Phase B1.3: Borders & Visual Accents
*   **Goal**: Replace heavy elevations and flat colors with clean, subtle outlines.
*   **Affected Files & Changes**:
    1.  `app/src/main/res/layout/activity_login.xml`:
        *   Add border to login `MaterialCardView`: `app:strokeWidth="1.5dp"` and `app:strokeColor="#E2E8F0"`.
    2.  `app/src/main/res/drawable/bg_input_cyan.xml`:
        *   Change border outline stroke width from `1dp` to `1.5dp`.
    3.  `app/src/main/res/layout/civitas_item_lost_item.xml`:
        *   Add border to CardView parent: `app:strokeWidth="1.5dp"` and `app:strokeColor="#E2E8F0"`.
        *   Change card background to pure white `#FFFFFF` instead of off-white `#F5F5F5` to emphasize outline structure.
    4.  `app/src/main/res/layout/civitas_item_report_progress.xml`:
        *   Add border to CardView parent: `app:strokeWidth="1.5dp"` and `app:strokeColor="#E2E8F0"`.
        *   Change card background to pure white `#FFFFFF`.
    5.  `app/src/main/res/layout/activity_detail_laporan.xml`:
        *   Add border to evidence CardView container: `app:strokeWidth="1.5dp"` and `app:strokeColor="#E2E8F0"`.
*   **Impact Level**: Medium (Subtle UI crispness enhancement).
*   **Risk Level**: Very Low.

---

### Phase B1.4: Icon Modernization
*   **Goal**: Migrate away from old system assets (`@android:drawable/*`) and legacy icons to modern vector assets based on "Material Symbols Rounded".
*   **Affected Files & Changes**:
    1.  **Add vector assets** under `app/src/main/res/drawable/`:
        *   `ic_rounded_chat.xml` (Replaces `stat_notify_chat`)
        *   `ic_rounded_notifications.xml` (Replaces `ic_popup_reminder`)
        *   `ic_rounded_account_circle.xml` (Replaces Profile grid default)
        *   `ic_rounded_my_location.xml` (Replaces `ic_menu_mylocation`)
        *   `ic_rounded_arrow_back.xml` (Replaces `ic_menu_revert`)
        *   `ic_rounded_image.xml` (Replaces `ic_menu_gallery`)
        *   `ic_rounded_search_off.xml` (Replaces Empty state lens)
        *   Category icons: `ic_rounded_gavel.xml` (Harassment), `ic_rounded_build.xml` (Facility), `ic_rounded_warning.xml` (Emergency), `ic_rounded_find_in_page.xml` (Lost/Found), `ic_rounded_more_horiz.xml` (Others).
    2.  Update source layouts reference paths to these newly generated resources.
*   **Impact Level**: High (Cleans up outdated, pixelated system visuals).
*   **Risk Level**: Low (Ensure SVG path data matches standard icons).

---

### Phase B1.5: Spacing Scale Normalization
*   **Goal**: Resolve layout alignment issues by enforcing standard grid increments (`4dp`, `8dp`, `12dp`, `16dp`, `24dp`, `32dp`).
*   **Affected Files & Changes**:
    1.  `app/src/main/res/layout/civitas_form_laporan.xml`:
        *   Change parent layout padding from `20dp` to `24dp`.
        *   Change footer margin bottom from `20dp` to `24dp`.
    2.  `app/src/main/res/layout/activity_detail_laporan.xml`:
        *   Change parent layout padding from `20dp` to `24dp`.
        *   Change status CardView bottom margin from `20dp` to `24dp`.
        *   Change evidence container bottom margin from `20dp` to `24dp`.
    3.  `app/src/main/res/layout/activity_panic.xml`:
        *   Change parent layout padding from `20dp` to `24dp`.
        *   Change progress indicator top margin from `20dp` to `24dp`.
        *   Change retry button top margin from `20dp` to `24dp`.
*   **Impact Level**: Medium (Subtle balance improvements).
*   **Risk Level**: Low (Verify layout heights on smaller screen dimensions).

---

### Phase B1.6: Elevation Softening
*   **Goal**: Soften card dropshadows to align with flat design aesthetics.
*   **Affected Files & Changes**:
    1.  `app/src/main/res/layout/activity_login.xml`:
        *   Reduce central card `app:cardElevation` from `8dp` to `2dp`.
    2.  `app/src/main/res/layout/civitas_item_report_progress.xml`:
        *   Reduce card `app:cardElevation` from `2dp` to `1dp`.
    3.  `app/src/main/res/layout/civitas_item_lost_item.xml`:
        *   Reduce card `app:cardElevation` from default to `1dp`.
    4.  `app/src/main/res/layout/activity_detail_laporan.xml`:
        *   Reduce status card `cardElevation` from `2dp` to `0dp` (reliance on border stroke layout).
        *   Reduce evidence card `cardElevation` from `1dp` to `0dp` (reliance on border stroke layout).
*   **Impact Level**: Medium (Softens visual weights).
*   **Risk Level**: Very Low.

---

## 2. Prioritized Execution Order

To run these modifications systematically, changes will be executed in **6 distinct sub-phases**:

1.  **Phase B1.1 Typography**: Normalizes text scales across all layout screens first, securing content readability bounds.
2.  **Phase B1.2 Radius**: Enforces consistent button, input, card, and dialog curvature boundaries.
3.  **Phase B1.3 Borders**: Introduces outlining rules for inputs, card structures, and indicators.
4.  **Phase B1.4 Icons**: Replaces old system drawings with crisp, vector Material Symbols Rounded assets.
5.  **Phase B1.5 Spacing**: Standardizes layout paddings and margins on standard grid intervals.
6.  **Phase B1.6 Elevation**: Normalizes dropshadows and removes stark visual gradients.
