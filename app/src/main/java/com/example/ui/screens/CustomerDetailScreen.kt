package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.CustomerLedgerEntry
import com.example.data.model.FuelType
import com.example.data.model.TransactionType
import com.example.ui.components.FuelTypeChip
import com.example.ui.localization.AppStrings
import com.example.ui.theme.DueRed
import com.example.ui.theme.FuelGreenPrimary
import com.example.ui.theme.PaidGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customer: Customer?,
    openingBalance: Double,
    ledgerEntries: List<CustomerLedgerEntry>,
    fuelFilter: FuelType?,
    language: String = "bn",
    onBackClick: () -> Unit,
    onFuelFilterSelect: (FuelType?) -> Unit,
    onNewSaleClick: () -> Unit,
    onReceivePaymentClick: () -> Unit,
    onLedgerEntryClick: (Long) -> Unit,
    onShareLedgerClick: () -> Unit
) {
    BackHandler {
        onBackClick()
    }

    val context = LocalContext.current

    if (customer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(if (language == "bn") "গ্রাহক খুঁজে পাওয়া যায়নি" else "Customer not found")
        }
        return
    }

    val finalBalance = ledgerEntries.lastOrNull()?.runningBalance ?: openingBalance

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = customer.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${customer.phone} ${if (customer.vehicleReg.isNotBlank()) "• ${customer.vehicleReg}" else ""}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShareLedgerClick) {
                        Icon(Icons.Default.Share, contentDescription = "Share Statement")
                    }
                    if (customer.phone.isNotBlank()) {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Call, contentDescription = "Call")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("customer_detail_screen"),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Customer Header & Financial Status Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (finalBalance > 0) DueRed.copy(alpha = 0.08f) else PaidGreen.copy(alpha = 0.08f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (finalBalance > 0) {
                                        AppStrings.outstandingBalance(language)
                                    } else if (finalBalance < 0) {
                                        AppStrings.advanceBalance(language)
                                    } else {
                                        AppStrings.clearDues(language)
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (finalBalance > 0) DueRed else PaidGreen
                                )
                                Text(
                                    text = AppStrings.formatCurrency(kotlin.math.abs(finalBalance), language),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (finalBalance > 0) DueRed else PaidGreen
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (finalBalance > 0) DueRed else PaidGreen)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (finalBalance > 0) {
                                        if (language == "bn") "বকেয়া বাকি" else "Due Credit"
                                    } else {
                                        if (language == "bn") "পরিশোধিত" else "Cleared"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == "bn") "প্রারম্ভিক বকেয়া (Opening):" else "Opening Balance:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = AppStrings.formatCurrency(openingBalance, language),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }

                        if (customer.vehicleReg.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (language == "bn") "গাড়ির নম্বর:" else "Vehicle Reg:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = customer.vehicleReg,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (customer.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "নোট: ${customer.notes}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Transaction Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNewSaleClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DueRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == "bn") "+ বাকি বিক্রি" else "+ Credit Sale", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onReceivePaymentClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PaidGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == "bn") "+ জমা গ্রহণ" else "+ Receive Payment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Fuel Filter Chips
            item {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
                    Text(
                        text = if (language == "bn") "খতিয়ান বিবরণী (Ledger Statement)" else "Ledger Statement",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = fuelFilter == null,
                                onClick = { onFuelFilterSelect(null) },
                                label = { Text(if (language == "bn") "সব তেল" else "All Fuel", fontSize = 12.sp) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = fuelFilter == FuelType.PETROL,
                                onClick = { onFuelFilterSelect(FuelType.PETROL) },
                                label = { Text(AppStrings.getFuelName(FuelType.PETROL, language), fontSize = 12.sp) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = fuelFilter == FuelType.OCTANE,
                                onClick = { onFuelFilterSelect(FuelType.OCTANE) },
                                label = { Text(AppStrings.getFuelName(FuelType.OCTANE, language), fontSize = 12.sp) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = fuelFilter == FuelType.DIESEL,
                                onClick = { onFuelFilterSelect(FuelType.DIESEL) },
                                label = { Text(AppStrings.getFuelName(FuelType.DIESEL, language), fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // Opening Balance Ledger Header Row
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (language == "bn") "প্রারম্ভিক বকেয়া (Opening Balance)" else "Opening Balance",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = AppStrings.formatDate(customer.createdAt, language, includeTime = false),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = AppStrings.formatCurrency(openingBalance, language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Ledger Entries
            if (ledgerEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (language == "bn") "এই গ্রাহকের কোন লেনদেন পাওয়া যায়নি" else "No transactions for this customer",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(ledgerEntries) { entry ->
                    LedgerEntryCard(
                        entry = entry,
                        language = language,
                        onClick = { onLedgerEntryClick(entry.transactionId) }
                    )
                }
            }
        }
    }
}

@Composable
fun LedgerEntryCard(
    entry: CustomerLedgerEntry,
    language: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Date & Receipt
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (entry.creditAmount > 0) DueRed else PaidGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.formatDate(entry.timestamp, language),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = entry.receiptNumber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = entry.description,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            if (entry.fuelDetails.isNotBlank()) {
                Text(
                    text = entry.fuelDetails,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Financial Summary Row: Credit / Payment / Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (entry.creditAmount > 0) {
                    Column {
                        Text(
                            text = if (language == "bn") "বাকি বিক্রি (+)" else "Credit (+)",
                            fontSize = 10.sp,
                            color = DueRed
                        )
                        Text(
                            text = AppStrings.formatCurrency(entry.creditAmount, language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DueRed
                        )
                    }
                }

                if (entry.paymentAmount > 0) {
                    Column {
                        Text(
                            text = if (language == "bn") "জমা গ্রহণ (-)" else "Payment (-)",
                            fontSize = 10.sp,
                            color = PaidGreen
                        )
                        Text(
                            text = AppStrings.formatCurrency(entry.paymentAmount, language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PaidGreen
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (language == "bn") "চলমান বকেয়া" else "Running Due",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = AppStrings.formatCurrency(entry.runningBalance, language),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (entry.runningBalance > 0) DueRed else PaidGreen
                    )
                }
            }
        }
    }
}
