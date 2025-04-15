package com.chupapis.bookit.data.model.login

data class LoginResponse(
    val access_token: String,
    val expires_in: Int,
    val refresh_token: String,
    val refresh_expires_in: Int
)