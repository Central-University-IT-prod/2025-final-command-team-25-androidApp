package com.chupapis.bookit.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chupapis.bookit.ui.main.datetime.TimeSlotCard
import com.chupapis.bookit.ui.main.datetime.findAllowedRange
import com.chupapis.bookit.ui.main.datetime.generateTimeSlots
import com.chupapis.bookit.ui.main.datetime.handleTimeSelection
import com.chupapis.bookit.ui.viewmodules.MapViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun TimeAlertDialog(
    mapViewModel: MapViewModel,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var selectedStartTime by remember { mutableStateOf(mapViewModel.startDateTime) }
    var selectedEndTime by remember { mutableStateOf(mapViewModel.endDateTime) }

    val timeSlots = remember {
        generateTimeSlots(
            startHour = 0,
            endHour = 23,
            stepMinutes = 30,
            startDate = mapViewModel.startDateTime!!.toLocalDate(),
            endDate = mapViewModel.startDateTime!!.toLocalDate().plusDays(1),
            blockedSlots = listOf()
        )
    }

    val allowedRange = selectedStartTime?.let { findAllowedRange(it, timeSlots) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите интервал времени") },
        text = {
            // Убрали Modifier.verticalScroll, чтобы не было бесконечных ограничений по высоте
            Column {
                /*Text(
                    text = "Выберите два времени. Диапазон ограничен ближайшими заблокированными слотами.",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )*/
                // Первый день
                Text(
                    text = mapViewModel.startDateTime!!.toLocalDate().toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .height(200.dp)
                        .padding(16.dp)
                ) {
                    items(timeSlots.subList(0, 48)) { slot ->
                        TimeSlotCard(
                            slot = slot,
                            selectedFirstTime = selectedStartTime,
                            selectedSecondTime = selectedEndTime,
                            allowedRange = allowedRange,
                            onTimeSelected = { time ->
                                handleTimeSelection(
                                    time,
                                    selectedStartTime,
                                    selectedEndTime,
                                    allowedRange
                                ) { newStart, newEnd ->
                                    selectedStartTime = newStart
                                    selectedEndTime = newEnd
                                }
                            },
                            index = timeSlots.indexOf(slot)
                        )
                    }
                }
                // Второй день
                Text(
                    text = mapViewModel.startDateTime!!.plusDays(1).toLocalDate().toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .height(200.dp)
                        .padding(16.dp)
                ) {
                    items(timeSlots.subList(48, timeSlots.size)) { slot ->
                        TimeSlotCard(
                            slot = slot,
                            selectedFirstTime = selectedStartTime,
                            selectedSecondTime = selectedEndTime,
                            allowedRange = allowedRange,
                            onTimeSelected = { time ->
                                handleTimeSelection(
                                    time,
                                    selectedStartTime,
                                    selectedEndTime,
                                    allowedRange
                                ) { newStart, newEnd ->
                                    selectedStartTime = newStart
                                    selectedEndTime = newEnd
                                }
                            },
                            index = timeSlots.indexOf(slot)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    mapViewModel.startDateTime = selectedStartTime!!
                    mapViewModel.endDateTime = selectedEndTime!!
                    onSave()
                },
                enabled = selectedStartTime != null && selectedEndTime != null
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
