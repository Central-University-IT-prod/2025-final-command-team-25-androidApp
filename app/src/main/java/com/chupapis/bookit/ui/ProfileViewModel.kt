package com.chupapis.bookit.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chupapis.bookit.data.network.ApiService
import com.chupapis.bookit.data.repository.auth.AuthRepository
import com.chupapis.bookit.ui.main.profile.UserStatus
import com.chupapis.bookit.ui.verification.PassportData
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val apiService: ApiService,
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf<ProfileUiState>(ProfileUiState.Loading)
    var passportSet by mutableStateOf(false)

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            println("Start loading")
            uiState = ProfileUiState.Loading

            try {
                val token = authRepository.getValidAccessToken()
                val response = apiService.getUserProfile("Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        uiState = ProfileUiState.Success(
                            profile = UserProfile(
                                name = user.username,
                                email = user.email,
                                phone = user.phone,
                                status = when (user.access_level) {
                                    "GOLD" -> UserStatus.GOLD
                                    "ADMIN" -> UserStatus.ADMIN
                                    else -> UserStatus.GUEST
                                }.name,
                                verificationLevel = user.verification_level
                            )
                        )
                    } ?: run {
                        uiState = ProfileUiState.Error("Empty response")
                    }
                } else {
                    uiState = ProfileUiState.Error("Failed to fetch profile: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = ProfileUiState.Error("Exception: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            uiState = ProfileUiState.Loading
            passportSet = false
        }
    }

    fun setPassportData(data: PassportData) {
        viewModelScope.launch {
            val result = apiService.savePassportData(
                "Bearer ${authRepository.getValidAccessToken()}",
                data
            )

            if(result.isSuccessful) {
                fetchUserProfile()

                passportSet = true
            }
        }
    }

    class ProfileViewModelFactory(
        private val authRepository: AuthRepository,
        private val apiService: ApiService
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) { // FIXED
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(apiService, authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String?,
    val status: String,
    val verificationLevel: String
)

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(
        val profile: UserProfile
    ) : ProfileUiState()

    data class Error(val message: String) : ProfileUiState()
}
