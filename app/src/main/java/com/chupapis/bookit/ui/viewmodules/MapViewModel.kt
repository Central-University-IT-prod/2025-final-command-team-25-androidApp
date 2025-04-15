package com.chupapis.bookit.ui.viewmodules

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chupapis.bookit.data.model.booking.BookingRequest
import com.chupapis.bookit.data.model.booking.SeatResponse
import com.chupapis.bookit.data.model.coworking.CoworkingResponse
import com.chupapis.bookit.data.model.figure.Figure
import com.chupapis.bookit.data.model.seats.FreeSeatResponse
import com.chupapis.bookit.data.model.user.UserResponse
import com.chupapis.bookit.data.network.ApiService
import com.chupapis.bookit.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class MapViewModel(
    private val authRepository: AuthRepository,
    private val api: ApiService,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    var startDateTime: LocalDateTime? by mutableStateOf(null)
    var endDateTime: LocalDateTime? by mutableStateOf(null)

    private val _seats = MutableStateFlow<List<FreeSeatResponse>>(emptyList())
    val seats: StateFlow<List<FreeSeatResponse>> = _seats.asStateFlow()


    var userProfile: UserResponse? by mutableStateOf(null)

    var tables by mutableStateOf<List<Figure>>(emptyList())

    private var coworkingData: CoworkingResponse? by mutableStateOf(null)

    init {
        viewModelScope.launch {
            coworkingData =
                api.getCoworkings("Bearer ${authRepository.getValidAccessToken()}").body()?.get(0)

            val offset = ZoneOffset.ofHours(coworkingData!!.tz_offset)
            val nowInUtc = Instant.now() // Current time in UTC
            val zonedDateTimeNow = nowInUtc.atOffset(offset)

            val now = zonedDateTimeNow.toLocalDateTime()
            val nearestStartDateTime = getNearestStartDateTime(now)

            println("TIMES")
            println(now)
            println(nearestStartDateTime)

            // Default booking is 30 minutes long
            val nearestEndDateTime = nearestStartDateTime.plusMinutes(30)

            startDateTime = nearestStartDateTime
            endDateTime = nearestEndDateTime
            println("Updated")

            println("Default booking: $startDateTime to $endDateTime")

            userProfile =
                api.getUserProfile("Bearer ${authRepository.getValidAccessToken()}").body()
        }
    }

    fun loadTables(coworkingId: String) {
        viewModelScope.launch {
            try {
                val response = api.getTables(coworkingId)
                if (response.isSuccessful) {
                    tables = response.body() ?: emptyList()
                    println("Таблицы загружены: $tables")
                } else {
                    println("Ошибка загрузки столов: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Ошибка при загрузке столов: ${e.message}")
            }
        }
    }


    fun updateDatesOnly(newDate: LocalDate) {
        // Create new DateTimes by combining the new date with the existing times

        endDateTime = if (endDateTime!!.toLocalDate() != startDateTime!!.toLocalDate()) {
            LocalDateTime.of(newDate.plusDays(1), endDateTime!!.toLocalTime())
        } else {
            LocalDateTime.of(newDate, endDateTime!!.toLocalTime())
        }
        startDateTime = LocalDateTime.of(newDate, startDateTime!!.toLocalTime())

        println("Updated Start DateTime: $startDateTime")
        println("Updated End DateTime: $endDateTime")
    }

    fun book() {
        viewModelScope.launch {
            val response = authRepository.getValidAccessToken()?.let {
                api.bookSeat(
                    it,
                    BookingRequest(
                        start_date = startDateTime.toString(),
                        end_date = endDateTime.toString(),
                        seats = seats.value.map { seat ->
                            SeatResponse(
                                seat_uuid = seat.seat_uuid,
                                seat_id = seat.seat_id
                            )
                        },
                    )
                )
                _seats.value = emptyList()
            }

            if (response == null) {
                throw Exception("Failed to get access token")
            }

            println("Booked seat: $response")
        }
    }

    fun getCoworkingId(): String {
        return sharedPreferences.getString("selected_coworking_id", "")!!
    }

    fun addSelect(seat: FreeSeatResponse) {
        _seats.value += seat
    }

    fun removeSelect(seat: FreeSeatResponse) {
        _seats.value -= seat
    }

    fun cleanSelection() {
        _seats.value = emptyList()
    }


    private fun getNearestStartDateTime(now: LocalDateTime): LocalDateTime {
        val currentMinutes = now.minute

        val nearestTime = when {
            currentMinutes < 30 -> now.truncatedTo(ChronoUnit.HOURS).plusMinutes(30)
            else -> now.truncatedTo(ChronoUnit.HOURS).plusHours(1)
        }

        return if (nearestTime.toLocalTime() >= LocalTime.of(23, 30)) {
            LocalDateTime.of(now.toLocalDate().plusDays(1), LocalTime.of(0, 0))
        } else {
            nearestTime
        }
    }

    class MapViewModelFactory(private val repository: AuthRepository, private val api: ApiService, private val sharedPreferences: SharedPreferences) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MapViewModel(repository, api, sharedPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

