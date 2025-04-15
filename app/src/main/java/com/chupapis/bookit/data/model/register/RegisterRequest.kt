package com.chupapis.bookit.data.model.register

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String
)