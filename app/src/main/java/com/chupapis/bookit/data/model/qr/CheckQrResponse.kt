package com.chupapis.bookit.data.model.qr

import com.chupapis.bookit.data.model.booking.Seat

data class CheckQrResponse(
    val booking_id: String,
    val start_date: String,
    val end_date: String,
    val seat: Seat,
    val user: User,
    val need_verification: Boolean
)

data class User(
    val email: String,
    val username: String,
    val client_id: String,
    val access_level: String
)
