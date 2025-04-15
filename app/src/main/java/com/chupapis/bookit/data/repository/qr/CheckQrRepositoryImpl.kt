package com.chupapis.bookit.data.repository.qr
import com.chupapis.bookit.data.model.qr.CheckQrRequest
import com.chupapis.bookit.data.model.qr.CheckQrResponse
import com.chupapis.bookit.data.network.ApiService
import com.chupapis.bookit.data.repository.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class CheckQrRepositoryImpl(
    private val api: ApiService,
    private val authRepository: AuthRepository
) : CheckQrRepository {
    override suspend fun checkQr(qrData: String): Result<CheckQrResponse> {
        return try {
            val token = authRepository.getValidAccessToken()
                ?: return Result.failure(Exception("Токен отсутствует акакак"))
            val request = CheckQrRequest(qr_data = qrData)
            val response: Response<CheckQrResponse> = withContext(Dispatchers.IO) {
                api.checkQr("Bearer $token", request)
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // Можно распарсить errorBody, если требуется
                Result.failure(Exception("Ошибка проверки QR, код: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}