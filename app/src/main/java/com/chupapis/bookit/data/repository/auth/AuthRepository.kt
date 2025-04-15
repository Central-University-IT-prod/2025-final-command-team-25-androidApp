package com.chupapis.bookit.data.repository.auth

import com.chupapis.bookit.data.model.login.LoginResponse
import com.chupapis.bookit.data.model.register.RegisterResponse
import com.chupapis.bookit.data.model.user.UserResponse

interface AuthRepository {
    suspend fun registerUser(
        email: String,
        username: String,
        password: String
    ): Result<RegisterResponse>

    suspend fun loginUser(login: String, password: String): Result<LoginResponse>
    suspend fun getUserInfo(): Result<UserResponse>
    suspend fun getValidAccessToken(): String?
    suspend fun logout()
}