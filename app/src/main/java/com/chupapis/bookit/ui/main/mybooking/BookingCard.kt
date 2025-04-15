package com.chupapis.bookit.ui.main.mybooking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chupapis.bookit.data.model.booking.MyBookingResponse
import com.chupapis.bookit.tools.convertIsoToCustomFormat

@Composable
fun BookingCard(booking: MyBookingResponse, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок бронирования
            Text(
                text = "Бронирование №${booking.booking_id}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Даты бронирования
            Text(
                text = "⏳ ${convertIsoToCustomFormat( booking.start_date)} - ${convertIsoToCustomFormat(booking.end_date)}",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // ID пользователя
            Text(
                text = "👤 ID Покупателя: ${booking.user_id}",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Список мест
            Column {
                Text(
                    text = "🔹 Забронированные места:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                booking.seats.forEach { seat ->
                    Text(
                        text = "- № места: ${seat.seat_id}",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                    )
                }
            }

            if (booking.invite_url != null) {
                Spacer(modifier = Modifier.height(12.dp))

                val clipboardManager = LocalClipboardManager.current
                val textToCopy = booking.invite_url

//                Text(textToCopy)

                Button(onClick = {
//                     Set the clipboard content to the desired text
                    clipboardManager.setText(AnnotatedString(textToCopy))
                },
                    modifier = Modifier.fillMaxWidth()
                    ) {
                    Text("Копировать приглашение")
                }
            }

            // Кнопка "Подробнее"
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text(text = "Подробнее", textAlign = TextAlign.Center)
            }
        }
    }
}
