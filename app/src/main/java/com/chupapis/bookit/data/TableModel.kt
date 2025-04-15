package com.chupapis.bookit.data

enum class TableSize(val width: Int, val height: Int) {
    SMALL(30, 10),
    MEDIUM(15, 45),
    LARGE(30, 30),
    TEST(200, 200)
}

enum class TableType {
    RECTANGLE,  // Обычный стол
    VERTICAL,   // Узкий вертикальный стол
    ROUND       // Круглый стол
}

data class Table(
    val x: Int,
    val y: Int,
    val size: TableSize,
    val type: TableType
)

