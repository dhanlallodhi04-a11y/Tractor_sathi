package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "NotifActionReceiver"
        const val ACTION_PAYMENT = "com.example.notification.ACTION_PAYMENT"
        const val ACTION_CALL = "com.example.notification.ACTION_CALL"
        const val ACTION_WHATSAPP = "com.example.notification.ACTION_WHATSAPP"
        const val ACTION_SNOOZE = "com.example.notification.ACTION_SNOOZE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val notificationId = intent.getIntExtra("notificationId", 0)
        val kisanId = intent.getStringExtra("kisanId")
        val phone = intent.getStringExtra("phone")

        if (notificationId != 0) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }

        when (action) {
            ACTION_PAYMENT -> {
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("deepLink", "add_payment:$kisanId")
                }
                context.startActivity(openIntent)
            }
            ACTION_CALL -> {
                if (!phone.isNullOrBlank()) {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(dialIntent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Cannot launch dialer", e)
                    }
                }
            }
            ACTION_WHATSAPP -> {
                if (!phone.isNullOrBlank()) {
                    val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
                    val fullNumber = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                    val message = "राम-राम जी! किसान साथी ऐप से आपके ट्रैक्टर के काम का बकाया हिसाब भेजा जा रहा है। कृपया सुविधानुसार भुगतान करने का कष्ट करें। धन्यवाद।"
                    val waIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://wa.me/$fullNumber?text=${Uri.encode(message)}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(waIntent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Cannot open WhatsApp", e)
                    }
                }
            }
            ACTION_SNOOZE -> {
                if (!kisanId.isNullOrEmpty()) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val dao = AppDatabase.getDatabase(context).dao()
                            val rule = dao.getReminderRuleForKisanSync(kisanId)
                            val twoDaysLater = System.currentTimeMillis() + (2L * 86400000L)
                            if (rule != null) {
                                dao.updateReminderRule(rule.copy(snoozeUntilMillis = twoDaysLater))
                            }
                            NotificationService.rescheduleAll(context)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error snoozing reminder", e)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
        }
    }
}
