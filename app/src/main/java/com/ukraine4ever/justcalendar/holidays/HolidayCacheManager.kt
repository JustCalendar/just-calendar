package com.ukraine4ever.justcalendar.holidays

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "holidays_cache")

class HolidayCacheManager(private val context: Context) {
    private val gson = Gson()

    private fun getHolidayKey(year: Int, locale: Locale): Preferences.Key<String> {
        return stringPreferencesKey("holidays_${year}_${locale.country}")
    }

    suspend fun saveHolidays(year: Int, locale: Locale, holidays: List<PublicHoliday>) {
        val json = gson.toJson(holidays)
        context.dataStore.edit { preferences ->
            preferences[getHolidayKey(year, locale)] = json
        }
    }

    fun getCachedHolidays(year: Int, locale: Locale): Flow<List<PublicHoliday>?> {
        return context.dataStore.data.map { preferences ->
            preferences[getHolidayKey(year, locale)]?.let { json ->
                gson.fromJson(json, Array<PublicHoliday>::class.java)?.toList()
            }
        }
    }
}