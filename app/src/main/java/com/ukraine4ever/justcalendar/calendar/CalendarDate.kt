package com.ukraine4ever.justcalendar.calendar

import java.time.LocalDate

sealed class CalendarEvent {
    data object WorkingDay : CalendarEvent()
    data object Holiday : CalendarEvent()
}

data class CalendarDay(val date: LocalDate, val event: CalendarEvent?)