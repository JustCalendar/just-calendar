package com.ukraine4ever.justcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ukraine4ever.justcalendar.calendar.CalendarDay
import com.ukraine4ever.justcalendar.calendar.CalendarEvent
import com.ukraine4ever.justcalendar.ui.theme.DaysToPaymentTheme
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DaysToPaymentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    CalendarScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun CalendarScreen(viewModel: MainViewModel) {
    val calendarState = viewModel.calendarState
    Box {
        val scrollState = rememberScrollState()
        var todayScrollPosition by remember { mutableIntStateOf(0) }

        ProgressIndicator(scrollState, viewModel.visibleYears)

        Box(
            modifier = Modifier
                .widthIn(max = 512.dp)
                .align(Alignment.TopCenter)
        ) {

            Column(modifier = Modifier.verticalScroll(scrollState)) {
                CalendarGrid(
                    days = calendarState.value,
                    scrollState = scrollState,
                    todayPosition = { todayScrollPosition = it }
                )
            }

            Column {

                Box {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(4.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                            .alpha(0.7f)
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(MaterialTheme.colorScheme.background)
                        )
                    }
                    val daysOfWeek = getLocalizedWeekdayNames(getLocale())
                    Row(modifier = Modifier.statusBarsPadding()) {
                        for (day in daysOfWeek) {
                            Text(
                                text = day,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 8.dp)
                                    .weight(1f)
                                    .background(
                                        MaterialTheme.colorScheme.onSurface
                                            .copy(alpha = 0.05f)
                                            .compositeOver(MaterialTheme.colorScheme.background),
                                        CircleShape
                                    ),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                }
            }
        }

        val scope = rememberCoroutineScope()
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 800.dp)
                .fillMaxWidth()
        ) {
            Button(
                onClick = {
                    scope.launch {
                        scrollState.scrollTo(todayScrollPosition)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .safeDrawingPadding()
                    .padding(8.dp),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {

                val icon = when {
                    (scrollState.value > todayScrollPosition) -> Icons.Default.KeyboardArrowUp
                    scrollState.value < todayScrollPosition -> Icons.Default.KeyboardArrowDown
                    else -> Icons.AutoMirrored.Default.KeyboardArrowRight
                }
                Icon(imageVector = icon, contentDescription = null)
                Text(text = stringResource(id = R.string.button_today))
            }
        }
    }
}

@Composable
private fun getLocale(): Locale =
    LocalContext.current.resources.configuration.locales[0] ?: Locale.getDefault()

@Composable
private fun ProgressIndicator(scrollState: ScrollState, visibleYears: Int) {
    val screenHeight =
        with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    LinearProgressIndicator(
        progress = {
            val oneYear = (scrollState.maxValue + screenHeight) / visibleYears
            val pr = (scrollState.value / oneYear) % 1
            pr
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CalendarGrid(
    days: List<CalendarDay>,
    scrollState: ScrollState,
    todayPosition: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 48.dp)
    ) {
        // Display first days row
        val firstDay = days.first()
        val firstDayOfWeek = getFirstDayOfWeek(getLocale())
        val daysBefore = (firstDay.date.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
        val daysInFirstWeek = 7 - daysBefore
        val firstWeekDays = days.take(daysInFirstWeek)

        var firstRowPosition by remember { mutableFloatStateOf(0F) }
        var itemPosition by remember { mutableFloatStateOf(0F) }
        Row(modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
            firstRowPosition = layoutCoordinates.positionInRoot().y
        }) {
            repeat(daysBefore) {
                Spacer(modifier = Modifier.weight(1f))
            }
            repeat(daysInFirstWeek) {
                CalendarDayCell(
                    day = firstWeekDays[it], modifier = Modifier.weight(1f)
                )
            }
        }

        LaunchedEffect(key1 = "scroll1") {
            val todayPos = itemPosition.toInt() - firstRowPosition.toInt()
            scrollState.scrollTo(todayPos)
            todayPosition(todayPos)
        }
        val today = LocalDate.now()
        // Display days in a grid
        val weeks = days.subList(daysInFirstWeek, days.size).chunked(7)
        for (week in weeks) {
            Row {
                for (day in week) {
                    val modifier = if (day.date.isEqual(today)) {
                        Modifier.onGloballyPositioned { layoutCoordinates ->
                            itemPosition = layoutCoordinates.positionInRoot().y
                        }
                    } else Modifier
                    CalendarDayCell(
                        day = day,
                        modifier = Modifier
                            .weight(1f)
                            .then(modifier)
                    )

                }
                if (week.size < 7) {
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

    }
}

@Composable
fun CalendarDayCell(
    day: CalendarDay,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf(false) }
    val isToday = day.date.isEqual(LocalDate.now())
    val bgColor = if (day.date.month.value % 2 == 1) MaterialTheme.colorScheme.onSurface.copy(
        alpha = 0.05f
    ) else Color.Transparent

    val bgShape = when (day.date.dayOfMonth) {
        1 -> RoundedCornerShape(topStart = 8.dp)
        day.date.lengthOfMonth() -> RoundedCornerShape(bottomEnd = 8.dp)
        else -> RectangleShape
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = bgShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                selected = !selected
            }
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .aspectRatio(1f)
                .alpha(if (day.date < LocalDate.now()) 0.5f else 1f)
                .then(
                    if (isToday) {
                        Modifier.background(
                            MaterialTheme.colorScheme.primary, CircleShape
                        )
                    } else if (selected) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    } else Modifier
                )
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                modifier = Modifier.align(Alignment.Center),
                style = if (day.event == CalendarEvent.Holiday) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                color = if (isToday) MaterialTheme.colorScheme.onPrimary else when (day.event) {
                    CalendarEvent.WorkingDay -> MaterialTheme.colorScheme.onSurface
                    CalendarEvent.Holiday -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
            if (day.date.dayOfMonth == 1) {
                Text(
                    text = day.date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    modifier = Modifier.align(Alignment.TopCenter),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (day.date.dayOfYear == 1) {
                    Text(
                        text = day.date.year.toString(),
                        modifier = Modifier.align(Alignment.BottomCenter),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

fun getLocalizedWeekdayNames(locale: Locale): List<String> {
    val firstDayOfWeek = getFirstDayOfWeek(locale)
    val daysOfWeek = DayOfWeek.entries
    val orderedDaysOfWeek = daysOfWeek.dropWhile { it != firstDayOfWeek } +
            daysOfWeek.takeWhile { it != firstDayOfWeek }
    return orderedDaysOfWeek.map { it.getDisplayName(TextStyle.SHORT, locale) }
}

fun getFirstDayOfWeek(locale: Locale): DayOfWeek {
    return WeekFields.of(locale).firstDayOfWeek
}
