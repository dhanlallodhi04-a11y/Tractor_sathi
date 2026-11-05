# KhetSaathi (खेत साथी) - Performance & Stability Architecture

## Overview
KhetSaathi is an offline-first tractor and implement bookkeeping application built specifically for rural tractor owners who operate machinery for hire. To deliver a smooth 60fps experience without Out-Of-Memory (OOM) crashes on budget smartphones (1–3 GB RAM running Android 8.0+ / API 26+), the codebase adheres to strict memory, render, and crash-safety patterns.

---

## Top 5 Causes of Crash & Lag on Low-End Android Phones & Verified Fixes

### 1. Large Camera Bitmaps Exhausting JVM Heap (OOM)
* **Root Cause:** Modern smartphone cameras produce 12MP to 48MP photos. Decoding a single 48MP image into an ARGB_8888 Bitmap requires ~192 MB of contiguous RAM, instantly killing apps on 1–2 GB RAM devices where standard JVM heap limits are 128 MB or 192 MB.
* **Architecture Fix:**
  - `ImageCompressor.kt`: Implements a two-pass decode. The first pass reads only image dimensions (`inJustDecodeBounds = true`), computes an integer `inSampleSize` power of 2, and decodes using `Bitmap.Config.RGB_565` (50% memory savings compared to ARGB_8888).
  - Scaled down so that the maximum dimension is strictly `<= 800px` (or `<= 256px` for avatars).
  - Compressed to JPEG at 70% quality (target size `<= 150 KB`) and written to private app storage (`context.filesDir/images/`).
  - Saved as a file URI (`file://...`) in Room database, never stored as raw Base64 strings or in-memory ByteArrays.
  - Image Bitmaps are immediately `.recycle()`-d after disk write.

### 2. Coil Unbounded Memory Caching on Constrained RAM
* **Root Cause:** By default, image loading libraries can allocate 25–40% of the maximum heap for memory caches, leaving inadequate room for Compose composition trees and Room query buffers.
* **Architecture Fix:**
  - `KhetApplication.kt` implements `ImageLoaderFactory`, creating a conservative Coil `ImageLoader`:
    - `MemoryCache.Builder(this).maxSizePercent(0.15).build()` restricts bitmap memory caching to 15% of available JVM heap.
    - Disk cache capped at 40 MB in `cacheDir/image_cache`.
    - Crossfade animation restricted to 150ms.
    - List view thumbnails load compact square views rather than full resolution assets.

### 3. Infinite Render Loops & Inner-Loop Allocations During Filtering
* **Root Cause:**
  - Calling mutable objects like `Calendar.getInstance().add(Calendar.DAY_OF_YEAR, -30)` inside `.filter { ... }` lambdas over large datasets mutates the calendar instance on every item iteration, compounding time offset errors and triggering continuous garbage collection (GC pauses / jank).
  - Un-debounced search inputs trigger full list reallocation and layout passes on every character keystroke.
* **Architecture Fix:**
  - Pre-calculate date filter boundaries once using `remember(selectedFilter)` before passing to `filteredList` or `derivedStateOf`.
  - Added 250ms debouncing (`LaunchedEffect(searchQuery) { delay(250); debouncedQuery = searchQuery }`) across Kisan and Implement search bars.
  - All `LazyColumn` items provide stable primitive `key = { it.id }` to avoid item recreation across recompositions.

### 4. Uncaught Exceptions Killing the App Process
* **Root Cause:** In Android WebView or Jetpack Compose, an unhandled runtime error or corrupt database record will crash the process with "app stopped".
* **Architecture Fix:**
  - **Application Uncaught Exception Handler:** Configured in `KhetApplication.onCreate()` via `Thread.setDefaultUncaughtExceptionHandler`. All fatal errors are written to an internal ring buffer and persistent log file (`filesDir/khet_diagnostics.log`).
  - **Compose Error Boundary (`KhetErrorBoundary.kt`):** Wraps all top-level destinations and screen transitions. If a runtime render error occurs, it presents a friendly Hindi/English "Something went wrong (कुछ गड़बड़ हुई)" recovery card with:
    - Assurance that user data remains safe.
    - "Reload Screen (पुनः लोड करें)" action.
    - "Copy Diagnostics (डायग्नोस्टिक्स कॉपी करें)" button.
  - **In-App Diagnostics Screen:** Accessible from More > "System Diagnostics & Error Log" with real-time JVM heap metrics, available RAM stats, and a one-tap copy button.

### 5. Double-Tap Race Conditions on Save Buttons
* **Root Cause:** Slow CPUs on entry-level phones create UI lag where users tap "Save" multiple times before navigation completes, resulting in duplicate jobs, duplicate khata balances, or database SQLiteConstraintExceptions.
* **Architecture Fix:**
  - Save buttons in `AddJobScreen`, `AddEditKisanScreen`, and `AddEditImplementScreen` use an `isSaving` boolean state flag.
  - The button is disabled (`enabled = !isSaving`) immediately on first tap.
  - Validations reset `isSaving = false` gracefully if required fields are missing.

---

## Android Manifest & Performance Flags
In `app/src/main/AndroidManifest.xml`:
- `android:largeHeap="true"`: Grants higher memory headroom on devices that support it, helping avoid OOM during multi-photo operations.
- `android:hardwareAccelerated="true"`: Ensures GPU rendering pipeline is active for smooth scrolling and animations.

---

## Diagnostics & Memory Profiling
To inspect app health:
1. Navigate to **More (अधिक)** -> **सिस्टम डायग्नोस्टिक्स एवं एरर लॉग (System Diagnostics & Error Log)**.
2. View current Max Heap, Used Heap, Total Heap, and Room entity counts.
3. Tap **कॉपी रिपोर्ट (Copy)** to export system specifications and stack traces to the clipboard.
