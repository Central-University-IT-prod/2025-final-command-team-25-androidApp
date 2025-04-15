package com.chupapis.bookit.data.repository.seats

import android.content.SharedPreferences
import com.chupapis.bookit.data.model.coworking.CoworkingResponse
import com.chupapis.bookit.data.model.seats.FreeSeatResponse
import com.chupapis.bookit.data.network.ApiService
import com.chupapis.bookit.data.repository.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class FreeSeatRepositoryImpl(
    private val api: ApiService,
    private val authRepository: AuthRepository
) : FreeSeatRepository {
    override suspend fun getFreeSeats(
        coworkingId: String,
        startDate: String,
        endDate: String,
        tags: List<String>
    ): Result<List<FreeSeatResponse>> {
        return try {
            val token = authRepository.getValidAccessToken()
            val response: Response<List<FreeSeatResponse>> = withContext(Dispatchers.IO) {
                println("KALLLL")
                api.getFreeSeats("Bearer $token", coworkingId, startDate, endDate, tags)
            }
            if (response.isSuccessful && response.body() != null) {
                println(response.body().toString())
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка загрузки свободных мест, код: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCoworkings(): Result<List<CoworkingResponse>> {
        return try {
            val token = authRepository.getValidAccessToken()
            val response: Response<List<CoworkingResponse>> = withContext(Dispatchers.IO) {
                api.getCoworkings("Bearer $token")
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка загрузки свободных мест, код: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createCoworking(
        name: String,
        address: String,
        tzOffset: Int,
        file: File
    ): Result<String> {
        return try {
            val token = authRepository.getValidAccessToken()
            if (token == null) {
                return Result.failure(Exception("Не удалось получить токен"))
            }
            val nameBody = name.toRequestBody("text/plain".toMediaType())
            val addressBody = address.toRequestBody("text/plain".toMediaType())
            val tzOffsetBody = tzOffset.toString().toRequestBody("text/plain".toMediaType())
            val mediaType = "image/svg+xml".toMediaTypeOrNull()
            val requestFile = file.asRequestBody(mediaType)
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = withContext(Dispatchers.IO) {
                api.createCoworking("Bearer $token", nameBody, addressBody, tzOffsetBody, filePart)
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка создания коворкинга, код: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}