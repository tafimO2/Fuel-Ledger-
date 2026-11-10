package com.example.ui.localization

import com.example.data.model.CustomerType
import com.example.data.model.FuelType
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionType
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppStrings {
    // Number formatting with Bengali digit conversion
    fun formatCurrency(amount: Double, language: String = "bn"): String {
        val df = DecimalFormat("#,##,##0.00")
        val formatted = df.format(amount)
        val withSymbol = "৳ $formatted"
        return if (language == "bn") toBengaliDigits(withSymbol) else withSymbol
    }

    fun formatNumber(number: Double, language: String = "bn", decimals: Int = 2): String {
        val pattern = if (decimals == 0) "#,##,##0" else "#,##,##0." + "0".repeat(decimals)
        val formatted = DecimalFormat(pattern).format(number)
        return if (language == "bn") toBengaliDigits(formatted) else formatted
    }

    fun toBengaliDigits(input: String): String {
        val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bengaliDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatDate(timestamp: Long, language: String = "bn", includeTime: Boolean = true): String {
        val pattern = if (includeTime) "dd MMM yyyy, hh:mm a" else "dd MMM yyyy"
        val sdf = SimpleDateFormat(pattern, Locale.US)
        val formatted = sdf.format(Date(timestamp))
        return if (language == "bn") toBengaliDigits(formatted) else formatted
    }

    fun getFuelName(fuelType: FuelType, language: String = "bn"): String {
        return when (fuelType) {
            FuelType.PETROL -> if (language == "bn") "পেট্রোল" else "Petrol"
            FuelType.OCTANE -> if (language == "bn") "অকটেন" else "Octane"
            FuelType.DIESEL -> if (language == "bn") "ডিজেল" else "Diesel"
        }
    }

    fun getTransactionTypeName(type: TransactionType, language: String = "bn"): String {
        return when (type) {
            TransactionType.CREDIT_SALE -> if (language == "bn") "বাকি বিক্রি" else "Credit Sale"
            TransactionType.CASH_SALE -> if (language == "bn") "নগদ বিক্রি" else "Cash Sale"
            TransactionType.PAYMENT -> if (language == "bn") "জমা গ্রহণ" else "Payment Received"
        }
    }

    fun getCustomerTypeName(type: CustomerType, language: String = "bn"): String {
        return when (type) {
            CustomerType.INDIVIDUAL -> if (language == "bn") "ব্যক্তিগত" else "Individual"
            CustomerType.BUSINESS -> if (language == "bn") "ব্যবসা / ফ্লিট" else "Business / Fleet"
            CustomerType.ORGANIZATION -> if (language == "bn") "প্রতিষ্ঠান / সংস্থা" else "Organization"
        }
    }

    fun getPaymentMethodName(method: PaymentMethod?, language: String = "bn"): String {
        if (method == null) return if (language == "bn") "ক্যাশ" else "Cash"
        return when (method) {
            PaymentMethod.CASH -> if (language == "bn") "ক্যাশ / নগদ" else "Cash"
            PaymentMethod.BKASH -> if (language == "bn") "বিকাশ (bKash)" else "bKash"
            PaymentMethod.NAGAD -> if (language == "bn") "নগদ (Nagad)" else "Nagad"
            PaymentMethod.BANK -> if (language == "bn") "ব্যাংক একাউন্ট" else "Bank Account"
            PaymentMethod.OTHER -> if (language == "bn") "অন্যান্য" else "Other"
        }
    }

    // Common UI Titles and Action Labels
    fun appTitle(language: String) = if (language == "bn") "ফুয়েল লেজার বিডি" else "Fuel Ledger BD"
    fun navHome(language: String) = if (language == "bn") "ড্যাশবোর্ড" else "Dashboard"
    fun navCustomers(language: String) = if (language == "bn") "গ্রাহক" else "Customers"
    fun navTransactions(language: String) = if (language == "bn") "লেনদেন" else "Transactions"
    fun navReports(language: String) = if (language == "bn") "রিপোর্ট" else "Reports"
    fun navSettings(language: String) = if (language == "bn") "সেটিংস" else "Settings"

    fun todayCreditSales(language: String) = if (language == "bn") "আজকের বাকি বিক্রি" else "Today's Credit Sales"
    fun todayCashSales(language: String) = if (language == "bn") "আজকের নগদ বিক্রি" else "Today's Cash Sales"
    fun todayCollection(language: String) = if (language == "bn") "আজকের মোট জমা" else "Today's Collection"
    fun totalOutstanding(language: String) = if (language == "bn") "মোট বকেয়া বাকি" else "Total Outstanding Dues"
    fun totalCustomers(language: String) = if (language == "bn") "মোট গ্রাহক সংখ্যা" else "Total Customers"
    fun todayTxCount(language: String) = if (language == "bn") "আজকের লেনদেন" else "Today's Tx Count"

    fun newCreditSale(language: String) = if (language == "bn") "+ বাকি বিক্রি" else "+ Credit Sale"
    fun receivePayment(language: String) = if (language == "bn") "+ জমা গ্রহণ" else "+ Receive Payment"
    fun addCustomer(language: String) = if (language == "bn") "+ নতুন গ্রাহক" else "+ Add Customer"
    fun viewAll(language: String) = if (language == "bn") "সব দেখুন" else "View All"
    fun customerLedger(language: String) = if (language == "bn") "গ্রাহকের খতিয়ান" else "Customer Ledger"
    fun outstandingBalance(language: String) = if (language == "bn") "বর্তমান বকেয়া" else "Current Due"
    fun advanceBalance(language: String) = if (language == "bn") "অগ্রিম জমা" else "Advance Balance"
    fun clearDues(language: String) = if (language == "bn") "পরিশোধিত" else "Cleared"
}
