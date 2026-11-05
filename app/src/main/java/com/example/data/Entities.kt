package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String = "default_user",
    val name: String = "",
    val dob: String = "",
    val phone: String = "+91 ",
    val village: String = "",
    val photoUri: String? = null,
    val language: String = "hi", // "hi" or "en"
    val termsAccepted: Boolean = false,
    val onboardingDone: Boolean = false,
    val localBighaSqFt: Double = 27000.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val brand: String = "",
    val model: String = "",
    val hp: Double = 45.0,
    val regNo: String = "",
    val type: String = "Tractor",
    val year: String = "",
    val isDefault: Boolean = false,
    val photoUri: String? = null,
    val serviceIntervalHours: Double = 250.0,
    val lastServiceHours: Double = 0.0,
    val insuranceExpiryDate: String = "",
    val pucExpiryDate: String = "",
    val fitnessExpiryDate: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "implements")
data class ImplementEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val hindiName: String = "",
    val category: String = "TILLAGE",
    val photo: String? = null,
    val icon: String = "rotavator",
    val rateType: String = "HOUR", // "HOUR", "ACRE", "BIGHA", "HECTARE", "GUNTHA", "KANAL", "MARLA", "CENT", "CHAKKAR", "DAY", "FIXED", "CUSTOM"
    val customUnitName: String? = null,
    val defaultRate: Double = 800.0,
    val details: String = "",
    val vehicleIds: String = "ALL",
    val active: Boolean = true,
    val sortOrder: Int = 0,
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val village: String = "",
    val notes: String = "",
    val dueDateMillis: Long? = null,
    val dueDayOfMonth: Int? = null,
    val repeatDays: Int = 5,
    val remindersEnabled: Boolean = true,
    val isUdhaarCustomer: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val customerName: String,
    val vehicleId: String,
    val vehicleName: String,
    val implementId: String,
    val implementName: String,
    val implementDetailsSnapshot: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED", // "COMPLETED", "SCHEDULED"
    val rateTypeSnapshot: String = "HOUR",
    val customUnitSnapshot: String = "",
    val rateSnapshot: Double = 800.0,
    val hours: Int = 0,
    val minutes: Int = 0,
    val area: Double = 0.0,
    val quantity: Double = 0.0,
    val extras: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val paidAmount: Double = 0.0,
    val paymentMethod: String = "NONE", // "NONE", "CASH", "UPI", "BANK"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val jobId: String? = null,
    val customerId: String,
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val method: String = "CASH",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val vehicleName: String,
    val category: String = "FUEL", // "FUEL", "MAINTENANCE", "OTHER"
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val litres: Double = 0.0,
    val notes: String = "",
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String = "",
    val phone: String = "",
    val issueType: String = "GENERAL",
    val message: String = "",
    val status: String = "OPEN", // "OPEN", "IN_PROGRESS", "RESOLVED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: String, // "MILESTONE", "PAYMENT", "REMINDER", "SUMMARY", "ALERT", "SYSTEM", "TIP"
    val channelId: String = "general", // "milestones", "payments", "reminders", "alerts"
    val title: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    val readAt: Long? = null,
    val deepLink: String = "", // e.g. "kisan_detail:id", "baki_list", "reports", "vehicles", "reminders"
    val payload: String = "",
    val dedupeKey: String = ""
)

@Entity(tableName = "reminder_rules")
data class ReminderRuleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val kisanId: String,
    val kisanName: String,
    val dueDateMillis: Long? = null,
    val dueDayOfMonth: Int? = null,
    val repeatDays: Int = 5,
    val active: Boolean = true,
    val snoozeUntilMillis: Long? = null,
    val lastFiredMillis: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey val key: String, // e.g. "kisan_50", "kisan_100", "first_tractor", "earnings_100000"
    val achievedAt: Long = System.currentTimeMillis(),
    val value: Double = 0.0,
    val title: String = ""
)

@Entity(tableName = "notification_prefs")
data class NotificationPrefsEntity(
    @PrimaryKey val id: String = "default_prefs",
    val masterEnabled: Boolean = true,
    val milestonesEnabled: Boolean = true,
    val paymentRemindersEnabled: Boolean = true,
    val monthlySummaryEnabled: Boolean = true,
    val weeklySummaryEnabled: Boolean = true,
    val jobRemindersEnabled: Boolean = true,
    val vehicleServiceEnabled: Boolean = true,
    val backupAlertsEnabled: Boolean = true,
    val tipsEnabled: Boolean = true,
    val paymentReminderTimeHour: Int = 9,
    val paymentReminderTimeMinute: Int = 0,
    val jobReminderTimeHour: Int = 8,
    val jobReminderTimeMinute: Int = 0,
    val quietHoursStartHour: Int = 21,
    val quietHoursEndHour: Int = 7,
    val defaultRepeatDays: Int = 5,
    val updatedAt: Long = System.currentTimeMillis()
)

