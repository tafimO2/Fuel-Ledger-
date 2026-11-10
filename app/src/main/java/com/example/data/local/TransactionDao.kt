package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction as RoomTransaction
import androidx.room.Update
import com.example.data.model.Transaction
import com.example.data.model.TransactionItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllActiveTransactionsFlow(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): Transaction?

    @Query("SELECT * FROM transactions WHERE receiptNumber = :receiptNumber LIMIT 1")
    suspend fun getTransactionByReceiptNumber(receiptNumber: String): Transaction?

    @Query("SELECT * FROM transactions WHERE customerId = :customerId AND isDeleted = 0 ORDER BY timestamp ASC, id ASC")
    fun getActiveTransactionsForCustomerFlow(customerId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId AND isDeleted = 0 ORDER BY timestamp ASC, id ASC")
    suspend fun getActiveTransactionsForCustomer(customerId: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime AND isDeleted = 0 ORDER BY timestamp DESC")
    fun getTransactionsBetweenFlow(startTime: Long, endTime: Long): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("UPDATE transactions SET isDeleted = 1 WHERE id = :transactionId")
    suspend fun softDeleteTransaction(transactionId: Long)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun hardDeleteTransaction(transactionId: Long)

    // Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<TransactionItem>)

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun getItemsForTransaction(transactionId: Long): List<TransactionItem>

    @Query("SELECT * FROM transaction_items WHERE transactionId IN (:transactionIds)")
    suspend fun getItemsForTransactions(transactionIds: List<Long>): List<TransactionItem>

    @Query("SELECT * FROM transaction_items")
    fun getAllItemsFlow(): Flow<List<TransactionItem>>

    @Query("DELETE FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun deleteItemsForTransaction(transactionId: Long)

    @Query("DELETE FROM transaction_items")
    suspend fun deleteAllItems()

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @RoomTransaction
    suspend fun saveTransactionWithItems(
        transaction: Transaction,
        items: List<TransactionItem>
    ): Long {
        val txId = insertTransaction(transaction)
        if (items.isNotEmpty()) {
            val boundItems = items.map { it.copy(transactionId = txId) }
            insertItems(boundItems)
        }
        return txId
    }

    @RoomTransaction
    suspend fun updateTransactionWithItems(
        transaction: Transaction,
        items: List<TransactionItem>
    ) {
        updateTransaction(transaction)
        deleteItemsForTransaction(transaction.id)
        if (items.isNotEmpty()) {
            val boundItems = items.map { it.copy(transactionId = transaction.id) }
            insertItems(boundItems)
        }
    }
}
