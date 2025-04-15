package com.chupapis.bookit.data.repository.mybooking

import com.chupapis.bookit.data.model.booking.MyBookingResponse
import com.chupapis.bookit.data.network.ApiService
import com.chupapis.bookit.data.repository.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MyBookingRepositoryImpl (
    private val api: ApiService,
    private val authRepository: AuthRepository
) : MyBookingRepository {

    override suspend fun getMyBookings(coworkingId: String): Result<List<MyBookingResponse>> {
        return try {
            val token = authRepository.getValidAccessToken()
                ?: return Result.failure(Exception("Ошибка: токен отсутствует аккесс"))

            val response = withContext(Dispatchers.IO) {
                api.getMyBookings("Bearer $token", coworkingId)
            }

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка загрузки бронирований, ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
