package com.hellodev.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_categories")
data class StockCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val rubberName: String,
    val rubberId: Int,
    val addedDate: Long = System.currentTimeMillis()
)
