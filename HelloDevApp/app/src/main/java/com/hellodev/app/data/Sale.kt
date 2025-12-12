package com.hellodev.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val rubberName: String,
    val rubberId: Int,
    val dealerName: String,
    val numberOfRolls: Int,
    val weightInKg: Double,
    val soldFor: Double,
    val saleDate: Long = System.currentTimeMillis(),
    val paymentStatus: String = "PENDING", // PAID or PENDING
    val paymentReceivedDate: Long? = null
)
