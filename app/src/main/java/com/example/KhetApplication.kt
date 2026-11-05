package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.util.DiagnosticsManager

class KhetApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Diagnostics
        DiagnosticsManager.initialize(this)

        // 2. Setup Global Uncaught Exception Handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                DiagnosticsManager.logCrash(throwable, this)
            } catch (_: Exception) {
                // Safeguard against logging error during fatal crash
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    override fun newImageLoader(): ImageLoader {
        // Conservative Coil configuration tailored for low-end 1-3 GB RAM Android phones
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    // Restrict in-memory bitmap cache to 15% of available JVM heap to prevent OOM
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(40L * 1024 * 1024) // 40 MB max disk cache
                    .build()
            }
            .crossfade(150)
            .respectCacheHeaders(false)
            .build()
    }
}
