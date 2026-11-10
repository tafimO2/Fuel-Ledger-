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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerSummary
import com.example.data.model.FuelType
import com.example.data.preferences.StationSettings
import com.example.data.repository.DashboardSummary
import com.example.ui.localization.AppStrings
import com.example.ui.theme.DieselColor
import com.example.ui.theme.DueRed
import com.example.ui.theme.DueRedContainer
import com.example.ui.theme.FuelGreenPrimary
import com.example.ui.theme.OctaneColor
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.PetrolColor

@Composable
fun HomeScreen(
    settings: StationSettings,
    summary: DashboardSummary,
    customers: List<CustomerSummary>,
    onNewCreditSaleClick: () -> Unit,
    onReceivePaymentClick: () -> Unit,
    onAddCustomerClick: () -> Unit,
    onViewAllTransactionsClick: () -> Unit,
    onCustomerClick: (Long) -> Unit
) {
    val language = settings.language

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Station Brand Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                FuelGreenPrimary,
                                FuelGreenPrimary.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = settings.stationName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = settings.stationAddress,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Big Total Outstanding Banner inside Header
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = AppStrings.totalOutstanding(language),
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = AppStrings.formatCurrency(summary.totalOutstandingDues, language),
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DueRed)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (language == "bn") "বকেয়া বাকি" else "Due Credit",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Today's Key Metrics Grid
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (language == "bn") "আজকের হিসাব ও লেনদেন" else "Today's Ledger Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = AppStrings.todayCreditSales(language),
                        amount = summary.todayCreditSales,
                        language = language,
                        icon = Icons.Default.TrendingUp,
                        accentColor = DueRed,
                        backgroundColor = DueRedContainer.copy(alpha = 0.4f)
                    )

                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = AppStrings.todayCollection(language),
                        amount = summary.todayCollection,
                        language = language,
                        icon = Icons.Default.TrendingDown,
                        accentColor = PaidGreen,
                        backgroundColor = PaidGreenContainer.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SmallStatsCard(
                        modifier = Modifier.weight(1f),
                        title = AppStrings.todayCashSales(language),
                        value = AppStrings.formatCurrency(summary.todayCashSales, language),
                        icon = Icons.Default.Payments,
                        accentColor = PaidGreen
                    )
                    SmallStatsCard(
                        modifier = Modifier.weight(1f),
                        title = AppStrings.totalCustomers(language),
                        value = AppStrings.toBengaliDigits("${summary.totalCustomerCount}"),
                        icon = Icons.Default.PersonAdd,
                        accentColor = FuelGreenPrimary
                    )
                    SmallStatsCard(
                        modifier = Modifier.weight(1f),
                        title = AppStrings.todayTxCount(language),
                        value = AppStrings.toBengaliDigits("${summary.todayTransactionCount}"),
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        accentColor = OctaneColor
                    )
                }
            }
        }

        // Quick Actions
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = if (language == "bn") "দ্রুত অ্যাকশন (Quick Actions)" else "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNewCreditSaleClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_credit_sale_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = DueRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = AppStrings.newCreditSale(language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onReceivePaymentClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_receive_payment_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PaidGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = AppStrings.receivePayment(language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onAddCustomerClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppStrings.addCustomer(language), fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onViewAllTransactionsClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == "bn") "সব লেনদেন" else "All Transactions", fontSize = 12.sp)
                    }
                }
            }
        }

        // 3 Fuel Category Cards (Petrol, Octane, Diesel)
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == "bn") "জ্বালানি ক্যাটাগরি বিক্রি ও বকেয়া" else "Fuel Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Petrol Card
                FuelCategoryCard(
                    fuelType = FuelType.PETROL,
                    color = PetrolColor,
                    salesAmount = summary.petrolTodayAmount,
                    liters = summary.petrolTodayLiters,
                    dueAmount = summary.petrolOutstanding,
                    ratePerLiter = settings.defaultPetrolPrice,
                    language = language
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Octane Card
                FuelCategoryCard(
                    fuelType = FuelType.OCTANE,
                    color = OctaneColor,
                    salesAmount = summary.octaneTodayAmount,
                    liters = summary.octaneTodayLiters,
                    dueAmount = summary.octaneOutstanding,
                    ratePerLiter = settings.defaultOctanePrice,
                    language = language
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Diesel Card
                FuelCategoryCard(
                    fuelType = FuelType.DIESEL,
                    color = DieselColor,
                    salesAmount = summary.dieselTodayAmount,
                    liters = summary.dieselTodayLiters,
                    dueAmount = summary.dieselOutstanding,
                    ratePerLiter = settings.defaultDieselPrice,
                    language = language
                )
            }
        }

        // Customers with Outstanding Dues Alert List
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = DueRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == "bn") "সর্বোচ্চ বকেয়া গ্রাহকগণ" else "Top Outstanding Customers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = onViewAllTransactionsClick) {
                        Text(AppStrings.viewAll(language), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val topDues = customers
                    .filter { it.currentBalance > 0 }
                    .sortedByDescending { it.currentBalance }
                    .take(5)

                if (topDues.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (language == "bn") "কোন বকেয়া বাকি নেই! সকল হিসাব পরিশোধিত।" else "No outstanding dues found!",
                            modifier = Modifier.padding(20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    topDues.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onCustomerClick(item.customer.id) },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(DueRed.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.customer.name.take(1),
                                            color = DueRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = item.customer.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${item.customer.phone} ${if (item.customer.vehicleReg.isNotBlank()) "• ${item.customer.vehicleReg}" else ""}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = AppStrings.formatCurrency(item.currentBalance, language),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = DueRed
                                        )
                                        Text(
                                            text = if (language == "bn") "বকেয়া" else "Due",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    language: String,
    icon: ImageVector,
    accentColor: Color,
    backgroundColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = AppStrings.formatCurrency(amount, language),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
fun SmallStatsCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun FuelCategoryCard(
    fuelType: FuelType,
    color: Color,
    salesAmount: Double,
    liters: Double,
    dueAmount: Double,
    ratePerLiter: Double,
    language: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.getFuelName(fuelType, language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(color.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "৳ ${AppStrings.formatNumber(ratePerLiter, language)}/L",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (language == "bn") "আজকের বিক্রি" else "Today's Sales",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = AppStrings.formatCurrency(salesAmount, language),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (language == "bn") "বিক্রিত পরিমাণ" else "Sold Quantity",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${AppStrings.formatNumber(liters, language)} L",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (language == "bn") "মোট বকেয়া" else "Est. Due",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = AppStrings.formatCurrency(dueAmount, language),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DueRed
                    )
                }
            }
        }
    }
}
