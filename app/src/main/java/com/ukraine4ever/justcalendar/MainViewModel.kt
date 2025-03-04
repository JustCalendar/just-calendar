package com.ukraine4ever.justcalendar

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ibm.icu.util.Calendar
import com.ibm.icu.util.ULocale
import com.ukraine4ever.justcalendar.calendar.CalendarDay
import com.ukraine4ever.justcalendar.calendar.CalendarEvent
import com.ukraine4ever.justcalendar.holidays.PublicHoliday
import com.ukraine4ever.justcalendar.holidays.PublicHolidayRepository
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

class MainViewModel(context: Context) : ViewModel() {
    private val _calendarState: MutableState<List<CalendarDay>> = mutableStateOf(emptyList())
    val calendarState: State<List<CalendarDay>> = _calendarState
    val visibleYears = 3
    private val locale = Locale.getDefault()
    private val repository = PublicHolidayRepository(context)

    init {
        initCalendar(LocalDate.now()) // Change year and month as needed
    }

    private fun initCalendar(now: LocalDate) {
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
        fetchPublicHolidays(List(visibleYears) { index -> firstYear + index }, Locale.getDefault())
    }

    private fun isWorkingDay(date: LocalDate): Boolean {
        val isWeekend = isWeekend(date.dayOfWeek)
        return !isWeekend
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

    private fun fetchPublicHolidays(years: List<Int>, locale: Locale) {
        viewModelScope.launch {
            try {
                val holidaysList = mutableListOf<PublicHoliday>()
                years.forEach { year ->
                    val result = repository.getPublicHolidays(year, locale)
                    holidaysList.addAll(result)
                }
                updateCalendarWithHolidays(holidaysList)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching holidays: ${e.message}", e)
            }
        }
    }

    private fun updateCalendarWithHolidays(holidays: List<PublicHoliday>) {
        val holidayDates: List<LocalDate> = holidays.map { LocalDate.parse(it.date) }
        val newCalendarState = _calendarState.value.toMutableList()
        var listWasUpdated = false
        newCalendarState.forEachIndexed { index, day ->
            holidayDates.forEach { holiday ->
                if (day.date == holiday && day.event == CalendarEvent.WorkingDay) {
                    newCalendarState[index] =
                        newCalendarState[index].copy(event = CalendarEvent.Holiday)
                    if (!listWasUpdated) {
                        listWasUpdated = true
                    }
                }
            }
        }

        if (listWasUpdated) {
            _calendarState.value = newCalendarState
        }
    }

}

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}