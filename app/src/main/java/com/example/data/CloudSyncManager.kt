package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "CloudSyncManager"

enum class SyncStatus {
    SYNCED,
    SYNCING,
    WAITING_INTERNET,
    ERROR
}

class CloudSyncManager(
    private val context: Context,
    private val dao: KhetSaathiDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val _syncStatus = MutableStateFlow(SyncStatus.SYNCED)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private var debounceJob: Job? = null

    fun triggerDebouncedSync() {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(4000) // 4 second debounce
            performSync()
        }
    }

    fun triggerImmediateSync() {
        debounceJob?.cancel()
        scope.launch {
            performSync()
        }
    }

    private fun isOnline(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val net = cm?.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    suspend fun performSync() {
        val user = Firebase.auth.currentUser
        if (user == null) {
            _syncStatus.value = SyncStatus.SYNCED
            return
        }

        if (!isOnline()) {
            _syncStatus.value = SyncStatus.WAITING_INTERNET
            return
        }

        _syncStatus.value = SyncStatus.SYNCING
        val uid = user.uid

        try {
            // 1. Sync User Profile
            val localUser = dao.getUserSync(uid) ?: dao.getFirstUserSync()
            if (localUser != null) {
                val userMap = hashMapOf<String, Any>(
                    "userId" to uid,
                    "name" to localUser.name.ifBlank { "Kisan Owner" },
                    "phone" to localUser.phone,
                    "village" to localUser.village,
                    "dob" to localUser.dob,
                    "language" to localUser.language,
                    "onboardingDone" to localUser.onboardingDone,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                localUser.photoUri?.let { userMap["photoUri"] = it }
                db.collection("users").document(uid).set(userMap, SetOptions.merge()).await()
            }

            // 2. Sync Vehicles
            val vehicles = dao.getAllVehiclesSync()
            for (v in vehicles) {
                val vMap = hashMapOf<String, Any>(
                    "id" to v.id,
                    "userId" to uid,
                    "brand" to v.brand,
                    "model" to v.model.ifBlank { v.name },
                    "hp" to v.hp,
                    "nickname" to v.name,
                    "regNo" to v.regNo,
                    "year" to v.year,
                    "isDefault" to v.isDefault,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                v.photoUri?.let { vMap["photoUri"] = it }
                db.collection("users").document(uid).collection("vehicles").document(v.id).set(vMap, SetOptions.merge()).await()
            }

            // 3. Sync Implements
            val implements = dao.getAllImplementsSync()
            for (imp in implements) {
                val impMap = hashMapOf<String, Any>(
                    "id" to imp.id,
                    "userId" to uid,
                    "name" to imp.name,
                    "hindiName" to imp.hindiName,
                    "category" to imp.category,
                    "rateType" to imp.rateType,
                    "defaultRate" to imp.defaultRate,
                    "details" to imp.details,
                    "active" to imp.active,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                imp.customUnitName?.let { impMap["customUnitName"] = it }
                imp.photo?.let { impMap["photo"] = it }
                db.collection("users").document(uid).collection("implements").document(imp.id).set(impMap, SetOptions.merge()).await()
            }

            // 4. Sync Customers
            val customers = dao.getAllCustomersSync()
            for (c in customers) {
                val cMap = hashMapOf<String, Any>(
                    "id" to c.id,
                    "userId" to uid,
                    "name" to c.name,
                    "phone" to c.phone,
                    "village" to c.village,
                    "notes" to c.notes,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(uid).collection("customers").document(c.id).set(cMap, SetOptions.merge()).await()
            }

            // 5. Sync Jobs
            val jobs = dao.getAllJobsSync()
            for (j in jobs) {
                val jMap = hashMapOf<String, Any>(
                    "id" to j.id,
                    "userId" to uid,
                    "customerId" to j.customerId,
                    "customerName" to j.customerName,
                    "vehicleId" to j.vehicleId,
                    "vehicleName" to j.vehicleName,
                    "implementId" to j.implementId,
                    "implementName" to j.implementName,
                    "implementDetailsSnapshot" to j.implementDetailsSnapshot,
                    "dateMillis" to j.dateMillis,
                    "status" to j.status,
                    "rateTypeSnapshot" to j.rateTypeSnapshot,
                    "customUnitSnapshot" to j.customUnitSnapshot,
                    "rateSnapshot" to j.rateSnapshot,
                    "hours" to j.hours,
                    "minutes" to j.minutes,
                    "area" to j.area,
                    "quantity" to j.quantity,
                    "extras" to j.extras,
                    "discount" to j.discount,
                    "total" to j.total,
                    "paidAmount" to j.paidAmount,
                    "paymentMethod" to j.paymentMethod,
                    "notes" to j.notes,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(uid).collection("jobs").document(j.id).set(jMap, SetOptions.merge()).await()
            }

            // 6. Sync Expenses
            val expenses = dao.getAllExpensesSync()
            for (e in expenses) {
                val eMap = hashMapOf<String, Any>(
                    "id" to e.id,
                    "userId" to uid,
                    "vehicleId" to e.vehicleId,
                    "vehicleName" to e.vehicleName,
                    "category" to e.category,
                    "amount" to e.amount,
                    "litres" to e.litres,
                    "dateMillis" to e.dateMillis,
                    "notes" to e.notes,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                e.photoUri?.let { eMap["photoUri"] = it }
                db.collection("users").document(uid).collection("expenses").document(e.id).set(eMap, SetOptions.merge()).await()
            }

            _lastSyncTime.value = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.SYNCED
            Log.d(TAG, "Cloud sync completed successfully for user $uid")
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            _syncStatus.value = SyncStatus.ERROR
        }
    }

    suspend fun restoreUserDataFromCloud(userId: String): Boolean {
        return try {
            _syncStatus.value = SyncStatus.SYNCING

            // 1. Restore User
            val userDoc = db.collection("users").document(userId).get().await()
            if (userDoc.exists()) {
                val name = userDoc.getString("name") ?: "Kisan"
                val phone = userDoc.getString("phone") ?: ""
                val village = userDoc.getString("village") ?: ""
                val dob = userDoc.getString("dob") ?: ""
                val photoUri = userDoc.getString("photoUri")
                val language = userDoc.getString("language") ?: "hi"
                val onboardingDone = userDoc.getBoolean("onboardingDone") ?: true

                dao.insertUser(
                    UserEntity(
                        uid = userId,
                        name = name,
                        phone = phone,
                        village = village,
                        dob = dob,
                        photoUri = photoUri,
                        language = language,
                        termsAccepted = true,
                        onboardingDone = onboardingDone
                    )
                )
            }

            // 2. Restore Vehicles
            val vDocs = db.collection("users").document(userId).collection("vehicles").get().await()
            val vehicles = vDocs.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val brand = doc.getString("brand") ?: ""
                val model = doc.getString("model") ?: ""
                val hp = (doc.get("hp") as? Number)?.toDouble() ?: 45.0
                val nickname = doc.getString("nickname") ?: "$brand $model".trim()
                val regNo = doc.getString("regNo") ?: ""
                val year = doc.getString("year") ?: ""
                val isDefault = doc.getBoolean("isDefault") ?: false
                val photoUri = doc.getString("photoUri")

                VehicleEntity(
                    id = id,
                    name = nickname.ifBlank { "Tractor" },
                    brand = brand,
                    model = model,
                    hp = hp,
                    regNo = regNo,
                    year = year,
                    isDefault = isDefault,
                    photoUri = photoUri
                )
            }
            if (vehicles.isNotEmpty()) {
                dao.insertVehicles(vehicles)
            }

            // 3. Restore Implements
            val impDocs = db.collection("users").document(userId).collection("implements").get().await()
            val implements = impDocs.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val name = doc.getString("name") ?: "Implement"
                val hindiName = doc.getString("hindiName") ?: ""
                val category = doc.getString("category") ?: "TILLAGE"
                val rateType = doc.getString("rateType") ?: "HOUR"
                val customUnit = doc.getString("customUnitName")
                val defaultRate = (doc.get("defaultRate") as? Number)?.toDouble() ?: 800.0
                val details = doc.getString("details") ?: ""
                val active = doc.getBoolean("active") ?: true
                val photo = doc.getString("photo")

                ImplementEntity(
                    id = id,
                    name = name,
                    hindiName = hindiName,
                    category = category,
                    rateType = rateType,
                    customUnitName = customUnit,
                    defaultRate = defaultRate,
                    details = details,
                    active = active,
                    photo = photo
                )
            }
            if (implements.isNotEmpty()) {
                dao.insertImplements(implements)
            }

            // 4. Restore Customers
            val cDocs = db.collection("users").document(userId).collection("customers").get().await()
            val customers = cDocs.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val name = doc.getString("name") ?: return@mapNotNull null
                val phone = doc.getString("phone") ?: ""
                val village = doc.getString("village") ?: ""
                val notes = doc.getString("notes") ?: ""
                CustomerEntity(id = id, name = name, phone = phone, village = village, notes = notes)
            }
            if (customers.isNotEmpty()) {
                dao.insertCustomers(customers)
            }

            // 5. Restore Jobs
            val jobDocs = db.collection("users").document(userId).collection("jobs").get().await()
            val jobs = jobDocs.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val customerId = doc.getString("customerId") ?: return@mapNotNull null
                val customerName = doc.getString("customerName") ?: "Kisan"
                val vehicleId = doc.getString("vehicleId") ?: ""
                val vehicleName = doc.getString("vehicleName") ?: ""
                val implementId = doc.getString("implementId") ?: ""
                val implementName = doc.getString("implementName") ?: ""
                val details = doc.getString("implementDetailsSnapshot") ?: ""
                val dateMillis = (doc.get("dateMillis") as? Number)?.toLong() ?: System.currentTimeMillis()
                val status = doc.getString("status") ?: "COMPLETED"
                val rateType = doc.getString("rateTypeSnapshot") ?: "HOUR"
                val customUnit = doc.getString("customUnitSnapshot") ?: ""
                val rate = (doc.get("rateSnapshot") as? Number)?.toDouble() ?: 0.0
                val hours = (doc.get("hours") as? Number)?.toInt() ?: 0
                val minutes = (doc.get("minutes") as? Number)?.toInt() ?: 0
                val area = (doc.get("area") as? Number)?.toDouble() ?: 0.0
                val qty = (doc.get("quantity") as? Number)?.toDouble() ?: 0.0
                val total = (doc.get("total") as? Number)?.toDouble() ?: 0.0
                val paid = (doc.get("paidAmount") as? Number)?.toDouble() ?: 0.0
                val method = doc.getString("paymentMethod") ?: "NONE"
                val notes = doc.getString("notes") ?: ""

                JobEntity(
                    id = id,
                    customerId = customerId,
                    customerName = customerName,
                    vehicleId = vehicleId,
                    vehicleName = vehicleName,
                    implementId = implementId,
                    implementName = implementName,
                    implementDetailsSnapshot = details,
                    dateMillis = dateMillis,
                    status = status,
                    rateTypeSnapshot = rateType,
                    customUnitSnapshot = customUnit,
                    rateSnapshot = rate,
                    hours = hours,
                    minutes = minutes,
                    area = area,
                    quantity = qty,
                    total = total,
                    paidAmount = paid,
                    paymentMethod = method,
                    notes = notes
                )
            }
            if (jobs.isNotEmpty()) {
                dao.insertJobs(jobs)
            }

            // 6. Restore Expenses
            val expDocs = db.collection("users").document(userId).collection("expenses").get().await()
            val expenses = expDocs.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val vehicleId = doc.getString("vehicleId") ?: ""
                val vehicleName = doc.getString("vehicleName") ?: ""
                val category = doc.getString("category") ?: "FUEL"
                val amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0
                val litres = (doc.get("litres") as? Number)?.toDouble() ?: 0.0
                val dateMillis = (doc.get("dateMillis") as? Number)?.toLong() ?: System.currentTimeMillis()
                val notes = doc.getString("notes") ?: ""
                val photoUri = doc.getString("photoUri")

                ExpenseEntity(
                    id = id,
                    vehicleId = vehicleId,
                    vehicleName = vehicleName,
                    category = category,
                    amount = amount,
                    litres = litres,
                    dateMillis = dateMillis,
                    notes = notes,
                    photoUri = photoUri
                )
            }
            if (expenses.isNotEmpty()) {
                dao.insertExpenses(expenses)
            }

            _syncStatus.value = SyncStatus.SYNCED
            true
        } catch (e: Exception) {
            Log.e(TAG, "Restore from cloud failed", e)
            _syncStatus.value = SyncStatus.ERROR
            false
        }
    }

    suspend fun submitSupportTicket(ticket: SupportTicketEntity): Boolean {
        return try {
            val user = Firebase.auth.currentUser
            val uid = user?.uid ?: ticket.userId
            val ticketMap = hashMapOf<String, Any>(
                "id" to ticket.id,
                "userId" to uid,
                "name" to ticket.name,
                "phone" to ticket.phone,
                "issueType" to ticket.issueType,
                "message" to ticket.message,
                "status" to "OPEN",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            dao.insertSupportTicket(ticket)
            if (isOnline()) {
                db.collection("support_tickets").document(ticket.id).set(ticketMap).await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit support ticket", e)
            false
        }
    }

    suspend fun exportBackupJson(): String {
        val root = JSONObject()
        val user = dao.getFirstUserSync()
        if (user != null) {
            root.put("user", JSONObject().apply {
                put("name", user.name)
                put("phone", user.phone)
                put("village", user.village)
                put("dob", user.dob)
                put("language", user.language)
            })
        }

        val vehiclesArr = JSONArray()
        dao.getAllVehiclesSync().forEach { v ->
            vehiclesArr.put(JSONObject().apply {
                put("name", v.name)
                put("brand", v.brand)
                put("model", v.model)
                put("hp", v.hp)
                put("regNo", v.regNo)
            })
        }
        root.put("vehicles", vehiclesArr)

        val impArr = JSONArray()
        dao.getAllImplementsSync().forEach { imp ->
            impArr.put(JSONObject().apply {
                put("name", imp.name)
                put("hindiName", imp.hindiName)
                put("category", imp.category)
                put("rateType", imp.rateType)
                put("defaultRate", imp.defaultRate)
                put("details", imp.details)
            })
        }
        root.put("implements", impArr)

        val custArr = JSONArray()
        dao.getAllCustomersSync().forEach { c ->
            custArr.put(JSONObject().apply {
                put("name", c.name)
                put("phone", c.phone)
                put("village", c.village)
            })
        }
        root.put("customers", custArr)

        val jobsArr = JSONArray()
        dao.getAllJobsSync().forEach { j ->
            jobsArr.put(JSONObject().apply {
                put("customerName", j.customerName)
                put("vehicleName", j.vehicleName)
                put("implementName", j.implementName)
                put("dateMillis", j.dateMillis)
                put("total", j.total)
                put("paidAmount", j.paidAmount)
            })
        }
        root.put("jobs", jobsArr)

        return root.toString(2)
    }
}
