package com.chupapis.bookit.ui.main.admin

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chupapis.bookit.R
import com.chupapis.bookit.data.model.admins.ChangeUserRequest
import com.chupapis.bookit.data.model.admins.ClientResponse
import com.chupapis.bookit.data.model.admins.UpdateBookingSeatRequest
import com.chupapis.bookit.data.model.booking.BookingResponse
import com.chupapis.bookit.data.model.booking.BookingResponseChange
import com.chupapis.bookit.data.network.RetrofitClient
import com.chupapis.bookit.data.repository.admin.AdminRepositoryImpl
import com.chupapis.bookit.data.repository.adminbooking.BookingRepositoryImpl
import com.chupapis.bookit.data.repository.auth.AuthRepository
import com.chupapis.bookit.tools.convertIsoToCustomFormat
import com.chupapis.bookit.ui.AdminViewModel
import com.chupapis.bookit.ui.main.datetime.DateSelection
import com.chupapis.bookit.ui.main.datetime.TimeSlotCard
import com.chupapis.bookit.ui.main.datetime.findAllowedRange
import com.chupapis.bookit.ui.main.datetime.generateDatesForMonthWithOffset
import com.chupapis.bookit.ui.main.datetime.generateTimeSlots
import com.chupapis.bookit.ui.main.datetime.handleTimeSelection
import com.chupapis.bookit.ui.main.profile.UserStatus
import com.chupapis.bookit.ui.viewmodules.BookingViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(authRepository: AuthRepository) {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(
        factory = AdminViewModel.AdminViewModelFactory(
            AdminRepositoryImpl(
                RetrofitClient.getRetrofitInstance(context),
                authRepository = authRepository
            )
        )
    )

    val bookingViewModel: BookingViewModel = viewModel(
        factory = BookingViewModel.BookingViewModelFactory(
            BookingRepositoryImpl(
                RetrofitClient.getRetrofitInstance(context),
                authRepository = authRepository
            )
        )
    )

    val clientsResult by viewModel.clientsResult.observeAsState()
    val bookingsResult by viewModel.bookingsResult.observeAsState()
    val deleteUserResult by viewModel.deleteUserResult.observeAsState()

    var selectedUser by remember { mutableStateOf<ClientResponse?>(null) }
    // Новый стейт для выбранного бронирования
    var selectedBooking by remember { mutableStateOf<BookingResponseChange?>(null) }
    var clients by remember { mutableStateOf<List<ClientResponse>>(emptyList()) }
    var clientToDelete by remember { mutableStateOf<ClientResponse?>(null) }
    var deletionUserId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    // Новые состояния для диалогов работы с бронями
    var showBookingUpdateDialog by remember { mutableStateOf(false) }
    var showBookingDeleteDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Начальная загрузка данных
    LaunchedEffect(Unit) {
        viewModel.fetchAllClients()
    }

    // Обновление списка клиентов с фильтрацией админов
    LaunchedEffect(clientsResult) {
        clientsResult?.getOrNull()?.let { list ->
            clients = list.filter { client ->
                client.access_level != "ADMIN"
            }
        }
    }

    // Обработка результата удаления пользователя
    LaunchedEffect(deleteUserResult) {
        deleteUserResult?.onSuccess {
            deletionUserId?.let { id ->
                clients = clients.filter { it.client_id != id }
            }
            deletionUserId = null
        }?.onFailure { error ->
            Toast.makeText(
                context,
                "Ошибка при удалении пользователя: ${error.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Обновляем список клиентов после редактирования
    LaunchedEffect(selectedUser) {
        selectedUser?.let {
            viewModel.fetchAllClients()
        }
    }

    // Отслеживание результата удаления брони
    val deleteBookingResult by bookingViewModel.deleteBookingResult.observeAsState()
    LaunchedEffect(deleteBookingResult) {
        deleteBookingResult?.onSuccess {
            selectedBooking = null
            selectedUser?.let { user ->
                viewModel.fetchUserBookings(user.client_id)
            }
        }?.onFailure { error ->
            Toast.makeText(
                context,
                "Ошибка при удалении брони: ${error.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Отслеживание результата обновления брони
    val updateBookingResult by bookingViewModel.updateSeatBookingResult.observeAsState()
    LaunchedEffect(updateBookingResult) {
        updateBookingResult?.onSuccess {
            // После успешного обновления – обновляем список броней
            selectedUser?.let { user ->
                viewModel.fetchUserBookings(user.client_id)
            }
            showBookingUpdateDialog = false
        }?.onFailure { error ->
            Toast.makeText(
                context,
                "Ошибка при переносе брони: ${error.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when {
                        selectedBooking != null -> Text("Детали бронирования")
                        selectedUser != null -> Text("Профиль: ${selectedUser!!.username}")
                        else -> Text("Список клиентов")
                    }
                },
                navigationIcon = {
                    when {
                        selectedBooking != null -> {
                            IconButton(onClick = { selectedBooking = null }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Назад"
                                )
                            }
                        }
                        selectedUser != null -> {
                            IconButton(onClick = { selectedUser = null }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Назад"
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Экран деталей бронирования
                selectedBooking != null -> {
                    AdminBookingDetailScreen(
                        booking = selectedBooking!!,
                        onBack = { selectedBooking = null },
                        onSelectDateTime = { showBookingUpdateDialog = true },
                        onDeleteBooking = { showBookingDeleteDialog = true }
                    )
                }
                // Экран профиля клиента с его бронями
                selectedUser != null -> {
                    LaunchedEffect(selectedUser) {
                        viewModel.fetchUserBookings(selectedUser!!.client_id)
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            ClientProfileView(
                                client = selectedUser!!,
                                onUserUpdate = { request ->
                                    viewModel.changeUser(
                                        userId = selectedUser!!.client_id,
                                        request = request
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Брони",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        when {
                            bookingsResult == null -> {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                            bookingsResult!!.isSuccess -> {
                                val bookings = bookingsResult!!.getOrNull() ?: emptyList()
                                if (bookings.isNotEmpty()) {
                                    val sortedBookings = bookings.sortedByDescending { Instant.parse(it.start_date) }
                                    items(sortedBookings) { booking ->
                                        BookingItem(booking = booking, onClick = { selectedBooking = booking })
                                    }
                                } else {
                                    item {
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = "Нет броней",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "Ошибка: ${bookingsResult!!.exceptionOrNull()?.message}",
                                            color = Color.Red,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // Экран списка клиентов
                else -> {
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(isRefreshing),
                        onRefresh = {
                            coroutineScope.launch {
                                isRefreshing = true
                                viewModel.fetchAllClients()
                                kotlinx.coroutines.delay(500)
                                isRefreshing = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            item {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    label = { Text("Поиск по логину или почте") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                )
                            }
                            val filteredClients = if (searchQuery.isNotEmpty()) {
                                clients.filter { client ->
                                    client.username.contains(searchQuery, ignoreCase = true) ||
                                            client.email.contains(searchQuery, ignoreCase = true)
                                }
                            } else {
                                clients
                            }
                            items(filteredClients.reversed(), key = { it.client_id }) { client ->
                                var isSwiped by remember { mutableStateOf(false) }
                                val offsetX = remember { Animatable(0f) }

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (isSwiped) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(end = 16.dp),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = {
                                                clientToDelete = client
                                                showDeleteDialog = true
                                            }) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Удалить",
                                                    tint = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                                            .pointerInput(Unit) {
                                                detectHorizontalDragGestures(
                                                    onDragEnd = {
                                                        coroutineScope.launch {
                                                            if (offsetX.value < -100) {
                                                                isSwiped = true
                                                                offsetX.animateTo(-200f)
                                                            } else {
                                                                offsetX.animateTo(0f)
                                                                isSwiped = false
                                                            }
                                                        }
                                                    },
                                                    onHorizontalDrag = { _, dragAmount ->
                                                        coroutineScope.launch {
                                                            val newOffset =
                                                                (offsetX.value + dragAmount).coerceIn(-200f, 0f)
                                                            offsetX.snapTo(newOffset)
                                                        }
                                                    }
                                                )
                                            }
                                    ) {
                                        ClientItem(client) { selectedUser = client }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог удаления пользователя (для клиентов)
    if (showDeleteDialog && clientToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                clientToDelete = null
            },
            title = { Text("Удаление пользователя") },
            text = { Text("Вы уверены, что хотите удалить ${clientToDelete?.username}?") },
            confirmButton = {
                TextButton(onClick = {
                    deletionUserId = clientToDelete?.client_id
                    deletionUserId?.let { viewModel.deleteUser(it) }
                    clientToDelete = null
                    showDeleteDialog = false
                }) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    clientToDelete = null
                }) {
                    Text("Нет")
                }
            }
        )
    }

    // Диалог переноса брони (объединённый выбор даты и времени)
    if (showBookingUpdateDialog && selectedBooking != null) {
        BookingTransferDialog(
            booking = selectedBooking!!,
            bookingViewModel = bookingViewModel,
            onDismiss = { showBookingUpdateDialog = false }
        )
    }

    // Диалог подтверждения удаления брони
    if (showBookingDeleteDialog && selectedBooking != null) {
        AlertDialog(
            onDismissRequest = { showBookingDeleteDialog = false },
            title = { Text("Удаление брони") },
            text = { Text("Вы уверены, что хотите удалить бронь с ID: ${selectedBooking!!.booking_id}?") },
            confirmButton = {
                TextButton(onClick = {
                    bookingViewModel.deleteBookingSeat(selectedBooking!!.booking_id)
                    showBookingDeleteDialog = false
                }) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookingDeleteDialog = false }) {
                    Text("Нет")
                }
            }
        )
    }
}

/*@Composable
fun BookingDetailScreen(
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
        // Кнопка для выбора новой даты/времени
        Button(onClick = onSelectDateTime, modifier = Modifier.fillMaxWidth()) {
            Text("Выбрать дату/время")
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Кнопка для удаления брони
        Button(onClick = onDeleteBooking, modifier = Modifier.fillMaxWidth()) {
            Text("Удалить бронь")
        }
    }
}*/

@Composable
    fun ClientItem(client: ClientResponse, onClick: () -> Unit) {
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
            // Имя пользователя
            Text(
                text = client.username,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Email
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = client.email,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Уровень доступа
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Access Level",
                    tint = Color(0xFF6200EE),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Доступ: ${client.access_level}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
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


@Composable
fun ClientProfileView(
    client: ClientResponse,
    onUserUpdate: (ChangeUserRequest) -> Unit
) {
    var username by remember { mutableStateOf(client.username) }
    var accessLevel by remember { mutableStateOf(client.access_level) }
    var verificationLevel by remember { mutableStateOf(client.access_level) }

    var passportData by remember { mutableStateOf("") }
    var passportSeries by remember { mutableStateOf("") }
    var passportNumber by remember { mutableStateOf("") }

    var showAccessDialog by remember { mutableStateOf(false) }

    val accessLevels = listOf("GUEST", "STUDENT", "ADMIN")
    val verificationLevels = listOf("AVAILABLE", "STANDARD", "PRO")

    val onAccessLevelSelected: (String) -> Unit = { selectedLevel ->
        accessLevel = selectedLevel
        showAccessDialog = false
    }

    val accessLevelColor = when (accessLevel) {
        "ADMIN" -> Color.Red
        "STUDENT" -> Color(0xFFD3C459)
        "GUEST" -> Color.Gray
        else -> Color.Transparent
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = username,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .background(accessLevelColor, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { showAccessDialog = true }
        ) {
            Text(
                text = accessLevel,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Уровень верификации: $verificationLevel",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (verificationLevel != "PRO") {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = passportSeries,
                onValueChange = { passportSeries = it },
                label = { Text("Серия паспорта") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = passportNumber,
                onValueChange = { passportNumber = it },
                label = { Text("Номер паспорта") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = passportData,
                onValueChange = { passportData = it },
                label = { Text("Имя Фамилия Отчество") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            onUserUpdate(
                ChangeUserRequest(
                    username = username,
                    access_level = accessLevel,
                    verification_level = verificationLevel
                )
            )
        }) {
            Text("Сохранить изменения")
        }
    }

    if (showAccessDialog) {
        AlertDialog(
            onDismissRequest = { showAccessDialog = false },
            title = { Text("Выберите уровень доступа") },
            text = {
                Column {
                    accessLevels.forEach { level ->
                        TextButton(onClick = { onAccessLevelSelected(level) }) {
                            Text(level)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccessDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun BookingItem(booking: BookingResponseChange, onClick: () -> Unit) {
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

            // Период бронирования
            Text(
                text = "⏳ ${convertIsoToCustomFormat(booking.start_date)} - ${convertIsoToCustomFormat(booking.end_date)}",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Список мест (с ID и UUID)
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
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

            // Кнопка "Подробнее"
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text(text = "Управление", textAlign = TextAlign.Center)
            }
        }
    }
    Log.d("MyLog", booking.toString())
}

@Composable
fun StatusBadge(status: UserStatus) {
    val badgeColor = when (status) {
        UserStatus.GUEST -> Color.Gray
        UserStatus.GOLD -> Color(0xFFFFD700)
        UserStatus.ADMIN -> Color.Red
        UserStatus.STUDENT -> Color(0xFFD3C459)
    }
    Box(
        modifier = Modifier
            .background(badgeColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.name,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BookingTransferDialog(
    booking: BookingResponseChange,
    bookingViewModel: BookingViewModel,
    onDismiss: () -> Unit
) {
    // Инициализация состояния для отображения этапов
    var currentStep by remember { mutableStateOf(1) } // 1 - выбор даты, 2 - выбор времени

    // Инициализация даты и времени из брони
    val initialStartDateTime = Instant.parse(booking.start_date)
        .atZone(ZoneId.systemDefault()).toLocalDateTime()
    val initialEndDateTime = Instant.parse(booking.end_date)
        .atZone(ZoneId.systemDefault()).toLocalDateTime()
    var selectedDate by remember { mutableStateOf(initialStartDateTime.toLocalDate()) }
    var selectedStartTime by remember { mutableStateOf(initialStartDateTime) }
    var selectedEndTime by remember { mutableStateOf(initialEndDateTime) }

    var selectedMonthYear by remember { mutableStateOf(selectedDate.withDayOfMonth(1)) }
    val today = LocalDate.now()
    val datesForMonth = remember(selectedMonthYear) {
        generateDatesForMonthWithOffset(selectedMonthYear.year, selectedMonthYear.monthValue)
    }

    // Генерация слотов времени для выбранной даты
    val timeSlots = remember(selectedDate) {
        generateTimeSlots(
            startHour = 0,
            endHour = 23,
            stepMinutes = 30,
            startDate = selectedDate,
            endDate = selectedDate.plusDays(1),
            blockedSlots = listOf()
        )
    }
    val allowedRange = selectedStartTime?.let { findAllowedRange(it, timeSlots) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Перенос брони") },
        text = {
            Column {
                // Шаг 1 - выбор даты
                if (currentStep == 1) {
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
                            onClick = { selectedMonthYear = selectedMonthYear.plusMonths(1) }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowForward,
                                contentDescription = "Следующий месяц"
                            )
                        }
                    }
                    DateSelection(
                        dates = datesForMonth,
                        selectedDate = selectedDate,
                        today = today,
                        onDateSelected = { date ->
                            selectedDate = date
                            // При смене даты можно сбросить время по умолчанию
                            selectedStartTime = LocalDateTime.of(date, initialStartDateTime.toLocalTime())
                            selectedEndTime = LocalDateTime.of(date, initialEndDateTime.toLocalTime())
                        }
                    )
                }

                // Шаг 2 - выбор времени
                if (currentStep == 2) {
                    Text(
                        text = "Выберите интервал времени",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = selectedDate.toString(),
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
                        val firstDaySlots = timeSlots.subList(0, timeSlots.size / 2)
                        items(firstDaySlots) { slot ->
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
                    Text(
                        text = selectedDate.plusDays(1).toString(),
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
                        val secondDaySlots = timeSlots.subList(timeSlots.size / 2, timeSlots.size)
                        items(secondDaySlots) { slot ->
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentStep == 1) {
                        currentStep = 2 // Переходим ко второму шагу (выбор времени)
                    } else {
                        val request = UpdateBookingSeatRequest(
                            start_date = selectedStartTime.toString(),
                            end_date = selectedEndTime.toString()
                        )
                        bookingViewModel.updateSeatBooking(booking.booking_id, request)
                    }
                },
                enabled = if (currentStep == 1) selectedDate != null else selectedStartTime != null && selectedEndTime != null
            ) {
                Text(if (currentStep == 1) "Далее" else "Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}