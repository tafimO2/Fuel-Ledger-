package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FuelType
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.components.FuelTypeChip
import com.example.ui.localization.AppStrings
import com.example.ui.theme.DueRed
import com.example.ui.theme.OctaneColor
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PetrolColor

enum class TxFilterType {
    ALL,
    CREDIT_SALES,
    PAYMENTS,
    PETROL,
    OCTANE,
    DIESEL
}

@Composable
fun TransactionsScreen(
    transactions: List<TransactionWithDetails>,
    language: String = "bn",
    onTransactionClick: (Long) -> Unit,
    onDeleteTransaction: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(TxFilterType.ALL) }
    var txToDelete by remember { mutableStateOf<TransactionWithDetails?>(null) }

    val filteredTransactions = transactions.filter { item ->
        val tx = item.transaction
        val q = searchQuery.trim().lowercase()
        val matchesSearch = q.isEmpty() ||
                tx.receiptNumber.lowercase().contains(q) ||
                (item.customerName?.lowercase()?.contains(q) == true) ||
                (item.customerPhone?.contains(q) == true) ||
                tx.reference.lowercase().contains(q) ||
                tx.notes.lowercase().contains(q)

        val matchesFilter = when (selectedFilter) {
            TxFilterType.ALL -> true
            TxFilterType.CREDIT_SALES -> tx.type == TransactionType.CREDIT_SALE
            TxFilterType.PAYMENTS -> tx.type == TransactionType.PAYMENT
            TxFilterType.PETROL -> item.items.any { it.fuelType == FuelType.PETROL }
            TxFilterType.OCTANE -> item.items.any { it.fuelType == FuelType.OCTANE }
            TxFilterType.DIESEL -> item.items.any { it.fuelType == FuelType.DIESEL }
        }

        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("transactions_screen")
    ) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (language == "bn") "লেনদেন ও রসিদ হিস্ট্রি" else "Transaction History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredTransactions.size} ${if (language == "bn") "টি লেনদেন প্রদর্শিত" else "transactions found"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (language == "bn") "রসিদ নং, গ্রাহকের নাম, মোবাইল..." else "Search receipt, customer, phone...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("transactions_search_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Filter Chips Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == TxFilterType.ALL,
                    onClick = { selectedFilter = TxFilterType.ALL },
                    label = { Text(if (language == "bn") "সব লেনদেন" else "All") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == TxFilterType.CREDIT_SALES,
                    onClick = { selectedFilter = TxFilterType.CREDIT_SALES },
                    label = { Text(if (language == "bn") "বাকি বিক্রি" else "Credit Sales") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == TxFilterType.PAYMENTS,
                    onClick = { selectedFilter = TxFilterType.PAYMENTS },
                    label = { Text(if (language == "bn") "জমা গ্রহণ" else "Payments") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == TxFilterType.OCTANE,
                    onClick = { selectedFilter = TxFilterType.OCTANE },
                    label = { Text(AppStrings.getFuelName(FuelType.OCTANE, language)) }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == TxFilterType.DIESEL,
                    onClick = { selectedFilter = TxFilterType.DIESEL },
                    label = { Text(AppStrings.getFuelName(FuelType.DIESEL, language)) }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == TxFilterType.PETROL,
                    onClick = { selectedFilter = TxFilterType.PETROL },
                    label = { Text(AppStrings.getFuelName(FuelType.PETROL, language)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transaction List
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (language == "bn") "কোন লেনদেন পাওয়া যায়নি" else "No transactions found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTransactions, key = { it.transaction.id }) { item ->
                    TransactionItemCard(
                        item = item,
                        language = language,
                        onClick = { onTransactionClick(item.transaction.id) },
                        onDelete = { txToDelete = item }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog with financial reversal explanation
    if (txToDelete != null) {
        val tx = txToDelete!!.transaction
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = {
                Text(
                    text = if (language == "bn") "লেনদেন বাতিল ও খতিয়ান সমন্বয়" else "Confirm Transaction Deletion",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (language == "bn") {
                        "রশিদ নং ${tx.receiptNumber} (${AppStrings.formatCurrency(tx.totalAmount, language)}) বাতিল করতে চান? এটি বাতিল করলে সংশ্লিষ্ট গ্রাহকের বর্তমান বকেয়া স্বয়ংক্রিয়ভাবে সমন্বয়/পুনর্গণনা হবে এবং অডিট ট্রেইলে সংরক্ষিত থাকবে।"
                    } else {
                        "Do you want to delete transaction ${tx.receiptNumber} (${AppStrings.formatCurrency(tx.totalAmount, language)})? Customer balance will be automatically reversed and audited."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(tx.id)
                        txToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DueRed)
                ) {
                    Text(if (language == "bn") "হ্যাঁ, বাতিল করুন" else "Yes, Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text(if (language == "bn") "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun TransactionItemCard(
    item: TransactionWithDetails,
    language: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val tx = item.transaction
    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (tx.type) {
                                    TransactionType.CREDIT_SALE -> DueRed.copy(alpha = 0.12f)
                                    TransactionType.CASH_SALE -> PaidGreen.copy(alpha = 0.12f)
                                    TransactionType.PAYMENT -> PaidGreen.copy(alpha = 0.12f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = AppStrings.getTransactionTypeName(tx.type, language),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (tx.type) {
                                TransactionType.CREDIT_SALE -> DueRed
                                else -> PaidGreen
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tx.receiptNumber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = AppStrings.formatDate(tx.timestamp, language),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(if (language == "bn") "রশিদ দেখুন / প্রিন্ট" else "View Receipt") },
                                leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (language == "bn") "মুছে ফেলুন" else "Delete", color = DueRed) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = DueRed) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Name & Total Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.customerName ?: if (language == "bn") "নগদ / অনির্ধারিত গ্রাহক" else "Cash Customer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (item.customerPhone != null) {
                        Text(
                            text = item.customerPhone,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = AppStrings.formatCurrency(tx.totalAmount, language),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = when (tx.type) {
                        TransactionType.CREDIT_SALE -> DueRed
                        else -> PaidGreen
                    }
                )
            }

            // Items breakdown or payment info
            if (item.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item.items.forEach { fItem ->
                        FuelTypeChip(
                            fuelType = fItem.fuelType,
                            isSelected = false,
                            language = language
                        )
                    }
                }
            } else if (tx.paymentMethod != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "মাধ্যম: ${AppStrings.getPaymentMethodName(tx.paymentMethod, language)}${if (tx.reference.isNotBlank()) " • Ref: ${tx.reference}" else ""}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
