package com.example.util

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

enum class LogLevel {
    INFO,
    WARN,
    ERROR,
    CRASH
}

data class DiagnosticsLogEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String,
    val stackTrace: String? = null
)

object DiagnosticsManager {
    private const val TAG = "KhetDiagnostics"
    private const val MAX_LOGS = 60
    private const val LOG_FILE_NAME = "khet_diagnostics.log"

    private val inMemoryLogs = CopyOnWriteArrayList<DiagnosticsLogEntry>()
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun initialize(context: Context) {
        logInfo("AppInit", "KhetSaathi Diagnostics initialized on Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        loadExistingLogsFromFile(context)
    }

    fun logInfo(tag: String, message: String) {
        try { Log.i(tag, message) } catch (_: Throwable) {}
        addEntry(DiagnosticsLogEntry(level = LogLevel.INFO, tag = tag, message = message))
    }

    fun logWarning(tag: String, message: String) {
        try { Log.w(tag, message) } catch (_: Throwable) {}
        addEntry(DiagnosticsLogEntry(level = LogLevel.WARN, tag = tag, message = message))
    }

    fun logError(tag: String, message: String, throwable: Throwable? = null, context: Context? = null) {
        val stack = throwable?.let { getStackTraceString(it) }
        try { Log.e(tag, "$message : $stack", throwable) } catch (_: Throwable) {}
        val entry = DiagnosticsLogEntry(
            level = LogLevel.ERROR,
            tag = tag,
            message = message,
            stackTrace = stack
        )
        addEntry(entry)
        context?.let { writeEntryToFile(it, entry) }
    }

    fun logCrash(throwable: Throwable, context: Context? = null) {
        val stack = getStackTraceString(throwable)
        try { Log.e("CrashHandler", "Uncaught exception: $stack", throwable) } catch (_: Throwable) {}
        val entry = DiagnosticsLogEntry(
            level = LogLevel.CRASH,
            tag = "CRASH",
            message = throwable.localizedMessage ?: throwable.javaClass.simpleName,
            stackTrace = stack
        )
        addEntry(entry)
        context?.let { writeEntryToFile(it, entry) }
    }

    private fun addEntry(entry: DiagnosticsLogEntry) {
        inMemoryLogs.add(0, entry)
        while (inMemoryLogs.size > MAX_LOGS) {
            inMemoryLogs.removeAt(inMemoryLogs.lastIndex)
        }
    }

    fun getLogs(): List<DiagnosticsLogEntry> {
        return inMemoryLogs.toList()
    }

    fun clearLogs(context: Context? = null) {
        inMemoryLogs.clear()
        if (context != null) {
            try {
                val file = File(context.filesDir, LOG_FILE_NAME)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear log file", e)
            }
        }
        logInfo("Diagnostics", "Logs cleared by user")
    }

    fun getSystemReport(context: Context): String {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val runtime = Runtime.getRuntime()
        val maxMemoryMb = runtime.maxMemory() / (1024 * 1024)
        val totalMemoryMb = runtime.totalMemory() / (1024 * 1024)
        val freeMemoryMb = runtime.freeMemory() / (1024 * 1024)
        val usedMemoryMb = totalMemoryMb - freeMemoryMb

        val totalRamMb = (memInfo.totalMem) / (1024 * 1024)
        val availRamMb = (memInfo.availMem) / (1024 * 1024)

        val sb = StringBuilder()
        sb.appendLine("=== KhetSaathi Diagnostics Report ===")
        sb.appendLine("Timestamp: ${dateFormatter.format(Date())}")
        sb.appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
        sb.appendLine("Android OS: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        sb.appendLine("App Heap (Max/Total/Used/Free): ${maxMemoryMb}MB / ${totalMemoryMb}MB / ${usedMemoryMb}MB / ${freeMemoryMb}MB")
        sb.appendLine("System RAM (Avail/Total): ${availRamMb}MB / ${totalRamMb}MB")
        sb.appendLine("Low Memory State: ${memInfo.lowMemory} (Threshold: ${memInfo.threshold / (1024 * 1024)}MB)")
        sb.appendLine("Log Entries Count: ${inMemoryLogs.size}")
        sb.appendLine()
        sb.appendLine("--- Recent Logs ---")

        if (inMemoryLogs.isEmpty()) {
            sb.appendLine("No errors or warnings recorded.")
        } else {
            inMemoryLogs.take(25).forEach { log ->
                val timeStr = dateFormatter.format(Date(log.timestamp))
                sb.appendLine("[$timeStr] [${log.level}] ${log.tag}: ${log.message}")
                if (!log.stackTrace.isNullOrBlank()) {
                    sb.appendLine("Stack:")
                    sb.appendLine(log.stackTrace.lines().take(6).joinToString("\n") { "  $it" })
                }
            }
        }
        sb.appendLine("=====================================")
        return sb.toString()
    }

    fun copyToClipboard(context: Context): Boolean {
        return try {
            val report = getSystemReport(context)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("KhetSaathi Diagnostics", report)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "डायग्नोस्टिक्स रिपोर्ट कॉपी हो गई (Diagnostics copied)", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy diagnostics", e)
            false
        }
    }

    private fun getStackTraceString(t: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        t.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    private fun writeEntryToFile(context: Context, entry: DiagnosticsLogEntry) {
        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            val line = "${entry.timestamp}|${entry.level}|${entry.tag}|${entry.message.replace("\n", " ")}\n"
            file.appendText(line)
        } catch (_: Exception) {
            // Ignore disk write failure during crash handling
        }
    }

    private fun loadExistingLogsFromFile(context: Context) {
        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            if (file.exists()) {
                val lines = file.readLines().takeLast(30)
                for (line in lines) {
                    val parts = line.split("|", limit = 4)
                    if (parts.size >= 4) {
                        val ts = parts[0].toLongOrNull() ?: System.currentTimeMillis()
                        val lvl = try { LogLevel.valueOf(parts[1]) } catch (_: Exception) { LogLevel.INFO }
                        val tag = parts[2]
                        val msg = parts[3]
                        inMemoryLogs.add(DiagnosticsLogEntry(timestamp = ts, level = lvl, tag = tag, message = msg))
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore read errors
        }
    }
}
