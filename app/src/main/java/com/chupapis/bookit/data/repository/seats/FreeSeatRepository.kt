package com.chupapis.bookit.data.repository.seats

import com.chupapis.bookit.data.model.coworking.CoworkingResponse
import com.chupapis.bookit.data.model.seats.FreeSeatResponse
import java.io.File

interface FreeSeatRepository {
    suspend fun getFreeSeats(
        coworkingId: String,
        startDate: String,
        endDate: String,
        tags: List<String> = emptyList()
    ): Result<List<FreeSeatResponse>>
    suspend fun getCoworkings(): Result<List<CoworkingResponse>>

    suspend fun createCoworking(
        title: String,
        address: String,
        tzOffset: Int,
        file: File
    ): Result<String>
}
