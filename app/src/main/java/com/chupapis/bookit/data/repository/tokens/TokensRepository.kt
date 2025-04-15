package com.chupapis.bookit.data.repository.tokens

interface TokensRepository {
    suspend fun getActualAccessToken(): String
    suspend fun refreshToken()
    suspend fun isReady(): Boolean
    suspend fun deleteTokens()
}