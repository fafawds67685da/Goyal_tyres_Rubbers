package com.hellodev.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Insert
    suspend fun insertSale(sale: Sale)
    
    @androidx.room.Delete
    suspend fun deleteSale(sale: Sale)
    
    @Query("UPDATE sales SET paymentStatus = :status, paymentReceivedDate = :receivedDate WHERE id = :saleId")
    suspend fun updatePaymentStatus(saleId: Int, status: String, receivedDate: Long?)
    
    @Query("SELECT * FROM sales ORDER BY saleDate DESC")
    fun getAllSales(): Flow<List<Sale>>
    
    @Query("SELECT * FROM sales WHERE paymentStatus = :status ORDER BY saleDate DESC")
    fun getSalesByPaymentStatus(status: String): Flow<List<Sale>>
    
    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()
    
    @Query("SELECT SUM(soldFor) FROM sales WHERE paymentStatus = 'PAID'")
    fun getTotalRevenue(): Flow<Double?>
    
    @Query("SELECT SUM(soldFor) FROM sales WHERE paymentStatus = 'PENDING'")
    fun getTotalPendingDue(): Flow<Double?>
    
    @Query("SELECT SUM(numberOfRolls) FROM sales")
    fun getTotalRollsSold(): Flow<Int?>
    
    @Query("SELECT SUM(weightInKg) FROM sales")
    fun getTotalWeightSold(): Flow<Double?>
    
    // Filter by date range
    @Query("SELECT * FROM sales WHERE saleDate >= :startDate AND saleDate <= :endDate ORDER BY saleDate DESC")
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<Sale>>
    
    @Query("SELECT SUM(soldFor) FROM sales WHERE saleDate >= :startDate AND saleDate <= :endDate AND paymentStatus = 'PAID'")
    fun getRevenueByDateRange(startDate: Long, endDate: Long): Flow<Double?>
    
    @Query("SELECT * FROM sales WHERE paymentStatus = :status AND saleDate >= :startDate AND saleDate <= :endDate ORDER BY saleDate DESC")
    fun getSalesByPaymentStatusAndDateRange(status: String, startDate: Long, endDate: Long): Flow<List<Sale>>
}
