package com.chupapis.bookit.ui.viewmodules

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chupapis.bookit.data.model.login.LoginResponse
import com.chupapis.bookit.data.model.register.RegisterResponse
import com.chupapis.bookit.data.model.user.UserResponse
import com.chupapis.bookit.data.repository.auth.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _registerResult = MutableLiveData<Result<RegisterResponse>>()
    val registerResult: LiveData<Result<RegisterResponse>> = _registerResult

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    private val _userResult = MutableLiveData<Result<UserResponse>>()
    val userResult: LiveData<Result<UserResponse>> = _userResult

    fun registerUser(email: String, username: String, password: String) {
        viewModelScope.launch {
            val response = repository.registerUser(email, username, password)

            if (response.isSuccess) {
                val bufferResults = repository.loginUser(email, password)
                println("Got login results: $bufferResults")
                if (bufferResults.isSuccess) {
                    println("Updating user results...")
                    _userResult.value = repository.getUserInfo()
                    println("Updated user results: ${_userResult.value?.getOrNull()}")
                    println("User access level: ${_userResult.value?.getOrNull()?.access_level} on login")
                }

                println("Setting login results")
                _loginResult.value = bufferResults
                println("Updated login results: ${_loginResult.value?.getOrNull()}")

                _registerResult.value = response
            } else {
                _registerResult.value = response
            }
        }
    }

    fun loginUser(login: String, password: String) {
        viewModelScope.launch {
            val bufferResults = repository.loginUser(login, password)
            println("Got login results: $bufferResults")
            if (bufferResults.isSuccess) {
                println("Updating user results...")
                _userResult.value = repository.getUserInfo()
                println("Updated user results: ${_userResult.value?.getOrNull()}")
                println("User access level: ${_userResult.value?.getOrNull()?.access_level} on login")
            }

            println("Setting login results")
            _loginResult.value = bufferResults
            println("Updated login results: ${_loginResult.value?.getOrNull()}")
        }
    }

    fun getUserInfo() {
        viewModelScope.launch {
            _userResult.value = repository.getUserInfo()
        }
    }

    class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}