package com.chupapis.bookit.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chupapis.bookit.data.model.admins.ChangeUserRequest
import com.chupapis.bookit.data.model.admins.ChangeUserResponse
import com.chupapis.bookit.data.model.admins.ClientResponse
import com.chupapis.bookit.data.model.booking.BookingResponse
import com.chupapis.bookit.data.model.booking.BookingResponseChange
import com.chupapis.bookit.data.repository.admin.AdminRepository
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _clientsResult = MutableLiveData<Result<List<ClientResponse>>>()
    val clientsResult: LiveData<Result<List<ClientResponse>>> = _clientsResult

    // Изменили тип с BookingResponse на BookingResponseChange
    private val _bookingsResult = MutableLiveData<Result<List<BookingResponseChange>>>()
    val bookingsResult: LiveData<Result<List<BookingResponseChange>>> = _bookingsResult

    private val _deleteUserResult = MutableLiveData<Result<Unit>>()
    val deleteUserResult: LiveData<Result<Unit>> = _deleteUserResult

    private val _changeUserResult = MutableLiveData<Result<ChangeUserResponse>>()
    val changeUserResult: LiveData<Result<ChangeUserResponse>> = _changeUserResult

    fun fetchAllClients() {
        viewModelScope.launch {
            _clientsResult.value = repository.getAllClients()
        }
    }

    fun fetchUserBookings(userId: String) {
        viewModelScope.launch {
            _bookingsResult.value = repository.getUserBookings(userId)
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _deleteUserResult.value = repository.deleteUser(userId)
        }
    }

    fun changeUser(userId: String, request: ChangeUserRequest) {
        viewModelScope.launch {
            _changeUserResult.value = repository.changeUser(userId, request)
        }
    }

    class AdminViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AdminViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}