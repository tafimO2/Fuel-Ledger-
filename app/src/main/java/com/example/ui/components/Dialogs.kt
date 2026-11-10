package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Customer
import com.example.data.model.CustomerSummary
import com.example.data.model.CustomerType
import com.example.data.model.FuelType
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionItem
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
import com.example.ui.viewmodel.SaleItemDraft
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun FuelTypeChip(
    fuelType: FuelType,
    isSelected: Boolean = false,
    language: String = "bn",
    onClick: (() -> Unit)? = null
) {
    val color = when (fuelType) {
        FuelType.PETROL -> PetrolColor
        FuelType.OCTANE -> OctaneColor
        FuelType.DIESEL -> DieselColor
    }
    val name = AppStrings.getFuelName(fuelType, language)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color else color.copy(alpha = 0.12f))
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                color = if (isSelected) Color.White else color,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

// Dialog: Add / Edit Customer
@Composable
fun CustomerFormDialog(
    initialCustomer: Customer? = null,
    language: String = "bn",
    onDismiss: () -> Unit,
    onSave: (id: Long, name: String, phone: String, address: String, vehicleReg: String, type: CustomerType, openingBalance: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialCustomer?.name ?: "") }
    var phone by remember { mutableStateOf(initialCustomer?.phone ?: "") }
    var address by remember { mutableStateOf(initialCustomer?.address ?: "") }
    var vehicleReg by remember { mutableStateOf(initialCustomer?.vehicleReg ?: "") }
    var customerType by remember { mutableStateOf(initialCustomer?.customerType ?: CustomerType.INDIVIDUAL) }
    var openingBalanceStr by remember { mutableStateOf(initialCustomer?.openingBalance?.toString() ?: "0.0") }
    var notes by remember { mutableStateOf(initialCustomer?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialCustomer == null) {
                            if (language == "bn") "নতুন গ্রাহক যোগ করুন" else "Add New Customer"
                        } else {
                            if (language == "bn") "গ্রাহকের তথ্য সম্পাদনা" else "Edit Customer"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (language == "bn") "গ্রাহকের নাম *" else "Customer Name *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (language == "bn") "মোবাইল নম্বর *" else "Mobile Number *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_phone_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = vehicleReg,
                    onValueChange = { vehicleReg = it },
                    label = { Text(if (language == "bn") "গাড়ির রেজিস্ট্রেশন নম্বর (যেমন: ঢাকা মেট্রো-গ ১২-৩৪৫৬)" else "Vehicle Reg No.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (language == "bn") "গ্রাহকের ধরন" else "Customer Type",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomerType.entries.forEach { type ->
                        FilterChip(
                            selected = customerType == type,
                            onClick = { customerType = type },
                            label = { Text(AppStrings.getCustomerTypeName(type, language), fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = openingBalanceStr,
                    onValueChange = { openingBalanceStr = it },
                    label = { Text(if (language == "bn") "প্রারম্ভিক বকেয়া (৳)" else "Opening Balance (৳)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (language == "bn") "ঠিকানা" else "Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (language == "bn") "মন্তব্য / নোট" else "Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (language == "bn") "বাতিল" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = if (language == "bn") "নাম প্রদান করা আবশ্যক" else "Name is required"
                                return@Button
                            }
                            if (phone.isBlank()) {
                                errorMessage = if (language == "bn") "মোবাইল নম্বর প্রদান আবশ্যক" else "Phone is required"
                                return@Button
                            }
                            val balance = openingBalanceStr.toDoubleOrNull() ?: 0.0
                            onSave(
                                initialCustomer?.id ?: 0L,
                                name,
                                phone,
                                address,
                                vehicleReg,
                                customerType,
                                balance,
                                notes
                            )
                            onDismiss()
                        },
                        modifier = Modifier.testTag("save_customer_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = FuelGreenPrimary)
                    ) {
                        Text(if (language == "bn") "সংরক্ষণ করুন" else "Save")
                    }
                }
            }
        }
    }
}

// Dialog: New Credit or Cash Sale
@Composable
fun NewSaleDialog(
    customers: List<CustomerSummary>,
    preselectedCustomerId: Long? = null,
    settings: StationSettings,
    language: String = "bn",
    onDismiss: () -> Unit,
    onSubmit: (customerId: Long?, items: List<SaleItemDraft>, timestamp: Long, notes: String, isCash: Boolean) -> Unit
) {
    var isCashSale by remember { mutableStateOf(false) }
    var selectedCustomerId by remember { mutableStateOf<Long?>(preselectedCustomerId ?: customers.firstOrNull()?.customer?.id) }
    var customerSearchQuery by remember { mutableStateOf("") }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    val defaultPriceFor = { type: FuelType ->
        when (type) {
            FuelType.PETROL -> settings.defaultPetrolPrice.toString()
            FuelType.OCTANE -> settings.defaultOctanePrice.toString()
            FuelType.DIESEL -> settings.defaultDieselPrice.toString()
        }
    }

    val itemsList = remember {
        mutableStateListOf(
            SaleItemDraft(
                fuelType = FuelType.OCTANE,
                quantity = "20",
                price = defaultPriceFor(FuelType.OCTANE),
                amount = 20.0 * (defaultPriceFor(FuelType.OCTANE).toDoubleOrNull() ?: 0.0)
            )
        )
    }

    val selectedCustomer = customers.find { it.customer.id == selectedCustomerId }

    val grandTotal = itemsList.sumOf { draft ->
        val q = draft.quantity.toDoubleOrNull() ?: 0.0
        val p = draft.price.toDoubleOrNull() ?: 0.0
        q * p
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isCashSale) {
                                if (language == "bn") "নগদ বিক্রি (Cash Sale)" else "New Cash Sale"
                            } else {
                                if (language == "bn") "বাকি বিক্রি এন্ট্রি (Credit Sale)" else "New Credit Sale"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (language == "bn") "জ্বালানি বিক্রি ও খতিয়ান আপডেট" else "Fuel sale & ledger update",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle Sale Type
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isCashSale) DueRed else Color.Transparent)
                            .clickable { isCashSale = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (language == "bn") "বাকি বিক্রি (Credit)" else "Credit Sale",
                            color = if (!isCashSale) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCashSale) PaidGreen else Color.Transparent)
                            .clickable { isCashSale = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (language == "bn") "নগদ বিক্রি (Cash)" else "Cash Sale",
                            color = if (isCashSale) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Customer selection
                Text(
                    text = if (language == "bn") "গ্রাহক নির্বাচন *" else "Select Customer *",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCustomerPicker = true }
                        .padding(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            if (selectedCustomer != null) {
                                Column {
                                    Text(
                                        text = selectedCustomer.customer.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "${selectedCustomer.customer.phone} ${if (selectedCustomer.customer.vehicleReg.isNotBlank()) "• ${selectedCustomer.customer.vehicleReg}" else ""}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text(
                                    text = if (language == "bn") "গ্রাহক নির্বাচন করুন" else "Select Customer",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (selectedCustomer != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (language == "bn") "বর্তমান বাকি" else "Current Due",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = AppStrings.formatCurrency(selectedCustomer.currentBalance, language),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCustomer.currentBalance > 0) DueRed else PaidGreen
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fuel items header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == "bn") "জ্বালানির বিবরণ (Fuel Items)" else "Fuel Items",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    TextButton(
                        onClick = {
                            itemsList.add(
                                SaleItemDraft(
                                    fuelType = FuelType.DIESEL,
                                    quantity = "10",
                                    price = defaultPriceFor(FuelType.DIESEL),
                                    amount = 10.0 * (defaultPriceFor(FuelType.DIESEL).toDoubleOrNull() ?: 0.0)
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == "bn") "+ আইটেম যোগ" else "+ Add Item", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Items list
                itemsList.forEachIndexed { index, draft ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FuelType.entries.forEach { fType ->
                                        FuelTypeChip(
                                            fuelType = fType,
                                            isSelected = draft.fuelType == fType,
                                            language = language,
                                            onClick = {
                                                val newPrice = defaultPriceFor(fType)
                                                val q = draft.quantity.toDoubleOrNull() ?: 0.0
                                                val p = newPrice.toDoubleOrNull() ?: 0.0
                                                itemsList[index] = draft.copy(
                                                    fuelType = fType,
                                                    price = newPrice,
                                                    amount = q * p
                                                )
                                            }
                                        )
                                    }
                                }

                                if (itemsList.size > 1) {
                                    IconButton(
                                        onClick = { itemsList.removeAt(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = draft.quantity,
                                    onValueChange = { newQ ->
                                        val q = newQ.toDoubleOrNull() ?: 0.0
                                        val p = draft.price.toDoubleOrNull() ?: 0.0
                                        itemsList[index] = draft.copy(
                                            quantity = newQ,
                                            amount = q * p
                                        )
                                    },
                                    label = { Text(if (language == "bn") "পরিমাণ (লিটার)" else "Qty (L)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("item_quantity_$index"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = draft.price,
                                    onValueChange = { newP ->
                                        val q = draft.quantity.toDoubleOrNull() ?: 0.0
                                        val p = newP.toDoubleOrNull() ?: 0.0
                                        itemsList[index] = draft.copy(
                                            price = newP,
                                            amount = q * p
                                        )
                                    },
                                    label = { Text(if (language == "bn") "দর (৳/লিটার)" else "Rate (৳/L)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .padding(top = 8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = if (language == "bn") "আইটেম মোট" else "Item Total",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val q = draft.quantity.toDoubleOrNull() ?: 0.0
                                    val p = draft.price.toDoubleOrNull() ?: 0.0
                                    Text(
                                        text = AppStrings.formatCurrency(q * p, language),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = FuelGreenPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (language == "bn") "মন্তব্য / চালান রেফারেন্স" else "Notes / Memo Ref") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Grand Total Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCashSale) PaidGreen.copy(alpha = 0.12f) else DueRed.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == "bn") "সর্বমোট টাকার পরিমাণ:" else "Grand Total Amount:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = AppStrings.formatCurrency(grandTotal, language),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = if (isCashSale) PaidGreen else DueRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (language == "bn") "বাতিল" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (!isCashSale && selectedCustomerId == null) {
                                return@Button
                            }
                            onSubmit(
                                if (isCashSale) selectedCustomerId else selectedCustomerId,
                                itemsList.toList(),
                                System.currentTimeMillis(),
                                notes,
                                isCashSale
                            )
                            onDismiss()
                        },
                        modifier = Modifier.testTag("submit_sale_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCashSale) PaidGreen else DueRed
                        )
                    ) {
                        Text(
                            text = if (isCashSale) {
                                if (language == "bn") "নগদ বিক্রি সম্পন্ন করুন" else "Complete Cash Sale"
                            } else {
                                if (language == "bn") "বাকি বিক্রি সংরক্ষণ করুন" else "Save Credit Sale"
                            }
                        )
                    }
                }
            }
        }
    }

    // Customer Picker Sub-Dialog
    if (showCustomerPicker) {
        Dialog(onDismissRequest = { showCustomerPicker = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == "bn") "গ্রাহক খুঁজুন" else "Select Customer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customerSearchQuery,
                        onValueChange = { customerSearchQuery = it },
                        placeholder = { Text(if (language == "bn") "নাম, মোবাইল বা গাড়ির নম্বর..." else "Search name, phone, vehicle...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val filtered = customers.filter {
                        val q = customerSearchQuery.lowercase()
                        it.customer.name.lowercase().contains(q) ||
                                it.customer.phone.contains(q) ||
                                it.customer.vehicleReg.lowercase().contains(q)
                    }

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filtered) { item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCustomerId = item.customer.id
                                        showCustomerPicker = false
                                    }
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedCustomerId == item.customer.id) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = item.customer.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${item.customer.phone} ${if (item.customer.vehicleReg.isNotBlank()) "• ${item.customer.vehicleReg}" else ""}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = AppStrings.formatCurrency(item.currentBalance, language),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (item.currentBalance > 0) DueRed else PaidGreen
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

// Dialog: Receive Payment
@Composable
fun ReceivePaymentDialog(
    customers: List<CustomerSummary>,
    preselectedCustomerId: Long? = null,
    language: String = "bn",
    onDismiss: () -> Unit,
    onSubmit: (customerId: Long, amount: Double, method: PaymentMethod, reference: String, notes: String) -> Unit
) {
    var selectedCustomerId by remember { mutableStateOf<Long?>(preselectedCustomerId ?: customers.firstOrNull()?.customer?.id) }
    var amountStr by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var reference by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showOverpaymentWarning by remember { mutableStateOf(false) }

    val selectedCustomer = customers.find { it.customer.id == selectedCustomerId }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (language == "bn") "জমা গ্রহণ (Receive Payment)" else "Receive Payment",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (language == "bn") "বকেয়া আদায় ও খতিয়ানে জমা" else "Collect due & credit customer ledger",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Customer banner
                Text(
                    text = if (language == "bn") "গ্রাহক নির্বাচন *" else "Select Customer *",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = selectedCustomer?.customer?.name ?: "গ্রাহক বাছাই করুন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = selectedCustomer?.customer?.phone ?: "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (language == "bn") "বর্তমান বাকি" else "Current Due",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = AppStrings.formatCurrency(selectedCustomer?.currentBalance ?: 0.0, language),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DueRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(if (language == "bn") "জমার পরিমাণ (৳) *" else "Payment Amount (৳) *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                // Quick amount shortcuts
                if (selectedCustomer != null && selectedCustomer.currentBalance > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { amountStr = selectedCustomer.currentBalance.toString() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (language == "bn") "সম্পূর্ণ বাকি" else "Full Due", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { amountStr = "1000" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("৳ ১,০০০", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { amountStr = "5000" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("৳ ৫,০০০", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Method
                Text(
                    text = if (language == "bn") "পেমেন্ট মাধ্যম (Payment Method) *" else "Payment Method *",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaymentMethod.entries.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(AppStrings.getPaymentMethodName(method, language), fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text(if (language == "bn") "রেফারেন্স / TrxID / চেক নং" else "Reference / TrxID / Cheque No.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (language == "bn") "মন্তব্য / নোট" else "Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (language == "bn") "বাতিল" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (amount <= 0 || selectedCustomerId == null) return@Button

                            val curDue = selectedCustomer?.currentBalance ?: 0.0
                            if (amount > curDue && curDue > 0) {
                                // Overpayment warning
                                showOverpaymentWarning = true
                            } else {
                                onSubmit(
                                    selectedCustomerId!!,
                                    amount,
                                    selectedMethod,
                                    reference,
                                    notes
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("submit_payment_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PaidGreen)
                    ) {
                        Text(if (language == "bn") "জমা গ্রহণ সম্পন্ন করুন" else "Receive Payment")
                    }
                }
            }
        }
    }

    // Overpayment Confirmation
    if (showOverpaymentWarning) {
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val curDue = selectedCustomer?.currentBalance ?: 0.0
        val advance = amount - curDue

        AlertDialog(
            onDismissRequest = { showOverpaymentWarning = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = OctaneColor) },
            title = {
                Text(
                    text = if (language == "bn") "অগ্রিম জমা নিশ্চিতকরণ" else "Advance Payment Confirmation",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (language == "bn") {
                        "পরিশোধের পরিমাণ (${AppStrings.formatCurrency(amount, language)}) গ্রাহকের বর্তমান বকেয়া (${AppStrings.formatCurrency(curDue, language)}) থেকে বেশি। আপনি কি ${AppStrings.formatCurrency(advance, language)} অতিরিক্ত অর্থ অগ্রিম হিসেবে গ্রহণ করতে চান?"
                    } else {
                        "Payment amount (${AppStrings.formatCurrency(amount, language)}) is higher than current due (${AppStrings.formatCurrency(curDue, language)}). Do you want to accept ${AppStrings.formatCurrency(advance, language)} as advance credit?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOverpaymentWarning = false
                        onSubmit(
                            selectedCustomerId!!,
                            amount,
                            selectedMethod,
                            reference,
                            notes
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PaidGreen)
                ) {
                    Text(if (language == "bn") "হ্যাঁ, অগ্রিম গ্রহণ করুন" else "Yes, Accept Advance")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverpaymentWarning = false }) {
                    Text(if (language == "bn") "সংশোধন করুন" else "Adjust Amount")
                }
            }
        )
    }
}

// Dialog: Printable & Shareable Receipt
@Composable
fun ReceiptDialog(
    transactionWithDetails: TransactionWithDetails,
    settings: StationSettings,
    language: String = "bn",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val tx = transactionWithDetails.transaction

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Receipt Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = settings.stationName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = settings.stationAddress,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "ফোন: ${settings.stationPhone}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (tx.type) {
                                    TransactionType.CREDIT_SALE -> DueRed.copy(alpha = 0.15f)
                                    TransactionType.CASH_SALE -> PaidGreen.copy(alpha = 0.15f)
                                    TransactionType.PAYMENT -> PaidGreen.copy(alpha = 0.15f)
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (tx.type) {
                                TransactionType.CREDIT_SALE -> if (language == "bn") "বাকি বিক্রির চালান (Credit Memo)" else "Credit Sale Memo"
                                TransactionType.CASH_SALE -> if (language == "bn") "নগদ বিক্রির রশিদ (Cash Memo)" else "Cash Memo"
                                TransactionType.PAYMENT -> if (language == "bn") "অর্থ জমার রশিদ (Money Receipt)" else "Payment Receipt"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = when (tx.type) {
                                TransactionType.CREDIT_SALE -> DueRed
                                else -> PaidGreen
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Metadata Rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (language == "bn") "রশিদ নং:" else "Receipt No:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = tx.receiptNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (language == "bn") "তারিখ:" else "Date:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = AppStrings.formatDate(tx.timestamp, language), fontSize = 12.sp)
                }

                if (transactionWithDetails.customerName != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (language == "bn") "গ্রাহক:" else "Customer:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = transactionWithDetails.customerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                if (transactionWithDetails.customerPhone != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (language == "bn") "মোবাইল:" else "Mobile:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = transactionWithDetails.customerPhone, fontSize = 12.sp)
                    }
                }

                if (tx.paymentMethod != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (language == "bn") "মাধ্যম:" else "Method:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = AppStrings.getPaymentMethodName(tx.paymentMethod, language),
                            fontSize = 12.sp
                        )
                    }
                }

                if (tx.reference.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (language == "bn") "রেফারেন্স:" else "Reference:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = tx.reference, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Itemized table if items exist
                if (transactionWithDetails.items.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (language == "bn") "আইটেম" else "Item",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1.5f)
                        )
                        Text(
                            text = if (language == "bn") "লিটার" else "Qty",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (language == "bn") "দর" else "Rate",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (language == "bn") "মোট" else "Total",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    transactionWithDetails.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = AppStrings.getFuelName(item.fuelType, language),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1.5f)
                            )
                            Text(
                                text = "${AppStrings.formatNumber(item.quantityLiters, language)}L",
                                fontSize = 12.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = AppStrings.formatNumber(item.pricePerLiter, language),
                                fontSize = 12.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = AppStrings.formatNumber(item.totalAmount, language),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Total Amount Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == "bn") "মোট টাকা:" else "Total Amount:",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = AppStrings.formatCurrency(tx.totalAmount, language),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = when (tx.type) {
                            TransactionType.CREDIT_SALE -> DueRed
                            else -> PaidGreen
                        }
                    )
                }

                if (tx.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "নোট: ${tx.notes}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cashier signature
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.width(90.dp).height(1.dp).background(Color.Gray))
                        Text(if (language == "bn") "গ্রাহকের স্বাক্ষর" else "Customer Sign", fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.width(90.dp).height(1.dp).background(Color.Gray))
                        Text(if (language == "bn") "ক্যাশিয়ারের স্বাক্ষর" else "Cashier Sign", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Share & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareBody = buildString {
                                appendLine("--- ${settings.stationName} ---")
                                appendLine("রশিদ নং: ${tx.receiptNumber}")
                                appendLine("তারিখ: ${AppStrings.formatDate(tx.timestamp, language)}")
                                if (transactionWithDetails.customerName != null) {
                                    appendLine("গ্রাহক: ${transactionWithDetails.customerName}")
                                }
                                appendLine("ধরন: ${AppStrings.getTransactionTypeName(tx.type, language)}")
                                appendLine("মোট টাকা: ${AppStrings.formatCurrency(tx.totalAmount, language)}")
                                if (tx.notes.isNotBlank()) appendLine("নোট: ${tx.notes}")
                                appendLine("ধন্যবাদ!")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Fuel Receipt: ${tx.receiptNumber}")
                                putExtra(Intent.EXTRA_TEXT, shareBody)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Receipt"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (language == "bn") "শেয়ার করুন" else "Share")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = FuelGreenPrimary)
                    ) {
                        Text(if (language == "bn") "ঠিক আছে" else "Close")
                    }
                }
            }
        }
    }
}
