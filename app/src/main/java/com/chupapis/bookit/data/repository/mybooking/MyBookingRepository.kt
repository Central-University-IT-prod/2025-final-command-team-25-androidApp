package com.chupapis.bookit.data.repository.mybooking

import com.chupapis.bookit.data.model.booking.MyBookingResponse

interface MyBookingRepository {
    suspend fun getMyBookings(coworkingId: String): Result<List<MyBookingResponse>>
}