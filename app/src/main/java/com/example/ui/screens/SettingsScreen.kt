package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.StationSettings
import com.example.ui.theme.DieselColor
import com.example.ui.theme.DueRed
import com.example.ui.theme.FuelGreenPrimary
import com.example.ui.theme.OctaneColor
import com.example.ui.theme.PetrolColor

@Composable
fun SettingsScreen(
    settings: StationSettings,
    language: String = "bn",
    onUpdateStationDetails: (name: String, address: String, phone: String) -> Unit,
    onUpdateFuelPrices: (petrol: Double, octane: Double, diesel: Double) -> Unit,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onSeedDemoData: (clearExisting: Boolean) -> Unit,
    onResetAllData: () -> Unit
) {
    val context = LocalContext.current

    var stationName by remember(settings.stationName) { mutableStateOf(settings.stationName) }
    var stationAddress by remember(settings.stationAddress) { mutableStateOf(settings.stationAddress) }
    var stationPhone by remember(settings.stationPhone) { mutableStateOf(settings.stationPhone) }

    var petrolPriceStr by remember(settings.defaultPetrolPrice) { mutableStateOf(settings.defaultPetrolPrice.toString()) }
    var octanePriceStr by remember(settings.defaultOctanePrice) { mutableStateOf(settings.defaultOctanePrice.toString()) }
    var dieselPriceStr by remember(settings.defaultDieselPrice) { mutableStateOf(settings.defaultDieselPrice.toString()) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showSeedDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Column {
                Text(
                    text = if (language == "bn") "অ্যাপ সেটিংস ও কনফিগারেশন" else "Settings & Configuration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (language == "bn") "স্টেশনের তথ্য, জ্বালানি দর ও ব্যাকআপ" else "Station info, fuel rates & backup",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Station Details Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = FuelGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == "bn") "ফুয়েল স্টেশন প্রোফাইল" else "Fuel Station Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = stationName,
                        onValueChange = { stationName = it },
                        label = { Text(if (language == "bn") "স্টেশন / ব্যবসার নাম" else "Station Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = stationAddress,
                        onValueChange = { stationAddress = it },
                        label = { Text(if (language == "bn") "স্টেশনের ঠিকানা" else "Station Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = stationPhone,
                        onValueChange = { stationPhone = it },
                        label = { Text(if (language == "bn") "যোগাযোগের ফোন নম্বর" else "Contact Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onUpdateStationDetails(stationName.trim(), stationAddress.trim(), stationPhone.trim())
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = FuelGreenPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (language == "bn") "স্টেশনের তথ্য সংরক্ষণ" else "Save Station Info")
                    }
                }
            }
        }

        // Fuel Price Management Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = OctaneColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == "bn") "জ্বালানির ডিফল্ট বিক্রয় দর (প্রতি লিটার)" else "Default Fuel Rates (per Liter)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = if (language == "bn") "দর পরিবর্তন করলে পূর্বের লেনদেনের সংরক্ষিত ঐতিহাসিক রেট অপরিবর্তিত থাকবে।" else "Updating rates will preserve historical transaction rates.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = octanePriceStr,
                            onValueChange = { octanePriceStr = it },
                            label = { Text("অকটেন (৳/L)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = dieselPriceStr,
                            onValueChange = { dieselPriceStr = it },
                            label = { Text("ডিজেল (৳/L)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = petrolPriceStr,
                            onValueChange = { petrolPriceStr = it },
                            label = { Text("পেট্রোল (৳/L)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val pet = petrolPriceStr.toDoubleOrNull() ?: settings.defaultPetrolPrice
                            val oct = octanePriceStr.toDoubleOrNull() ?: settings.defaultOctanePrice
                            val die = dieselPriceStr.toDoubleOrNull() ?: settings.defaultDieselPrice
                            onUpdateFuelPrices(pet, oct, die)
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = OctaneColor)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (language == "bn") "জ্বালানির দর সংরক্ষণ" else "Save Fuel Rates")
                    }
                }
            }
        }

        // Language & Theme Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Language
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == "bn") "ভাষা পরিবর্তন (Language)" else "App Language",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.language == "bn",
                            onClick = { onLanguageChange("bn") },
                            label = { Text("বাংলা (Bengali)") }
                        )
                        FilterChip(
                            selected = settings.language == "en",
                            onClick = { onLanguageChange("en") },
                            label = { Text("English") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Theme
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == "bn") "থিম নির্বাচন (Theme)" else "Theme Selection",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.themeMode == "SYSTEM",
                            onClick = { onThemeChange("SYSTEM") },
                            label = { Text(if (language == "bn") "সিস্টেম অনুযায়ী" else "System") }
                        )
                        FilterChip(
                            selected = settings.themeMode == "LIGHT",
                            onClick = { onThemeChange("LIGHT") },
                            label = { Text(if (language == "bn") "লাইট মোড" else "Light") }
                        )
                        FilterChip(
                            selected = settings.themeMode == "DARK",
                            onClick = { onThemeChange("DARK") },
                            label = { Text(if (language == "bn") "ডার্ক মোড" else "Dark") }
                        )
                    }
                }
            }
        }

        // Backup, Restore & Sample Data
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = FuelGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == "bn") "ডাটা ব্যাকআপ ও ব্যবস্থাপনা" else "Data Backup & Maintenance",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            val backupText = "Fuel Ledger BD Offline Backup - Station: ${settings.stationName}\nDate: ${System.currentTimeMillis()}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Fuel Ledger BD Backup")
                                putExtra(Intent.EXTRA_TEXT, backupText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Export Backup"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (language == "bn") "ডাটাবেজ ব্যাকআপ এক্সপোর্ট করুন" else "Export Database Backup")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showSeedDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (language == "bn") "নমুনা টেস্ট ডাটা লোড করুন (Seed Sample)" else "Load Sample Fuel Station Data")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DueRed)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = DueRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (language == "bn") "সমস্ত ডাটা রিসেট করুন (Reset All)" else "Reset All Database Records")
                    }
                }
            }
        }

        // App Information Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Fuel Ledger BD (ফুয়েল লেজার বিডি)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (language == "bn") "ভার্সন: ১.০.০ • ১০০% অফলাইন ও নিরাপদ লোকাল স্টোরেজ" else "Version: 1.0.0 • 100% Offline & Secure Local Room Storage",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Reset Data Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = DueRed) },
            title = {
                Text(
                    text = if (language == "bn") "সমস্ত ডাটা মুছে ফেলতে চান?" else "Reset All Data?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (language == "bn") {
                        "সতর্কতা: এই পদক্ষেপটি পূর্বাবস্থায় ফিরিয়ে আনা সম্ভব নয়। সকল গ্রাহক, লেনদেন এবং খতিয়ান স্থায়ীভাবে মুছে যাবে।"
                    } else {
                        "Warning: This will permanently delete all customers, transactions and ledger history."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DueRed)
                ) {
                    Text(if (language == "bn") "হ্যাঁ, সব মুছে ফেলুন" else "Yes, Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(if (language == "bn") "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Seed Sample Data Confirmation Dialog
    if (showSeedDialog) {
        AlertDialog(
            onDismissRequest = { showSeedDialog = false },
            title = {
                Text(
                    text = if (language == "bn") "নমুনা ডাটা যোগ করবেন?" else "Seed Sample Data?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (language == "bn") {
                        "বাস্তবধর্মী ৫ জন গ্রাহক (ট্রান্সপোর্ট কোম্পানি, বাস, প্রাইভেট কার) ও তেল বিক্রির নমুনা খতিয়ান লোড করতে চান?"
                    } else {
                        "Do you want to load realistic sample customers (Transport fleet, private cars) and fuel transactions?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSeedDemoData(false)
                        showSeedDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FuelGreenPrimary)
                ) {
                    Text(if (language == "bn") "লোড করুন" else "Load Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSeedDialog = false }) {
                    Text(if (language == "bn") "বাতিল" else "Cancel")
                }
            }
        )
    }
}
