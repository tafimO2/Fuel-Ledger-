package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE isDeleted = 0 ORDER BY name COLLATE NOCASE ASC")
    fun getAllActiveCustomersFlow(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE ASC")
    fun getAllCustomersIncludingDeletedFlow(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerByIdFlow(id: Long): Flow<Customer?>

    @Query("""
        SELECT * FROM customers 
        WHERE isDeleted = 0 AND (
            name LIKE '%' || :query || '%' OR 
            phone LIKE '%' || :query || '%' OR 
            vehicleReg LIKE '%' || :query || '%'
        )
        ORDER BY name COLLATE NOCASE ASC
    """)
    fun searchCustomersFlow(query: String): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("UPDATE customers SET isDeleted = 1 WHERE id = :customerId")
    suspend fun softDeleteCustomer(customerId: Long)

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<Customer>)
}
