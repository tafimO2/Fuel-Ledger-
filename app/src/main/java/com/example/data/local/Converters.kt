package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.CustomerType
import com.example.data.model.FuelType
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromCustomerType(value: CustomerType): String = value.name

    @TypeConverter
    fun toCustomerType(value: String): CustomerType = try {
        CustomerType.valueOf(value)
    } catch (_: Exception) {
        CustomerType.INDIVIDUAL
    }

    @TypeConverter
    fun fromFuelType(value: FuelType): String = value.name

    @TypeConverter
    fun toFuelType(value: String): FuelType = try {
        FuelType.valueOf(value)
    } catch (_: Exception) {
        FuelType.PETROL
    }

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = try {
        TransactionType.valueOf(value)
    } catch (_: Exception) {
        TransactionType.CREDIT_SALE
    }

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod?): String? = value?.name

    @TypeConverter
    fun toPaymentMethod(value: String?): PaymentMethod? = value?.let {
        try {
            PaymentMethod.valueOf(it)
        } catch (_: Exception) {
            null
        }
    }
}
