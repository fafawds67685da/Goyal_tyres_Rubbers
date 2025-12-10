package com.hellodev.app.data

import kotlinx.coroutines.flow.Flow

class StockRepository(private val stockDao: StockDao) {
    
    val allStock: Flow<List<RubberStock>> = stockDao.getAllStock()
    val totalRolls: Flow<Int?> = stockDao.getTotalRolls()
    val totalWeight: Flow<Double?> = stockDao.getTotalWeight()
    val stockByType: Flow<List<StockByType>> = stockDao.getStockByType()
    
    suspend fun insert(stock: RubberStock) {
        stockDao.insertStock(stock)
    }
    
    suspend fun deleteAll() {
        stockDao.deleteAllStock()
    }
}

class SaleRepository(private val saleDao: SaleDao) {
    
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()
    val totalRevenue: Flow<Double?> = saleDao.getTotalRevenue()
    
    suspend fun insert(sale: Sale) {
        saleDao.insertSale(sale)
    }
    
    suspend fun deleteAll() {
        saleDao.deleteAllSales()
    }
}
