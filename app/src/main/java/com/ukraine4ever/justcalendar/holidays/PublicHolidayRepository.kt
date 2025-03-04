package com.ukraine4ever.justcalendar.holidays

import android.content.Context
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class PublicHolidayRepository(context: Context) {
    private val api: PublicHolidayApiService
    private val cacheManager = HolidayCacheManager(context)

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://date.nager.at/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(PublicHolidayApiService::class.java)
    }

    suspend fun getPublicHolidays(year: Int, locale: Locale): List<PublicHoliday> {
        // Fetch from cache first
        val cachedHolidays = cacheManager.getCachedHolidays(year, locale).first()
        if (cachedHolidays != null) {
            return cachedHolidays
        }

        // Fetch from API if not cached
        val holidays = api.getPublicHolidays(year, locale.country)
        cacheManager.saveHolidays(year, locale, holidays)

        return holidays
    }
}