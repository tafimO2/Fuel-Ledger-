package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "station_settings")

data class StationSettings(
    val stationName: String = "পদ্মা ওয়েল ফুয়েল ফিলিং স্টেশন",
    val stationAddress: String = "ঢাকা-চট্টগ্রাম মহাসড়ক, কুমিল্লা",
    val stationPhone: String = "+880 1712-345678",
    val defaultPetrolPrice: Double = 125.0,
    val defaultOctanePrice: Double = 130.0,
    val defaultDieselPrice: Double = 105.0,
    val language: String = "bn", // "bn" or "en"
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val currencySymbol: String = "৳"
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val STATION_NAME = stringPreferencesKey("station_name")
        val STATION_ADDRESS = stringPreferencesKey("station_address")
        val STATION_PHONE = stringPreferencesKey("station_phone")
        val PETROL_PRICE = doublePreferencesKey("petrol_price")
        val OCTANE_PRICE = doublePreferencesKey("octane_price")
        val DIESEL_PRICE = doublePreferencesKey("diesel_price")
        val LANGUAGE = stringPreferencesKey("app_language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CURRENCY = stringPreferencesKey("currency_symbol")
    }

    val settingsFlow: Flow<StationSettings> = context.dataStore.data.map { prefs ->
        StationSettings(
            stationName = prefs[Keys.STATION_NAME] ?: "পদ্মা ওয়েল ফুয়েল ফিলিং স্টেশন",
            stationAddress = prefs[Keys.STATION_ADDRESS] ?: "ঢাকা-চট্টগ্রাম মহাসড়ক, কুমিল্লা",
            stationPhone = prefs[Keys.STATION_PHONE] ?: "+880 1712-345678",
            defaultPetrolPrice = prefs[Keys.PETROL_PRICE] ?: 125.0,
            defaultOctanePrice = prefs[Keys.OCTANE_PRICE] ?: 130.0,
            defaultDieselPrice = prefs[Keys.DIESEL_PRICE] ?: 105.0,
            language = prefs[Keys.LANGUAGE] ?: "bn",
            themeMode = prefs[Keys.THEME_MODE] ?: "SYSTEM",
            currencySymbol = prefs[Keys.CURRENCY] ?: "৳"
        )
    }

    suspend fun updateStationDetails(name: String, address: String, phone: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STATION_NAME] = name
            prefs[Keys.STATION_ADDRESS] = address
            prefs[Keys.STATION_PHONE] = phone
        }
    }

    suspend fun updateFuelPrices(petrol: Double, octane: Double, diesel: Double) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PETROL_PRICE] = petrol
            prefs[Keys.OCTANE_PRICE] = octane
            prefs[Keys.DIESEL_PRICE] = diesel
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }
}
