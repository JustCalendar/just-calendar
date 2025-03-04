package com.ukraine4ever.justcalendar.holidays

import com.google.gson.annotations.SerializedName

data class PublicHoliday(
    @SerializedName("date") val date: String,
    @SerializedName("localName") val localName: String,
    @SerializedName("name") val name: String,
    @SerializedName("countryCode") val countryCode: String
)