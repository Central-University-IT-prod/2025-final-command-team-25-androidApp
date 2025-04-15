package com.chupapis.bookit.data.repository.adminbooking

import com.chupapis.bookit.data.model.admins.UpdateBookingSeatRequest
import com.chupapis.bookit.data.model.admins.UpdateBookingSeatResponse
import com.chupapis.bookit.data.model.booking.BookingRequest
import com.chupapis.bookit.data.model.booking.BookingResponse
import com.chupapis.bookit.data.model.booking.BookingResponseChange
import com.chupapis.bookit.data.model.booking.OccupiedSeatsResponse

interface BookingRepository {
    suspend fun getUserBookings(userId: String): Result<List<BookingResponseChange>>
    suspend fun changeBookSeat(bookingId: String, request: BookingRequest): Result<BookingResponseChange>
    suspend fun getOccupiedSeats(seatUuids: List<String>, days: List<String>): Result<List<OccupiedSeatsResponse>>
    suspend fun updateSeatBooking(bookingId: String, request: UpdateBookingSeatRequest): Result<UpdateBookingSeatResponse>
    suspend fun deleteBookingSeat(bookingId: String): Result<Unit>
}