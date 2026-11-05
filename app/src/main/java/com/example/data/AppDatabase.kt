package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        VehicleEntity::class,
        ImplementEntity::class,
        CustomerEntity::class,
        JobEntity::class,
        PaymentEntity::class,
        ExpenseEntity::class,
        SupportTicketEntity::class,
        NotificationEntity::class,
        ReminderRuleEntity::class,
        MilestoneEntity::class,
        NotificationPrefsEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): KhetSaathiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "khetsaathi_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default vehicle and implements on creation
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).dao()
                            seedInitialDefaults(dao)
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialDefaults(dao: KhetSaathiDao) {
            val v1 = VehicleEntity(
                id = "veh_swaraj_744",
                name = "Swaraj 744 XT",
                regNo = "MP-04-TA-4521",
                type = "50 HP DI",
                isDefault = true
            )
            val v2 = VehicleEntity(
                id = "veh_mahindra_575",
                name = "Mahindra 575 DI",
                regNo = "MP-04-TB-8812",
                type = "45 HP",
                isDefault = false
            )
            dao.insertVehicle(v1)
            dao.insertVehicle(v2)

            val defaultImplements = listOf(
                ImplementEntity(
                    id = "imp_rotavator",
                    name = "Rotavator",
                    hindiName = "रोटावेटर",
                    icon = "rotavator",
                    details = "8 feet wide • 42 blades",
                    rateType = "HOUR",
                    defaultRate = 800.0,
                    sortOrder = 1,
                    usageCount = 10
                ),
                ImplementEntity(
                    id = "imp_cultivator",
                    name = "Cultivator",
                    hindiName = "कल्टीवेटर",
                    icon = "cultivator",
                    details = "9 tines • heavy spring loaded",
                    rateType = "HOUR",
                    defaultRate = 700.0,
                    sortOrder = 2,
                    usageCount = 8
                ),
                ImplementEntity(
                    id = "imp_plough",
                    name = "MB Plough",
                    hindiName = "एमबी प्लाउ",
                    icon = "plough",
                    details = "3 bottoms • 50 HP required",
                    rateType = "HOUR",
                    defaultRate = 1100.0,
                    sortOrder = 3,
                    usageCount = 5
                ),
                ImplementEntity(
                    id = "imp_thresher",
                    name = "Thresher",
                    hindiName = "थ्रेशर",
                    icon = "thresher",
                    details = "Multi-crop • 35 HP required",
                    rateType = "HOUR",
                    defaultRate = 950.0,
                    sortOrder = 4,
                    usageCount = 6
                ),
                ImplementEntity(
                    id = "imp_laser",
                    name = "Laser Leveller",
                    hindiName = "लेज़र लेवलर",
                    icon = "laser",
                    details = "7 feet wide • dual slope transmitter",
                    rateType = "HOUR",
                    defaultRate = 1200.0,
                    sortOrder = 5,
                    usageCount = 4
                ),
                ImplementEntity(
                    id = "imp_seed_drill",
                    name = "Seed Drill",
                    hindiName = "सीड ड्रिल",
                    icon = "seed_drill",
                    details = "9 tines • seed cum fertilizer",
                    rateType = "ACRE",
                    defaultRate = 500.0,
                    sortOrder = 6,
                    usageCount = 3
                ),
                ImplementEntity(
                    id = "imp_trolley",
                    name = "Trolley / Haulage",
                    hindiName = "ट्राली / ढुलाई",
                    icon = "trolley",
                    details = "12 feet • 10 ton capacity",
                    rateType = "CHAKKAR",
                    defaultRate = 600.0,
                    sortOrder = 7,
                    usageCount = 4
                ),
                ImplementEntity(
                    id = "imp_reaper",
                    name = "Straw Reaper",
                    hindiName = "भूसा रीपर",
                    icon = "reaper",
                    details = "5 feet cutter • 55 HP required",
                    rateType = "ACRE",
                    defaultRate = 1300.0,
                    sortOrder = 8,
                    usageCount = 2
                )
            )
            dao.insertImplements(defaultImplements)

            // Default profile
            val defaultUser = UserEntity(
                uid = "default_user",
                name = "रामसिंह लोधी (Ramsingh)",
                dob = "1988-06-15",
                phone = "+91 9826012345",
                language = "hi",
                termsAccepted = true
            )
            dao.insertUser(defaultUser)
        }
    }
}
