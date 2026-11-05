package com.example.data

import kotlinx.coroutines.flow.Flow

class KhetSaathiRepository(private val dao: KhetSaathiDao) {
    val user: Flow<UserEntity?> = dao.getUser()
    val vehicles: Flow<List<VehicleEntity>> = dao.getAllVehicles()
    val implements: Flow<List<ImplementEntity>> = dao.getAllImplements()
    val customers: Flow<List<CustomerEntity>> = dao.getAllCustomers()
    val jobs: Flow<List<JobEntity>> = dao.getAllJobs()
    val payments: Flow<List<PaymentEntity>> = dao.getAllPayments()
    val expenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    val supportTickets: Flow<List<SupportTicketEntity>> = dao.getAllSupportTickets()
    val notifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = dao.getUnreadNotificationsCount()
    val reminderRules: Flow<List<ReminderRuleEntity>> = dao.getAllReminderRules()
    val milestones: Flow<List<MilestoneEntity>> = dao.getAllMilestones()
    val notificationPrefs: Flow<NotificationPrefsEntity?> = dao.getNotificationPrefs()

    suspend fun insertNotification(notification: NotificationEntity) = dao.insertNotification(notification)
    suspend fun markNotificationAsRead(id: String) = dao.markNotificationAsRead(id)
    suspend fun markAllNotificationsAsRead() = dao.markAllNotificationsAsRead()
    suspend fun deleteNotification(id: String) = dao.deleteNotification(id)
    suspend fun clearAllNotifications() = dao.deleteAllNotifications()

    suspend fun insertReminderRule(rule: ReminderRuleEntity) = dao.insertReminderRule(rule)
    suspend fun updateReminderRule(rule: ReminderRuleEntity) = dao.updateReminderRule(rule)
    suspend fun deleteReminderRuleForKisan(kisanId: String) = dao.deleteReminderRuleForKisan(kisanId)

    suspend fun insertMilestone(milestone: MilestoneEntity) = dao.insertMilestone(milestone)
    suspend fun getMilestoneSync(key: String): MilestoneEntity? = dao.getMilestoneSync(key)

    suspend fun updateNotificationPrefs(prefs: NotificationPrefsEntity) = dao.insertNotificationPrefs(prefs)
    suspend fun getNotificationPrefsSync(): NotificationPrefsEntity? = dao.getNotificationPrefsSync()

    suspend fun insertSupportTicket(ticket: SupportTicketEntity) = dao.insertSupportTicket(ticket)

    suspend fun saveUser(user: UserEntity) = dao.insertUser(user)
    suspend fun getUserSync(): UserEntity? = dao.getUserSync()

    suspend fun insertVehicle(vehicle: VehicleEntity) = dao.insertVehicle(vehicle)
    suspend fun updateVehicle(vehicle: VehicleEntity) = dao.updateVehicle(vehicle)
    suspend fun deleteVehicle(id: String) = dao.deleteVehicle(id)
    suspend fun setDefaultVehicle(id: String) = dao.setDefaultVehicle(id)

    suspend fun insertImplement(implement: ImplementEntity) = dao.insertImplement(implement)
    suspend fun updateImplement(implement: ImplementEntity) = dao.updateImplement(implement)
    suspend fun deleteImplement(id: String) = dao.deleteImplement(id)
    suspend fun incrementImplementUsage(id: String) = dao.incrementImplementUsage(id)

    suspend fun insertCustomer(customer: CustomerEntity) = dao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = dao.updateCustomer(customer)
    suspend fun deleteCustomer(id: String) = dao.deleteCustomer(id)

    suspend fun insertJob(job: JobEntity) {
        dao.insertJob(job)
        dao.incrementImplementUsage(job.implementId)
        if (job.paidAmount > 0) {
            dao.insertPayment(
                PaymentEntity(
                    jobId = job.id,
                    customerId = job.customerId,
                    amount = job.paidAmount,
                    dateMillis = job.dateMillis,
                    method = if (job.paymentMethod.isNotEmpty() && job.paymentMethod != "NONE") job.paymentMethod else "CASH",
                    notes = "Job payment (${job.implementName})"
                )
            )
        }
    }

    suspend fun updateJob(job: JobEntity) = dao.updateJob(job)
    suspend fun deleteJob(id: String) = dao.deleteJob(id)

    suspend fun addPayment(payment: PaymentEntity) {
        dao.insertPayment(payment)
    }

    suspend fun insertExpense(expense: ExpenseEntity) = dao.insertExpense(expense)
    suspend fun updateExpense(expense: ExpenseEntity) = dao.updateExpense(expense)
    suspend fun deleteExpense(id: String) = dao.deleteExpense(id)

    suspend fun seedDemoData() = DemoDataSeeder.seedFullDemoData(dao)

    suspend fun resetAllData() {
        dao.clearAllJobs()
        dao.clearAllExpenses()
        dao.clearAllPayments()
        dao.clearAllCustomers()
        AppDatabase.seedInitialDefaults(dao)
    }
}
