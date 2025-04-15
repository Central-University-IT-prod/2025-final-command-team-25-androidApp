package com.chupapis.bookit.data.model.user

data class UserResponse(
    val email: String,
    val username: String,
    val client_id: String,
    val access_level: String,
    val verification_level: String,
    val phone: String?
)