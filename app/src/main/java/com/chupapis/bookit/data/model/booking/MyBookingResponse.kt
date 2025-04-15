package com.chupapis.bookit.data.model.booking

data class MyBookingResponse(
    val booking_id: String,
    val user_id: String,
    val start_date: String,
    val end_date: String,
    val seats: List<Seat>,
    val invite_url: String?
)