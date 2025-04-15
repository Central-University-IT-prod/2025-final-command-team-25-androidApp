package com.chupapis.bookit.data.model.token

data class RefreshTokenResponse(
    val access_token: String,
    val expires_in: Int,
    val refresh_token: String,
    val refresh_expires_in: Int
)