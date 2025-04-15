package com.chupapis.bookit.ui.main.mybooking

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chupapis.bookit.data.network.RetrofitClient
import com.chupapis.bookit.data.repository.auth.AuthRepositoryImpl
import com.chupapis.bookit.data.repository.mybooking.MyBookingRepositoryImpl
import com.chupapis.bookit.ui.MyBookingViewModel
import org.json.JSONObject

@Composable
fun MyBookingsScreen(navController: NavController, authRepository: AuthRepositoryImpl, sharedPreferences: SharedPreferences) {
    val context = LocalContext.current
    // Получаем экземпляр ApiService и SharedPreferences для авторизации
    val apiService = RetrofitClient.getRetrofitInstance(context)

    // Создаем реализацию репозитория для my booking
    val repository = MyBookingRepositoryImpl(apiService, authRepository)
    val viewModel: MyBookingViewModel = viewModel(
        factory = MyBookingViewModel.MyBookingViewModelFactory(repository, sharedPreferences)
    )

    // Загружаем бронирования при старте экрана
    LaunchedEffect(Unit) {
        viewModel.getMyBookings()
    }

    // Наблюдаем за результатом запроса
    val bookingResult = viewModel.myBookingResult.value

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            // Заголовок "Мои брони"
            Text(
                text = "Мои брони",
                style = MaterialTheme.typography.headlineLarge, // Крупный заголовок
                fontWeight = FontWeight.Bold, // Жирный текст
                modifier = Modifier.padding(bottom = 16.dp, top = 16.dp) // Отступ вниз
            )

            if (bookingResult == null) {
                // Пока данные не загрузились – отображаем индикатор загрузки
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                bookingResult.fold(
                    onSuccess = { bookings ->
                        if (bookings.isEmpty()) {
                            Text(
                                text = "Нет бронирований",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxSize().padding(top = 32.dp),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(bookings) { booking ->
                                    BookingCard(booking = booking, onClick = {
                                        val hasOwnerSeat = booking.seats.any { seat -> seat.is_owner }
                                        if (hasOwnerSeat) {
                                            booking.seats.forEach { seat ->
                                                if (seat.is_owner) {
                                                    navController.navigate(
                                                        "qr_code/${
                                                            createJson(
                                                                booking.booking_id,
                                                                seat.seat_uuid
                                                            )
                                                        }"
                                                    )
                                                }
                                            }
                                        } else {
                                            navController.navigate(
                                                "qr_code/${
                                                    createJson(
                                                        booking.booking_id,
                                                        booking.seats[0].seat_uuid
                                                    )
                                                }"
                                            )
                                        }
                                    })
                                }
                            }
                        }
                    },
                    onFailure = { error ->
                        Text(
                            text = "Ошибка загрузки бронирований: ${error.message}",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                )
            }
        }
    }

}


fun createJson(booking_id: String, seat_uuid: String): String {
    val json = JSONObject()
    json.put("booking_id", booking_id)
    json.put("seat_uuid", seat_uuid)
    return json.toString()
}
