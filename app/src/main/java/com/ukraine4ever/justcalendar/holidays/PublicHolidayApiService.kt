package com.ukraine4ever.justcalendar.holidays

import retrofit2.http.GET
import retrofit2.http.Path

interface PublicHolidayApiService {
    @GET("api/v3/PublicHolidays/{year}/{countryCode}")
    suspend fun getPublicHolidays(
        @Path("year") year: Int,
        @Path("countryCode") countryCode: String
    ): List<PublicHoliday>
}