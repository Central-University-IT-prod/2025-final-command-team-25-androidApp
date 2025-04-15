package com.chupapis.bookit.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun CalendarAlertDialog(
    mapViewModel: MapViewModel,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(mapViewModel.startDateTime!!.toLocalDate()) }
    var selectedMonthYear by remember {
        mutableStateOf(LocalDate.now().withDayOfMonth(1))
    } // Изначально текущий месяц
    val today = remember { LocalDate.now() }
    val datesForMonth = remember(selectedMonthYear) {
        generateDatesForMonthWithOffset(selectedMonthYear.year, selectedMonthYear.monthValue)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            // Заголовок с навигацией по месяцам
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (selectedMonthYear.isAfter(today.withDayOfMonth(1))) {
                            selectedMonthYear = selectedMonthYear.minusMonths(1)
                        }
                    },
                    enabled = selectedMonthYear.isAfter(today.withDayOfMonth(1))
                ) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Предыдущий месяц"
                    )
                }
                Text(
                    text = "${selectedMonthYear.month.name} ${selectedMonthYear.year}",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(
                    onClick = {
                        selectedMonthYear = selectedMonthYear.plusMonths(1)
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowForward,
                        contentDescription = "Следующий месяц"
                    )
                }
            }
        },
        text = {
            // Тело диалога с выбором даты
            DateSelection(
                dates = datesForMonth,
                selectedDate = selectedDate,
                today = today,
                onDateSelected = { date ->
                    selectedDate = date
                }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    mapViewModel.updateDatesOnly(selectedDate)
                    onSave()
                },
                enabled = selectedDate != null
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

/**
 * Генерирует список дат для указанного месяца с пустыми ячейками для выравнивания первого дня недели.
 */
fun generateDatesForMonthWithOffset(year: Int, month: Int): List<LocalDate?> {
    val yearMonth = YearMonth.of(year, month)
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek.value // 1 = понедельник, 7 = воскресенье
    val daysInMonth = yearMonth.lengthOfMonth()

    val dates = mutableListOf<LocalDate?>()

    // Добавляем пустые ячейки для выравнивания первого дня
    for (i in 1 until firstDayOfMonth) {
        dates.add(null)
    }

    // Добавляем реальные дни
    for (day in 1..daysInMonth) {
        dates.add(LocalDate.of(year, month, day))
    }

    return dates
}

@Composable
fun DateSelection(
    dates: List<LocalDate?>, // Некоторые значения могут быть null для пустых ячеек
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

        // Заголовки дней недели
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

        // LazyVerticalGrid с фиксированной высотой для предотвращения бесконечных ограничений
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(top = 8.dp)
                .height(300.dp) // Фиксированная высота – можно изменить по необходимости
        ) {
            items(dates) { date ->
                if (date == null) {
                    Spacer(modifier = Modifier.size(48.dp))
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
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(text = date.dayOfMonth.toString(), color = textColor)
                        }
                    }
                }
            }
        }
    }
}

