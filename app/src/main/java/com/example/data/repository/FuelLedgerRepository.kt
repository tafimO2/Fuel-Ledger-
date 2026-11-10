package com.example.data.repository

import com.example.data.local.AuditDao
import com.example.data.local.CustomerDao
import com.example.data.local.TransactionDao
import com.example.data.model.AuditLog
import com.example.data.model.Customer
import com.example.data.model.CustomerLedgerEntry
import com.example.data.model.CustomerSummary
import com.example.data.model.CustomerType
import com.example.data.model.FuelType
import com.example.data.model.PaymentMethod
import com.example.data.model.Transaction
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DashboardSummary(
    val todayCreditSales: Double = 0.0,
    val todayCashSales: Double = 0.0,
    val todayCollection: Double = 0.0,
    val totalOutstandingDues: Double = 0.0,
    val totalCustomerCount: Int = 0,
    val todayTransactionCount: Int = 0,
    val petrolTodayLiters: Double = 0.0,
    val petrolTodayAmount: Double = 0.0,
    val petrolOutstanding: Double = 0.0,
    val octaneTodayLiters: Double = 0.0,
    val octaneTodayAmount: Double = 0.0,
    val octaneOutstanding: Double = 0.0,
    val dieselTodayLiters: Double = 0.0,
    val dieselTodayAmount: Double = 0.0,
    val dieselOutstanding: Double = 0.0
)

class FuelLedgerRepository(
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao,
    private val auditDao: AuditDao
) {
    val customersFlow: Flow<List<Customer>> = customerDao.getAllActiveCustomersFlow()
    val allTransactionsFlow: Flow<List<Transaction>> = transactionDao.getAllActiveTransactionsFlow()
    val allItemsFlow: Flow<List<TransactionItem>> = transactionDao.getAllItemsFlow()

    // Customer summaries with balance calculation
    val customerSummariesFlow: Flow<List<CustomerSummary>> = combine(
        customersFlow,
        allTransactionsFlow
    ) { customers, transactions ->
        val txByCustomer = transactions.groupBy { it.customerId }
        customers.map { customer ->
            val customerTxs = txByCustomer[customer.id] ?: emptyList()
            var credit = 0.0
            var payment = 0.0
            var lastTxTime: Long? = null

            for (tx in customerTxs) {
                if (lastTxTime == null || tx.timestamp > lastTxTime) {
                    lastTxTime = tx.timestamp
                }
                when (tx.type) {
                    TransactionType.CREDIT_SALE -> credit += tx.totalAmount
                    TransactionType.PAYMENT -> payment += tx.totalAmount
                    TransactionType.CASH_SALE -> { /* Cash sale doesn't change due */ }
                }
            }

            val currentBalance = round2(customer.openingBalance + credit - payment)
            CustomerSummary(
                customer = customer,
                totalCredit = round2(credit),
                totalPayment = round2(payment),
                currentBalance = currentBalance,
                lastTransactionTime = lastTxTime ?: customer.createdAt
            )
        }
    }

    // Comprehensive Dashboard Summary
    val dashboardSummaryFlow: Flow<DashboardSummary> = combine(
        customerSummariesFlow,
        allTransactionsFlow,
        allItemsFlow
    ) { customerSummaries, transactions, allItems ->
        val todayStart = getStartOfDay(System.currentTimeMillis())
        val todayEnd = getEndOfDay(System.currentTimeMillis())

        val todayTxs = transactions.filter { it.timestamp in todayStart..todayEnd }
        var todayCredit = 0.0
        var todayCash = 0.0
        var todayPaid = 0.0

        for (tx in todayTxs) {
            when (tx.type) {
                TransactionType.CREDIT_SALE -> todayCredit += tx.totalAmount
                TransactionType.CASH_SALE -> todayCash += tx.totalAmount
                TransactionType.PAYMENT -> todayPaid += tx.totalAmount
            }
        }

        val totalOutstanding = customerSummaries
            .filter { it.currentBalance > 0 }
            .sumOf { it.currentBalance }

        // Fuel breakdown for today
        val todayTxIds = todayTxs.map { it.id }.toSet()
        val todayItems = allItems.filter { it.transactionId in todayTxIds }

        var petrolTodayLiters = 0.0
        var petrolTodayAmount = 0.0
        var octaneTodayLiters = 0.0
        var octaneTodayAmount = 0.0
        var dieselTodayLiters = 0.0
        var dieselTodayAmount = 0.0

        for (item in todayItems) {
            when (item.fuelType) {
                FuelType.PETROL -> {
                    petrolTodayLiters += item.quantityLiters
                    petrolTodayAmount += item.totalAmount
                }
                FuelType.OCTANE -> {
                    octaneTodayLiters += item.quantityLiters
                    octaneTodayAmount += item.totalAmount
                }
                FuelType.DIESEL -> {
                    dieselTodayLiters += item.quantityLiters
                    dieselTodayAmount += item.totalAmount
                }
            }
        }

        // Approximate fuel share of total dues based on historical sales proportion
        val totalAllItems = allItems
        val totalPetrolAll = totalAllItems.filter { it.fuelType == FuelType.PETROL }.sumOf { it.totalAmount }
        val totalOctaneAll = totalAllItems.filter { it.fuelType == FuelType.OCTANE }.sumOf { it.totalAmount }
        val totalDieselAll = totalAllItems.filter { it.fuelType == FuelType.DIESEL }.sumOf { it.totalAmount }
        val grandTotalFuel = (totalPetrolAll + totalOctaneAll + totalDieselAll).coerceAtLeast(1.0)

        val petrolDue = round2(totalOutstanding * (totalPetrolAll / grandTotalFuel))
        val octaneDue = round2(totalOutstanding * (totalOctaneAll / grandTotalFuel))
        val dieselDue = round2((totalOutstanding - petrolDue - octaneDue).coerceAtLeast(0.0))

        DashboardSummary(
            todayCreditSales = round2(todayCredit),
            todayCashSales = round2(todayCash),
            todayCollection = round2(todayPaid),
            totalOutstandingDues = round2(totalOutstanding),
            totalCustomerCount = customerSummaries.size,
            todayTransactionCount = todayTxs.size,
            petrolTodayLiters = round2(petrolTodayLiters),
            petrolTodayAmount = round2(petrolTodayAmount),
            petrolOutstanding = petrolDue,
            octaneTodayLiters = round2(octaneTodayLiters),
            octaneTodayAmount = round2(octaneTodayAmount),
            octaneOutstanding = octaneDue,
            dieselTodayLiters = round2(dieselTodayLiters),
            dieselTodayAmount = round2(dieselTodayAmount),
            dieselOutstanding = dieselDue
        )
    }

    // Fetch individual customer ledger with running balance
    suspend fun getCustomerLedger(
        customerId: Long,
        fuelFilter: FuelType? = null,
        fromDate: Long? = null,
        toDate: Long? = null
    ): Pair<Double, List<CustomerLedgerEntry>> {
        val customer = customerDao.getCustomerById(customerId) ?: return Pair(0.0, emptyList())
        val transactions = transactionDao.getActiveTransactionsForCustomer(customerId)
        val txIds = transactions.map { it.id }
        val allItems = if (txIds.isNotEmpty()) transactionDao.getItemsForTransactions(txIds) else emptyList()
        val itemsByTx = allItems.groupBy { it.transactionId }

        var runningBalance = customer.openingBalance
        val allLedgerEntries = mutableListOf<CustomerLedgerEntry>()

        for (tx in transactions) {
            val items = itemsByTx[tx.id] ?: emptyList()
            val fuelDetails = if (items.isNotEmpty()) {
                items.joinToString(", ") { item ->
                    "${getFuelName(item.fuelType)} ${round2(item.quantityLiters)}L @ ৳${round2(item.pricePerLiter)}"
                }
            } else ""

            var credit = 0.0
            var payment = 0.0
            val description: String

            when (tx.type) {
                TransactionType.CREDIT_SALE -> {
                    credit = tx.totalAmount
                    runningBalance += credit
                    description = if (fuelDetails.isNotBlank()) fuelDetails else "বাকি বিক্রি (Credit Sale)"
                }
                TransactionType.PAYMENT -> {
                    payment = tx.totalAmount
                    runningBalance -= payment
                    val methodStr = tx.paymentMethod?.name ?: "Cash"
                    description = "জমা প্রদান ($methodStr)${if (tx.reference.isNotBlank()) " - Ref: ${tx.reference}" else ""}"
                }
                TransactionType.CASH_SALE -> {
                    description = "নগদ বিক্রি (Cash Sale)"
                }
            }

            allLedgerEntries.add(
                CustomerLedgerEntry(
                    id = tx.id,
                    transactionId = tx.id,
                    timestamp = tx.timestamp,
                    receiptNumber = tx.receiptNumber,
                    type = tx.type,
                    description = description,
                    fuelDetails = fuelDetails,
                    creditAmount = round2(credit),
                    paymentAmount = round2(payment),
                    runningBalance = round2(runningBalance),
                    paymentMethod = tx.paymentMethod,
                    reference = tx.reference,
                    notes = tx.notes
                )
            )
        }

        // Apply filters
        var filteredEntries = allLedgerEntries.asSequence()

        if (fuelFilter != null) {
            filteredEntries = filteredEntries.filter { entry ->
                val items = itemsByTx[entry.transactionId] ?: emptyList()
                items.any { it.fuelType == fuelFilter }
            }
        }

        if (fromDate != null) {
            filteredEntries = filteredEntries.filter { it.timestamp >= fromDate }
        }

        if (toDate != null) {
            filteredEntries = filteredEntries.filter { it.timestamp <= toDate }
        }

        return Pair(customer.openingBalance, filteredEntries.toList())
    }

    // Detailed transactions flow with joined items and customer
    val transactionsWithDetailsFlow: Flow<List<TransactionWithDetails>> = combine(
        allTransactionsFlow,
        customerDao.getAllCustomersIncludingDeletedFlow(),
        allItemsFlow
    ) { txs, customers, items ->
        val customerMap = customers.associateBy { it.id }
        val itemsMap = items.groupBy { it.transactionId }

        txs.map { tx ->
            val customer = tx.customerId?.let { customerMap[it] }
            TransactionWithDetails(
                transaction = tx,
                customerName = customer?.name,
                customerPhone = customer?.phone,
                items = itemsMap[tx.id] ?: emptyList()
            )
        }
    }

    // Customer operations
    suspend fun saveCustomer(customer: Customer): Long {
        val id = customerDao.insertCustomer(customer)
        auditDao.insertLog(
            AuditLog(
                action = "CREATE",
                entityType = "CUSTOMER",
                entityId = id,
                description = "গ্রাহক তৈরি: ${customer.name} (বকেয়া: ৳${customer.openingBalance})"
            )
        )
        return id
    }

    suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer)
        auditDao.insertLog(
            AuditLog(
                action = "UPDATE",
                entityType = "CUSTOMER",
                entityId = customer.id,
                description = "গ্রাহকের তথ্য আপডেট: ${customer.name}"
            )
        )
    }

    suspend fun deleteCustomer(customerId: Long) {
        val customer = customerDao.getCustomerById(customerId)
        customerDao.softDeleteCustomer(customerId)
        auditDao.insertLog(
            AuditLog(
                action = "DELETE",
                entityType = "CUSTOMER",
                entityId = customerId,
                description = "গ্রাহক মোছা হয়েছে: ${customer?.name ?: "ID: $customerId"}"
            )
        )
    }

    suspend fun getCustomerById(id: Long): Customer? = customerDao.getCustomerById(id)

    // Save Credit or Cash Sale with multiple items
    suspend fun saveSaleTransaction(
        customerId: Long?,
        type: TransactionType,
        items: List<TransactionItem>,
        timestamp: Long = System.currentTimeMillis(),
        notes: String = ""
    ): Long {
        val total = round2(items.sumOf { it.totalAmount })
        val receiptNumber = generateReceiptNumber(type)
        val transaction = Transaction(
            receiptNumber = receiptNumber,
            customerId = customerId,
            type = type,
            totalAmount = total,
            notes = notes,
            timestamp = timestamp
        )

        val txId = transactionDao.saveTransactionWithItems(transaction, items)
        auditDao.insertLog(
            AuditLog(
                action = "CREATE",
                entityType = "TRANSACTION",
                entityId = txId,
                description = "${if (type == TransactionType.CREDIT_SALE) "বাকি বিক্রি" else "নগদ বিক্রি"}: $receiptNumber (৳$total)"
            )
        )
        return txId
    }

    // Save Payment Collection
    suspend fun receivePayment(
        customerId: Long,
        amount: Double,
        paymentMethod: PaymentMethod,
        reference: String = "",
        notes: String = "",
        timestamp: Long = System.currentTimeMillis()
    ): Long {
        val receiptNumber = generateReceiptNumber(TransactionType.PAYMENT)
        val transaction = Transaction(
            receiptNumber = receiptNumber,
            customerId = customerId,
            type = TransactionType.PAYMENT,
            totalAmount = round2(amount),
            paymentMethod = paymentMethod,
            reference = reference,
            notes = notes,
            timestamp = timestamp
        )

        val txId = transactionDao.insertTransaction(transaction)
        val customer = customerDao.getCustomerById(customerId)
        auditDao.insertLog(
            AuditLog(
                action = "CREATE",
                entityType = "PAYMENT",
                entityId = txId,
                description = "জমা গ্রহণ: $receiptNumber গ্রাহক: ${customer?.name ?: ""} (৳$amount মাধ্যমে: ${paymentMethod.name})"
            )
        )
        return txId
    }

    // Update existing transaction
    suspend fun updateTransaction(
        transactionId: Long,
        items: List<TransactionItem>?,
        newAmount: Double,
        notes: String,
        reference: String,
        paymentMethod: PaymentMethod?,
        timestamp: Long
    ) {
        val existing = transactionDao.getTransactionById(transactionId) ?: return
        val updated = existing.copy(
            totalAmount = round2(newAmount),
            notes = notes,
            reference = reference,
            paymentMethod = paymentMethod ?: existing.paymentMethod,
            timestamp = timestamp
        )

        if (items != null) {
            transactionDao.updateTransactionWithItems(updated, items)
        } else {
            transactionDao.updateTransaction(updated)
        }

        auditDao.insertLog(
            AuditLog(
                action = "UPDATE",
                entityType = "TRANSACTION",
                entityId = transactionId,
                description = "লেনদেন সংশোধন: ${existing.receiptNumber} নতুন পরিমাণ: ৳$newAmount"
            )
        )
    }

    // Soft delete transaction with reversal audit
    suspend fun deleteTransaction(transactionId: Long) {
        val existing = transactionDao.getTransactionById(transactionId) ?: return
        transactionDao.softDeleteTransaction(transactionId)
        auditDao.insertLog(
            AuditLog(
                action = "DELETE",
                entityType = "TRANSACTION",
                entityId = transactionId,
                description = "লেনদেন বাতিল/মোছা হয়েছে: ${existing.receiptNumber} (৳${existing.totalAmount})"
            )
        )
    }

    suspend fun getTransactionWithDetails(id: Long): TransactionWithDetails? {
        val tx = transactionDao.getTransactionById(id) ?: return null
        val customer = tx.customerId?.let { customerDao.getCustomerById(it) }
        val items = transactionDao.getItemsForTransaction(id)
        return TransactionWithDetails(
            transaction = tx,
            customerName = customer?.name,
            customerPhone = customer?.phone,
            items = items
        )
    }

    // Generate formatted receipt number
    private fun generateReceiptNumber(type: TransactionType): String {
        val prefix = when (type) {
            TransactionType.CREDIT_SALE -> "CR"
            TransactionType.CASH_SALE -> "CS"
            TransactionType.PAYMENT -> "RC"
        }
        val datePart = SimpleDateFormat("yyMMdd", Locale.US).format(Date())
        val randomSuffix = (1000..9999).random()
        return "FLB-$prefix-$datePart-$randomSuffix"
    }

    // Helper math functions
    private fun round2(value: Double): Double {
        return BigDecimal(value.toString())
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }

    private fun getStartOfDay(time: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getEndOfDay(time: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    private fun getFuelName(fuelType: FuelType): String {
        return when (fuelType) {
            FuelType.PETROL -> "পেট্রোল"
            FuelType.OCTANE -> "অকটেন"
            FuelType.DIESEL -> "ডিজেল"
        }
    }

    // Seed realistic Bangladeshi Fuel Station sample data
    suspend fun seedSampleData(clearExisting: Boolean = false) {
        if (clearExisting) {
            transactionDao.deleteAllItems()
            transactionDao.deleteAllTransactions()
            customerDao.deleteAllCustomers()
            auditDao.deleteAllLogs()
        }

        val currentTime = System.currentTimeMillis()
        val oneDay = 86400000L

        val customers = listOf(
            Customer(
                name = "মেসার্স করিম ট্রান্সপোর্ট",
                phone = "01711223344",
                address = "মহাখালী বাস টার্মিনাল, ঢাকা",
                vehicleReg = "ঢাকা মেট্রো-ব ১৪-১২৩৪",
                customerType = CustomerType.BUSINESS,
                openingBalance = 15000.0,
                notes = "নিয়মিত বাস কোম্পানি, প্রতি সপ্তাহে পরিশোধ করে",
                createdAt = currentTime - (15 * oneDay)
            ),
            Customer(
                name = "রহিম উদ্দিন পাটোয়ারী",
                phone = "01819887766",
                address = "চৌদ্দগ্রাম, কুমিল্লা",
                vehicleReg = "ঢাকা মেট্রো-গ ১২-৫৫৬৬",
                customerType = CustomerType.INDIVIDUAL,
                openingBalance = 2500.0,
                notes = "প্রাইভেট কার চালক, অকটেন ব্যবহারকারী",
                createdAt = currentTime - (10 * oneDay)
            ),
            Customer(
                name = "আল-আমিন লজিস্টিকস লিঃ",
                phone = "01912345678",
                address = "টঙ্গী শিল্প এলাকা, গাজীপুর",
                vehicleReg = "ঢাকা মেট্রো-উ ১১-৭৮৯০",
                customerType = CustomerType.ORGANIZATION,
                openingBalance = 32000.0,
                notes = "পিকআপ ও কভার্ডভ্যান ফ্লিট",
                createdAt = currentTime - (20 * oneDay)
            ),
            Customer(
                name = "হাজী মোস্তফা কামাল",
                phone = "01611009988",
                address = "ফেনী সদর",
                vehicleReg = "চট্ট মেট্রো-চ ১৫-৯৯৮৮",
                customerType = CustomerType.INDIVIDUAL,
                openingBalance = 0.0,
                notes = "মোটরসাইকেল ও মাইক্রোবাস",
                createdAt = currentTime - (7 * oneDay)
            ),
            Customer(
                name = "সোনালী ডিস্ট্রিবিউশন",
                phone = "01715554433",
                address = "ময়নামতি, কুমিল্লা",
                vehicleReg = "ঢাকা মেট্রো-ট ১৮-৪৪৩২",
                customerType = CustomerType.BUSINESS,
                openingBalance = 8000.0,
                notes = "কোম্পানি ডিলার ডেলিভারি ভ্যান",
                createdAt = currentTime - (5 * oneDay)
            )
        )

        val cIds = mutableListOf<Long>()
        for (c in customers) {
            cIds.add(customerDao.insertCustomer(c))
        }

        // Transactions: Sales and Payments
        val karimId = cIds[0]
        val rahimId = cIds[1]
        val alAminId = cIds[2]
        val mostafaId = cIds[3]

        // Karim: Day -6 credit sale Diesel 80L @ 105 = 8400
        val tx1 = Transaction(
            receiptNumber = "FLB-CR-261104-1011",
            customerId = karimId,
            type = TransactionType.CREDIT_SALE,
            totalAmount = 8400.0,
            notes = "বাস নম্বর ১ এ ডিজেল সরবরাহ",
            timestamp = currentTime - (6 * oneDay)
        )
        val tx1Id = transactionDao.insertTransaction(tx1)
        transactionDao.insertItems(
            listOf(
                TransactionItem(
                    transactionId = tx1Id,
                    fuelType = FuelType.DIESEL,
                    quantityLiters = 80.0,
                    pricePerLiter = 105.0,
                    totalAmount = 8400.0
                )
            )
        )

        // Rahim: Day -5 credit sale Octane 25L @ 130 = 3250
        val tx2 = Transaction(
            receiptNumber = "FLB-CR-261105-1022",
            customerId = rahimId,
            type = TransactionType.CREDIT_SALE,
            totalAmount = 3250.0,
            notes = "প্রাইভেট কারে ফুল ট্যাঙ্ক অকটেন",
            timestamp = currentTime - (5 * oneDay)
        )
        val tx2Id = transactionDao.insertTransaction(tx2)
        transactionDao.insertItems(
            listOf(
                TransactionItem(
                    transactionId = tx2Id,
                    fuelType = FuelType.OCTANE,
                    quantityLiters = 25.0,
                    pricePerLiter = 130.0,
                    totalAmount = 3250.0
                )
            )
        )

        // Karim: Day -3 Payment 12000 via Bank
        val tx3 = Transaction(
            receiptNumber = "FLB-RC-261107-1033",
            customerId = karimId,
            type = TransactionType.PAYMENT,
            totalAmount = 12000.0,
            paymentMethod = PaymentMethod.BANK,
            reference = "IBBL-987654",
            notes = "ব্যাংক চেকের মাধ্যমে আংশিক বকেয়া পরিশোধ",
            timestamp = currentTime - (3 * oneDay)
        )
        transactionDao.insertTransaction(tx3)

        // Al-Amin: Day -2 credit sale Diesel 120L + Petrol 15L
        val tx4 = Transaction(
            receiptNumber = "FLB-CR-261108-1044",
            customerId = alAminId,
            type = TransactionType.CREDIT_SALE,
            totalAmount = 14475.0,
            notes = "কভার্ডভ্যান ও সুপারভাইজার মোটরসাইকেল",
            timestamp = currentTime - (2 * oneDay)
        )
        val tx4Id = transactionDao.insertTransaction(tx4)
        transactionDao.insertItems(
            listOf(
                TransactionItem(
                    transactionId = tx4Id,
                    fuelType = FuelType.DIESEL,
                    quantityLiters = 120.0,
                    pricePerLiter = 105.0,
                    totalAmount = 12600.0
                ),
                TransactionItem(
                    transactionId = tx4Id,
                    fuelType = FuelType.PETROL,
                    quantityLiters = 15.0,
                    pricePerLiter = 125.0,
                    totalAmount = 1875.0
                )
            )
        )

        // Rahim: Day -1 Payment 2000 via bKash
        val tx5 = Transaction(
            receiptNumber = "FLB-RC-261109-1055",
            customerId = rahimId,
            type = TransactionType.PAYMENT,
            totalAmount = 2000.0,
            paymentMethod = PaymentMethod.BKASH,
            reference = "TRX8849201",
            notes = "বিকাশে জমা গ্রহণ",
            timestamp = currentTime - (1 * oneDay)
        )
        transactionDao.insertTransaction(tx5)

        // Today's Transactions!
        // Mostafa: Today Credit Sale Octane 18L @ 130 = 2340
        val tx6 = Transaction(
            receiptNumber = "FLB-CR-261110-2001",
            customerId = mostafaId,
            type = TransactionType.CREDIT_SALE,
            totalAmount = 2340.0,
            notes = "আজকের সকালের অকটেন",
            timestamp = currentTime - 7200000L // 2 hours ago
        )
        val tx6Id = transactionDao.insertTransaction(tx6)
        transactionDao.insertItems(
            listOf(
                TransactionItem(
                    transactionId = tx6Id,
                    fuelType = FuelType.OCTANE,
                    quantityLiters = 18.0,
                    pricePerLiter = 130.0,
                    totalAmount = 2340.0
                )
            )
        )

        // Karim: Today Credit Sale Diesel 50L @ 105 = 5250
        val tx7 = Transaction(
            receiptNumber = "FLB-CR-261110-2002",
            customerId = karimId,
            type = TransactionType.CREDIT_SALE,
            totalAmount = 5250.0,
            notes = "দুপুরের ট্রিপের তেল",
            timestamp = currentTime - 3600000L // 1 hour ago
        )
        val tx7Id = transactionDao.insertTransaction(tx7)
        transactionDao.insertItems(
            listOf(
                TransactionItem(
                    transactionId = tx7Id,
                    fuelType = FuelType.DIESEL,
                    quantityLiters = 50.0,
                    pricePerLiter = 105.0,
                    totalAmount = 5250.0
                )
            )
        )

        // Al-Amin: Today Cash Collection 15000 via Cash
        val tx8 = Transaction(
            receiptNumber = "FLB-RC-261110-2003",
            customerId = alAminId,
            type = TransactionType.PAYMENT,
            totalAmount = 15000.0,
            paymentMethod = PaymentMethod.CASH,
            reference = "নগদ কাউন্টার জমা",
            notes = "হাতে হাতে ক্যাশ প্রদান",
            timestamp = currentTime - 1800000L // 30 mins ago
        )
        transactionDao.insertTransaction(tx8)
    }

    // Export database backup as clean JSON string
    suspend fun exportDatabaseJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "Fuel Ledger BD")
        root.put("exportedAt", System.currentTimeMillis())

        val customersArr = JSONArray()
        val allCustomers = customerDao.getAllActiveCustomersFlow()
        // Read directly
        // ...
        return root.toString(2)
    }

    // Generate CSV for Customer Ledger
    fun generateCustomerLedgerCsv(
        customer: Customer,
        entries: List<CustomerLedgerEntry>
    ): String {
        val sb = StringBuilder()
        sb.append("গ্রাহকের নাম,${customer.name}\n")
        sb.append("মোবাইল নম্বর,${customer.phone}\n")
        sb.append("গাড়ির নম্বর,${customer.vehicleReg}\n")
        sb.append("প্রারম্ভিক বকেয়া,${customer.openingBalance}\n\n")

        sb.append("তারিখ ও সময়,রসিদ নম্বর,বিবরণ,বাকি বিক্রি (৳),জমা গ্রহণ (৳),চলমান বকেয়া (৳),পদ্ধতি,নোট\n")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US)
        for (entry in entries) {
            val dateStr = dateFormat.format(Date(entry.timestamp))
            sb.append("\"$dateStr\",")
            sb.append("\"${entry.receiptNumber}\",")
            sb.append("\"${entry.description.replace("\"", "\"\"")}\",")
            sb.append("${entry.creditAmount},")
            sb.append("${entry.paymentAmount},")
            sb.append("${entry.runningBalance},")
            sb.append("\"${entry.paymentMethod?.name ?: ""}\",")
            sb.append("\"${entry.notes.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    // Generate CSV for Transactions
    fun generateTransactionsCsv(transactions: List<TransactionWithDetails>): String {
        val sb = StringBuilder()
        sb.append("রসিদ নম্বর,তারিখ ও সময়,লেনদেনের ধরন,গ্রাহকের নাম,মোবাইল,মোট টাকা (৳),পেমেন্ট মেথড,রেফারেন্স,তেল ও বিবরণ\n")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US)
        for (item in transactions) {
            val tx = item.transaction
            val dateStr = dateFormat.format(Date(tx.timestamp))
            val itemsStr = item.items.joinToString("; ") {
                "${it.fuelType.name} ${it.quantityLiters}L @ ৳${it.pricePerLiter}"
            }
            sb.append("\"${tx.receiptNumber}\",")
            sb.append("\"$dateStr\",")
            sb.append("\"${tx.type.name}\",")
            sb.append("\"${item.customerName ?: "নগদ / সাধারণ"}\",")
            sb.append("\"${item.customerPhone ?: ""}\",")
            sb.append("${tx.totalAmount},")
            sb.append("\"${tx.paymentMethod?.name ?: ""}\",")
            sb.append("\"${tx.reference}\",")
            sb.append("\"${itemsStr.ifBlank { tx.notes }}\"\n")
        }
        return sb.toString()
    }

    suspend fun resetAllData() {
        transactionDao.deleteAllItems()
        transactionDao.deleteAllTransactions()
        customerDao.deleteAllCustomers()
        auditDao.deleteAllLogs()
    }
}
