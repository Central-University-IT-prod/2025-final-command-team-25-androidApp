package com.chupapis.bookit.data.repository.qr

import com.chupapis.bookit.data.model.qr.CheckQrResponse

interface CheckQrRepository {
    suspend fun checkQr(qrData: String): Result<CheckQrResponse>
}
