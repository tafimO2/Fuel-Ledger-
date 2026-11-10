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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.data.model.Customer
import com.example.data.model.CustomerSummary
import com.example.ui.localization.AppStrings
import com.example.ui.theme.DueRed
import com.example.ui.theme.FuelGreenPrimary
import com.example.ui.theme.PaidGreen

enum class CustomerFilter {
    ALL,
    HAS_DUES,
    HIGH_DUES,
    CLEARED
}

@Composable
fun CustomersScreen(
    customers: List<CustomerSummary>,
    language: String = "bn",
    onCustomerClick: (Long) -> Unit,
    onAddCustomerClick: () -> Unit,
    onEditCustomerClick: (Customer) -> Unit,
    onDeleteCustomerClick: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(CustomerFilter.ALL) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    val filteredCustomers = customers.filter { summary ->
        val c = summary.customer
        val query = searchQuery.trim().lowercase()
        val matchesQuery = query.isEmpty() ||
                c.name.lowercase().contains(query) ||
                c.phone.contains(query) ||
                c.vehicleReg.lowercase().contains(query)

        val matchesFilter = when (selectedFilter) {
            CustomerFilter.ALL -> true
            CustomerFilter.HAS_DUES -> summary.currentBalance > 0
            CustomerFilter.HIGH_DUES -> summary.currentBalance >= 10000.0
            CustomerFilter.CLEARED -> summary.currentBalance <= 0
        }

        matchesQuery && matchesFilter
    }

    val totalDues = customers.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCustomerClick,
                containerColor = FuelGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .testTag("add_customer_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("customers_screen")
        ) {
            // Header summary strip
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (language == "bn") "গ্রাহক ব্যবস্থাপনা" else "Customer Management",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${customers.size} ${if (language == "bn") "জন গ্রাহক তালিকাভুক্ত" else "customers registered"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (language == "bn") "সর্বমোট বাকি" else "Total Due",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = AppStrings.formatCurrency(totalDues, language),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DueRed
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        if (language == "bn") "গ্রাহকের নাম, মোবাইল বা গাড়ি দিয়ে খুঁজুন..." else "Search by name, phone, vehicle..."
                    )
                },
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
                    .testTag("customer_search_input"),
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
                        selected = selectedFilter == CustomerFilter.ALL,
                        onClick = { selectedFilter = CustomerFilter.ALL },
                        label = { Text(if (language == "bn") "সকল গ্রাহক" else "All") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CustomerFilter.HAS_DUES,
                        onClick = { selectedFilter = CustomerFilter.HAS_DUES },
                        label = { Text(if (language == "bn") "বকেয়া আছে" else "Has Dues") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CustomerFilter.HIGH_DUES,
                        onClick = { selectedFilter = CustomerFilter.HIGH_DUES },
                        label = { Text(if (language == "bn") "১০,০০০+ বকেয়া" else "10k+ Due") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CustomerFilter.CLEARED,
                        onClick = { selectedFilter = CustomerFilter.CLEARED },
                        label = { Text(if (language == "bn") "পরিশোধিত / অগ্রিম" else "Cleared / Advance") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer List
            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (language == "bn") "কোন গ্রাহক পাওয়া যায়নি" else "No customers found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCustomers, key = { it.customer.id }) { item ->
                        CustomerCardItem(
                            summary = item,
                            language = language,
                            onClick = { onCustomerClick(item.customer.id) },
                            onEdit = { onEditCustomerClick(item.customer) },
                            onDelete = { customerToDelete = item.customer }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (customerToDelete != null) {
        val cust = customerToDelete!!
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = {
                Text(
                    text = if (language == "bn") "গ্রাহক মুছে ফেলা নিশ্চিত করুন" else "Confirm Delete",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (language == "bn") {
                        "${cust.name} কে মুছে ফেলতে চান? গ্রাহকের বিগত লেনদেনের হিসাব এবং খতিয়ান অডিট রেকর্ডে অক্ষুণ্ণ থাকবে।"
                    } else {
                        "Are you sure you want to delete ${cust.name}? Historical transactions will remain preserved in audit logs."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCustomerClick(cust.id)
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DueRed)
                ) {
                    Text(if (language == "bn") "মুছে ফেলুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text(if (language == "bn") "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun CustomerCardItem(
    summary: CustomerSummary,
    language: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val customer = summary.customer
    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (summary.currentBalance > 0) DueRed.copy(alpha = 0.12f) else PaidGreen.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customer.name.take(1),
                            color = if (summary.currentBalance > 0) DueRed else PaidGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = customer.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = AppStrings.getCustomerTypeName(customer.customerType, language),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = customer.phone,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (customer.vehicleReg.isNotBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = customer.vehicleReg,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (language == "bn") "সম্পাদনা" else "Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit()
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

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Balance Strip
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (language == "bn") "মোট বাকি বিক্রি" else "Total Credit",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = AppStrings.formatCurrency(summary.totalCredit, language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (language == "bn") "মোট পরিশোধ" else "Total Paid",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = AppStrings.formatCurrency(summary.totalPayment, language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PaidGreen
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (summary.currentBalance > 0) {
                                AppStrings.outstandingBalance(language)
                            } else if (summary.currentBalance < 0) {
                                AppStrings.advanceBalance(language)
                            } else {
                                AppStrings.clearDues(language)
                            },
                            fontSize = 10.sp,
                            color = if (summary.currentBalance > 0) DueRed else PaidGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = AppStrings.formatCurrency(kotlin.math.abs(summary.currentBalance), language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (summary.currentBalance > 0) DueRed else PaidGreen
                        )
                    }
                }
            }
        }
    }
}
