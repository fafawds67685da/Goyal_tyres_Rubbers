package com.hellodev.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Insert
    suspend fun insertSale(sale: Sale)
    
    @Query("SELECT * FROM sales ORDER BY saleDate DESC")
    fun getAllSales(): Flow<List<Sale>>
    
    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()
    
    @Query("SELECT SUM(soldFor) FROM sales")
    fun getTotalRevenue(): Flow<Double?>
}
