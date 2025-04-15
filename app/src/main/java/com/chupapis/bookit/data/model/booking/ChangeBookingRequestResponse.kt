package com.chupapis.bookit.data.model.booking

data class BookingResponseChange(
    val booking_id: String,
    val start_date: String,
    val end_date: String,
    val seats: List<SeatResponse>
)

data class SeatResponse(
    val seat_id: String,
    val seat_uuid: String
)

data class BookingRequest(
    val start_date: String,
    val end_date: String,
    val seats: List<SeatResponse>
)

data class OccupiedSeatsResponse(
    val time: String,
    val slot_type: String
)
