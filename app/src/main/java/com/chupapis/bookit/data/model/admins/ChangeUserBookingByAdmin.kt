package com.chupapis.bookit.data.model.admins

// Модель запроса для обновления бронирования места
data class UpdateBookingSeatRequest(
    val start_date: String, // Дата начала в формате ISO
    val end_date: String    // Дата окончания в формате ISO
)

// Модель ответа для обновления бронирования места
data class UpdateBookingSeatResponse(
    val booking_id: String,
    val start_date: String,
    val end_date: String,
    val seats: List<UpdatedSeatInfo>
)

// Модель для описания отдельного места
data class UpdatedSeatInfo(
    val seat_id: String,
    val seat_uuid: String,
    val is_owner: Boolean
)
