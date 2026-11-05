package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.CustomerEntity
import com.example.data.JobEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShareHelper {
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale("hi", "IN"))

    fun formatJobBill(job: JobEntity, lang: String = "hi"): String {
        val dateStr = dateFormatter.format(Date(job.dateMillis))
        val due = (job.total - job.paidAmount).coerceAtLeast(0.0)
        val durationStr = when (job.rateTypeSnapshot) {
            "HOUR" -> "${job.hours}h ${job.minutes}m"
            "ACRE" -> "${job.area} एकड़"
            else -> "${job.quantity.toInt()} यूनिट"
        }

        return if (lang == "hi") {
            """
            🚜 *खेत साथी - काम की रसीद*
            ━━━━━━━━━━━━━━━━━━━━
            👤 किसान: ${job.customerName}
            🌾 यंत्र: ${job.implementName}
            🚜 ट्रैक्टर: ${job.vehicleName}
            📅 तारीख: $dateStr
            ⏱️ समय/माप: $durationStr (दर: ₹${job.rateSnapshot.toInt()})
            ━━━━━━━━━━━━━━━━━━━━
            💰 कुल रकम: ₹${job.total.toInt()}
            ✅ जमा राशि: ₹${job.paidAmount.toInt()}
            ⚠️ *बाकी रकम (Due): ₹${due.toInt()}*
            ━━━━━━━━━━━━━━━━━━━━
            _ट्रैक्टर सेवा के लिए धन्यवाद!_
            _खेत साथी ऐप द्वारा जनरेट_
            """.trimIndent()
        } else {
            """
            🚜 *KhetSaathi - Work Invoice*
            ━━━━━━━━━━━━━━━━━━━━
            👤 Customer: ${job.customerName}
            🌾 Implement: ${job.implementName}
            🚜 Tractor: ${job.vehicleName}
            📅 Date: $dateStr
            ⏱️ Duration/Units: $durationStr (@ ₹${job.rateSnapshot.toInt()})
            ━━━━━━━━━━━━━━━━━━━━
            💰 Total Bill: ₹${job.total.toInt()}
            ✅ Paid: ₹${job.paidAmount.toInt()}
            ⚠️ *Balance Due: ₹${due.toInt()}*
            ━━━━━━━━━━━━━━━━━━━━
            _Thank you for your business!_
            _Generated via KhetSaathi_
            """.trimIndent()
        }
    }

    fun formatCustomerReminder(
        customer: CustomerEntity,
        totalBilled: Double,
        totalPaid: Double,
        lang: String = "hi"
    ): String {
        val due = (totalBilled - totalPaid).coerceAtLeast(0.0)
        return if (lang == "hi") {
            """
            🚜 *खेत साथी - हिसाब सूचना*
            ━━━━━━━━━━━━━━━━━━━━
            किसान भाई श्री *${customer.name}* जी,
            गांव: ${customer.village.ifEmpty { "-" }}
            
            आपके ट्रैक्टर कार्य का कुल हिसाब:
            • कुल काम: ₹${totalBilled.toInt()}
            • जमा किया: ₹${totalPaid.toInt()}
            -----------------------
            ⚠️ *कुल बकाया राशि: ₹${due.toInt()}*
            ━━━━━━━━━━━━━━━━━━━━
            कृपया शेष बकाया राशि का भुगतान जल्द करने का कष्ट करें।
            धन्यवाद!
            """.trimIndent()
        } else {
            """
            🚜 *KhetSaathi - Balance Reminder*
            ━━━━━━━━━━━━━━━━━━━━
            Dear Farmer *${customer.name}*,
            Village: ${customer.village.ifEmpty { "-" }}
            
            Your tractor work account summary:
            • Total Billed: ₹${totalBilled.toInt()}
            • Paid so far: ₹${totalPaid.toInt()}
            -----------------------
            ⚠️ *Total Due Balance: ₹${due.toInt()}*
            ━━━━━━━━━━━━━━━━━━━━
            Please arrange payment of the outstanding balance.
            Thank you!
            """.trimIndent()
        }
    }

    fun formatTodayReport(
        jobsCount: Int,
        earned: Double,
        spent: Double,
        baki: Double,
        profit: Double,
        lang: String = "hi"
    ): String {
        val todayStr = dateFormatter.format(Date())
        return if (lang == "hi") {
            """
            🚜 *खेत साथी - आज का हिसाब ($todayStr)*
            ━━━━━━━━━━━━━━━━━━━━
            📋 कुल काम: $jobsCount
            💰 कुल कमाई: ₹${earned.toInt()}
            ⛽ डीजल/खर्चा: ₹${spent.toInt()}
            ⚠️ बाकी उधारी: ₹${baki.toInt()}
            ━━━━━━━━━━━━━━━━━━━━
            🏆 *आज की शुद्ध बचत (Profit): ₹${profit.toInt()}*
            ━━━━━━━━━━━━━━━━━━━━
            _खेत साथी - ट्रैक्टर मालिक का डिजिटल साथी_
            """.trimIndent()
        } else {
            """
            🚜 *KhetSaathi - Today's Summary ($todayStr)*
            ━━━━━━━━━━━━━━━━━━━━
            📋 Jobs Done: $jobsCount
            💰 Total Earned: ₹${earned.toInt()}
            ⛽ Spent (Fuel/Maint): ₹${spent.toInt()}
            ⚠️ Due (Baki): ₹${baki.toInt()}
            ━━━━━━━━━━━━━━━━━━━━
            🏆 *Net Profit: ₹${profit.toInt()}*
            ━━━━━━━━━━━━━━━━━━━━
            _KhetSaathi - Digital Tractor Ledger_
            """.trimIndent()
        }
    }

    fun shareWhatsApp(context: Context, text: String, phone: String? = null) {
        try {
            val cleanPhone = phone?.replace("+", "")?.replace(" ", "")?.replace("-", "")
            val uri = if (!cleanPhone.isNullOrBlank() && cleanPhone.length >= 10) {
                val formattedPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(text)}")
            } else {
                Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(text)}")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            shareText(context, text)
        }
    }

    fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    fun makeCall(context: Context, phone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun sendSms(context: Context, phone: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone")).apply {
                putExtra("sms_body", message)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
