package com.chupapis.bookit.ui

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chupapis.bookit.data.model.booking.MyBookingResponse
import com.chupapis.bookit.data.repository.mybooking.MyBookingRepository
import kotlinx.coroutines.launch

class MyBookingViewModel(private val repository: MyBookingRepository, private val sharedPreferences: SharedPreferences) : ViewModel() {

    private val _myBookingResult = MutableLiveData<Result<List<MyBookingResponse>>>()
    val myBookingResult: LiveData<Result<List<MyBookingResponse>>> = _myBookingResult

    fun getMyBookings() {
        viewModelScope.launch {
            val currentCoworkingId = sharedPreferences.getString("selected_coworking_id", "")
            _myBookingResult.value = repository.getMyBookings(currentCoworkingId!!)
        }
    }

    class MyBookingViewModelFactory(private val repository: MyBookingRepository, private val sharedPreferences: SharedPreferences) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyBookingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MyBookingViewModel(repository, sharedPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
