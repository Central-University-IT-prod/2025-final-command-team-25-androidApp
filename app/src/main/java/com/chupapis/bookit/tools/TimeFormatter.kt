package com.chupapis.bookit.tools

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


fun convertIsoToCustomFormat(isoDate: String): String {
    try {
        val dateTime = OffsetDateTime.parse(isoDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val outputFormatter = DateTimeFormatter.ofPattern("HH:mm dd.MM")
        return dateTime.format(outputFormatter)
    } catch (e: Exception) {
        return "0"
    }

}