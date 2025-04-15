package com.chupapis.bookit.data.model.booking

data class BookingResponseGetBookingByUserID (
    val booking_id: String,
    val start_date: String,
    val end_date: String,
    val seats: List<Seat>,
    val user_id: String,
    val username: String
)