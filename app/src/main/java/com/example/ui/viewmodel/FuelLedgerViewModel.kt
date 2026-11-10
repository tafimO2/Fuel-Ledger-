package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.CustomerLedgerEntry
import com.example.data.model.CustomerSummary
import com.example.data.model.CustomerType
import com.example.data.model.FuelType
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.preferences.StationSettings
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.DashboardSummary
import com.example.data.repository.FuelLedgerRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Customers : Screen("customers")
    data object CustomerDetail : Screen("customer_detail")
    data object Transactions : Screen("transactions")
    data object Reports : Screen("reports")
    data object Settings : Screen("settings")
}

data class SaleItemDraft(
    val fuelType: FuelType = FuelType.PETROL,
    val quantity: String = "20",
    val price: String = "125.0",
    val amount: Double = 2500.0
)

class FuelLedgerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = FuelLedgerRepository(
        database.customerDao(),
        database.transactionDao(),
        database.auditDao()
    )
    private val preferencesRepository = UserPreferencesRepository(application)

    // Navigation and active screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Navigation history for BackHandler
    private val screenStack = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        return if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeAt(screenStack.size - 1)
            true
        } else false
    }

    // App Preferences
    val settingsState: StateFlow<StationSettings> = preferencesRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StationSettings())

    // Dashboard State
    val dashboardSummary: StateFlow<DashboardSummary> = repository.dashboardSummaryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    // Customers List & Filter
    val customerSummaries: StateFlow<List<CustomerSummary>> = repository.customerSummariesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionWithDetails>> = repository.transactionsWithDetailsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Toast / Notification Events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Active Selected Customer for Detail & Ledger
    private val _selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedCustomerId: StateFlow<Long?> = _selectedCustomerId.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _customerLedger = MutableStateFlow<Pair<Double, List<CustomerLedgerEntry>>>(Pair(0.0, emptyList()))
    val customerLedger: StateFlow<Pair<Double, List<CustomerLedgerEntry>>> = _customerLedger.asStateFlow()

    // Ledger Filters
    private val _ledgerFuelFilter = MutableStateFlow<FuelType?>(null)
    val ledgerFuelFilter: StateFlow<FuelType?> = _ledgerFuelFilter.asStateFlow()

    // Printable Receipt State for Dialog preview
    private val _receiptToPrint = MutableStateFlow<TransactionWithDetails?>(null)
    val receiptToPrint: StateFlow<TransactionWithDetails?> = _receiptToPrint.asStateFlow()

    init {
        // Auto-seed initial realistic demo data if database is empty so app is instantly rich and operational
        viewModelScope.launch {
            val customers = database.customerDao().getCustomerById(1)
            if (customers == null) {
                repository.seedSampleData(clearExisting = false)
            }
        }
    }

    fun selectCustomerForDetail(customerId: Long) {
        _selectedCustomerId.value = customerId
        viewModelScope.launch {
            _selectedCustomer.value = repository.getCustomerById(customerId)
            refreshCustomerLedger()
            navigateTo(Screen.CustomerDetail)
        }
    }

    fun setLedgerFuelFilter(fuelType: FuelType?) {
        _ledgerFuelFilter.value = fuelType
        refreshCustomerLedger()
    }

    fun refreshCustomerLedger() {
        val cid = _selectedCustomerId.value ?: return
        viewModelScope.launch {
            _selectedCustomer.value = repository.getCustomerById(cid)
            _customerLedger.value = repository.getCustomerLedger(
                customerId = cid,
                fuelFilter = _ledgerFuelFilter.value
            )
        }
    }

    // Customer Form: Add / Edit
    fun saveCustomer(
        id: Long = 0,
        name: String,
        phone: String,
        address: String,
        vehicleReg: String,
        type: CustomerType,
        openingBalance: Double,
        notes: String
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                repository.saveCustomer(
                    Customer(
                        name = name.trim(),
                        phone = phone.trim(),
                        address = address.trim(),
                        vehicleReg = vehicleReg.trim(),
                        customerType = type,
                        openingBalance = openingBalance,
                        notes = notes.trim()
                    )
                )
                _userMessage.emit("নতুন গ্রাহক সফলভাবে যোগ করা হয়েছে")
            } else {
                val existing = repository.getCustomerById(id)
                if (existing != null) {
                    repository.updateCustomer(
                        existing.copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            address = address.trim(),
                            vehicleReg = vehicleReg.trim(),
                            customerType = type,
                            openingBalance = openingBalance,
                            notes = notes.trim()
                        )
                    )
                    _userMessage.emit("গ্রাহকের তথ্য আপডেট করা হয়েছে")
                    if (_selectedCustomerId.value == id) {
                        _selectedCustomer.value = repository.getCustomerById(id)
                    }
                }
            }
        }
    }

    fun deleteCustomer(customerId: Long) {
        viewModelScope.launch {
            repository.deleteCustomer(customerId)
            _userMessage.emit("গ্রাহক সফলভাবে মুছে ফেলা হয়েছে")
            if (_selectedCustomerId.value == customerId) {
                navigateBack()
            }
        }
    }

    // Save Credit / Cash Sale
    fun submitCreditSale(
        customerId: Long?,
        itemsDraft: List<SaleItemDraft>,
        timestamp: Long,
        notes: String,
        isCashSale: Boolean = false,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val items = itemsDraft.mapNotNull { draft ->
                val qty = draft.quantity.toDoubleOrNull() ?: return@mapNotNull null
                val price = draft.price.toDoubleOrNull() ?: return@mapNotNull null
                if (qty <= 0 || price <= 0) return@mapNotNull null
                TransactionItem(
                    transactionId = 0,
                    fuelType = draft.fuelType,
                    quantityLiters = qty,
                    pricePerLiter = price,
                    totalAmount = BigDecimal((qty * price).toString()).setScale(2, RoundingMode.HALF_UP).toDouble()
                )
            }

            if (items.isEmpty()) {
                _userMessage.emit("অনুগ্রহ করে কমপক্ষে একটি সঠিক জ্বালানি আইটেম যোগ করুন")
                return@launch
            }

            val type = if (isCashSale) TransactionType.CASH_SALE else TransactionType.CREDIT_SALE
            val txId = repository.saveSaleTransaction(
                customerId = customerId,
                type = type,
                items = items,
                timestamp = timestamp,
                notes = notes
            )

            _userMessage.emit(if (type == TransactionType.CREDIT_SALE) "বাকি বিক্রি সংরক্ষিত হয়েছে" else "নগদ বিক্রি সংরক্ষিত হয়েছে")
            refreshCustomerLedger()
            onSuccess(txId)
        }
    }

    // Receive Payment
    fun submitPayment(
        customerId: Long,
        amount: Double,
        method: PaymentMethod,
        reference: String,
        notes: String,
        timestamp: Long,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            if (amount <= 0) {
                _userMessage.emit("পরিশোধের পরিমাণ ০ এর বেশি হতে হবে")
                return@launch
            }

            val txId = repository.receivePayment(
                customerId = customerId,
                amount = amount,
                paymentMethod = method,
                reference = reference.trim(),
                notes = notes.trim(),
                timestamp = timestamp
            )

            _userMessage.emit("জমা গ্রহণ সফল হয়েছে")
            refreshCustomerLedger()
            onSuccess(txId)
        }
    }

    // Edit Transaction
    fun updateTransaction(
        txId: Long,
        itemsDraft: List<SaleItemDraft>?,
        totalAmount: Double,
        notes: String,
        reference: String,
        paymentMethod: PaymentMethod?,
        timestamp: Long
    ) {
        viewModelScope.launch {
            val items = itemsDraft?.mapNotNull { draft ->
                val qty = draft.quantity.toDoubleOrNull() ?: return@mapNotNull null
                val price = draft.price.toDoubleOrNull() ?: return@mapNotNull null
                TransactionItem(
                    transactionId = txId,
                    fuelType = draft.fuelType,
                    quantityLiters = qty,
                    pricePerLiter = price,
                    totalAmount = BigDecimal((qty * price).toString()).setScale(2, RoundingMode.HALF_UP).toDouble()
                )
            }
            val finalAmount = items?.sumOf { it.totalAmount } ?: totalAmount
            repository.updateTransaction(
                transactionId = txId,
                items = items,
                newAmount = finalAmount,
                notes = notes,
                reference = reference,
                paymentMethod = paymentMethod,
                timestamp = timestamp
            )
            _userMessage.emit("লেনদেন সফলভাবে আপডেট করা হয়েছে")
            refreshCustomerLedger()
        }
    }

    // Delete Transaction with reversal and balance recalculation
    fun deleteTransaction(txId: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(txId)
            _userMessage.emit("লেনদেন বাতিল করা হয়েছে এবং খতিয়ান পুনর্গণনা করা হয়েছে")
            refreshCustomerLedger()
        }
    }

    fun showReceiptDialog(txId: Long) {
        viewModelScope.launch {
            _receiptToPrint.value = repository.getTransactionWithDetails(txId)
        }
    }

    fun dismissReceiptDialog() {
        _receiptToPrint.value = null
    }

    // Settings
    fun updateStationSettings(name: String, address: String, phone: String) {
        viewModelScope.launch {
            preferencesRepository.updateStationDetails(name, address, phone)
            _userMessage.emit("স্টেশনের তথ্য সংরক্ষণ করা হয়েছে")
        }
    }

    fun updateFuelPrices(petrol: Double, octane: Double, diesel: Double) {
        viewModelScope.launch {
            preferencesRepository.updateFuelPrices(petrol, octane, diesel)
            _userMessage.emit("জ্বালানির ডিফল্ট মূল্য আপডেট করা হয়েছে")
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(lang)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun seedDemoData(clearExisting: Boolean) {
        viewModelScope.launch {
            repository.seedSampleData(clearExisting)
            _userMessage.emit("নমুনা ডাটা সফলভাবে যোগ করা হয়েছে")
            refreshCustomerLedger()
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            _userMessage.emit("সমস্ত ডাটা সফলভাবে মুছে ফেলা হয়েছে")
            refreshCustomerLedger()
        }
    }

    fun getExportCustomerLedgerCsv(customer: Customer, entries: List<CustomerLedgerEntry>): String {
        return repository.generateCustomerLedgerCsv(customer, entries)
    }

    fun getExportTransactionsCsv(transactions: List<TransactionWithDetails>): String {
        return repository.generateTransactionsCsv(transactions)
    }
}
