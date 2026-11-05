package com.example.data

import java.util.Calendar

object DemoDataSeeder {
    suspend fun seedFullDemoData(dao: KhetSaathiDao) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        // Customers
        val c1 = CustomerEntity("cust_1", "सुरेश पटेल (Suresh Patel)", "+91 9827011223", "रामपुर (Rampur)", "नियमित ग्राहक")
        val c2 = CustomerEntity("cust_2", "महेंद्र वर्मा (Mahendra Verma)", "+91 9425022334", "पिपरिया (Pipariya)", "20 एकड़ खेत")
        val c3 = CustomerEntity("cust_3", "दिनेश लोधी (Dinesh Lodhi)", "+91 9755033445", "गोपालपुर (Gopalpur)", "समय पर भुगतान")
        val c4 = CustomerEntity("cust_4", "राजेश शर्मा (Rajesh Sharma)", "+91 9893044556", "रामपुर (Rampur)", "गेहूं और धान")
        val c5 = CustomerEntity("cust_5", "अनिल यादव (Anil Yadav)", "+91 9179055667", "नयागांव (Nayagaon)", "लेज़र लेवलिंग")
        dao.insertCustomers(listOf(c1, c2, c3, c4, c5))

        // Time offsets
        val oneHour = 3600 * 1000L
        val oneDay = 24 * oneHour

        // Jobs
        val jobs = listOf(
            JobEntity(
                id = "job_1",
                customerId = c1.id,
                customerName = c1.name,
                vehicleId = "veh_swaraj_744",
                vehicleName = "Swaraj 744 XT",
                implementId = "imp_rotavator",
                implementName = "रोटावेटर (Rotavator)",
                dateMillis = now - 2 * oneHour,
                status = "COMPLETED",
                rateTypeSnapshot = "HOUR",
                rateSnapshot = 800.0,
                hours = 2,
                minutes = 30,
                extras = 100.0,
                discount = 0.0,
                total = 2100.0,
                paidAmount = 1500.0,
                paymentMethod = "UPI",
                notes = "उत्तर वाले 5 एकड़ खेत में गहरी जुताई"
            ),
            JobEntity(
                id = "job_2",
                customerId = c2.id,
                customerName = c2.name,
                vehicleId = "veh_swaraj_744",
                vehicleName = "Swaraj 744 XT",
                implementId = "imp_plough",
                implementName = "एमबी प्लाउ (MB Plough)",
                dateMillis = now - 6 * oneHour,
                status = "COMPLETED",
                rateTypeSnapshot = "HOUR",
                rateSnapshot = 1100.0,
                hours = 3,
                minutes = 0,
                extras = 0.0,
                discount = 0.0,
                total = 3300.0,
                paidAmount = 3300.0,
                paymentMethod = "CASH",
                notes = "नया खेत तैयार किया"
            ),
            JobEntity(
                id = "job_3",
                customerId = c3.id,
                customerName = c3.name,
                vehicleId = "veh_swaraj_744",
                vehicleName = "Swaraj 744 XT",
                implementId = "imp_cultivator",
                implementName = "कल्टीवेटर (Cultivator)",
                dateMillis = now - 1 * oneDay,
                status = "COMPLETED",
                rateTypeSnapshot = "HOUR",
                rateSnapshot = 700.0,
                hours = 1,
                minutes = 45,
                extras = 0.0,
                discount = 25.0,
                total = 1200.0,
                paidAmount = 0.0,
                paymentMethod = "NONE",
                notes = "कल पैसे देंगे बोले"
            ),
            JobEntity(
                id = "job_4",
                customerId = c4.id,
                customerName = c4.name,
                vehicleId = "veh_swaraj_744",
                vehicleName = "Swaraj 744 XT",
                implementId = "imp_laser",
                implementName = "लेज़र लेवलर (Laser Leveller)",
                dateMillis = now - 3 * oneDay,
                status = "COMPLETED",
                rateTypeSnapshot = "HOUR",
                rateSnapshot = 1200.0,
                hours = 4,
                minutes = 0,
                extras = 0.0,
                discount = 0.0,
                total = 4800.0,
                paidAmount = 4800.0,
                paymentMethod = "BANK",
                notes = "समतलीकरण का काम"
            ),
            JobEntity(
                id = "job_5",
                customerId = c5.id,
                customerName = c5.name,
                vehicleId = "veh_mahindra_575",
                vehicleName = "Mahindra 575 DI",
                implementId = "imp_trolley",
                implementName = "ट्राली / ढुलाई (Trolley)",
                dateMillis = now - 5 * oneDay,
                status = "COMPLETED",
                rateTypeSnapshot = "TRIP",
                rateSnapshot = 600.0,
                quantity = 3.0,
                extras = 0.0,
                discount = 0.0,
                total = 1800.0,
                paidAmount = 1000.0,
                paymentMethod = "CASH",
                notes = "मंडी तक धान की ढुलाई"
            ),
            JobEntity(
                id = "job_6",
                customerId = c1.id,
                customerName = c1.name,
                vehicleId = "veh_swaraj_744",
                vehicleName = "Swaraj 744 XT",
                implementId = "imp_thresher",
                implementName = "थ्रेशर (Thresher)",
                dateMillis = now - 10 * oneDay,
                status = "COMPLETED",
                rateTypeSnapshot = "HOUR",
                rateSnapshot = 950.0,
                hours = 5,
                minutes = 0,
                extras = 0.0,
                discount = 0.0,
                total = 4750.0,
                paidAmount = 3000.0,
                paymentMethod = "CASH",
                notes = "चना थ्रेशिंग"
            ),
            JobEntity(
                id = "job_7",
                customerId = c2.id,
                customerName = c2.name,
                vehicleId = "veh_swaraj_744",
                vehicleName = "Swaraj 744 XT",
                implementId = "imp_reaper",
                implementName = "भूसा रीपर (Reaper)",
                dateMillis = now - 18 * oneDay,
                status = "COMPLETED",
                rateTypeSnapshot = "ACRE",
                rateSnapshot = 1300.0,
                area = 6.0,
                extras = 200.0,
                discount = 0.0,
                total = 8000.0,
                paidAmount = 8000.0,
                paymentMethod = "UPI",
                notes = "गेहूं का भूसा बनाया"
            )
        )
        dao.insertJobs(jobs)

        // Expenses
        val expenses = listOf(
            ExpenseEntity(
                id = "exp_1",
                vehicleId = "veh_swaraj_744",
                vehicleName = "Swaraj 744 XT",
                category = "FUEL",
                amount = 3680.0,
                dateMillis = now - 1 * oneDay,
                litres = 40.0,
                notes = "इंडियन ऑयल पंप, रेट ₹92/ली"
            ),
            ExpenseEntity(
                id = "exp_2",
                vehicleId = "veh_swaraj_744",
                vehicleName = "Swaraj 744 XT",
                category = "FUEL",
                amount = 4600.0,
                dateMillis = now - 7 * oneDay,
                litres = 50.0,
                notes = "टंकी फुल कराई"
            ),
            ExpenseEntity(
                id = "exp_3",
                vehicleId = "veh_swaraj_744",
                vehicleName = "Swaraj 744 XT",
                category = "MAINTENANCE",
                amount = 2800.0,
                dateMillis = now - 12 * oneDay,
                litres = 0.0,
                notes = "इंजन ऑयल (7.5L) + डीजल फिल्टर बदला"
            ),
            ExpenseEntity(
                id = "exp_4",
                vehicleId = "veh_swaraj_744",
                vehicleName = "Swaraj 744 XT",
                category = "MAINTENANCE",
                amount = 850.0,
                dateMillis = now - 4 * oneDay,
                litres = 0.0,
                notes = "रोटावेटर ब्लेड धार व ग्रीसिंग"
            ),
            ExpenseEntity(
                id = "exp_5",
                vehicleId = "veh_mahindra_575",
                vehicleName = "Mahindra 575 DI",
                category = "FUEL",
                amount = 2760.0,
                dateMillis = now - 5 * oneDay,
                litres = 30.0,
                notes = "ट्राली काम के लिए डीजल"
            )
        )
        dao.insertExpenses(expenses)
    }
}
