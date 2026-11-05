package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationAlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "NotificationAlarmRecv"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notificationId", 1001)
        val channelId = intent.getStringExtra("channelId") ?: NotificationService.CHANNEL_REMINDERS
        val type = intent.getStringExtra("type") ?: "REMINDER"
        val title = intent.getStringExtra("title") ?: "किसान साथी"
        val body = intent.getStringExtra("body") ?: ""
        val deepLink = intent.getStringExtra("deepLink") ?: ""
        val dedupeKey = intent.getStringExtra("dedupeKey") ?: ""
        val phone = intent.getStringExtra("phone")
        val kisanId = intent.getStringExtra("kisanId")

        Log.d(TAG, "Alarm received: id=$notificationId, title=$title, key=$dedupeKey")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getDatabase(context).dao()

                // Check if this is a payment reminder and if the kisan has already cleared the due
                if (type == "PAYMENT" && !kisanId.isNullOrEmpty()) {
                    val jobs = dao.getAllJobsSync()
                    val kisanJobs = jobs.filter { it.customerId == kisanId }
                    val totalBilled = kisanJobs.sumOf { it.total }
                    val totalPaid = kisanJobs.sumOf { it.paidAmount }
                    val baki = (totalBilled - totalPaid).coerceAtLeast(0.0)

                    if (baki <= 0.0) {
                        Log.d(TAG, "Skipping payment reminder for kisan $kisanId because baki is 0")
                        return@launch
                    }
                }

                // Save in-app notification
                NotificationService.saveInAppNotification(
                    context = context,
                    type = type,
                    channelId = channelId,
                    title = title,
                    body = body,
                    deepLink = deepLink,
                    dedupeKey = dedupeKey
                )

                // Show system notification
                NotificationService.showSystemNotification(
                    context = context,
                    notificationId = notificationId,
                    channelId = channelId,
                    title = title,
                    body = body,
                    deepLink = deepLink,
                    phone = phone,
                    kisanId = kisanId
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error handling alarm notification", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
