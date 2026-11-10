package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerSummary
import com.example.data.model.FuelType
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.preferences.StationSettings
import com.example.ui.localization.AppStrings
import com.example.ui.theme.DieselColor
import com.example.ui.theme.DueRed
import com.example.ui.theme.FuelGreenPrimary
import com.example.ui.theme.OctaneColor
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PetrolColor
import java.util.Calendar

enum class ReportPeriod {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    ALL_TIME
}

@Composable
fun ReportsScreen(
    transactions: List<TransactionWithDetails>,
    customers: List<CustomerSummary>,
    settings: StationSettings,
    language: String = "bn"
) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.THIS_MONTH) }

    val currentTime = System.currentTimeMillis()
    val periodStartTime = when (selectedPeriod) {
        ReportPeriod.TODAY -> {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        ReportPeriod.THIS_WEEK -> currentTime - (7 * 86400000L)
        ReportPeriod.THIS_MONTH -> currentTime - (30 * 86400000L)
        ReportPeriod.ALL_TIME -> 0L
    }

    val periodTransactions = transactions.filter { it.transaction.timestamp >= periodStartTime }

    // Computations
    var totalCreditSales = 0.0
    var totalPayments = 0.0
    var totalPetrolCredit = 0.0
    var totalPetrolLiters = 0.0
    var totalOctaneCredit = 0.0
    var totalOctaneLiters = 0.0
    var totalDieselCredit = 0.0
    var totalDieselLiters = 0.0

    val paymentMethodTotals = mutableMapOf<PaymentMethod, Double>()

    for (item in periodTransactions) {
        val tx = item.transaction
        when (tx.type) {
            TransactionType.CREDIT_SALE -> {
                totalCreditSales += tx.totalAmount
                for (fItem in item.items) {
                    when (fItem.fuelType) {
                        FuelType.PETROL -> {
                            totalPetrolCredit += fItem.totalAmount
                            totalPetrolLiters += fItem.quantityLiters
                        }
                        FuelType.OCTANE -> {
                            totalOctaneCredit += fItem.totalAmount
                            totalOctaneLiters += fItem.quantityLiters
                        }
                        FuelType.DIESEL -> {
                            totalDieselCredit += fItem.totalAmount
                            totalDieselLiters += fItem.quantityLiters
                        }
                    }
                }
            }
            TransactionType.PAYMENT -> {
                totalPayments += tx.totalAmount
                val method = tx.paymentMethod ?: PaymentMethod.CASH
                paymentMethodTotals[method] = (paymentMethodTotals[method] ?: 0.0) + tx.totalAmount
            }
            TransactionType.CASH_SALE -> { /* Separate cash sale tracking */ }
        }
    }

    val totalDues = customers.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
    val totalFuelVolume = totalPetrolLiters + totalOctaneLiters + totalDieselLiters

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Title & Export Button
        item {
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
                            text = if (language == "bn") "রিপোর্ট ও অ্যানালিটিক্স" else "Reports & Analytics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (language == "bn") "জ্বালানি বিক্রি, বাকি ও আদায়ের হিসাব" else "Sales, credit & collection stats",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val reportText = buildString {
                                appendLine("=== ${settings.stationName} ===")
                                appendLine("রিপোর্ট সময়কাল: ${selectedPeriod.name}")
                                appendLine("মোট বাকি বিক্রি: ৳ $totalCreditSales")
                                appendLine("মোট জমা আদায়: ৳ $totalPayments")
                                appendLine("মোট বকেয়া: ৳ $totalDues")
                                appendLine("--- জ্বালানি ভিত্তিক বিক্রি ---")
                                appendLine("অকটেন: $totalOctaneLiters L (৳ $totalOctaneCredit)")
                                appendLine("ডিজেল: $totalDieselLiters L (৳ $totalDieselCredit)")
                                appendLine("পেট্রোল: $totalPetrolLiters L (৳ $totalPetrolCredit)")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Fuel Sales Report")
                                putExtra(Intent.EXTRA_TEXT, reportText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Report"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == "bn") "শেয়ার" else "Share", fontSize = 12.sp)
                    }
                }
            }
        }

        // Time Period Filter Chips
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedPeriod == ReportPeriod.TODAY,
                        onClick = { selectedPeriod = ReportPeriod.TODAY },
                        label = { Text(if (language == "bn") "আজকের দিন" else "Today") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedPeriod == ReportPeriod.THIS_WEEK,
                        onClick = { selectedPeriod = ReportPeriod.THIS_WEEK },
                        label = { Text(if (language == "bn") "গত ৭ দিন" else "7 Days") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedPeriod == ReportPeriod.THIS_MONTH,
                        onClick = { selectedPeriod = ReportPeriod.THIS_MONTH },
                        label = { Text(if (language == "bn") "এই মাস (৩০ দিন)" else "30 Days") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedPeriod == ReportPeriod.ALL_TIME,
                        onClick = { selectedPeriod = ReportPeriod.ALL_TIME },
                        label = { Text(if (language == "bn") "সর্বমোট সময়" else "All Time") }
                    )
                }
            }
        }

        // Top Financial Summary Cards
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = DueRed.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (language == "bn") "মোট বাকি বিক্রি" else "Total Credit Sales",
                                fontSize = 11.sp,
                                color = DueRed,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = AppStrings.formatCurrency(totalCreditSales, language),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DueRed
                            )
                        }
                    }

                    ElevatedCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = PaidGreen.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (language == "bn") "মোট জমা আদায়" else "Total Collected",
                                fontSize = 11.sp,
                                color = PaidGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = AppStrings.formatCurrency(totalPayments, language),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PaidGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Outstanding Dues card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (language == "bn") "গ্রাহকদের সর্বমোট বর্তমান বকেয়া" else "Total Outstanding Dues",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = AppStrings.formatCurrency(totalDues, language),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DueRed
                            )
                        }

                        Text(
                            text = "${customers.count { it.currentBalance > 0 }} ${if (language == "bn") "জন বকেয়া গ্রাহক" else "due customers"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Fuel Distribution Chart & Breakdown
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (language == "bn") "জ্বালানি ভিত্তিক বিক্রির বণ্টন (Fuel Distribution)" else "Fuel Sales Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Visual Segmented Bar
                        val safeVol = totalFuelVolume.coerceAtLeast(1.0)
                        val octaneRatio = (totalOctaneLiters / safeVol).toFloat()
                        val dieselRatio = (totalDieselLiters / safeVol).toFloat()
                        val petrolRatio = (totalPetrolLiters / safeVol).toFloat()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            if (octaneRatio > 0) {
                                Box(modifier = Modifier.weight(octaneRatio.coerceAtLeast(0.01f)).fillMaxSize().background(OctaneColor))
                            }
                            if (dieselRatio > 0) {
                                Box(modifier = Modifier.weight(dieselRatio.coerceAtLeast(0.01f)).fillMaxSize().background(DieselColor))
                            }
                            if (petrolRatio > 0) {
                                Box(modifier = Modifier.weight(petrolRatio.coerceAtLeast(0.01f)).fillMaxSize().background(PetrolColor))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Octane Row
                        FuelReportRow(
                            name = AppStrings.getFuelName(FuelType.OCTANE, language),
                            liters = totalOctaneLiters,
                            amount = totalOctaneCredit,
                            color = OctaneColor,
                            language = language
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Diesel Row
                        FuelReportRow(
                            name = AppStrings.getFuelName(FuelType.DIESEL, language),
                            liters = totalDieselLiters,
                            amount = totalDieselCredit,
                            color = DieselColor,
                            language = language
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Petrol Row
                        FuelReportRow(
                            name = AppStrings.getFuelName(FuelType.PETROL, language),
                            liters = totalPetrolLiters,
                            amount = totalPetrolCredit,
                            color = PetrolColor,
                            language = language
                        )
                    }
                }
            }
        }

        // Payment Collection by Method
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = if (language == "bn") "পেমেন্ট মাধ্যম ভিত্তিক আদায় (Payment Methods)" else "Collection by Payment Method",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PaymentMethod.entries.forEach { method ->
                            val amount = paymentMethodTotals[method] ?: 0.0
                            val share = if (totalPayments > 0) (amount / totalPayments).toFloat() else 0f

                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = AppStrings.getPaymentMethodName(method, language),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = AppStrings.formatCurrency(amount, language),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PaidGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { share },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = PaidGreen,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Customer Dues Table
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (language == "bn") "গ্রাহক ভিত্তিক বকেয়া তালিকা (Top Customer Dues)" else "Customer Dues Ranking",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val topList = customers.filter { it.currentBalance > 0 }.sortedByDescending { it.currentBalance }
                        if (topList.isEmpty()) {
                            Text(
                                text = if (language == "bn") "কোন বকেয়া বাকি নেই" else "No outstanding customers",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        } else {
                            topList.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${idx + 1}.",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(text = item.customer.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(text = item.customer.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Text(
                                        text = AppStrings.formatCurrency(item.currentBalance, language),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = DueRed
                                    )
                                }
                                if (idx < topList.size - 1) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
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
fun FuelReportRow(
    name: String,
    liters: Double,
    amount: Double,
    color: Color,
    language: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "${AppStrings.formatNumber(liters, language)} L",
                    fontSize = 12.sp,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Text(
            text = AppStrings.formatCurrency(amount, language),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
