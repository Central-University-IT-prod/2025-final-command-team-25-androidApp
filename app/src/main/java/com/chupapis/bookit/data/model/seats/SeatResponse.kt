package com.chupapis.bookit.data.model.seats

import androidx.compose.ui.graphics.TransformOrigin

data class FreeSeatResponse(
    val seat_uuid: String,
    val seat_id: String,
    val required_level: String,
    val pos_x: Float,
    val pos_y: Float,
    val width: Float,
    val height: Float,
    val rx: Float,
    val is_free: Boolean,
    val price: Int,
    val rotation: Float,
    val rotationCenter: TransformOrigin,
    val seat_type: String
)