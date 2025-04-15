package com.chupapis.bookit.data.repository.admin

import com.chupapis.bookit.data.model.admins.ChangeUserRequest
import com.chupapis.bookit.data.model.admins.ChangeUserResponse
import com.chupapis.bookit.data.model.admins.ClientResponse
import com.chupapis.bookit.data.model.booking.BookingResponse
import com.chupapis.bookit.data.model.booking.BookingResponseChange

interface AdminRepository {
    suspend fun getAllClients(): Result<List<ClientResponse>>
    suspend fun getUserBookings(userId: String): Result<List<BookingResponseChange>>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun changeUser(userId: String, request: ChangeUserRequest): Result<ChangeUserResponse>
}