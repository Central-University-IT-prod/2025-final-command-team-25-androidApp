package com.chupapis.bookit.ui.main.datetime

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chupapis.bookit.ui.viewmodules.MapViewModel
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    mapViewModel: MapViewModel,
    onSave: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(mapViewModel.startDateTime!!.toLocalDate()) }
    var selectedMonthYear by remember {
        mutableStateOf(
            LocalDate.now().withDayOfMonth(1)
        )
    } // Start with the current month
    val today = remember { LocalDate.now() }

    // Generate the dates for the selected month and year
    val datesForMonth = remember(selectedMonthYear) {
        generateDatesForMonthWithOffset(selectedMonthYear.year, selectedMonthYear.monthValue)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${selectedMonthYear.month.name} ${selectedMonthYear.year}",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    // Disable going to previous month if it's the current month
                    IconButton(
                        onClick = {
                            if (selectedMonthYear.isAfter(today.withDayOfMonth(1))) {
                                selectedMonthYear =
                                    selectedMonthYear.minusMonths(1) // Go to the previous month
                            }
                        },
                        enabled = selectedMonthYear.isAfter(today.withDayOfMonth(1)) // Disable button if already on current month
                    ) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Previous Month"
                        )
                    }

                    IconButton(onClick = {
                        selectedMonthYear = selectedMonthYear.plusMonths(1) // Go to the next month
                    }) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowForward,
                            contentDescription = "Next Month"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            DateSelection(
                dates = datesForMonth,
                selectedDate = selectedDate,
                today = today,
                onDateSelected = { date ->

                    selectedDate = date
                }
            )

            Spacer(modifier = Modifier.weight(1f)) // Spacer to push the Save button to the bottom

            // Save button
            Button(
                onClick = {
                    mapViewModel.updateDatesOnly(selectedDate)

                    onSave()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = selectedDate != null // Enable only if a date is selected
            ) {
                Text("Save")
            }
        }
    }
}

/**
 * Generates all dates for the given month with empty "offset" days at the start to align correctly to the day of the week.
 */
fun generateDatesForMonthWithOffset(year: Int, month: Int): List<LocalDate?> {
    val yearMonth = YearMonth.of(year, month)
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek.value // 1 = Monday, 7 = Sunday
    val daysInMonth = yearMonth.lengthOfMonth()

    val dates = mutableListOf<LocalDate?>()

    // Add offset empty days to align first day correctly
    for (i in 1 until firstDayOfMonth) {
        dates.add(null) // Empty cell for offset
    }

    // Add actual days
    for (day in 1..daysInMonth) {
        dates.add(LocalDate.of(year, month, day))
    }

    return dates
}

@Composable
fun DateSelection(
    dates: List<LocalDate?>, // Some may be null for offset days
    selectedDate: LocalDate?,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Выберите дату:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Day-of-week headers
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7), // 7 days in a week
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            items(dates) { date ->
                if (date == null) {
                    Spacer(modifier = Modifier.size(width = 48.dp, height = 48.dp))
                } else {
                    val isToday = date == today
                    val isFuture = !date.isBefore(today)
                    val isSelected = date == selectedDate

                    val backgroundColor = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.secondary
                        isFuture -> MaterialTheme.colorScheme.surface
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    val textColor = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isToday -> MaterialTheme.colorScheme.onSecondary
                        isFuture -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Card(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(enabled = isFuture) { onDateSelected(date) },
                        colors = CardDefaults.cardColors(containerColor = backgroundColor)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = date.dayOfMonth.toString(), color = textColor)
                        }
                    }
                }
            }
        }
    }
}
