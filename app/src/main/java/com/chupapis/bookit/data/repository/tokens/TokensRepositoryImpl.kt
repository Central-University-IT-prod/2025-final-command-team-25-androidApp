package com.chupapis.bookit.data.repository.tokens

import android.content.SharedPreferences
import com.chupapis.bookit.data.model.token.RefreshTokenRequest
import com.chupapis.bookit.data.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

class TokensRepositoryImpl(
    private var accessToken: String,
    private var expiresAt: Long,
    private var refreshToken: String,
    private var refreshExpiresAt: Long,
    private val api: ApiService,
    private val sharedPreferences: SharedPreferences
) : TokensRepository {
    init {
        save()
    }

    override suspend fun getActualAccessToken(): String {
        val currentTimestamp = Instant.now().epochSecond

        if (currentTimestamp < expiresAt) {
            return accessToken
        }

        if (currentTimestamp > refreshExpiresAt) {
            throw Exception("Refresh token expired")
        }

        refreshToken()

        return accessToken
    }

    override suspend fun refreshToken() {
        val response = withContext(Dispatchers.IO) {
            api.refreshToken(
                RefreshTokenRequest(
                    refreshToken
                )
            )
        }

        if (!response.isSuccessful) {
            throw Exception("Failed to refresh token")
        }

        val tokenResponse = response.body()!!
        accessToken = tokenResponse.access_token
        expiresAt = tokenResponse.expires_in + Instant.now().epochSecond
        refreshToken = tokenResponse.refresh_token
        refreshExpiresAt = tokenResponse.refresh_expires_in + Instant.now().epochSecond
        save()
    }

    override suspend fun isReady(): Boolean {
        val currentTimestamp = Instant.now().epochSecond

        return currentTimestamp < refreshExpiresAt
    }

    override suspend fun deleteTokens() {
        api.logoutUser(refreshToken)
        accessToken = ""
        expiresAt = 0
        refreshToken = ""
        refreshExpiresAt = 0
        sharedPreferences.edit().clear().apply()
    }

    private fun save() {
        val editor = sharedPreferences.edit()
        editor.putString("access_token", accessToken)
        editor.putLong("expires_at", expiresAt)
        editor.putString("refresh_token", refreshToken)
        editor.putLong("refresh_expires_at", refreshExpiresAt)
        editor.apply()
    }
}