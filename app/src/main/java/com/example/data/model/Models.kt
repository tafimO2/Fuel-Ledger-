package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CustomerType {
    INDIVIDUAL,
    BUSINESS,
    ORGANIZATION
}

enum class FuelType(val code: String) {
    PETROL("PETROL"),
    OCTANE("OCTANE"),
    DIESEL("DIESEL")
}

enum class TransactionType {
    CREDIT_SALE, // বাকি বিক্রি
    CASH_SALE,   // নগদ বিক্রি
    PAYMENT      // জমা গ্রহণ
}

enum class PaymentMethod {
    CASH,
    BKASH,
    NAGAD,
    BANK,
    OTHER
}

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String = "",
    val vehicleReg: String = "",
    val customerType: CustomerType = CustomerType.INDIVIDUAL,
    val openingBalance: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["timestamp"]),
        Index(value = ["receiptNumber"], unique = true)
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptNumber: String,
    val customerId: Long?,
    val type: TransactionType,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod? = null,
    val reference: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["transactionId"])
    ]
)
data class TransactionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val fuelType: FuelType,
    val quantityLiters: Double,
    val pricePerLiter: Double,
    val totalAmount: Double
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val action: String, // CREATE, UPDATE, DELETE
    val entityType: String, // TRANSACTION, CUSTOMER, PAYMENT
    val entityId: Long,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class TransactionWithDetails(
    val transaction: Transaction,
    val customerName: String?,
    val customerPhone: String?,
    val items: List<TransactionItem> = emptyList()
)

data class CustomerLedgerEntry(
    val id: Long,
    val transactionId: Long,
    val timestamp: Long,
    val receiptNumber: String,
    val type: TransactionType,
    val description: String,
    val fuelDetails: String = "",
    val creditAmount: Double,  // বাকি বিক্রি (+ to balance)
    val paymentAmount: Double, // জমা (- from balance)
    val runningBalance: Double,
    val paymentMethod: PaymentMethod? = null,
    val reference: String = "",
    val notes: String = ""
)

data class CustomerSummary(
    val customer: Customer,
    val totalCredit: Double,
    val totalPayment: Double,
    val currentBalance: Double,
    val lastTransactionTime: Long?
)
