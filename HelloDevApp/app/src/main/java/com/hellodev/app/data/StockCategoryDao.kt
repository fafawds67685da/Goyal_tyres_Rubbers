package com.hellodev.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface StockCategoryDao {
    @Insert
    suspend fun insertCategory(category: StockCategory)
    
    @Query("SELECT * FROM stock_categories ORDER BY rubberName ASC")
    fun getAllCategories(): Flow<List<StockCategory>>
    
    @Query("SELECT * FROM stock_categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): StockCategory?
    
    @Query("SELECT * FROM stock_categories WHERE rubberName = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): StockCategory?
    
    @Query("DELETE FROM stock_categories")
    suspend fun clearAllCategories()
    
    @Delete
    suspend fun deleteCategory(category: StockCategory)
}
