package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

sealed class VoiceParsedResult {
    data class JobDraft(
        val implementName: String,
        val hours: Int,
        val minutes: Int,
        val area: Double = 0.0,
        val customerName: String? = null,
        val originalText: String
    ) : VoiceParsedResult()

    data class PaymentDraft(
        val customerName: String,
        val amount: Double,
        val method: String, // CASH, UPI
        val originalText: String
    ) : VoiceParsedResult()

    data class Unrecognized(val originalText: String) : VoiceParsedResult()
}

object VoiceParser {
    private val hindiNumberMap = mapOf(
        "एक" to 1.0, "दो" to 2.0, "तीन" to 3.0, "चार" to 4.0, "पांच" to 5.0,
        "छह" to 6.0, "सात" to 7.0, "आठ" to 8.0, "नौ" to 9.0, "दस" to 10.0,
        "ग्यारह" to 11.0, "बारह" to 12.0, "पंद्रह" to 15.0, "बीस" to 20.0,
        "पच्चीस" to 25.0, "तीस" to 30.0, "पैंतीस" to 35.0, "चालीस" to 40.0,
        "पचास" to 50.0, "साठ" to 60.0, "सौ" to 100.0, "हज़ार" to 1000.0,
        "हजार" to 1000.0, "लाख" to 100000.0,
        "डेढ़" to 1.5, "ढाई" to 2.5, "one" to 1.0, "two" to 2.0, "three" to 3.0,
        "four" to 4.0, "five" to 5.0, "hundred" to 100.0, "thousand" to 1000.0
    )

    fun parse(text: String): VoiceParsedResult {
        val lower = text.lowercase(Locale.getDefault())

        // Check for Payment: e.g. "Ramesh ne 2000 diye cash" or "सुरेश 1500 रुपये नकद"
        if (lower.contains("diye") || lower.contains("दिए") || lower.contains("रुपये") ||
            lower.contains("rupee") || lower.contains("rupees") || lower.contains("paid") ||
            lower.contains("cash") || lower.contains("upi") || lower.contains("नकद")
        ) {
            val amount = extractAmount(lower)
            if (amount > 0) {
                val method = if (lower.contains("upi") || lower.contains("ऑनलाइन")) "UPI" else "CASH"
                val customer = extractCustomerName(lower)
                return VoiceParsedResult.PaymentDraft(
                    customerName = customer,
                    amount = amount,
                    method = method,
                    originalText = text
                )
            }
        }

        // Check for Job: e.g. "रोटावेटर एक घंटा बीस मिनट" or "Cultivator 2 hours"
        val implement = extractImplement(lower)
        if (implement != null) {
            val (h, m) = extractHoursMinutes(lower)
            val area = extractArea(lower)
            return VoiceParsedResult.JobDraft(
                implementName = implement,
                hours = h,
                minutes = m,
                area = area,
                originalText = text
            )
        }

        return VoiceParsedResult.Unrecognized(text)
    }

    private fun extractAmount(text: String): Double {
        // Try digits first: 2000, 1500
        val digitRegex = Regex("""(\d+)""")
        val match = digitRegex.find(text)
        if (match != null) {
            return match.groupValues[1].toDoubleOrNull() ?: 0.0
        }

        // Try hindi words: e.g. "दो हज़ार"
        var total = 0.0
        var current = 0.0
        val tokens = text.split(" ", "।", ",")
        for (token in tokens) {
            val clean = token.trim()
            val num = hindiNumberMap[clean]
            if (num != null) {
                if (num >= 100.0) {
                    total += if (current > 0) current * num else num
                    current = 0.0
                } else {
                    current += num
                }
            }
        }
        total += current
        return total
    }

    private fun extractCustomerName(text: String): String {
        val clean = text.replace("ne", "")
            .replace("ने", "")
            .replace("diye", "")
            .replace("दिए", "")
            .replace("cash", "")
            .replace("नकद", "")
            .replace("rupees", "")
            .replace("रुपये", "")
            .replace(Regex("""\d+"""), "")
            .trim()
        val words = clean.split(" ").filter { it.isNotBlank() }
        return if (words.isNotEmpty()) words.first().replaceFirstChar { it.uppercase() } else "किसान (Customer)"
    }

    private fun extractImplement(text: String): String? {
        return when {
            text.contains("रोटावेटर") || text.contains("rotavator") -> "रोटावेटर (Rotavator)"
            text.contains("कल्टीवेटर") || text.contains("cultivator") -> "कल्टीवेटर (Cultivator)"
            text.contains("प्लाउ") || text.contains("plough") -> "एमबी प्लाउ (MB Plough)"
            text.contains("थ्रेशर") || text.contains("thresher") -> "थ्रेशर (Thresher)"
            text.contains("लेज़र") || text.contains("laser") -> "लेज़र लेवलर (Laser Leveller)"
            text.contains("सीड ड्रिल") || text.contains("seed drill") -> "सीड ड्रिल (Seed Drill)"
            text.contains("ट्राली") || text.contains("trolley") -> "ट्राली / ढुलाई (Trolley)"
            text.contains("रीपर") || text.contains("reaper") -> "भूसा रीपर (Straw Reaper)"
            else -> null
        }
    }

    private fun extractHoursMinutes(text: String): Pair<Int, Int> {
        var hours = 0
        var minutes = 0

        if (text.contains("डेढ़ घंटा") || text.contains("1.5")) {
            return Pair(1, 30)
        }
        if (text.contains("ढाई घंटा") || text.contains("2.5")) {
            return Pair(2, 30)
        }

        // Check hour: "2 घंटा", "दो घंटा", "2 hours"
        val hourMatch = Regex("""(\d+)\s*(घंटा|घंटे|hour|hours|h)""").find(text)
        if (hourMatch != null) {
            hours = hourMatch.groupValues[1].toIntOrNull() ?: 0
        } else {
            // Words: "दो घंटा", "एक घंटा"
            for ((word, num) in hindiNumberMap) {
                if (text.contains("$word घंटा") || text.contains("$word घंटे") || text.contains("$word hour")) {
                    hours = num.toInt()
                    break
                }
            }
        }

        // Check minutes: "20 मिनट", "बीस मिनट", "30 min"
        val minMatch = Regex("""(\d+)\s*(मिनट|minute|minutes|min)""").find(text)
        if (minMatch != null) {
            minutes = minMatch.groupValues[1].toIntOrNull() ?: 0
        } else {
            for ((word, num) in hindiNumberMap) {
                if (text.contains("$word मिनट") || text.contains("$word min")) {
                    minutes = num.toInt()
                    break
                }
            }
        }

        if (hours == 0 && minutes == 0) {
            // Default 1 hour if only implement spoken
            hours = 1
        }

        return Pair(hours, minutes)
    }

    private fun extractArea(text: String): Double {
        val match = Regex("""(\d+(\.\d+)?)\s*(एकड़|acre|acres)""").find(text)
        return match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    }
}
