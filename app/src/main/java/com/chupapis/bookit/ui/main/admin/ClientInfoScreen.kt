package com.chupapis.bookit.ui.main.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chupapis.bookit.data.model.booking.BookingResponseChange
import com.chupapis.bookit.data.network.RetrofitClient
import com.chupapis.bookit.data.repository.admin.AdminRepositoryImpl
import com.chupapis.bookit.data.repository.auth.AuthRepository
import com.chupapis.bookit.ui.AdminViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientInfoScreen(
    userId: String,
    onBookingClick: (BookingResponseChange) -> Unit,
    onBack: () -> Unit,
    authRepository: AuthRepository
) {
    val context = LocalContext.current

    val viewModel: AdminViewModel = viewModel(
        factory = AdminViewModel.AdminViewModelFactory(
            AdminRepositoryImpl(
                RetrofitClient.getRetrofitInstance(context),
                authRepository = authRepository
            )
        )
    )

    // Результаты с сервера
    val clientsResult by viewModel.clientsResult.observeAsState()
    val bookingsResult by viewModel.bookingsResult.observeAsState()

    // Запрашиваем и клиентов, и брони, единоразово
    LaunchedEffect(userId) {
        viewModel.fetchAllClients()
        viewModel.fetchUserBookings(userId)
    }

    Column {
        Text(
            text = "Информация о клиенте",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.padding(16.dp, bottom = 0.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {

            // Пока один из результатов == null, значит запрос ещё не завершён
            if (clientsResult == null || bookingsResult == null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                // Проверяем – не упал ли один из запросов
                val clientsError = clientsResult?.exceptionOrNull()
                val bookingsError = bookingsResult?.exceptionOrNull()

                when {
                    clientsError != null -> {
                        Text(
                            text = "Ошибка при загрузке клиента: ${clientsError.message}",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    bookingsError != null -> {
                        Text(
                            text = "Ошибка при загрузке броней: ${bookingsError.message}",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        // Оба результата успешные — достаём
                        val clients = clientsResult!!.getOrNull().orEmpty()
                        val bookings = bookingsResult!!.getOrNull().orEmpty()

                        val currentClient = clients.firstOrNull { it.client_id == userId }

                        if (currentClient == null) {
                            Text(
                                text = "Пользователь с ID = $userId не найден",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            // Теперь у нас есть и клиент, и список его броней
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding()
                            ) {
                                item {
                                    // Ваш Composable для профиля
                                    ClientProfileView(
                                        client = currentClient,
                                        onUserUpdate = { request ->
                                            viewModel.changeUser(currentClient.client_id, request)
                                        }
                                    )

                                    Spacer(Modifier.height(24.dp))
                                }

                                item {
                                    Text(
                                        "Список броней",
                                        style = MaterialTheme.typography.headlineSmall,
                                        modifier = Modifier.padding(16.dp, bottom = 0.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))

                                    if (bookings.isEmpty()) {
                                        Text("Нет броней")

                                    }
                                }
                                items(bookings) { booking ->
                                    BookingItem(
                                        booking = booking,
                                        onClick = {
                                            onBookingClick(booking)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

