package com.chupapis.bookit.ui.main.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chupapis.bookit.data.model.booking.BookingResponseChange

@Composable
    fun AdminBookingDetailScreen(
    booking: BookingResponseChange,
    onBack: () -> Unit,
    onSelectDateTime: () -> Unit,
    onDeleteBooking: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        BookingItem(booking = booking) { }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSelectDateTime, modifier = Modifier.fillMaxWidth()) {
            Text("Выбрать дату/время")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onDeleteBooking, modifier = Modifier.fillMaxWidth()) {
            Text("Удалить бронь")
        }
    }
}