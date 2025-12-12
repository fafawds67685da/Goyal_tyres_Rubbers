package com.hellodev.app.data

import kotlinx.coroutines.flow.Flow

class StockRepository(private val stockDao: StockDao) {
    
    val allStock: Flow<List<RubberStock>> = stockDao.getAllStock()
    val totalRolls: Flow<Int?> = stockDao.getTotalRolls()
    val totalWeight: Flow<Double?> = stockDao.getTotalWeight()
    val totalStockWorth: Flow<Double?> = stockDao.getTotalStockWorth()
    val stockByType: Flow<List<StockByType>> = stockDao.getStockByType()
    
    suspend fun insert(stock: RubberStock) {
        stockDao.insertStock(stock)
    }
    
    suspend fun update(stock: RubberStock) {
        stockDao.updateStock(stock)
    }
    
    suspend fun getStockByName(name: String): RubberStock? {
        return stockDao.getStockByName(name)
    }
    
    suspend fun deleteAll() {
        stockDao.deleteAllStock()
    }
}

class SaleRepository(private val saleDao: SaleDao) {
    
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()
    val totalRevenue: Flow<Double?> = saleDao.getTotalRevenue()
    val totalRollsSold: Flow<Int?> = saleDao.getTotalRollsSold()
    val totalWeightSold: Flow<Double?> = saleDao.getTotalWeightSold()
    
    suspend fun insert(sale: Sale) {
        saleDao.insertSale(sale)
    }
    
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<Sale>> {
        return saleDao.getSalesByDateRange(startDate, endDate)
    }
    
    fun getRevenueByDateRange(startDate: Long, endDate: Long): Flow<Double?> {
        return saleDao.getRevenueByDateRange(startDate, endDate)
    }
    
    suspend fun deleteAll() {
        saleDao.deleteAllSales()
    }
}
