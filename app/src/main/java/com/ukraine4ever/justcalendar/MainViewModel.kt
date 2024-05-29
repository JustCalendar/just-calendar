package com.ukraine4ever.justcalendar

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ibm.icu.util.Calendar
import com.ibm.icu.util.ULocale
import com.ukraine4ever.justcalendar.calendar.CalendarDay
import com.ukraine4ever.justcalendar.calendar.CalendarEvent
import com.ukraine4ever.justcalendar.calendar.Holidays2024
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

class MainViewModel : ViewModel() {
    private val _calendarState: MutableState<List<CalendarDay>> = mutableStateOf(emptyList())
    val calendarState: State<List<CalendarDay>> = _calendarState
    val visibleYears = 10
    private val locale = Locale.getDefault()

    init {
        updateCalendar(LocalDate.now()) // Change year and month as needed
    }

    private fun updateCalendar(now: LocalDate) {
        // Generate all days of the month with events
        val allDays: MutableList<CalendarDay> = mutableListOf()

        val firstYear = now.minusYears(visibleYears / 2.toLong()).year
        repeat(visibleYears) { yearIndex ->
            val firstDateOfYear = LocalDate.ofYearDay(firstYear + yearIndex, 1)
            val days = MutableList(firstDateOfYear.lengthOfYear()) {
                val date = firstDateOfYear.withDayOfYear(it + 1)
                CalendarDay(
                    date = date,
                    event = if (isWorkingDay(date)) {
                        CalendarEvent.WorkingDay
                    } else {
                        CalendarEvent.Holiday
                    }
                )
            }
            allDays.addAll(days)
        }

        _calendarState.value = allDays
    }

    private fun isWorkingDay(date: LocalDate): Boolean {
        val isPublicHoliday = if (Locale.getDefault().country == "UA") {
            Holidays2024.find { it.isEqual(date) } != null
        } else false
        val isWeekend = isWeekend(date.dayOfWeek)
        return !isWeekend && !isPublicHoliday
    }

    private fun isWeekend(dayOfWeek: DayOfWeek): Boolean {
        val uLocale = ULocale.forLocale(locale)
        val calendar = Calendar.getInstance(uLocale)

        val weekendStart = calendar.weekData.weekendOnset
        val weekendEnd = calendar.weekData.weekendCease

        val dayOfWeekICU = when (dayOfWeek) {
            DayOfWeek.SUNDAY -> Calendar.SUNDAY
            DayOfWeek.MONDAY -> Calendar.MONDAY
            DayOfWeek.TUESDAY -> Calendar.TUESDAY
            DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
            DayOfWeek.THURSDAY -> Calendar.THURSDAY
            DayOfWeek.FRIDAY -> Calendar.FRIDAY
            DayOfWeek.SATURDAY -> Calendar.SATURDAY
        }

        return when {
            weekendStart <= weekendEnd -> dayOfWeekICU in weekendStart..weekendEnd
            else -> dayOfWeekICU >= weekendStart || dayOfWeekICU <= weekendEnd
        }
    }

}