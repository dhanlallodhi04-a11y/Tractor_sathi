package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CustomerEntity
import com.example.data.ExpenseEntity
import com.example.data.ImplementEntity
import com.example.data.JobEntity
import com.example.data.KhetSaathiRepository
import com.example.data.MilestoneEntity
import com.example.data.NotificationEntity
import com.example.data.NotificationPrefsEntity
import com.example.data.PaymentEntity
import com.example.data.ReminderRuleEntity
import com.example.data.UserEntity
import com.example.data.VehicleEntity
import com.example.notification.NotificationService
import com.example.ui.components.CelebrationData
import com.example.util.VoiceParsedResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class TodayStats(
    val jobsCount: Int = 0,
    val earned: Double = 0.0,
    val spent: Double = 0.0,
    val baki: Double = 0.0,
    val netProfit: Double = 0.0
)

data class CustomerLedger(
    val customer: CustomerEntity,
    val totalBilled: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalDue: Double = 0.0,
    val jobsCount: Int = 0
)

data class VehicleMetric(
    val vehicle: VehicleEntity,
    val jobsCount: Int = 0,
    val totalEarnings: Double = 0.0,
    val fuelLitres: Double = 0.0,
    val fuelSpent: Double = 0.0,
    val runningHours: Double = 0.0,
    val dieselAvgLitresPerHour: Double = 0.0
)

data class ImplementStats(
    val timesUsed: Int = 0,
    val totalWorkedStr: String = "",
    val totalEarned: Double = 0.0
)

enum class ReportPeriod {
    TODAY, WEEK, MONTH, KHARIF, RABI, FINANCIAL_YEAR, ALL_TIME
}

data class AnalyticsState(
    val period: ReportPeriod = ReportPeriod.MONTH,
    val compareWithPrevious: Boolean = true,
    val totalBilled: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalDue: Double = 0.0,
    val totalSpent: Double = 0.0,
    val netProfit: Double = 0.0,
    // Comparisons
    val prevBilled: Double = 0.0,
    val prevCollected: Double = 0.0,
    val prevSpent: Double = 0.0,
    val prevNetProfit: Double = 0.0,
    // Breakdowns
    val implementEarnings: List<Pair<String, Double>> = emptyList(),
    val topCustomers: List<Pair<String, Double>> = emptyList(),
    val expenseBreakdown: List<Pair<String, Double>> = emptyList(),
    val trendPoints: List<Pair<String, Double>> = emptyList() // Date label -> Profit
)

class KhetSaathiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: KhetSaathiRepository
    val syncManager: com.example.data.CloudSyncManager
    val authManager: com.example.util.AuthManager
    val aiAssistantManager: com.example.util.AiAssistantManager

    val syncStatus: StateFlow<com.example.data.SyncStatus>
    val lastSyncTime: StateFlow<Long>

    val supportTickets: StateFlow<List<com.example.data.SupportTicketEntity>>

    // AI Assistant State
    private val _chatMessages = MutableStateFlow<List<com.example.util.ChatMessage>>(
        listOf(
            com.example.util.ChatMessage(
                sender = com.example.util.MessageSender.ASSISTANT,
                text = "राम-राम किसान भाई! मैं आपका 'किसान AI साथी' हूँ। आप मुझसे रोटावेटर/कल्टीवेटर के सही रेट, डीजल बचत या अपने बहीखाते का हिसाब पूछ सकते हैं।"
            )
        )
    )
    val chatMessages: StateFlow<List<com.example.util.ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _isAiTtsEnabled = MutableStateFlow(true)
    val isAiTtsEnabled: StateFlow<Boolean> = _isAiTtsEnabled.asStateFlow()

    // Guided Tour State
    private val _isTourActive = MutableStateFlow(false)
    val isTourActive: StateFlow<Boolean> = _isTourActive.asStateFlow()

    private val _tourStepIndex = MutableStateFlow(0)
    val tourStepIndex: StateFlow<Int> = _tourStepIndex.asStateFlow()

    private val _isTourMuted = MutableStateFlow(false)
    val isTourMuted: StateFlow<Boolean> = _isTourMuted.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = KhetSaathiRepository(db.dao())
        syncManager = com.example.data.CloudSyncManager(application, db.dao(), viewModelScope)
        authManager = com.example.util.AuthManager(application, viewModelScope)
        aiAssistantManager = com.example.util.AiAssistantManager(application)

        syncStatus = syncManager.syncStatus
        lastSyncTime = syncManager.lastSyncTime
        supportTickets = repository.supportTickets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        syncManager.triggerDebouncedSync()

        NotificationService.initialize(application)
        NotificationService.rescheduleAll(application)
    }

    val user: StateFlow<UserEntity?> = repository.user
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val reminderRules: StateFlow<List<ReminderRuleEntity>> = repository.reminderRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val milestones: StateFlow<List<MilestoneEntity>> = repository.milestones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationPrefs: StateFlow<NotificationPrefsEntity?> = repository.notificationPrefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _celebrationData = MutableStateFlow<CelebrationData?>(null)
    val celebrationData: StateFlow<CelebrationData?> = _celebrationData.asStateFlow()

    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    val vehicles: StateFlow<List<VehicleEntity>> = repository.vehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val implements: StateFlow<List<ImplementEntity>> = repository.implements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = repository.customers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val jobs: StateFlow<List<JobEntity>> = repository.jobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> = repository.payments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state filters
    private val _selectedVehicleId = MutableStateFlow<String?>(null) // null = all
    val selectedVehicleId: StateFlow<String?> = _selectedVehicleId.asStateFlow()

    private val _currentLanguage = MutableStateFlow("hi")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.MONTH)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    private val _compareWithPrevious = MutableStateFlow(true)
    val compareWithPrevious: StateFlow<Boolean> = _compareWithPrevious.asStateFlow()

    // Undo buffer for deleted job
    private var recentlyDeletedJob: JobEntity? = null
    private val _undoMessage = MutableStateFlow<String?>(null)
    val undoMessage: StateFlow<String?> = _undoMessage.asStateFlow()

    // Voice recognition draft state
    private val _activeVoiceDraft = MutableStateFlow<VoiceParsedResult?>(null)
    val activeVoiceDraft: StateFlow<VoiceParsedResult?> = _activeVoiceDraft.asStateFlow()

    // Today's summary computation
    val todayStats: StateFlow<TodayStats> = combine(jobs, expenses, selectedVehicleId) { allJobs, allExpenses, vehId ->
        val cal = Calendar.getInstance()
        val startOfToday = cal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfToday = startOfToday + 86400000L

        val filteredJobs = allJobs.filter {
            it.dateMillis in startOfToday..endOfToday && (vehId == null || it.vehicleId == vehId)
        }
        val filteredExpenses = allExpenses.filter {
            it.dateMillis in startOfToday..endOfToday && (vehId == null || it.vehicleId == vehId)
        }

        val earned = filteredJobs.sumOf { it.total }
        val spent = filteredExpenses.sumOf { it.amount }
        val baki = filteredJobs.sumOf { (it.total - it.paidAmount).coerceAtLeast(0.0) }
        val profit = earned - spent

        TodayStats(
            jobsCount = filteredJobs.size,
            earned = earned,
            spent = spent,
            baki = baki,
            netProfit = profit
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayStats())

    // Customer ledgers & who owes me
    val customerLedgers: StateFlow<List<CustomerLedger>> = combine(customers, jobs) { allCustomers, allJobs ->
        allCustomers.map { customer ->
            val cJobs = allJobs.filter { it.customerId == customer.id }
            val billed = cJobs.sumOf { it.total }
            val paid = cJobs.sumOf { it.paidAmount }
            val due = (billed - paid).coerceAtLeast(0.0)
            CustomerLedger(
                customer = customer,
                totalBilled = billed,
                totalPaid = paid,
                totalDue = due,
                jobsCount = cJobs.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Vehicle statistics & Diesel consumption
    val vehicleMetrics: StateFlow<List<VehicleMetric>> = combine(vehicles, jobs, expenses) { vList, jList, eList ->
        vList.map { vehicle ->
            val vJobs = jList.filter { it.vehicleId == vehicle.id }
            val vExpenses = eList.filter { it.vehicleId == vehicle.id }
            val earnings = vJobs.sumOf { it.total }
            val fuelLitres = vExpenses.filter { it.category == "FUEL" }.sumOf { it.litres }
            val fuelSpent = vExpenses.filter { it.category == "FUEL" }.sumOf { it.amount }
            val hours = vJobs.sumOf { it.hours + (it.minutes / 60.0) }
            val avg = if (hours > 0) fuelLitres / hours else 0.0

            VehicleMetric(
                vehicle = vehicle,
                jobsCount = vJobs.size,
                totalEarnings = earnings,
                fuelLitres = fuelLitres,
                fuelSpent = fuelSpent,
                runningHours = hours,
                dieselAvgLitresPerHour = avg
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Analytics state for Reports & Analytics
    val analytics: StateFlow<AnalyticsState> = combine(
        jobs, expenses, selectedPeriod, compareWithPrevious, selectedVehicleId
    ) { allJobs, allExpenses, period, compare, vehId ->
        computeAnalytics(allJobs, allExpenses, period, compare, vehId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsState())

    init {
        viewModelScope.launch {
            user.collect { u ->
                if (u != null) {
                    _currentLanguage.value = u.language
                }
            }
        }
    }

    fun selectVehicleFilter(vehicleId: String?) {
        _selectedVehicleId.value = vehicleId
    }

    fun setPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
    }

    fun setCompareWithPrevious(compare: Boolean) {
        _compareWithPrevious.value = compare
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
        viewModelScope.launch {
            val current = user.value ?: UserEntity()
            repository.saveUser(current.copy(language = lang))
        }
    }

    fun saveUser(name: String, dob: String, phone: String) {
        viewModelScope.launch {
            val current = user.value ?: UserEntity()
            repository.saveUser(
                current.copy(
                    name = name,
                    dob = dob,
                    phone = phone,
                    termsAccepted = true
                )
            )
        }
    }

    fun saveJob(
        job: JobEntity,
        alsoUpdateDefaultRate: Boolean = false,
        newDefaultRate: Double = 0.0
    ) {
        viewModelScope.launch {
            val isFirstJob = jobs.value.isEmpty()
            repository.insertJob(job)
            if (alsoUpdateDefaultRate && newDefaultRate > 0) {
                val imp = implements.value.find { it.id == job.implementId }
                if (imp != null) {
                    repository.updateImplement(imp.copy(defaultRate = newDefaultRate))
                }
            }

            if (isFirstJob) {
                _celebrationData.value = CelebrationData(
                    title = "पहला काम दर्ज हुआ! 🚜",
                    message = "शाबाश! आपका पहला काम दर्ज हो गया। अब हर काम का हिसाब आपके पास रहेगा।",
                    buttonText = "शानदार!",
                    shareText = "मैंने किसान साथी ऐप में अपना पहला ट्रैक्टर काम दर्ज किया! 🚜"
                )
            }

            NotificationService.rescheduleAll(getApplication())
        }
    }

    private var recentlyDeletedCustomer: CustomerEntity? = null

    fun deleteJob(job: JobEntity) {
        recentlyDeletedJob = job
        viewModelScope.launch {
            repository.deleteJob(job.id)
            _undoMessage.value = "Job deleted"
        }
    }

    fun undoDeleteJob() {
        if (recentlyDeletedCustomer != null) {
            undoDeleteCustomer()
            return
        }
        if (recentlyDeletedImplement != null) {
            undoDeleteImplement()
            return
        }
        recentlyDeletedJob?.let { job ->
            viewModelScope.launch {
                repository.insertJob(job)
                recentlyDeletedJob = null
                _undoMessage.value = null
            }
        }
    }

    fun clearUndoMessage() {
        _undoMessage.value = null
    }

    fun addPaymentToJob(job: JobEntity, amount: Double, method: String) {
        viewModelScope.launch {
            val isFirstPayment = payments.value.isEmpty()
            val updatedJob = job.copy(
                paidAmount = job.paidAmount + amount,
                paymentMethod = method
            )
            repository.updateJob(updatedJob)
            repository.addPayment(
                PaymentEntity(
                    jobId = job.id,
                    customerId = job.customerId,
                    amount = amount,
                    dateMillis = System.currentTimeMillis(),
                    method = method,
                    notes = "Partial/full payment for ${job.implementName}"
                )
            )

            if (isFirstPayment) {
                _celebrationData.value = CelebrationData(
                    title = "पहला भुगतान मिला! 💰",
                    message = "बहुत बढ़िया! पहला भुगतान दर्ज हुआ, अब हिसाब पक्का रहेगा।",
                    buttonText = "आगे बढ़ें"
                )
            }
            NotificationService.rescheduleAll(getApplication())
        }
    }

    fun recordCustomerPayment(customerId: String, amount: Double, method: String, notes: String = "") {
        viewModelScope.launch {
            val isFirstPayment = payments.value.isEmpty()
            // Apply payment across unpaid jobs for this customer starting with oldest
            var remaining = amount
            val custJobs = jobs.value.filter { it.customerId == customerId && (it.total - it.paidAmount) > 0.01 }
                .sortedBy { it.dateMillis }

            for (j in custJobs) {
                if (remaining <= 0.0) break
                val due = j.total - j.paidAmount
                val payForThis = minOf(due, remaining)
                repository.updateJob(j.copy(paidAmount = j.paidAmount + payForThis))
                remaining -= payForThis
            }

            repository.addPayment(
                PaymentEntity(
                    jobId = custJobs.firstOrNull()?.id ?: "direct_payment",
                    customerId = customerId,
                    amount = amount,
                    dateMillis = System.currentTimeMillis(),
                    method = method,
                    notes = notes.ifEmpty { "Khata payment received" }
                )
            )

            if (isFirstPayment) {
                _celebrationData.value = CelebrationData(
                    title = "पहला भुगतान मिला! 💰",
                    message = "बहुत बढ़िया! पहला भुगतान दर्ज हुआ, अब हिसाब पक्का रहेगा।",
                    buttonText = "आगे बढ़ें"
                )
            }
            NotificationService.rescheduleAll(getApplication())
        }
    }

    fun saveCustomer(name: String, phone: String, village: String, notes: String = "") {
        viewModelScope.launch {
            val isFirstKisan = customers.value.isEmpty()
            val newCust = CustomerEntity(
                name = name,
                phone = phone,
                village = village,
                notes = notes
            )
            repository.insertCustomer(newCust)

            val newCount = customers.value.size + 1
            if (isFirstKisan) {
                _celebrationData.value = CelebrationData(
                    title = "पहला किसान जुड़ गया! 🎉",
                    message = "बधाई हो ${user.value?.name ?: ""}! आपने अपना पहला किसान जोड़ना सीख लिया है। अब उसका पहला काम भी दर्ज करें।",
                    buttonText = "काम जोड़ें",
                    shareText = "मैंने किसान साथी ऐप में अपना पहला ग्राहक जोड़ा! 🌾"
                )
            } else {
                val milestone = NotificationService.checkKisanMilestones(getApplication(), newCount)
                if (milestone != null) {
                    _celebrationData.value = CelebrationData(
                        title = milestone.title,
                        message = "बधाई हो! आपके ${milestone.value.toInt()} किसान पूरे हो गए हैं। लोग आप पर भरोसा करते हैं।",
                        buttonText = "शानदार!",
                        shareText = "मेरे किसान साथी ऐप में ${milestone.value.toInt()} किसान जुड़ चुके हैं! 🏆"
                    )
                }
            }
            NotificationService.rescheduleAll(getApplication())
        }
    }

    fun saveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            val isFirstKisan = customers.value.isEmpty()
            repository.insertCustomer(customer)

            val newCount = customers.value.size + 1
            if (isFirstKisan) {
                _celebrationData.value = CelebrationData(
                    title = "पहला किसान जुड़ गया! 🎉",
                    message = "बधाई हो ${user.value?.name ?: ""}! आपने अपना पहला किसान जोड़ना सीख लिया है। अब उसका पहला काम भी दर्ज करें।",
                    buttonText = "काम जोड़ें"
                )
            } else {
                val milestone = NotificationService.checkKisanMilestones(getApplication(), newCount)
                if (milestone != null) {
                    _celebrationData.value = CelebrationData(
                        title = milestone.title,
                        message = "बधाई हो! आपके ${milestone.value.toInt()} किसान पूरे हो गए हैं। लोग आप पर भरोसा करते हैं।",
                        buttonText = "शानदार!"
                    )
                }
            }
            NotificationService.rescheduleAll(getApplication())
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            NotificationService.rescheduleAll(getApplication())
        }
    }

    fun saveCustomerInline(name: String, phone: String, village: String, onSaved: (CustomerEntity) -> Unit) {
        val newCust = CustomerEntity(name = name, phone = phone, village = village)
        viewModelScope.launch {
            val isFirstKisan = customers.value.isEmpty()
            repository.insertCustomer(newCust)
            onSaved(newCust)

            if (isFirstKisan) {
                _celebrationData.value = CelebrationData(
                    title = "पहला किसान जुड़ गया! 🎉",
                    message = "बधाई हो ${user.value?.name ?: ""}! आपने अपना पहला किसान जोड़ना सीख लिया है। अब उसका पहला काम भी दर्ज करें।",
                    buttonText = "काम जोड़ें"
                )
            }
            NotificationService.rescheduleAll(getApplication())
        }
    }

    fun deleteCustomerWithUndo(customer: CustomerEntity) {
        recentlyDeletedCustomer = customer
        viewModelScope.launch {
            repository.deleteCustomer(customer.id)
            _undoMessage.value = "Kisan removed"
            NotificationService.rescheduleAll(getApplication())
        }
    }

    fun undoDeleteCustomer() {
        recentlyDeletedCustomer?.let { cust ->
            viewModelScope.launch {
                repository.insertCustomer(cust)
                recentlyDeletedCustomer = null
                _undoMessage.value = null
                NotificationService.rescheduleAll(getApplication())
            }
        }
    }

    fun deleteCustomer(id: String) {
        val cust = customers.value.find { it.id == id }
        if (cust != null) {
            deleteCustomerWithUndo(cust)
        } else {
            viewModelScope.launch {
                repository.deleteCustomer(id)
                NotificationService.rescheduleAll(getApplication())
            }
        }
    }

    fun saveVehicle(name: String, regNo: String, type: String, isDefault: Boolean) {
        viewModelScope.launch {
            val isFirst = vehicles.value.isEmpty()
            val v = VehicleEntity(
                name = name,
                regNo = regNo,
                type = type,
                isDefault = isDefault
            )
            repository.insertVehicle(v)
            if (isDefault) {
                repository.setDefaultVehicle(v.id)
            }

            _celebrationData.value = CelebrationData(
                title = "नया ट्रैक्टर जुड़ा! 🚜",
                message = if (isFirst) "बधाई हो! आपका पहला ट्रैक्टर $name (${type}) जुड़ गया है। अब इसके काम और डीज़ल का अलग हिसाब रखें।"
                          else "बधाई हो! आपके वाहन सेक्शन में नया सदस्य जुड़ा: $name। 🚜",
                buttonText = "शानदार!",
                shareText = "मेरे ट्रैक्टर बेड़े में नया सदस्य जुड़ा: $name 🚜"
            )
            NotificationService.rescheduleAll(getApplication())
        }
    }

    fun setDefaultVehicle(id: String) {
        viewModelScope.launch {
            repository.setDefaultVehicle(id)
        }
    }

    private var recentlyDeletedImplement: ImplementEntity? = null

    fun saveImplement(implement: ImplementEntity) {
        viewModelScope.launch {
            val isFirst = implements.value.isEmpty()
            repository.insertImplement(implement)

            _celebrationData.value = CelebrationData(
                title = "नया औज़ार जुड़ा! 🛠️",
                message = "नया औज़ार जुड़ा: ${implement.name} (${implement.details.ifEmpty { "दर: ₹" + implement.defaultRate }})। अब इसका रेट सेव है।",
                buttonText = "ठीक है"
            )
            NotificationService.rescheduleAll(getApplication())
        }
    }

    fun dismissCelebration() {
        _celebrationData.value = null
    }

    fun triggerCelebration(data: CelebrationData) {
        _celebrationData.value = data
    }

    fun setPermissionDialogVisible(visible: Boolean) {
        _showPermissionDialog.value = visible
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun updateNotificationPrefs(prefs: NotificationPrefsEntity) {
        viewModelScope.launch {
            repository.updateNotificationPrefs(prefs)
            NotificationService.rescheduleAll(getApplication())
        }
    }

    suspend fun sendTestNotification() {
        NotificationService.sendTestNotification(getApplication())
    }

    fun deleteImplementWithUndo(implement: ImplementEntity) {
        recentlyDeletedImplement = implement
        viewModelScope.launch {
            repository.deleteImplement(implement.id)
            _undoMessage.value = "Implement deleted"
        }
    }

    fun undoDeleteImplement() {
        recentlyDeletedImplement?.let { imp ->
            viewModelScope.launch {
                repository.insertImplement(imp)
                recentlyDeletedImplement = null
                _undoMessage.value = null
            }
        }
    }

    fun deleteImplement(id: String) {
        val imp = implements.value.find { it.id == id }
        if (imp != null) {
            deleteImplementWithUndo(imp)
        } else {
            viewModelScope.launch {
                repository.deleteImplement(id)
            }
        }
    }

    fun getImplementStats(implementId: String): ImplementStats {
        val impJobs = jobs.value.filter { it.implementId == implementId }
        val timesUsed = impJobs.size
        val totalEarned = impJobs.sumOf { it.total }
        val totalWorked = if (impJobs.isNotEmpty()) {
            val firstRateType = impJobs.first().rateTypeSnapshot
            when (firstRateType) {
                "HOUR" -> {
                    val totalMins = impJobs.sumOf { it.hours * 60 + it.minutes }
                    val h = totalMins / 60
                    val m = totalMins % 60
                    "${h}h ${m}m"
                }
                "ACRE", "BIGHA", "HECTARE", "GUNTHA", "KANAL", "MARLA", "CENT" -> {
                    val totalArea = impJobs.sumOf { it.area }
                    "${"%.1f".format(totalArea)} $firstRateType"
                }
                "CHAKKAR" -> {
                    val trips = impJobs.sumOf { it.quantity.toInt() }
                    "$trips चक्कर"
                }
                "DAY" -> {
                    val days = impJobs.sumOf { it.quantity.toInt() }
                    "$days दिन"
                }
                else -> {
                    val qty = impJobs.sumOf { it.quantity }
                    if (qty > 0) "${qty.toInt()} यूनिट" else "-"
                }
            }
        } else ""
        return ImplementStats(timesUsed, totalWorked, totalEarned)
    }


    fun saveExpense(
        vehicleId: String,
        vehicleName: String,
        category: String,
        amount: Double,
        litres: Double,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertExpense(
                ExpenseEntity(
                    vehicleId = vehicleId,
                    vehicleName = vehicleName,
                    category = category,
                    amount = amount,
                    litres = litres,
                    notes = notes
                )
            )
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            repository.deleteExpense(id)
        }
    }

    fun setVoiceDraft(draft: VoiceParsedResult?) {
        _activeVoiceDraft.value = draft
    }

    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedDemoData()
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }

    private fun computeAnalytics(
        allJobs: List<JobEntity>,
        allExpenses: List<ExpenseEntity>,
        period: ReportPeriod,
        compare: Boolean,
        vehId: String?
    ): AnalyticsState {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        val (curStart, curEnd, prevStart, prevEnd) = calculatePeriodBounds(period, cal)

        val curJobs = allJobs.filter {
            it.dateMillis in curStart..curEnd && (vehId == null || it.vehicleId == vehId)
        }
        val curExpenses = allExpenses.filter {
            it.dateMillis in curStart..curEnd && (vehId == null || it.vehicleId == vehId)
        }

        val billed = curJobs.sumOf { it.total }
        val collected = curJobs.sumOf { it.paidAmount }
        val due = (billed - collected).coerceAtLeast(0.0)
        val spent = curExpenses.sumOf { it.amount }
        val net = billed - spent

        // Previous period for comparison
        val prevJobs = allJobs.filter {
            it.dateMillis in prevStart..prevEnd && (vehId == null || it.vehicleId == vehId)
        }
        val prevExpenses = allExpenses.filter {
            it.dateMillis in prevStart..prevEnd && (vehId == null || it.vehicleId == vehId)
        }

        val pBilled = prevJobs.sumOf { it.total }
        val pCollected = prevJobs.sumOf { it.paidAmount }
        val pSpent = prevExpenses.sumOf { it.amount }
        val pNet = pBilled - pSpent

        // Implement breakdown
        val impMap = mutableMapOf<String, Double>()
        for (j in curJobs) {
            val key = j.implementName
            impMap[key] = (impMap[key] ?: 0.0) + j.total
        }
        val implementEarnings = impMap.toList().sortedByDescending { it.second }

        // Top customers
        val custMap = mutableMapOf<String, Double>()
        for (j in curJobs) {
            val key = j.customerName
            custMap[key] = (custMap[key] ?: 0.0) + j.total
        }
        val topCustomers = custMap.toList().sortedByDescending { it.second }.take(5)

        // Expense breakdown
        val expMap = mutableMapOf<String, Double>()
        for (e in curExpenses) {
            val catName = when (e.category) {
                "FUEL" -> "डीजल (Diesel)"
                "MAINTENANCE" -> "मेंटेनेंस (Maintenance)"
                else -> "अन्य (Other)"
            }
            expMap[catName] = (expMap[catName] ?: 0.0) + e.amount
        }
        val expenseBreakdown = expMap.toList().sortedByDescending { it.second }

        // Trend points
        val trendPoints = listOf(
            Pair("P-4", (pNet * 0.4).coerceAtLeast(0.0)),
            Pair("P-3", (pNet * 0.7).coerceAtLeast(0.0)),
            Pair("P-2", pNet.coerceAtLeast(0.0)),
            Pair("P-1", (net * 0.8).coerceAtLeast(0.0)),
            Pair("Current", net.coerceAtLeast(0.0))
        )

        return AnalyticsState(
            period = period,
            compareWithPrevious = compare,
            totalBilled = billed,
            totalCollected = collected,
            totalDue = due,
            totalSpent = spent,
            netProfit = net,
            prevBilled = pBilled,
            prevCollected = pCollected,
            prevSpent = pSpent,
            prevNetProfit = pNet,
            implementEarnings = implementEarnings,
            topCustomers = topCustomers,
            expenseBreakdown = expenseBreakdown,
            trendPoints = trendPoints
        )
    }

    private fun calculatePeriodBounds(period: ReportPeriod, cal: Calendar): LongArray {
        val now = System.currentTimeMillis()
        val c = Calendar.getInstance()

        return when (period) {
            ReportPeriod.TODAY -> {
                c.set(Calendar.HOUR_OF_DAY, 0)
                c.set(Calendar.MINUTE, 0)
                c.set(Calendar.SECOND, 0)
                val start = c.timeInMillis
                val end = start + 86400000L
                val prevStart = start - 86400000L
                val prevEnd = start
                longArrayOf(start, end, prevStart, prevEnd)
            }
            ReportPeriod.WEEK -> {
                c.add(Calendar.DAY_OF_YEAR, -7)
                val start = c.timeInMillis
                val end = now
                val prevStart = start - (7 * 86400000L)
                val prevEnd = start
                longArrayOf(start, end, prevStart, prevEnd)
            }
            ReportPeriod.MONTH -> {
                c.add(Calendar.DAY_OF_YEAR, -30)
                val start = c.timeInMillis
                val end = now
                val prevStart = start - (30 * 86400000L)
                val prevEnd = start
                longArrayOf(start, end, prevStart, prevEnd)
            }
            ReportPeriod.KHARIF -> { // June to October
                c.set(Calendar.MONTH, Calendar.JUNE)
                c.set(Calendar.DAY_OF_MONTH, 1)
                val start = c.timeInMillis
                c.set(Calendar.MONTH, Calendar.OCTOBER)
                c.set(Calendar.DAY_OF_MONTH, 31)
                val end = c.timeInMillis
                c.add(Calendar.YEAR, -1)
                val prevEnd = c.timeInMillis
                c.set(Calendar.MONTH, Calendar.JUNE)
                c.set(Calendar.DAY_OF_MONTH, 1)
                val prevStart = c.timeInMillis
                longArrayOf(start, end, prevStart, prevEnd)
            }
            ReportPeriod.RABI -> { // November to March
                c.set(Calendar.MONTH, Calendar.NOVEMBER)
                c.set(Calendar.DAY_OF_MONTH, 1)
                val start = c.timeInMillis
                c.add(Calendar.YEAR, 1)
                c.set(Calendar.MONTH, Calendar.MARCH)
                c.set(Calendar.DAY_OF_MONTH, 31)
                val end = c.timeInMillis
                val prevStart = start - 365 * 86400000L
                val prevEnd = end - 365 * 86400000L
                longArrayOf(start, end, prevStart, prevEnd)
            }
            ReportPeriod.FINANCIAL_YEAR, ReportPeriod.ALL_TIME -> {
                c.set(Calendar.MONTH, Calendar.APRIL)
                c.set(Calendar.DAY_OF_MONTH, 1)
                val start = c.timeInMillis
                val end = now
                val prevStart = start - 365 * 86400000L
                val prevEnd = start
                longArrayOf(start, end, prevStart, prevEnd)
            }
        }
    }

    // Cloud Sync
    fun triggerCloudSync() {
        syncManager.triggerImmediateSync()
    }

    fun restoreCloudData(userId: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = syncManager.restoreUserDataFromCloud(userId)
            onDone(result)
        }
    }

    // Support Ticket
    fun submitSupportTicket(issueType: String, message: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val currentUser = user.value
            val ticket = com.example.data.SupportTicketEntity(
                userId = currentUser?.uid ?: authManager.getUserId(),
                name = currentUser?.name ?: "Kisan Owner",
                phone = currentUser?.phone ?: "",
                issueType = issueType,
                message = message,
                status = "OPEN"
            )
            val success = syncManager.submitSupportTicket(ticket)
            onDone(success)
        }
    }

    // AI Chat Assistant
    fun sendAiMessage(prompt: String) {
        val query = prompt.trim()
        if (query.isBlank()) return

        val userMsg = com.example.util.ChatMessage(
            sender = com.example.util.MessageSender.USER,
            text = query
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val stats = todayStats.value
            val contextSummary = "User: ${user.value?.name ?: "Farmer"}, Tractors: ${vehicles.value.size}, Jobs Today: ${stats.jobsCount}, Earnings Today: ₹${stats.earned}, Spent Today: ₹${stats.spent}, Due: ₹${stats.baki}"
            val response = aiAssistantManager.askAssistant(query, contextSummary)

            val assistantMsg = com.example.util.ChatMessage(
                sender = com.example.util.MessageSender.ASSISTANT,
                text = response
            )
            _chatMessages.value = _chatMessages.value + assistantMsg
            _isAiLoading.value = false

            if (_isAiTtsEnabled.value) {
                aiAssistantManager.speak(response)
            }
        }
    }

    fun toggleAiTts() {
        _isAiTtsEnabled.value = !_isAiTtsEnabled.value
        if (!_isAiTtsEnabled.value) {
            aiAssistantManager.stopSpeaking()
        }
    }

    // Guided Tour Controls
    fun startTour() {
        _tourStepIndex.value = 0
        _isTourActive.value = true
    }

    fun nextTourStep(totalSteps: Int) {
        if (_tourStepIndex.value < totalSteps - 1) {
            _tourStepIndex.value += 1
        } else {
            dismissTour()
        }
    }

    fun prevTourStep() {
        if (_tourStepIndex.value > 0) {
            _tourStepIndex.value -= 1
        }
    }

    fun dismissTour() {
        _isTourActive.value = false
        aiAssistantManager.stopSpeaking()
    }

    fun toggleTourMute() {
        _isTourMuted.value = !_isTourMuted.value
        if (_isTourMuted.value) {
            aiAssistantManager.stopSpeaking()
        }
    }

    // Onboarding
    fun saveUserWithDetails(name: String, village: String, dob: String, phone: String, photoUri: String? = null) {
        viewModelScope.launch {
            val cur = user.value ?: UserEntity()
            val updated = cur.copy(
                name = name,
                village = village,
                dob = dob,
                phone = phone,
                photoUri = photoUri ?: cur.photoUri,
                termsAccepted = true
            )
            repository.saveUser(updated)
            syncManager.triggerDebouncedSync()
        }
    }

    fun completeOnboarding(
        updatedUser: UserEntity,
        newTractors: List<VehicleEntity>,
        newImplements: List<ImplementEntity>
    ) {
        viewModelScope.launch {
            repository.saveUser(updatedUser.copy(onboardingDone = true, termsAccepted = true))
            for (t in newTractors) {
                repository.insertVehicle(t)
            }
            for (imp in newImplements) {
                repository.insertImplement(imp)
            }
            syncManager.triggerDebouncedSync()
            // Automatically launch guided tour
            startTour()
        }
    }

    // Custom Spec Additions
    fun addVehicleWithSpecs(
        brand: String,
        model: String,
        hp: Double,
        nickname: String,
        regNo: String,
        year: String,
        photoUri: String? = null,
        isDefault: Boolean = false
    ) {
        viewModelScope.launch {
            val v = VehicleEntity(
                brand = brand,
                model = model,
                hp = hp,
                name = nickname.ifBlank { "$brand $model".trim() },
                regNo = regNo,
                year = year,
                photoUri = photoUri,
                isDefault = isDefault
            )
            repository.insertVehicle(v)
            syncManager.triggerDebouncedSync()
        }
    }

    fun addImplementWithSpecs(
        name: String,
        hindiName: String,
        category: String,
        rateType: String,
        defaultRate: Double,
        customUnitName: String?,
        details: String,
        photo: String? = null
    ) {
        viewModelScope.launch {
            val imp = ImplementEntity(
                name = name,
                hindiName = hindiName,
                category = category,
                rateType = rateType,
                defaultRate = defaultRate,
                customUnitName = customUnitName,
                details = details,
                photo = photo,
                sortOrder = implements.value.size + 1
            )
            repository.insertImplement(imp)
            syncManager.triggerDebouncedSync()
        }
    }
}
