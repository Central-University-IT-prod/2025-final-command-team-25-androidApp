package com.chupapis.bookit.data.model.admins

data class ChangeUserRequest(
    val username: String,
    val access_level: String,
    val verification_level: String
)

// Модель ответа при успешном изменении пользователя
data class ChangeUserResponse(
    val email: String,
    val username: String,
    val client_id: String,
    val access_level: String
)