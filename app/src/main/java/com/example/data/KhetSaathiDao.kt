package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KhetSaathiDao {
    // User
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun getUser(uid: String = "default_user"): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserSync(uid: String = "default_user"): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getFirstUserSync(): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    fun getFirstUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Vehicles
    @Query("SELECT * FROM vehicles ORDER BY isDefault DESC, createdAt ASC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles")
    suspend fun getAllVehiclesSync(): List<VehicleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<VehicleEntity>)

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteVehicle(id: String)

    @Query("UPDATE vehicles SET isDefault = (CASE WHEN id = :defaultId THEN 1 ELSE 0 END)")
    suspend fun setDefaultVehicle(defaultId: String)

    // Implements
    @Query("SELECT * FROM implements ORDER BY usageCount DESC, sortOrder ASC")
    fun getAllImplements(): Flow<List<ImplementEntity>>

    @Query("SELECT * FROM implements")
    suspend fun getAllImplementsSync(): List<ImplementEntity>

    @Query("SELECT * FROM implements WHERE id = :id LIMIT 1")
    fun getImplementById(id: String): Flow<ImplementEntity?>

    @Query("SELECT * FROM implements WHERE id = :id LIMIT 1")
    suspend fun getImplementByIdSync(id: String): ImplementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImplement(implement: ImplementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImplements(implements: List<ImplementEntity>)

    @Update
    suspend fun updateImplement(implement: ImplementEntity)

    @Query("DELETE FROM implements WHERE id = :id")
    suspend fun deleteImplement(id: String)

    @Query("UPDATE implements SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementImplementUsage(id: String)

    // Customers
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers")
    suspend fun getAllCustomersSync(): List<CustomerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomer(id: String)

    // Jobs
    @Query("SELECT * FROM jobs ORDER BY dateMillis DESC, createdAt DESC")
    fun getAllJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs")
    suspend fun getAllJobsSync(): List<JobEntity>

    @Query("SELECT * FROM jobs WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun getJobsBetween(startMillis: Long, endMillis: Long): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE customerId = :customerId ORDER BY dateMillis DESC")
    fun getJobsForCustomer(customerId: String): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    @Update
    suspend fun updateJob(job: JobEntity)

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun deleteJob(id: String)

    // Payments
    @Query("SELECT * FROM payments ORDER BY dateMillis DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY dateMillis DESC")
    fun getPaymentsForCustomer(customerId: String): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePayment(id: String)

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpensesSync(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun getExpensesBetween(startMillis: Long, endMillis: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: String)

    // Support Tickets
    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllSupportTickets(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupportTicket(ticket: SupportTicketEntity)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC LIMIT 200")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE readAt IS NULL")
    fun getUnreadNotificationsCount(): Flow<Int>

    @Query("SELECT * FROM notifications WHERE dedupeKey = :key LIMIT 1")
    suspend fun getNotificationByDedupeKey(key: String): NotificationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET readAt = :readAt WHERE id = :id")
    suspend fun markNotificationAsRead(id: String, readAt: Long = System.currentTimeMillis())

    @Query("UPDATE notifications SET readAt = :readAt WHERE readAt IS NULL")
    suspend fun markAllNotificationsAsRead(readAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    @Query("DELETE FROM notifications WHERE id NOT IN (SELECT id FROM notifications ORDER BY createdAt DESC LIMIT 200)")
    suspend fun pruneOldNotifications()

    // Reminder Rules
    @Query("SELECT * FROM reminder_rules ORDER BY createdAt DESC")
    fun getAllReminderRules(): Flow<List<ReminderRuleEntity>>

    @Query("SELECT * FROM reminder_rules WHERE active = 1")
    suspend fun getAllActiveReminderRulesSync(): List<ReminderRuleEntity>

    @Query("SELECT * FROM reminder_rules WHERE kisanId = :kisanId LIMIT 1")
    suspend fun getReminderRuleForKisanSync(kisanId: String): ReminderRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminderRule(rule: ReminderRuleEntity)

    @Update
    suspend fun updateReminderRule(rule: ReminderRuleEntity)

    @Query("DELETE FROM reminder_rules WHERE kisanId = :kisanId")
    suspend fun deleteReminderRuleForKisan(kisanId: String)

    // Milestones
    @Query("SELECT * FROM milestones")
    fun getAllMilestones(): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones")
    suspend fun getAllMilestonesSync(): List<MilestoneEntity>

    @Query("SELECT * FROM milestones WHERE `key` = :key LIMIT 1")
    suspend fun getMilestoneSync(key: String): MilestoneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity)

    // Notification Prefs
    @Query("SELECT * FROM notification_prefs WHERE id = 'default_prefs' LIMIT 1")
    fun getNotificationPrefs(): Flow<NotificationPrefsEntity?>

    @Query("SELECT * FROM notification_prefs WHERE id = 'default_prefs' LIMIT 1")
    suspend fun getNotificationPrefsSync(): NotificationPrefsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationPrefs(prefs: NotificationPrefsEntity)

    // Clear all for reset / logout
    @Query("DELETE FROM jobs")
    suspend fun clearAllJobs()

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()

    @Query("DELETE FROM payments")
    suspend fun clearAllPayments()

    @Query("DELETE FROM customers")
    suspend fun clearAllCustomers()

    @Query("DELETE FROM vehicles")
    suspend fun clearAllVehicles()

    @Query("DELETE FROM implements")
    suspend fun clearAllImplements()

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    @Query("DELETE FROM reminder_rules")
    suspend fun clearAllReminderRules()

    @Query("DELETE FROM milestones")
    suspend fun clearAllMilestones()
}
