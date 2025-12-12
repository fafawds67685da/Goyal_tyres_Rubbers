package com.hellodev.app.data

data class CategoryHistory(
    val rubberName: String,
    val rubberId: Int,
    val rollsAdded: Int,
    val weightAdded: Double,
    val rollsSold: Int,
    val weightSold: Double,
    val totalRevenue: Double,
    val stockAdditions: List<StockAddition> = emptyList(),
    val salesRecords: List<SaleRecord> = emptyList()
)

data class StockAddition(
    val date: Long,
    val rolls: Int,
    val weight: Double,
    val cost: Double
)

data class SaleRecord(
    val date: Long,
    val dealerName: String,
    val rolls: Int,
    val weight: Double,
    val amount: Double,
    val paymentStatus: String
)
