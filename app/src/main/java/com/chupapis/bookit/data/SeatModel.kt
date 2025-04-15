package com.chupapis.bookit.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset

data class SeatDrawable(
    val id: Int,
    val level: String,
    val x: Int,
    val y: Int,
    val isTaken: Boolean
)

@Composable
fun SeatMap(seatDrawables: List<SeatDrawable>, seatSize: Int = 10) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        seatDrawables.forEach { seat ->
            SeatItem(seatDrawable = seat, seatSize = seatSize)
        }
    }
}

@Composable
fun SeatItem(seatDrawable: SeatDrawable, seatSize: Int) {
    Box(
        modifier = Modifier
            .offset { IntOffset(seatDrawable.x * seatSize, seatDrawable.y * seatSize) }
            .size(seatSize.dp)
            .background(
                color = if (seatDrawable.isTaken) Color.Gray else Color.Green,
                shape = RoundedCornerShape(4.dp)
            )
            .alpha(if (seatDrawable.isTaken) 0.5f else 1f) // Если занято — делаем полупрозрачным
    )
}

@Preview
@Composable
fun PreviewSeats() {
    val seatDrawables = listOf(
        SeatDrawable(1, "A", 10, 10, false),
        SeatDrawable(2, "A", 10, 30, true),
        SeatDrawable(3, "A", 30, 100, false),
        SeatDrawable(4, "B", 55, 50, true),
        SeatDrawable(5, "B", 15, 70, false),
    )

    SeatMap(seatDrawables)
}
