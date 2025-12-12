package com.hellodev.app.data

data class CategoryHistory(
    val rubberName: String,
    val rubberId: Int,
    val rollsAdded: Int,
    val weightAdded: Double,
    val rollsSold: Int,
    val weightSold: Double,
    val totalRevenue: Double
)
