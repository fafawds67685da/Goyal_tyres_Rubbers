package com.hellodev.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Insert
    suspend fun insertStock(stock: RubberStock)
    
    @Query("SELECT * FROM rubber_stock ORDER BY id DESC")
    fun getAllStock(): Flow<List<RubberStock>>
    
    @Query("DELETE FROM rubber_stock")
    suspend fun deleteAllStock()
    
    @Query("SELECT SUM(numberOfRolls) FROM rubber_stock")
    fun getTotalRolls(): Flow<Int?>
    
    @Query("SELECT SUM(weightInKg) FROM rubber_stock")
    fun getTotalWeight(): Flow<Double?>
    
    @Query("SELECT rubberName, SUM(weightInKg) as totalWeight FROM rubber_stock GROUP BY rubberName")
    fun getStockByType(): Flow<List<StockByType>>
}

data class StockByType(
    val rubberName: String,
    val totalWeight: Double
)
