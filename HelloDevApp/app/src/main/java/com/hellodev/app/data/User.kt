package com.hellodev.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rubber_stock")
data class RubberStock(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val rubberName: String,
    val numberOfRolls: Int,
    val weightInKg: Double,
    val costOfStock: Double,
    val addedDate: Long = System.currentTimeMillis()
) {
    val stockWorth: Double
        get() = costOfStock
}
