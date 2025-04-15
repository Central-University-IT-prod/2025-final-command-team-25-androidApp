package com.chupapis.bookit.ui.main.homepage

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.chupapis.bookit.data.model.figure.Figure
import com.chupapis.bookit.data.model.seats.FreeSeatResponse
import com.chupapis.bookit.data.network.RetrofitClient
import com.chupapis.bookit.data.repository.auth.AuthRepository
import com.chupapis.bookit.data.repository.seats.FreeSeatRepositoryImpl
import com.chupapis.bookit.tools.convertIsoToCustomFormat
import com.chupapis.bookit.ui.FreeSeatViewModel
import com.chupapis.bookit.ui.dialogs.CalendarAlertDialog
import com.chupapis.bookit.ui.dialogs.TimeAlertDialog
import com.chupapis.bookit.ui.viewmodels.FigureViewModel
import com.chupapis.bookit.ui.viewmodules.MapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavHostController,
    authRepository: AuthRepository,
    mapViewModel: MapViewModel
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val coworkingName = sharedPreferences.getString("selected_coworking_name", "коворкинг") ?: "коворкинг"

    val freeSeatRepository =
        FreeSeatRepositoryImpl(api = RetrofitClient.getRetrofitInstance(context), authRepository)
    val freeSeatViewModel: FreeSeatViewModel = viewModel(
        factory = FreeSeatViewModel.FreeSeatViewModelFactory(freeSeatRepository)
    )
    val figureViewModel: FigureViewModel = viewModel(
        factory = FigureViewModel.Factory(RetrofitClient.getRetrofitInstance(LocalContext.current))
    )

    // Получаем даты из MapViewModel
    val startDate = mapViewModel.startDateTime
    val endDate = mapViewModel.endDateTime

    // Остальной код для вычисления minDate и работы с датами остается без изменений
    val minDate = listOfNotNull(startDate, endDate).minOrNull()?.toLocalDate()?.toString() ?: ""
    val startDateString = mapViewModel.startDateTime.toString()
    val endDateString = mapViewModel.endDateTime.toString()
    println("start: $startDateString, end: $endDateString")

    // Состояния для управления показом диалогов выбора даты и времени
    var showCalendarDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var showSheet by remember { mutableStateOf(false) }
    val selectedSeats = mapViewModel.seats




    Column(modifier = Modifier.fillMaxSize()) {
        // Верхняя панель с обновлённым дизайном для кнопок и поля выбора
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Верхняя строка с иконкой и названием коворкинга из SharedPreferences
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("coworking_selection")
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Геопозиция"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = coworkingName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }


            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = "$minDate\n${(startDateString)} - ${(endDateString)}",
                onValueChange = {},
                label = { Text("Выбрано") },
                readOnly = true,
                enabled = false,
                shape = RoundedCornerShape(12.dp), // Скругление поля выбора
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCalendarDialog = true },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = Color.Black
                )
            )
        }
        // Отображаем сообщение об ошибке, если оно есть
        val error by freeSeatViewModel.error.observeAsState()
        if (error != null) {
            Text(
                text = error ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Если уже выбраны даты – загружаем свободные места и фигуры
        if (startDate != null && endDate != null) {
            println("Ready")
            val freeSeats by freeSeatViewModel.freeSeats.observeAsState(emptyList())
            val figures by figureViewModel.figures.observeAsState(emptyList())
            LaunchedEffect(Unit) {
                freeSeatViewModel.loadFreeSeats(
                    startDate = startDateString,
                    endDate = endDateString
                )
                figureViewModel.loadFigures(
                    coworkingId = mapViewModel.getCoworkingId()
                )
            }



            // Основное содержимое – карта с отрисовкой столов и сидений
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(0.9f, 3f)
                            val focusShiftX = (centroid.x - offsetX) * (newScale / scale - 1)
                            val focusShiftY = (centroid.y - offsetY) * (newScale / scale - 1)
                            offsetX += pan.x - focusShiftX
                            offsetY += pan.y - focusShiftY
                            scale = newScale
                        }
                    }
                    .background(Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        }
                ) {
                    figures.forEach { figure ->
                        DrawFigures(figure, scale)
                    }
                    freeSeats.forEach { seat ->
                        DrawSeat(
                            seat,
                            scale,
                            mapViewModel.seats.value.any {
                                it.seat_id == seat.seat_id
                            }
                        ) {
                            println("Selected")

                            if (mapViewModel.seats.value.any { it.seat_type == "AUDIENCE" }) {
                                mapViewModel.removeSelect(mapViewModel.seats.value.first { it.seat_type == "AUDIENCE" })
                            }

                            if (seat.seat_type == "AUDIENCE") {
                                mapViewModel.cleanSelection()
                            }

                            if (mapViewModel.seats.value.any { it.seat_id == seat.seat_id }) {
                                mapViewModel.removeSelect(seat)
                            } else {
                                mapViewModel.addSelect(seat)
                            }

                            println(showSheet)
                        }
                    }
                }
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
                modifier = Modifier.fillMaxSize()
            ) {
                val seats by mapViewModel.seats.collectAsState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 250.dp)
                        .padding(16.dp)
                ) {

                    item {
                        SeatInfoScreen(
                            seats = seats,
                            onClose = { showSheet = false },
                            onNavigate = { navController.navigate("booking") }
                        )
                    }
                }
            }
        }

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        val seats by mapViewModel.seats.collectAsState()
        val isEnabled = seats.isNotEmpty()
        println("$isEnabled is enabled")

        ElevatedButton(
            onClick = {
                showSheet = true
            },
            enabled = isEnabled,
            shape = CircleShape,
            modifier = Modifier.size(64.dp), // Same size as FloatingActionButton,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = Color.Blue, // Enabled color
                disabledContainerColor = Color.Gray, // Disabled color (optional)
                contentColor = Color.White,
                disabledContentColor = Color.LightGray
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                contentDescription = "Добавить место",
                modifier = Modifier.scale(3f),
            )
        }
    }

    // Показываем диалог календаря для выбора даты
    if (showCalendarDialog) {
        CalendarAlertDialog(
            mapViewModel = mapViewModel,
            onDismiss = { showCalendarDialog = false },
            onSave = {
                showCalendarDialog = false
                // После выбора даты открываем диалог выбора времени
                showTimeDialog = true
            }
        )
    }

    // Показываем диалог для выбора времени
    if (showTimeDialog) {
        TimeAlertDialog(
            mapViewModel = mapViewModel,
            onDismiss = { showTimeDialog = false },
            onSave = { showTimeDialog = false }
        )
    }
}


@Composable
fun DrawFigures(figure: Figure, scale: Float) {
    Box(
        modifier = Modifier
            .offset((figure.pos_x * scale).dp, (figure.pos_y * scale).dp) // Размещаем фигуру
            .graphicsLayer(
                rotationZ = figure.rotation, // Применяем вращение
                transformOrigin = TransformOrigin(
                    0f,
                    0f
                ) // Вращаем относительно верхнего левого угла
            )
            .size((figure.width * scale).dp, (figure.height * scale).dp)
            .clip(RoundedCornerShape((figure.rx * scale).dp))
            .background(Color.DarkGray)
    )
}


@Composable
fun DrawSeat(seat: FreeSeatResponse, scale: Float, isSelected: Boolean, onClick: () -> Unit) {
    // Определяем цвет сиденья, если оно свободное, в зависимости от required_level,
    // иначе используем серый цвет
    val seatColor = if (seat.is_free) {
        when (seat.required_level.uppercase()) {
            "AVAILABLE" -> Color.Blue
            "STANDARD", "PRO" -> Color(0xFFFFD700) // золотой
            else -> Color.Green // запасной вариант
        }
    } else {
        Color.Gray
    }

    println(seat)
    println(seat.is_free)

    // Если сиденье свободное, добавляем модификатор clickable, иначе оставляем как есть
    val modifier = Modifier
        .offset((seat.pos_x * scale).dp, (seat.pos_y * scale).dp)
        .graphicsLayer(
            rotationZ = seat.rotation, // Применяем вращение
            transformOrigin = TransformOrigin(0f, 0f) // Вращаем относительно верхнего левого угла
        )
        .size((seat.width * scale).dp, (seat.height * scale).dp)
        .clip(RoundedCornerShape((seat.rx * scale).dp))
        .background(seatColor)
        .then(if (seat.is_free) Modifier.clickable { onClick() } else Modifier)
        .then(if (isSelected) Modifier.border(2.dp, Color.Black) else Modifier)

    Box(modifier = modifier)
}

@Composable
fun SeatInfoScreen(
    seats: List<FreeSeatResponse>,
    onClose: () -> Unit,
    onNavigate: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Информация о местах", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Iterate over the list of seats and display aggregated information
        seats.forEach { seat ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ID места: ${seat.seat_id}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Статус: ${if (seat.is_free) "Свободно" else "Занято"}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp)) // Add space between seats
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display a summary of the seats (for example, count of free and occupied seats)
        Text(
            text = "Всего мест: ${seats.size}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNavigate) {
            Text("Перейти к брони")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onClose) {
            Text("Закрыть")
        }
    }

}