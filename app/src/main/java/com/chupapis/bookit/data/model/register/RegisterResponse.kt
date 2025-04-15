package com.chupapis.bookit.data.model.register

data class RegisterResponse(
    val email: String,
    val username: String,
    val client_id: String,
    val access_level: String
)