package com.chupapis.bookit.ui.viewmodules

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chupapis.bookit.data.model.admins.UpdateBookingSeatRequest
import com.chupapis.bookit.data.model.admins.UpdateBookingSeatResponse
import com.chupapis.bookit.data.model.booking.BookingRequest
import com.chupapis.bookit.data.model.booking.BookingResponse
import com.chupapis.bookit.data.model.booking.BookingResponseChange
import com.chupapis.bookit.data.model.booking.OccupiedSeatsResponse
import com.chupapis.bookit.data.repository.adminbooking.BookingRepository
import kotlinx.coroutines.launch

class BookingViewModel(private val repository: BookingRepository) : ViewModel() {
    private val _bookingResult = MutableLiveData<Result<List<BookingResponseChange>>>()
    val bookingResult: LiveData<Result<List<BookingResponseChange>>> = _bookingResult

    private val _occupiedSeatsResult = MutableLiveData<Result<List<OccupiedSeatsResponse>>>()
    val occupiedSeatsResult: LiveData<Result<List<OccupiedSeatsResponse>>> = _occupiedSeatsResult

    private val _changeBookingResult = MutableLiveData<Result<BookingResponseChange>>()
    val changeBookingResult: LiveData<Result<BookingResponseChange>> = _changeBookingResult

    private val _updateSeatBookingResult = MutableLiveData<Result<UpdateBookingSeatResponse>>()
    val updateSeatBookingResult: LiveData<Result<UpdateBookingSeatResponse>> get() = _updateSeatBookingResult

    private val _deleteBookingResult = MutableLiveData<Result<Unit>>()
    val deleteBookingResult: LiveData<Result<Unit>> = _deleteBookingResult

    fun updateSeatBooking(bookingId: String, request: UpdateBookingSeatRequest) {
        viewModelScope.launch {
            _updateSeatBookingResult.value = repository.updateSeatBooking(bookingId, request)
        }
    }

    fun fetchUserBookings(userId: String) {
        viewModelScope.launch {
            _bookingResult.value = repository.getUserBookings(userId)
        }
    }

    fun changeBookSeat(bookingId: String, request: BookingRequest) {
        viewModelScope.launch {
            _changeBookingResult.value = repository.changeBookSeat(bookingId, request)
        }
    }

    fun fetchOccupiedSeats(seatUuids: List<String>, days: List<String>) {
        viewModelScope.launch {
            _occupiedSeatsResult.value = repository.getOccupiedSeats(seatUuids, days)
        }
    }

    fun deleteBookingSeat(bookingId: String) {
        viewModelScope.launch {
            _deleteBookingResult.value = repository.deleteBookingSeat(bookingId)
        }
    }

    class BookingViewModelFactory(private val repository: BookingRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BookingViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}