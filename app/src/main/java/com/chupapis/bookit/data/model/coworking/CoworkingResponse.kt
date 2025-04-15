package com.chupapis.bookit.data.model.coworking


data class CoworkingResponse (
    val coworking_id: String,
    val title: String,
    val address: String,
    val tz_offset: Int,
    val timezone_str: String
)
