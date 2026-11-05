package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.CustomerEntity
import com.example.data.JobEntity
import com.example.data.MilestoneEntity
import com.example.data.NotificationEntity
import com.example.data.NotificationPrefsEntity
import com.example.data.ReminderRuleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs

object NotificationService {
    private const val TAG = "NotificationService"

    const val CHANNEL_MILESTONES = "milestones"
    const val CHANNEL_PAYMENTS = "payments"
    const val CHANNEL_REMINDERS = "reminders"
    const val CHANNEL_ALERTS = "alerts"

    private var templatesJson: JSONObject? = null

    fun initialize(context: Context) {
        createNotificationChannels(context)
        loadTemplates(context)
    }

    private fun loadTemplates(context: Context) {
        if (templatesJson != null) return
        try {
            val input: InputStream = context.assets.open("notifications.json")
            val text = input.bufferedReader().use { it.readText() }
            templatesJson = JSONObject(text)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load notifications.json", e)
        }
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_MILESTONES,
                    "बधाई (Celebrations)",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "उपलब्धियां, नए ट्रैक्टर/औज़ार और स्वागत संदेश"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_PAYMENTS,
                    "बाकी/वसूली (Payment Reminders)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "किसानों से बाकी पैसे और तारीख के तकादे"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_REMINDERS,
                    "याद (Reminders & Jobs)",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "आगामी काम, शाम का हिसाब और सर्विस की याद"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_ALERTS,
                    "ज़रूरी (Alerts & System)",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "बैकअप, टिप्स और सिस्टम सूचनाएं"
                }
            )

            notificationManager.createNotificationChannels(channels)
        }
    }

    fun getDeterministicId(key: String): Int {
        val hash = key.hashCode()
        return if (hash == Int.MIN_VALUE) 0 else abs(hash)
    }

    /**
     * Checks whether notifications are allowed on this device
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Save an in-app notification in Room database and prune old rows
     */
    suspend fun saveInAppNotification(
        context: Context,
        type: String,
        channelId: String,
        title: String,
        body: String,
        deepLink: String = "",
        payload: String = "",
        dedupeKey: String = ""
    ): NotificationEntity {
        val dao = AppDatabase.getDatabase(context).dao()

        if (dedupeKey.isNotEmpty()) {
            val existing = dao.getNotificationByDedupeKey(dedupeKey)
            if (existing != null) {
                return existing
            }
        }

        val notification = NotificationEntity(
            id = UUID.randomUUID().toString(),
            type = type,
            channelId = channelId,
            title = title,
            body = body,
            createdAt = System.currentTimeMillis(),
            readAt = null,
            deepLink = deepLink,
            payload = payload,
            dedupeKey = dedupeKey
        )

        dao.insertNotification(notification)
        dao.pruneOldNotifications()
        return notification
    }

    /**
     * Post a system notification immediately
     */
    fun showSystemNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        body: String,
        deepLink: String = "",
        phone: String? = null,
        kisanId: String? = null
    ) {
        if (!areNotificationsEnabled(context)) {
            Log.d(TAG, "Notifications disabled by user/system")
            return
        }

        try {
            // Tap intent (open MainActivity with deepLink)
            val tapIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (deepLink.isNotEmpty()) {
                    putExtra("deepLink", deepLink)
                }
            }
            val tapPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(if (channelId == CHANNEL_PAYMENTS) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(tapPendingIntent)
                .setAutoCancel(true)
                .setColor(0xFF14452F.toInt())

            // Add action buttons for payments
            if (channelId == CHANNEL_PAYMENTS && kisanId != null) {
                // 1. "भुगतान मिला" Action
                val payIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_PAYMENT
                    putExtra("kisanId", kisanId)
                    putExtra("notificationId", notificationId)
                }
                val payPendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId * 10 + 1,
                    payIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(R.mipmap.ic_launcher, "भुगतान मिला", payPendingIntent)

                // 2. "कॉल करें" if phone exists
                if (!phone.isNullOrBlank() && phone.length >= 10) {
                    val callIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                        action = NotificationActionReceiver.ACTION_CALL
                        putExtra("phone", phone)
                        putExtra("notificationId", notificationId)
                    }
                    val callPendingIntent = PendingIntent.getBroadcast(
                        context,
                        notificationId * 10 + 2,
                        callIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    builder.addAction(R.mipmap.ic_launcher, "कॉल करें", callPendingIntent)
                }

                // 3. "2 दिन बाद याद दिलाएं" Snooze
                val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_SNOOZE
                    putExtra("kisanId", kisanId)
                    putExtra("notificationId", notificationId)
                }
                val snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId * 10 + 3,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(R.mipmap.ic_launcher, "2 दिन बाद", snoozePendingIntent)
            }

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying system notification", e)
        }
    }

    /**
     * Schedules a device notification using AlarmManager (offline, survives reboot)
     */
    fun scheduleAlarm(
        context: Context,
        triggerAtMillis: Long,
        channelId: String,
        type: String,
        title: String,
        body: String,
        deepLink: String = "",
        dedupeKey: String = "",
        phone: String? = null,
        kisanId: String? = null
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val notifId = getDeterministicId(dedupeKey.ifEmpty { "$type:$triggerAtMillis" })

        val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
            putExtra("notificationId", notifId)
            putExtra("channelId", channelId)
            putExtra("type", type)
            putExtra("title", title)
            putExtra("body", body)
            putExtra("deepLink", deepLink)
            putExtra("dedupeKey", dedupeKey)
            putExtra("phone", phone)
            putExtra("kisanId", kisanId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
            Log.d(TAG, "Scheduled alarm for $notifId at $triggerAtMillis (key: $dedupeKey)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm", e)
        }
    }

    /**
     * Cancels a scheduled alarm by dedupeKey
     */
    fun cancelScheduledAlarm(context: Context, dedupeKey: String) {
        val notifId = getDeterministicId(dedupeKey)
        val intent = Intent(context, NotificationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for $dedupeKey")
        }
    }

    /**
     * Reschedules rolling 14-day window of notifications
     */
    fun rescheduleAll(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loadTemplates(context)
                val dao = AppDatabase.getDatabase(context).dao()
                val prefs = dao.getNotificationPrefsSync() ?: NotificationPrefsEntity()
                if (!prefs.masterEnabled) {
                    Log.d(TAG, "Notifications disabled globally in prefs")
                    return@launch
                }

                val customers = dao.getAllCustomersSync()
                val jobs = dao.getAllJobsSync()
                val vehicles = dao.getAllVehiclesSync()
                val now = System.currentTimeMillis()

                // Calculate dues per customer
                val customerDues = mutableMapOf<String, Double>()
                val customerOldestUnpaidJob = mutableMapOf<String, JobEntity>()
                val customerUnpaidJobsCount = mutableMapOf<String, Int>()

                val sortedJobs = jobs.sortedBy { it.dateMillis }
                for (job in sortedJobs) {
                    val due = (job.total - job.paidAmount).coerceAtLeast(0.0)
                    if (due > 0) {
                        customerDues[job.customerId] = (customerDues[job.customerId] ?: 0.0) + due
                        customerUnpaidJobsCount[job.customerId] = (customerUnpaidJobsCount[job.customerId] ?: 0) + 1
                        if (!customerOldestUnpaidJob.containsKey(job.customerId)) {
                            customerOldestUnpaidJob[job.customerId] = job
                        }
                    }
                }

                // 1. Rolling 14 days schedule for payment reminders
                if (prefs.paymentRemindersEnabled) {
                    val cal = Calendar.getInstance()
                    val dayFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

                    // Group reminders by date string (e.g. 20261106)
                    data class ScheduledReminder(
                        val customer: CustomerEntity,
                        val oldestJob: JobEntity?,
                        val dueAmount: Double,
                        val jobCount: Int,
                        val triggerMillis: Long
                    )

                    val remindersByDay = mutableMapOf<String, MutableList<ScheduledReminder>>()

                    for (customer in customers) {
                        val due = customerDues[customer.id] ?: 0.0
                        if (due <= 0.0 || !customer.remindersEnabled) continue

                        val repeatDays = if (customer.repeatDays > 0) customer.repeatDays else prefs.defaultRepeatDays
                        val baseDueTime = customer.dueDateMillis ?: now

                        // Determine next occurrences in the next 14 days
                        for (dayOffset in 0..14) {
                            val targetCal = Calendar.getInstance().apply {
                                timeInMillis = now + (dayOffset * 86400000L)
                                set(Calendar.HOUR_OF_DAY, prefs.paymentReminderTimeHour)
                                set(Calendar.MINUTE, prefs.paymentReminderTimeMinute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            // Adjust if quiet hours
                            adjustQuietHours(targetCal, prefs)

                            val triggerMillis = targetCal.timeInMillis
                            if (triggerMillis <= now) continue

                            var isDueDay = false
                            // Case 1: Specific due date with repeat
                            if (customer.dueDateMillis != null) {
                                val diffDays = ((triggerMillis - customer.dueDateMillis) / 86400000L).toInt()
                                if (diffDays >= 0 && diffDays % repeatDays == 0) {
                                    isDueDay = true
                                }
                            } else if (customer.dueDayOfMonth != null) {
                                // Case 2: Monthly repeating day
                                if (targetCal.get(Calendar.DAY_OF_MONTH) == customer.dueDayOfMonth) {
                                    isDueDay = true
                                }
                            } else {
                                // Default repeating reminder every repeatDays
                                val diffDays = ((triggerMillis - customer.createdAt) / 86400000L).toInt()
                                if (diffDays > 0 && diffDays % repeatDays == 0) {
                                    isDueDay = true
                                }
                            }

                            if (isDueDay) {
                                val dateKey = dayFormat.format(Date(triggerMillis))
                                val list = remindersByDay.getOrPut(dateKey) { mutableListOf() }
                                list.add(
                                    ScheduledReminder(
                                        customer = customer,
                                        oldestJob = customerOldestUnpaidJob[customer.id],
                                        dueAmount = due,
                                        jobCount = customerUnpaidJobsCount[customer.id] ?: 1,
                                        triggerMillis = triggerMillis
                                    )
                                )
                            }
                        }
                    }

                    // Schedule reminders per day: max 3 separate, or 1 merged summary
                    for ((dayKey, list) in remindersByDay) {
                        if (list.size <= 3) {
                            for (rem in list) {
                                val dedupeKey = "due:${rem.customer.id}:$dayKey"
                                val body = formatPaymentDueMessage(rem.customer.name, rem.oldestJob, rem.dueAmount, rem.jobCount)
                                scheduleAlarm(
                                    context = context,
                                    triggerAtMillis = rem.triggerMillis,
                                    channelId = CHANNEL_PAYMENTS,
                                    type = "PAYMENT",
                                    title = "बाकी पैसे की याद 💵",
                                    body = body,
                                    deepLink = "kisan_detail:${rem.customer.id}",
                                    dedupeKey = dedupeKey,
                                    phone = rem.customer.phone,
                                    kisanId = rem.customer.id
                                )
                            }
                        } else {
                            // Merge into 1 grouped summary
                            val totalDues = list.sumOf { it.dueAmount }
                            val triggerMillis = list.first().triggerMillis
                            val dedupeKey = "due_group:$dayKey"
                            val body = "आज ${list.size} किसानों से कुल ₹${formatRupee(totalDues)} लेने हैं। बहीखाते में देखकर बात करें।"
                            scheduleAlarm(
                                context = context,
                                triggerAtMillis = triggerMillis,
                                channelId = CHANNEL_PAYMENTS,
                                type = "PAYMENT",
                                title = "आज के बाकी तकादे 🔔",
                                body = body,
                                deepLink = "baki_list",
                                dedupeKey = dedupeKey
                            )
                        }
                    }
                }

                // 2. Monthly 1st summary at 9:00 AM
                if (prefs.monthlySummaryEnabled) {
                    val cal1st = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, prefs.paymentReminderTimeHour)
                        set(Calendar.MINUTE, prefs.paymentReminderTimeMinute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (cal1st.timeInMillis <= now) {
                        cal1st.add(Calendar.MONTH, 1)
                    }
                    val totalBaki = customerDues.values.sum()
                    if (totalBaki > 0 && (cal1st.timeInMillis - now) <= (14L * 86400000L)) {
                        val kisanWithBakiCount = customerDues.count { it.value > 0 }
                        val monthKey = SimpleDateFormat("yyyyMM", Locale.US).format(cal1st.time)
                        scheduleAlarm(
                            context = context,
                            triggerAtMillis = cal1st.timeInMillis,
                            channelId = CHANNEL_PAYMENTS,
                            type = "SUMMARY",
                            title = "महीने की शुरुआत - कुल बाकी 📊",
                            body = "इस महीने की शुरुआत: कुल ₹${formatRupee(totalBaki)} बाकी है ($kisanWithBakiCount किसानों से)। आज से वसूली शुरू करें।",
                            deepLink = "baki_list",
                            dedupeKey = "monthly_summary:$monthKey"
                        )
                    }
                }

                // 3. Weekly Summary (Sunday 7:00 PM)
                if (prefs.weeklySummaryEnabled) {
                    val sunCal = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                        set(Calendar.HOUR_OF_DAY, 19)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (sunCal.timeInMillis <= now) {
                        sunCal.add(Calendar.WEEK_OF_YEAR, 1)
                    }
                    if ((sunCal.timeInMillis - now) <= (14L * 86400000L)) {
                        val weekKey = SimpleDateFormat("yyyyww", Locale.US).format(sunCal.time)
                        scheduleAlarm(
                            context = context,
                            triggerAtMillis = sunCal.timeInMillis,
                            channelId = CHANNEL_REMINDERS,
                            type = "SUMMARY",
                            title = "हफ्ते का हिसाब-किताब 📈",
                            body = "हफ्ते का रिपोर्ट कार्ड तैयार है। अपनी कमाई, खर्च और मुनाफा देखें!",
                            deepLink = "reports",
                            dedupeKey = "weekly_summary:$weekKey"
                        )
                    }
                }

                // 4. Evening Nudge (8:00 PM)
                val eveningCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 20)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (eveningCal.timeInMillis > now) {
                    val dayKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(eveningCal.time)
                    scheduleAlarm(
                        context = context,
                        triggerAtMillis = eveningCal.timeInMillis,
                        channelId = CHANNEL_REMINDERS,
                        type = "REMINDER",
                        title = "आज का काम दर्ज करें 📝",
                        body = "आज का काम दर्ज करना न भूलें! 2 मिनट में हिसाब जोड़ें और निश्चिंत सोएं।",
                        deepLink = "add_job",
                        dedupeKey = "evening_nudge:$dayKey"
                    )
                }

                // 5. Vehicle Service Reminders (at 90% and 100% of interval)
                if (prefs.vehicleServiceEnabled) {
                    for (vehicle in vehicles) {
                        val vehicleJobs = jobs.filter { it.vehicleId == vehicle.id }
                        val totalHours = vehicleJobs.sumOf { it.hours + (it.minutes / 60.0) }
                        val interval = if (vehicle.serviceIntervalHours > 0) vehicle.serviceIntervalHours else 250.0
                        val currentCycleHours = totalHours - vehicle.lastServiceHours

                        if (currentCycleHours >= (interval * 0.9)) {
                            val dedupeKey = "service:${vehicle.id}:${(currentCycleHours / 10).toInt()}"
                            val body = "${vehicle.name} की सर्विस का समय पास है (${currentCycleHours.toInt()} घंटे चले)। इंजन ऑयल और फिल्टर चेक करें।"
                            // Post in-app notification if not logged already
                            saveInAppNotification(
                                context = context,
                                type = "ALERT",
                                channelId = CHANNEL_ALERTS,
                                title = "ट्रैक्टर सर्विस का समय 🔧",
                                body = body,
                                deepLink = "vehicles",
                                dedupeKey = dedupeKey
                            )
                        }
                    }
                }

                Log.d(TAG, "Completed rescheduleAll successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error during rescheduleAll", e)
            }
        }
    }

    private fun adjustQuietHours(cal: Calendar, prefs: NotificationPrefsEntity) {
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val isQuiet = if (prefs.quietHoursStartHour > prefs.quietHoursEndHour) {
            hour >= prefs.quietHoursStartHour || hour < prefs.quietHoursEndHour
        } else {
            hour in prefs.quietHoursStartHour until prefs.quietHoursEndHour
        }

        if (isQuiet) {
            cal.set(Calendar.HOUR_OF_DAY, prefs.quietHoursEndHour)
            cal.set(Calendar.MINUTE, 0)
            if (hour >= prefs.quietHoursStartHour) {
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }

    private fun formatPaymentDueMessage(
        kisanName: String,
        job: JobEntity?,
        dueAmount: Double,
        jobCount: Int
    ): String {
        val endings = listOf(
            "आज एक कॉल कर लीजिए!",
            "जल्दी वसूल करें और अपने लक्ष्य पूरे करें!",
            "आपकी मेहनत की कमाई है, समय पर मांगें।",
            "थोड़ी मेहनत, पूरा पैसा!",
            "बात करने से काम आसान हो जाता है।"
        )
        val ending = endings.random()

        return if (job != null) {
            val jobDate = SimpleDateFormat("dd MMM", Locale("hi", "IN")).format(Date(job.dateMillis))
            val durationOrArea = if (job.hours > 0 || job.minutes > 0) {
                "${job.hours} घंटे ${if (job.minutes > 0) "${job.minutes} मि." else ""}".trim()
            } else if (job.area > 0) {
                "${job.area} बीघा/एकड़"
            } else "काम"

            val multipleJobsText = if (jobCount > 1) " (कुल बाकी ₹${formatRupee(dueAmount)})" else ""
            "आपको $kisanName से $jobDate को किया हुआ ${job.implementName} का $durationOrArea, ₹${formatRupee(dueAmount)} लेना है। $ending$multipleJobsText"
        } else {
            "आपको $kisanName से कुल ₹${formatRupee(dueAmount)} लेने हैं। $ending"
        }
    }

    fun formatRupee(amount: Double): String {
        val num = amount.toLong()
        val s = abs(num).toString()
        if (s.length <= 3) return s
        val lastThree = s.substring(s.length - 3)
        val otherNumbers = s.substring(0, s.length - 3)
        val formattedOther = StringBuilder()
        for (i in otherNumbers.indices) {
            if (i > 0 && (otherNumbers.length - i) % 2 == 0) {
                formattedOther.append(",")
            }
            formattedOther.append(otherNumbers[i])
        }
        return "$formattedOther,$lastThree"
    }

    /**
     * Checks kisan count milestone and records it in database once
     */
    suspend fun checkKisanMilestones(context: Context, activeKisanCount: Int): MilestoneEntity? {
        val dao = AppDatabase.getDatabase(context).dao()
        val milestones = listOf(50, 100, 200, 500, 1000)

        for (m in milestones) {
            if (activeKisanCount >= m) {
                val key = "kisan_$m"
                val existing = dao.getMilestoneSync(key)
                if (existing == null) {
                    val titles = mapOf(
                        50 to "50 किसान! 🌱",
                        100 to "100 किसान पूरे! 🏆",
                        200 to "200 किसान! 🥇",
                        500 to "500 किसान - बड़ी उपलब्धि! 👑",
                        1000 to "1000 किसान! इतिहास रच दिया 🌟"
                    )
                    val msgs = mapOf(
                        50 to "50 किसान! आपका नेटवर्क बढ़ रहा है और लोग आप पर भरोसा करते हैं।",
                        100 to "100 किसान जुड़ गए, कमाल! अब आप इलाके के भरोसेमंद नाम हैं।",
                        200 to "शाबाश! 200 किसान जुड़ चुके हैं। आपका काम और साख दोनों बुलंद हैं।",
                        500 to "500 किसान, आप इलाके के भरोसेमंद नाम हैं! आपकी लगन को सलाम।",
                        1000 to "1000 किसान! आपने बहुत बड़ा मुकाम पाया। आप सच में किसानों के सच्चे साथी हैं!"
                    )
                    val milestone = MilestoneEntity(
                        key = key,
                        achievedAt = System.currentTimeMillis(),
                        value = m.toDouble(),
                        title = titles[m] ?: "$m किसान!"
                    )
                    dao.insertMilestone(milestone)

                    // Post In-App & System Notification
                    saveInAppNotification(
                        context = context,
                        type = "MILESTONE",
                        channelId = CHANNEL_MILESTONES,
                        title = milestone.title,
                        body = msgs[m] ?: milestone.title,
                        deepLink = "achievements",
                        dedupeKey = key
                    )
                    showSystemNotification(
                        context = context,
                        notificationId = getDeterministicId(key),
                        channelId = CHANNEL_MILESTONES,
                        title = milestone.title,
                        body = msgs[m] ?: milestone.title,
                        deepLink = "achievements"
                    )
                    return milestone
                }
            }
        }
        return null
    }

    /**
     * Send test notification immediately
     */
    suspend fun sendTestNotification(context: Context) {
        val title = "किसान साथी टेस्ट नोटिफिकेशन 🔔"
        val body = "बधाई हो! आपकी सूचनाएं बिल्कुल सही काम कर रही हैं। बाकी पैसे और काम की समय पर याद मिलेगी।"
        saveInAppNotification(
            context = context,
            type = "SYSTEM",
            channelId = CHANNEL_ALERTS,
            title = title,
            body = body,
            deepLink = "notification_center",
            dedupeKey = "test_${System.currentTimeMillis()}"
        )
        showSystemNotification(
            context = context,
            notificationId = (1000..9999).random(),
            channelId = CHANNEL_ALERTS,
            title = title,
            body = body,
            deepLink = "notification_center"
        )
    }
}
