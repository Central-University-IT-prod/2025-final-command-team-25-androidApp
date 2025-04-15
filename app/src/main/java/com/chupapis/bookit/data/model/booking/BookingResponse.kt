package com.chupapis.bookit.data.model.booking

data class BookingResponse(
    val booking_id: String,
    val start_date: String,
    val end_date: String,
    val seats: List<Seat>
)