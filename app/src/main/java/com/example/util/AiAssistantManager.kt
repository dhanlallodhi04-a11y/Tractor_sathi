package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.Locale

private const val TAG = "AiAssistantManager"

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    ASSISTANT
}

class AiAssistantManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.ENGLISH)
            }
            isTtsReady = true
        }
    }

    fun speak(text: String) {
        if (isTtsReady) {
            try {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_AI_SAATHI")
            } catch (e: Exception) {
                Log.w(TAG, "TTS speak failed", e)
            }
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
    }

    // Lazy initialization of Gemini model to prevent startup crashes
    private val generativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 800
            }
        )
    }

    private fun loadHelpFaqs(): String {
        return try {
            val stream = context.assets.open("help.json")
            val jsonStr = stream.bufferedReader().use { it.readText() }
            val root = JSONObject(jsonStr)
            val faqs = root.optJSONArray("faqs")
            val sb = StringBuilder()
            if (faqs != null) {
                for (i in 0 until faqs.length()) {
                    val item = faqs.getJSONObject(i)
                    sb.append("प्र: ${item.optString("q")}\nउ: ${item.optString("a")}\n\n")
                }
            }
            sb.toString()
        } catch (_: Exception) {
            ""
        }
    }

    private fun getOfflineResponse(query: String): String {
        val q = query.lowercase(Locale.getDefault())
        return when {
            q.contains("रेट") || q.contains("rate") || q.contains("किराया") ->
                "रोटावेटर का औसत किराया ₹700-900 प्रति घंटा, कल्टीवेटर ₹600-800 प्रति घंटा और ट्रॉली ₹500-800 प्रति चक्कर रहता है। आप 'More > मेरे इंप्लीमेंट' में जाकर अपना रेट बदल सकते हैं।"
            q.contains("डीजल") || q.contains("diesel") || q.contains("खर्च") ->
                "डीजल खर्च कम करने के लिए सही RPM (1500-1800) पर काम करें, टायर का एयर प्रेशर सही रखें और रोटावेटर के ब्लेड धारदार रखें। हर डीजल भरवाने पर 'खर्च' टैब में एंट्री करें।"
            q.contains("काम") || q.contains("job") || q.contains("जोड़ें") ->
                "नया काम जोड़ने के लिए होम स्क्रीन या काम स्क्रीन पर नीचे दाईं तरफ '+' बटन दबाएं। किसान और उपकरण चुनकर घंटे या एकड़ दर्ज करें।"
            q.contains("बाकी") || q.contains("बकाया") || q.contains("हिसाब") ->
                "किसानों का बकाया देखने के लिए 'More > किसान' पर जाएं। वहां हर किसान के नाम के आगे लाल रंग में बकाया दिखेगा और WhatsApp तकादा संदेश भी भेज सकते हैं।"
            else ->
                "नमस्ते! मैं आपका किसान AI साथी हूँ। आप मुझसे ट्रेक्टर काम के रेट, डीजल बचत, किसान बहीखाता या ऐप उपयोग के बारे में कभी भी पूछ सकते हैं।"
        }
    }

    suspend fun askAssistant(userQuery: String, contextSummary: String = ""): String {
        return withContext(Dispatchers.IO) {
            try {
                val faqs = loadHelpFaqs()
                val prompt = buildString {
                    append("You are 'किसान AI साथी' (Kisan AI Saathi), an expert digital advisor for Indian tractor owners and custom hiring operators.\n")
                    append("Answer politely, warmly, and concisely in conversational Hindi (or Hinglish if appropriate).\n")
                    append("Keep responses under 3-4 short sentences so they are easy to read and listen on mobile.\n")
                    if (contextSummary.isNotBlank()) {
                        append("\nCurrent App Data:\n$contextSummary\n")
                    }
                    if (faqs.isNotBlank()) {
                        append("\nReference App FAQs:\n$faqs\n")
                    }
                    append("\nUser Question: $userQuery\n")
                    append("Answer:")
                }

                val response = withTimeout(30_000L) {
                    generativeModel.generateContent(prompt)
                }
                response.text?.trim()?.ifBlank { getOfflineResponse(userQuery) }
                    ?: getOfflineResponse(userQuery)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Gemini inference failed, falling back to offline", e)
                getOfflineResponse(userQuery)
            }
        }
    }

    suspend fun extractProfileDetails(speechText: String): Pair<String, String> {
        // Fast regex attempt first
        val regexResult = extractProfileRegex(speechText)
        if (regexResult.first.isNotBlank()) {
            return regexResult
        }

        // Fallback to Gemini with quick timeout
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    Extract the person's name and village/town from this spoken sentence:
                    "$speechText"
                    Respond strictly in this format:
                    NAME: <extracted name or blank>
                    VILLAGE: <extracted village or blank>
                """.trimIndent()

                val response = withTimeout(10_000L) {
                    generativeModel.generateContent(prompt)
                }
                val text = response.text.orEmpty()
                var name = ""
                var village = ""
                text.lines().forEach { line ->
                    if (line.startsWith("NAME:", ignoreCase = true)) {
                        name = line.substringAfter(":").trim()
                    } else if (line.startsWith("VILLAGE:", ignoreCase = true)) {
                        village = line.substringAfter(":").trim()
                    }
                }
                Pair(name, village)
            } catch (_: Exception) {
                regexResult
            }
        }
    }

    private fun extractProfileRegex(text: String): Pair<String, String> {
        var name = ""
        var village = ""

        // Name patterns: "मेरा नाम X है", "नाम X", "I am X", "My name is X"
        val nameMatch = Regex("""(?:मेरा नाम|नाम|my name is|i am)\s+([A-Za-z\u0900-\u097F\s]{2,25})(?:\s+है|\s+hai|,|\.|\s+और|\s+and|$)""", RegexOption.IGNORE_CASE).find(text)
        if (nameMatch != null) {
            name = nameMatch.groupValues[1].trim()
        }

        // Village patterns: "गांव X", "गाँव X", "village X", "from X"
        val villageMatch = Regex("""(?:गांव|गाँव|village|from|रहने वाला)\s+(?:है\s+)?([A-Za-z\u0900-\u097F\s]{2,25})(?:\s+का|\s+se|\s+से|\.|\s+है|$)""", RegexOption.IGNORE_CASE).find(text)
        if (villageMatch != null) {
            village = villageMatch.groupValues[1].trim()
        }

        if (name.isBlank() && village.isBlank()) {
            // If just words spoken, treat first couple words as name
            val words = text.split(" ").filter { it.isNotBlank() }
            if (words.size in 1..3) {
                name = text.trim()
            }
        }

        return Pair(name, village)
    }
}
