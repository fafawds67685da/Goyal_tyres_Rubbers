package com.hellodev.app.data

import kotlinx.coroutines.flow.Flow

class StockCategoryRepository(private val categoryDao: StockCategoryDao) {
    
    val allCategories: Flow<List<StockCategory>> = categoryDao.getAllCategories()
    
    suspend fun insertCategory(category: StockCategory) {
        categoryDao.insertCategory(category)
    }
    
    suspend fun getCategoryById(categoryId: Int): StockCategory? {
        return categoryDao.getCategoryById(categoryId)
    }
    
    suspend fun getCategoryByName(name: String): StockCategory? {
        return categoryDao.getCategoryByName(name)
    }
    
    suspend fun clearAllCategories() {
        categoryDao.clearAllCategories()
    }
    
    suspend fun deleteCategory(category: StockCategory) {
        categoryDao.deleteCategory(category)
    }
}
