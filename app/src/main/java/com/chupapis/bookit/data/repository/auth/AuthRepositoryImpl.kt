package com.chupapis.bookit.data.repository.auth

import android.content.SharedPreferences
import com.chupapis.bookit.data.model.login.LoginRequest
import com.chupapis.bookit.data.model.login.LoginResponse
import com.chupapis.bookit.data.model.token.RefreshTokenResponse
import com.chupapis.bookit.data.model.register.RegisterRequest
import com.chupapis.bookit.data.model.register.RegisterResponse
import com.chupapis.bookit.data.model.user.UserResponse
import com.chupapis.bookit.data.network.ApiService
import com.chupapis.bookit.data.repository.tokens.TokensRepository
import com.chupapis.bookit.data.repository.tokens.TokensRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.Instant

class ConflictException(val field: String) : Exception("Conflict on field: $field")

class AuthRepositoryImpl(
    private val api: ApiService,
    private val sharedPreferences: SharedPreferences
) : AuthRepository {
    private var tokens: TokensRepository? = null

    override suspend fun registerUser(
        email: String,
        username: String,
        password: String
    ): Result<RegisterResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.registerUser(RegisterRequest(email, username, password))
            }
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                if (response.code() == 409) {
                    val errorBody = response.errorBody()?.string()
                    val place = try {
                        val json = JSONObject(errorBody ?: "{}")
                        json.optJSONObject("detail")?.optString("place")
                    } catch (e: Exception) {
                        null
                    }

                    return when (place) {
                        "email" -> Result.failure(ConflictException("email"))
                        "username" -> Result.failure(ConflictException("username"))
                        else -> Result.failure(Exception("Conflict error, but unknown place"))
                    }
                }
                Result.failure(Exception("Ошибка регистрации, ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginUser(login: String, password: String): Result<LoginResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.loginUser(LoginRequest(login, password))
            }
            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                tokens = TokensRepositoryImpl(
                    tokenResponse.access_token,
                    tokenResponse.expires_in.toLong() + Instant.now().epochSecond,
                    tokenResponse.refresh_token,
                    tokenResponse.refresh_expires_in.toLong() + Instant.now().epochSecond,
                    api,
                    sharedPreferences
                )
                Result.success(tokenResponse)
            } else {
                Result.failure(Exception("Ошибка авторизации"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getValidAccessToken(): String? {
        if (tokens == null) {
            val accessToken = sharedPreferences.getString("access_token", null)
            val expiresAt = sharedPreferences.getLong("expires_at", 0)
            val refreshToken = sharedPreferences.getString("refresh_token", null)
            val refreshExpiresAt = sharedPreferences.getLong("refresh_expires_at", 0)
            if (accessToken != null && expiresAt != 0L && refreshToken != null && refreshExpiresAt != 0L) {
                tokens = TokensRepositoryImpl(
                    accessToken,
                    expiresAt,
                    refreshToken,
                    refreshExpiresAt,
                    api,
                    sharedPreferences
                )
            }
        }

        return tokens?.getActualAccessToken()
    }

    override suspend fun logout() {
        tokens?.deleteTokens()
        tokens = null
    }

    override suspend fun getUserInfo(): Result<UserResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getUserProfile(getValidAccessToken() ?: "")
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка получения данных пользователя, ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
