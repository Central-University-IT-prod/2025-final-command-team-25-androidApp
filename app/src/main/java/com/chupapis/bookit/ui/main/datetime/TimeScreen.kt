package com.chupapis.bookit.ui.main.datetime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chupapis.bookit.ui.viewmodules.MapViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class TimeSlot(
    val time: LocalDateTime,
    val isBlocked: Boolean = false
)

fun findAllowedRange(
    selectedTime: LocalDateTime,
    timeSlots: List<TimeSlot>
): Pair<LocalDateTime, LocalDateTime> {
    val sorted = timeSlots.sortedBy { it.time }
    val index = sorted.indexOfFirst { it.time == selectedTime }
    var lowerBound = sorted.first().time
    var upperBound = sorted.last().time
    for (i in index downTo 0) {
        if (sorted[i].isBlocked) {
            lowerBound = if (i + 1 < sorted.size) sorted[i + 1].time else selectedTime
            break
        }
    }

    for (i in index until sorted.size) {
        if (sorted[i].isBlocked) {
            upperBound = if (i - 1 >= 0) sorted[i - 1].time else selectedTime
            break
        }
    }

    return Pair(lowerBound, upperBound)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeScreen(
    mapViewModel: MapViewModel,
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
            blockedSlots = listOf(
                LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 30)),
                LocalDateTime.of(LocalDate.now(), LocalTime.of(15, 0))
            )
        )
    }

    val allowedRange = selectedStartTime?.let { findAllowedRange(it, timeSlots) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Выберите интервал времени") }) },
        bottomBar = {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = selectedStartTime != null && selectedEndTime != null,
                onClick = {
                    println("Selected start time: $selectedStartTime")
                    println("Selected end time: $selectedEndTime")

                    mapViewModel.startDateTime = selectedStartTime!!
                    mapViewModel.endDateTime = selectedEndTime!!

                    onSave()
                }
            ) {
                Text("Сохранить")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            item {
                Text(
                    text = "Выберите два времени. Диапазон ограничен ближайшими заблокированными слотами.",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            item {
                Text(
                    mapViewModel.startDateTime!!.toLocalDate().toString()
                )
            }

            items(12) { i ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 0.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    for (j in 0..3) {
                        TimeSlotCard(
                            slot = timeSlots[i * 4 + j],
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
                            i * 4 + j
                        )
                    }
                }
            }

            item {
                Text(
                    mapViewModel.startDateTime!!.plusDays(1).toLocalDate().toString()
                )
            }


            items(12) { i ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 0.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    for (j in 0..3) {
                        TimeSlotCard(
                            slot = timeSlots[i * 4 + j + 48],
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
                            i * 4 + j + 48
                        )
                    }
                }
            }
        }

    }
}


@Composable
fun TimeSlotCard(
    slot: TimeSlot,
    selectedFirstTime: LocalDateTime?,
    selectedSecondTime: LocalDateTime?,
    allowedRange: Pair<LocalDateTime, LocalDateTime>?,
    onTimeSelected: (LocalDateTime) -> Unit,
    index: Int
) {
    val selectedStartTime: LocalDateTime?
    val selectedEndTime: LocalDateTime?

    if (selectedFirstTime != null && selectedSecondTime != null) {
        selectedStartTime =
            if (selectedFirstTime < selectedSecondTime) selectedFirstTime else selectedSecondTime
        selectedEndTime =
            if (selectedFirstTime < selectedSecondTime) selectedSecondTime else selectedFirstTime
    } else {
        selectedStartTime = selectedFirstTime
        selectedEndTime = selectedSecondTime
    }

    val (lowerBound, upperBound) = allowedRange ?: (null to null)

    val hardBlocked = slot.isBlocked
    val userBlocked = !hardBlocked && allowedRange != null &&
            (slot.time < lowerBound!! || slot.time > upperBound!!)

    val isStart = selectedStartTime == slot.time
    val isEnd = selectedEndTime == slot.time
    val isInRange = isTimeInRange(slot.time, selectedStartTime, selectedEndTime)

    val backgroundColor = when {
        hardBlocked -> Color(0xFF9E9E9E)
        userBlocked -> Color.LightGray
        isStart || isEnd -> MaterialTheme.colorScheme.primary
        isInRange -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        hardBlocked -> Color(0xFF616161)
        userBlocked -> Color.Gray
        isStart || isEnd -> MaterialTheme.colorScheme.onPrimary
        isInRange -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    var needRightRound = false
    var needLeftRound = false

    if (isInRange && index % 4 == 0) {
        needLeftRound = true
    } else if (isInRange && index % 4 == 3) {
        needRightRound = true
    }

    if (isStart) {
        needLeftRound = true
    } else if (isEnd) {
        needRightRound = true
    }

    Box(
        modifier = Modifier
            .width(82.dp)
            .height(48.dp)
            .let { if (!hardBlocked) it.clickable { onTimeSelected(slot.time) } else it }
            .background(
                backgroundColor,
                shape = RoundedCornerShape(
                    topStart = if (needLeftRound) 8.dp else 0.dp,
                    topEnd = if (needRightRound) 8.dp else 0.dp,
                    bottomStart = if (needLeftRound) 8.dp else 0.dp,
                    bottomEnd = if (needRightRound) 8.dp else 0.dp
                )
            ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = slot.time.toLocalTime().toString(), color = textColor)
        }
    }
}

fun handleTimeSelection(
    time: LocalDateTime,
    selectedStartTime: LocalDateTime?,
    selectedEndTime: LocalDateTime?,
    allowedRange: Pair<LocalDateTime, LocalDateTime>?,
    onSelectionChanged: (LocalDateTime?, LocalDateTime?) -> Unit
) {
    // If the same time is selected again, reset both times
    if (selectedStartTime == time || selectedEndTime == time) {
        onSelectionChanged(null, null)
        return
    }

    if (selectedStartTime != null && selectedEndTime != null) {
        onSelectionChanged(time, null)
        return
    }

    // If no start time is selected, select this time as the start time
    if (selectedStartTime == null) {
        onSelectionChanged(time, null)
        return
    }

    val (lowerBound, upperBound) = allowedRange ?: (null to null)

    // Check if the time is within the allowed range
    if (allowedRange != null && (time !in lowerBound!!..upperBound!! || time == selectedStartTime)) {
        onSelectionChanged(time, null)
        return
    }

    // Ensure the times are ordered correctly
    val newStartTime = minOf(selectedStartTime, time)
    val newEndTime = maxOf(selectedStartTime, time)

    onSelectionChanged(newStartTime, newEndTime)
}


fun isTimeInRange(
    time: LocalDateTime,
    start: LocalDateTime?,
    end: LocalDateTime?
): Boolean {
    if (start == null || end == null) return false
    val (lower, upper) = if (start <= end) start to end else end to start
    return time.isAfter(lower) && time.isBefore(upper)
}

fun generateTimeSlots(
    startHour: Int,
    endHour: Int,
    stepMinutes: Int,
    startDate: LocalDate,
    endDate: LocalDate,
    blockedSlots: List<LocalDateTime>
): List<TimeSlot> {
    val slots = mutableListOf<TimeSlot>()
    var currentTime = LocalDateTime.of(startDate, LocalTime.of(startHour, 0))

    while (currentTime <= LocalDateTime.of(startDate, LocalTime.of(endHour, stepMinutes))) {
        slots.add(TimeSlot(currentTime, currentTime in blockedSlots))
        currentTime = currentTime.plusMinutes(stepMinutes.toLong())
    }
    currentTime = LocalDateTime.of(endDate, LocalTime.of(startHour, 0))
    while (currentTime <= LocalDateTime.of(endDate, LocalTime.of(endHour, stepMinutes))) {
        slots.add(TimeSlot(currentTime, currentTime in blockedSlots))
        currentTime = currentTime.plusMinutes(stepMinutes.toLong())
    }

    println("Slots: " + slots.size)

    return slots
}
