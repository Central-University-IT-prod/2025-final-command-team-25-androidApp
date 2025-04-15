package com.chupapis.bookit.ui

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chupapis.bookit.data.model.coworking.CoworkingResponse
import com.chupapis.bookit.data.model.seats.FreeSeatResponse
import com.chupapis.bookit.data.repository.seats.FreeSeatRepository
import kotlinx.coroutines.launch
import java.io.File

class FreeSeatViewModel(
    private val repository: FreeSeatRepository
) : ViewModel() {

    private val _freeSeats = MutableLiveData<List<FreeSeatResponse>>()
    val freeSeats: LiveData<List<FreeSeatResponse>> = _freeSeats

    private val _coworkings = MutableLiveData<List<CoworkingResponse>>()
    val coworkings: LiveData<List<CoworkingResponse>> = _coworkings

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadFreeSeats(
        startDate: String,
        endDate: String,
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val response = repository.getCoworkings()
            response.onSuccess {
                repository.getFreeSeats(it[0].coworking_id, startDate, endDate, tags)
                    .onSuccess { seats ->
                        //println("SEats: $seats is free ${seats[0].is_free}")
                        _freeSeats.value = seats
                        _error.value = null
                    }
                    .onFailure { e ->
                        _error.value = e.message
                    }
            }
        }
    }

    fun loadCoworkings() {
        viewModelScope.launch {
            val response = repository.getCoworkings()
            response.onSuccess { coworkingList ->
                _coworkings.value = coworkingList
                _error.value = null
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun createCoworking(
        name: String,
        address: String,
        tzOffset: Int,
        file: File,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.createCoworking(name, address, tzOffset, file)
            result.onSuccess {
                // После успешного создания вызываем полное обновление списка
                loadCoworkings()
                onSuccess()
            }.onFailure { e ->
                onError(e.message ?: "Неизвестная ошибка")
            }
        }
    }



    class FreeSeatViewModelFactory(
        private val repository: FreeSeatRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FreeSeatViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FreeSeatViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
