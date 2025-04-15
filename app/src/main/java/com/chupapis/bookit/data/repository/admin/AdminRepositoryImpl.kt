package com.chupapis.bookit.data.repository.admin

import android.content.SharedPreferences
import com.chupapis.bookit.data.model.admins.ChangeUserRequest
import com.chupapis.bookit.data.model.admins.ChangeUserResponse
import com.chupapis.bookit.data.model.admins.ClientResponse
import com.chupapis.bookit.data.model.booking.BookingResponse
import com.chupapis.bookit.data.model.booking.BookingResponseChange
import com.chupapis.bookit.data.model.booking.SeatResponse
import com.chupapis.bookit.data.network.ApiService
import com.chupapis.bookit.data.repository.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminRepositoryImpl(
    private val api: ApiService,
    private val authRepository: AuthRepository
) : AdminRepository {

    override suspend fun getAllClients(): Result<List<ClientResponse>> {
        return try {
            val token = authRepository.getValidAccessToken()
            val response = withContext(Dispatchers.IO) {
                api.getAllClients("Bearer $token")
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка получения списка клиентов, ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserBookings(userId: String): Result<List<BookingResponseChange>> {
        return try {
            val token = authRepository.getValidAccessToken()
            val response = withContext(Dispatchers.IO) {
                api.getUserBookings(userId, "Bearer $token")
            }
            if (response.isSuccessful && response.body() != null) {
                // Маппинг из BookingResponse в BookingResponseChange
                val bookings = response.body()!!.map { booking ->
                    BookingResponseChange(
                        booking_id = booking.booking_id,
                        start_date = booking.start_date,
                        end_date = booking.end_date,
                        seats = booking.seats.map { seat ->
                            SeatResponse(
                                seat_id = seat.seat_id,
                                seat_uuid = seat.seat_uuid
                            )
                        }
                    )
                }
                Result.success(bookings)
            } else {
                Result.failure(Exception("Ошибка получения бронирований пользователя, ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            val token = authRepository.getValidAccessToken()
            val response = withContext(Dispatchers.IO) {
                api.deleteUser(userId, "Bearer $token")
            }
            if (response.isSuccessful) {
                // При успешном удалении ожидаем код 204 No Content
                Result.success(Unit)
            } else {
                // Если возникает ошибка (например, 422), возвращаем failure с описанием ошибки
                Result.failure(Exception("Ошибка удаления пользователя, код ответа: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changeUser(userId: String, request: ChangeUserRequest): Result<ChangeUserResponse> {
        return try {
            val token = authRepository.getValidAccessToken()
            val response = withContext(Dispatchers.IO) {
                api.changeUser(userId, request, "Bearer $token")
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка изменения пользователя, код ответа: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}