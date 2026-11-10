package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.ui.components.CustomerFormDialog
import com.example.ui.components.NewSaleDialog
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.ReceivePaymentDialog
import com.example.ui.localization.AppStrings
import com.example.ui.screens.CustomerDetailScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.FuelGreenPrimary
import com.example.ui.theme.FuelLedgerTheme
import com.example.ui.viewmodel.FuelLedgerViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: FuelLedgerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settingsState.collectAsStateWithLifecycle()

            FuelLedgerTheme(themeMode = settings.themeMode) {
                FuelLedgerApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FuelLedgerApp(viewModel: FuelLedgerViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val dashboardSummary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val customers by viewModel.customerSummaries.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val customerLedger by viewModel.customerLedger.collectAsStateWithLifecycle()
    val ledgerFuelFilter by viewModel.ledgerFuelFilter.collectAsStateWithLifecycle()
    val receiptToPrint by viewModel.receiptToPrint.collectAsStateWithLifecycle()

    // Dialog controllers
    var showNewSaleDialog by remember { mutableStateOf(false) }
    var salePreselectedCustomerId by remember { mutableStateOf<Long?>(null) }

    var showReceivePaymentDialog by remember { mutableStateOf(false) }
    var paymentPreselectedCustomerId by remember { mutableStateOf<Long?>(null) }

    var showCustomerFormDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }

    val language = settings.language

    // Listen for toast messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Show bottom navigation on main tabs
            if (currentScreen != Screen.CustomerDetail) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.Dashboard,
                        onClick = { viewModel.navigateTo(Screen.Dashboard) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text(AppStrings.navHome(language), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.Customers,
                        onClick = { viewModel.navigateTo(Screen.Customers) },
                        icon = { Icon(Icons.Default.People, contentDescription = "Customers") },
                        label = { Text(AppStrings.navCustomers(language), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_item_customers")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.Transactions,
                        onClick = { viewModel.navigateTo(Screen.Transactions) },
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Transactions") },
                        label = { Text(AppStrings.navTransactions(language), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_item_transactions")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.Reports,
                        onClick = { viewModel.navigateTo(Screen.Reports) },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Reports") },
                        label = { Text(AppStrings.navReports(language), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_item_reports")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.Settings,
                        onClick = { viewModel.navigateTo(Screen.Settings) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text(AppStrings.navSettings(language), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Dashboard -> {
                    HomeScreen(
                        settings = settings,
                        summary = dashboardSummary,
                        customers = customers,
                        onNewCreditSaleClick = {
                            salePreselectedCustomerId = null
                            showNewSaleDialog = true
                        },
                        onReceivePaymentClick = {
                            paymentPreselectedCustomerId = null
                            showReceivePaymentDialog = true
                        },
                        onAddCustomerClick = {
                            customerToEdit = null
                            showCustomerFormDialog = true
                        },
                        onViewAllTransactionsClick = {
                            viewModel.navigateTo(Screen.Transactions)
                        },
                        onCustomerClick = { customerId ->
                            viewModel.selectCustomerForDetail(customerId)
                        }
                    )
                }

                Screen.Customers -> {
                    CustomersScreen(
                        customers = customers,
                        language = language,
                        onCustomerClick = { customerId ->
                            viewModel.selectCustomerForDetail(customerId)
                        },
                        onAddCustomerClick = {
                            customerToEdit = null
                            showCustomerFormDialog = true
                        },
                        onEditCustomerClick = { customer ->
                            customerToEdit = customer
                            showCustomerFormDialog = true
                        },
                        onDeleteCustomerClick = { customerId ->
                            viewModel.deleteCustomer(customerId)
                        }
                    )
                }

                Screen.CustomerDetail -> {
                    CustomerDetailScreen(
                        customer = selectedCustomer,
                        openingBalance = customerLedger.first,
                        ledgerEntries = customerLedger.second,
                        fuelFilter = ledgerFuelFilter,
                        language = language,
                        onBackClick = { viewModel.navigateBack() },
                        onFuelFilterSelect = { filter ->
                            viewModel.setLedgerFuelFilter(filter)
                        },
                        onNewSaleClick = {
                            salePreselectedCustomerId = selectedCustomer?.id
                            showNewSaleDialog = true
                        },
                        onReceivePaymentClick = {
                            paymentPreselectedCustomerId = selectedCustomer?.id
                            showReceivePaymentDialog = true
                        },
                        onLedgerEntryClick = { txId ->
                            viewModel.showReceiptDialog(txId)
                        },
                        onShareLedgerClick = {
                            val cust = selectedCustomer
                            if (cust != null) {
                                val csv = viewModel.getExportCustomerLedgerCsv(cust, customerLedger.second)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Customer Ledger: ${cust.name}")
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Ledger CSV"))
                            }
                        }
                    )
                }

                Screen.Transactions -> {
                    TransactionsScreen(
                        transactions = allTransactions,
                        language = language,
                        onTransactionClick = { txId ->
                            viewModel.showReceiptDialog(txId)
                        },
                        onDeleteTransaction = { txId ->
                            viewModel.deleteTransaction(txId)
                        }
                    )
                }

                Screen.Reports -> {
                    ReportsScreen(
                        transactions = allTransactions,
                        customers = customers,
                        settings = settings,
                        language = language
                    )
                }

                Screen.Settings -> {
                    SettingsScreen(
                        settings = settings,
                        language = language,
                        onUpdateStationDetails = { name, address, phone ->
                            viewModel.updateStationSettings(name, address, phone)
                        },
                        onUpdateFuelPrices = { petrol, octane, diesel ->
                            viewModel.updateFuelPrices(petrol, octane, diesel)
                        },
                        onLanguageChange = { lang ->
                            viewModel.setLanguage(lang)
                        },
                        onThemeChange = { mode ->
                            viewModel.setThemeMode(mode)
                        },
                        onSeedDemoData = { clearExisting ->
                            viewModel.seedDemoData(clearExisting)
                        },
                        onResetAllData = {
                            viewModel.resetAllData()
                        }
                    )
                }
            }
        }
    }

    // Modal Dialog: New Sale (Credit or Cash)
    if (showNewSaleDialog) {
        NewSaleDialog(
            customers = customers,
            preselectedCustomerId = salePreselectedCustomerId,
            settings = settings,
            language = language,
            onDismiss = { showNewSaleDialog = false },
            onSubmit = { customerId, items, timestamp, notes, isCash ->
                viewModel.submitCreditSale(
                    customerId = customerId,
                    itemsDraft = items,
                    timestamp = timestamp,
                    notes = notes,
                    isCashSale = isCash
                ) { txId ->
                    viewModel.showReceiptDialog(txId)
                }
            }
        )
    }

    // Modal Dialog: Receive Payment
    if (showReceivePaymentDialog) {
        ReceivePaymentDialog(
            customers = customers,
            preselectedCustomerId = paymentPreselectedCustomerId,
            language = language,
            onDismiss = { showReceivePaymentDialog = false },
            onSubmit = { customerId, amount, method, reference, notes ->
                viewModel.submitPayment(
                    customerId = customerId,
                    amount = amount,
                    method = method,
                    reference = reference,
                    notes = notes,
                    timestamp = System.currentTimeMillis()
                ) { txId ->
                    viewModel.showReceiptDialog(txId)
                }
            }
        )
    }

    // Modal Dialog: Add / Edit Customer
    if (showCustomerFormDialog) {
        CustomerFormDialog(
            initialCustomer = customerToEdit,
            language = language,
            onDismiss = { showCustomerFormDialog = false },
            onSave = { id, name, phone, address, vehicleReg, type, openingBalance, notes ->
                viewModel.saveCustomer(
                    id = id,
                    name = name,
                    phone = phone,
                    address = address,
                    vehicleReg = vehicleReg,
                    type = type,
                    openingBalance = openingBalance,
                    notes = notes
                )
            }
        )
    }

    // Modal Dialog: Printable / Shareable Receipt
    if (receiptToPrint != null) {
        ReceiptDialog(
            transactionWithDetails = receiptToPrint!!,
            settings = settings,
            language = language,
            onDismiss = { viewModel.dismissReceiptDialog() }
        )
    }
}
