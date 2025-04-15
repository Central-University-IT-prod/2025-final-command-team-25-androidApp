package com.chupapis.bookit.data.repository.adminbooking

import com.chupapis.bookit.data.model.admins.ClientResponse
import com.chupapis.bookit.data.model.admins.UpdateBookingSeatRequest
import com.chupapis.bookit.data.model.admins.UpdateBookingSeatResponse
import com.chupapis.bookit.data.model.booking.BookingRequest
import com.chupapis.bookit.data.model.booking.BookingResponse
import com.chupapis.bookit.data.model.booking.BookingResponseChange
import com.chupapis.bookit.data.model.booking.OccupiedSeatsResponse
import com.chupapis.bookit.data.model.booking.SeatResponse
import com.chupapis.bookit.data.network.ApiService
import com.chupapis.bookit.data.repository.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookingRepositoryImpl(
    private val api: ApiService,
    private val authRepository: AuthRepository
) : BookingRepository {

    override suspend fun updateSeatBooking(bookingId: String, request: UpdateBookingSeatRequest): Result<UpdateBookingSeatResponse> {
        return try {
            val token = authRepository.getValidAccessToken()
                ?: return Result.failure(Exception("Ошибка: токен отсутствует"))
            // При необходимости можно получить токен через authRepository
            val response = withContext(Dispatchers.IO) {
                api.updateSeatBookingReservation("Bearer $token", bookingId, request)
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка обновления бронирования места, код ответа: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserBookings(userId: String): Result<List<BookingResponseChange>> {
        return try {
            val token = authRepository.getValidAccessToken()
                ?: return Result.failure(Exception("Ошибка: токен отсутствует"))

            val response = withContext(Dispatchers.IO) {
                api.getUserBookingsByUserId(userId, "Bearer $token")
            }

            if (response.isSuccessful && response.body() != null) {
                // Здесь response.body() имеет тип List<BookingResponseGetBookingByUserID>
                val bookings = response.body()!!.map { booking ->
                    BookingResponseChange(
                        booking_id = booking.booking_id,
                        start_date = booking.start_date,
                        end_date = booking.end_date,
                        seats = booking.seats.map { seat ->
                            SeatResponse(seat_id = seat.seat_id, seat_uuid = seat.seat_uuid)
                        }
                    )
                }
                Result.success(bookings)
            } else {
                Result.failure(Exception("Ошибка загрузки бронирований, ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changeBookSeat(bookingId: String, request: BookingRequest): Result<BookingResponseChange> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.changeBookSeat(bookingId, request)
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка обновления бронирования, ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOccupiedSeats(seatUuids: List<String>, days: List<String>): Result<List<OccupiedSeatsResponse>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getOccupiedSeats(seatUuids, days)
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка получения занятых мест, ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBookingSeat(bookingId: String): Result<Unit> {
        return try {
            val token = authRepository.getValidAccessToken()
                ?: return Result.failure(Exception("Ошибка: токен отсутствует"))
            val response = withContext(Dispatchers.IO) {
                api.deleteBookingSeat("Bearer $token", bookingId)
            }
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка удаления бронирования, код ответа: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}