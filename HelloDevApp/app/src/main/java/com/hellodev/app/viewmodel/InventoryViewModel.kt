package com.hellodev.app.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hellodev.app.data.*
import com.hellodev.app.utils.ExportUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

enum class DateFilter {
    TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR, ALL
}

enum class HistoryPeriod {
    TODAY, ONE_WEEK, ONE_MONTH, ONE_YEAR
}

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val stockRepository: StockRepository
    private val saleRepository: SaleRepository
    private val categoryRepository: StockCategoryRepository
    
    // Stock States
    private val _stocks = MutableStateFlow<List<RubberStock>>(emptyList())
    val stocks: StateFlow<List<RubberStock>> = _stocks.asStateFlow()
    
    private val _totalRolls = MutableStateFlow(0)
    val totalRolls: StateFlow<Int> = _totalRolls.asStateFlow()
    
    private val _totalWeight = MutableStateFlow(0.0)
    val totalWeight: StateFlow<Double> = _totalWeight.asStateFlow()
    
    private val _totalStockWorth = MutableStateFlow(0.0)
    val totalStockWorth: StateFlow<Double> = _totalStockWorth.asStateFlow()
    
    private val _availableRolls = MutableStateFlow(0)
    val availableRolls: StateFlow<Int> = _availableRolls.asStateFlow()
    
    private val _availableWeight = MutableStateFlow(0.0)
    val availableWeight: StateFlow<Double> = _availableWeight.asStateFlow()
    
    private val _stockByType = MutableStateFlow<List<StockByType>>(emptyList())
    val stockByType: StateFlow<List<StockByType>> = _stockByType.asStateFlow()
    
    // Sales States
    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales.asStateFlow()
    
    private val _filteredSales = MutableStateFlow<List<Sale>>(emptyList())
    val filteredSales: StateFlow<List<Sale>> = _filteredSales.asStateFlow()
    
    private val _totalRevenue = MutableStateFlow(0.0)
    val totalRevenue: StateFlow<Double> = _totalRevenue.asStateFlow()
    
    private val _filteredRevenue = MutableStateFlow(0.0)
    val filteredRevenue: StateFlow<Double> = _filteredRevenue.asStateFlow()
    
    private val _totalRollsSold = MutableStateFlow(0)
    val totalRollsSold: StateFlow<Int> = _totalRollsSold.asStateFlow()
    
    private val _totalWeightSold = MutableStateFlow(0.0)
    val totalWeightSold: StateFlow<Double> = _totalWeightSold.asStateFlow()
    
    // Pending Payments States
    private val _pendingPayments = MutableStateFlow<List<Sale>>(emptyList())
    val pendingPayments: StateFlow<List<Sale>> = _pendingPayments.asStateFlow()
    
    private val _totalPendingDue = MutableStateFlow(0.0)
    val totalPendingDue: StateFlow<Double> = _totalPendingDue.asStateFlow()
    
    // Category States
    private val _categories = MutableStateFlow<List<StockCategory>>(emptyList())
    val categories: StateFlow<List<StockCategory>> = _categories.asStateFlow()
    
    // Date Filter State
    private val _selectedDateFilter = MutableStateFlow(DateFilter.ALL)
    val selectedDateFilter: StateFlow<DateFilter> = _selectedDateFilter.asStateFlow()
    
    // History States
    private val _historyData = MutableStateFlow<List<CategoryHistory>>(emptyList())
    val historyData: StateFlow<List<CategoryHistory>> = _historyData.asStateFlow()
    
    private val _selectedHistoryPeriod = MutableStateFlow(HistoryPeriod.TODAY)
    val selectedHistoryPeriod: StateFlow<HistoryPeriod> = _selectedHistoryPeriod.asStateFlow()
    
    val dbPath: String
    
    init {
        val database = AppDatabase.getDatabase(application)
        stockRepository = StockRepository(database.stockDao())
        saleRepository = SaleRepository(database.saleDao())
        categoryRepository = StockCategoryRepository(database.categoryDao())
        dbPath = application.getDatabasePath("rubber_inventory_database").absolutePath
        
        // Collect stock data
        viewModelScope.launch {
            stockRepository.allStock.collect { stockList ->
                _stocks.value = stockList
            }
        }
        
        viewModelScope.launch {
            stockRepository.totalRolls.collect { total ->
                _totalRolls.value = total ?: 0
                updateAvailableStock()
            }
        }
        
        viewModelScope.launch {
            stockRepository.totalWeight.collect { total ->
                _totalWeight.value = total ?: 0.0
                updateAvailableWeight()
            }
        }
        
        viewModelScope.launch {
            stockRepository.totalStockWorth.collect { total ->
                _totalStockWorth.value = total ?: 0.0
            }
        }
        
        viewModelScope.launch {
            stockRepository.stockByType.collect { types ->
                _stockByType.value = types
            }
        }
        
        // Collect sales data
        viewModelScope.launch {
            saleRepository.allSales.collect { salesList ->
                _sales.value = salesList
                applyDateFilter(_selectedDateFilter.value)
            }
        }
        
        viewModelScope.launch {
            saleRepository.totalRevenue.collect { total ->
                _totalRevenue.value = total ?: 0.0
            }
        }
        
        viewModelScope.launch {
            saleRepository.totalRollsSold.collect { total ->
                _totalRollsSold.value = total ?: 0
                updateAvailableStock()
            }
        }
        
        viewModelScope.launch {
            saleRepository.totalWeightSold.collect { total ->
                _totalWeightSold.value = total ?: 0.0
                updateAvailableWeight()
            }
        }
        
        // Collect pending payments
        viewModelScope.launch {
            saleRepository.getSalesByPaymentStatus("PENDING").collect { pending ->
                _pendingPayments.value = pending
            }
        }
        
        viewModelScope.launch {
            saleRepository.totalPendingDue.collect { total ->
                _totalPendingDue.value = total ?: 0.0
            }
        }
        
        // Collect categories
        viewModelScope.launch {
            categoryRepository.allCategories.collect { categoryList ->
                _categories.value = categoryList
                // Recalculate history when categories change
                calculateHistoryData(_selectedHistoryPeriod.value)
            }
        }
        
        // Initialize history with today's data
        calculateHistoryData(HistoryPeriod.TODAY)
    }
    
    private fun updateAvailableStock() {
        _availableRolls.value = _totalRolls.value - _totalRollsSold.value
    }
    
    private fun updateAvailableWeight() {
        _availableWeight.value = _totalWeight.value - _totalWeightSold.value
    }
    
    // Stock Operations
    fun insertStock(stock: RubberStock) {
        viewModelScope.launch {
            stockRepository.insert(stock)
        }
    }
    
    fun clearAllStock() {
        viewModelScope.launch {
            stockRepository.deleteAll()
        }
    }
    
    // Sale Operations
    fun insertSale(sale: Sale) {
        viewModelScope.launch {
            saleRepository.insert(sale)
        }
    }
    
    fun deleteSale(sale: Sale) {
        viewModelScope.launch {
            saleRepository.deleteSale(sale)
        }
    }
    
    fun clearAllSales() {
        viewModelScope.launch {
            saleRepository.deleteAll()
        }
    }
    
    fun deleteStock(stock: RubberStock) {
        viewModelScope.launch {
            stockRepository.deleteStock(stock)
        }
    }
    
    fun deleteCategory(category: StockCategory) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
        }
    }
    
    // Payment Operations
    fun approvePayment(saleId: Int, paymentReceivedDate: Long) {
        viewModelScope.launch {
            saleRepository.updatePaymentStatus(saleId, "PAID", paymentReceivedDate)
        }
    }
    
    // Category Operations
    fun insertCategory(category: StockCategory) {
        viewModelScope.launch {
            categoryRepository.insertCategory(category)
        }
    }
    
    fun clearAllCategories() {
        viewModelScope.launch {
            categoryRepository.clearAllCategories()
        }
    }
    
    // Date Filter Operations
    fun setDateFilter(filter: DateFilter) {
        _selectedDateFilter.value = filter
        applyDateFilter(filter)
    }
    
    private fun applyDateFilter(filter: DateFilter) {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        
        val startDate = when (filter) {
            DateFilter.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            DateFilter.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            DateFilter.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            DateFilter.THIS_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            DateFilter.ALL -> {
                _filteredSales.value = _sales.value
                _filteredRevenue.value = _sales.value.filter { it.paymentStatus == "PAID" }.sumOf { it.soldFor }
                return
            }
        }
        
        viewModelScope.launch {
            saleRepository.getSalesByDateRange(startDate, endDate).collect { filtered ->
                _filteredSales.value = filtered
            }
            
            saleRepository.getRevenueByDateRange(startDate, endDate).collect { revenue ->
                _filteredRevenue.value = revenue ?: 0.0
            }
        }
    }
    
    // Export Operations
    fun exportSalesToCSV(): Intent? {
        return ExportUtils.exportSalesToCSV(
            getApplication(),
            _filteredSales.value
        )
    }
    
    fun exportStockToCSV(): Intent? {
        return ExportUtils.exportStockToCSV(
            getApplication(),
            _stocks.value
        )
    }
    
    fun exportPendingPaymentsToCSV(): Intent? {
        return ExportUtils.exportPendingPaymentsToCSV(
            getApplication(),
            _pendingPayments.value
        )
    }
    
    fun exportCategoriesToCSV(): Intent? {
        return ExportUtils.exportCategoriesToCSV(
            getApplication(),
            _categories.value
        )
    }
    
    // History Operations
    fun setHistoryPeriod(period: HistoryPeriod) {
        _selectedHistoryPeriod.value = period
        calculateHistoryData(period)
    }
    
    private fun calculateHistoryData(period: HistoryPeriod) {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        
        val startDate = when (period) {
            HistoryPeriod.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            HistoryPeriod.ONE_WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis
            }
            HistoryPeriod.ONE_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }
            HistoryPeriod.ONE_YEAR -> {
                calendar.add(Calendar.YEAR, -1)
                calendar.timeInMillis
            }
        }
        
        viewModelScope.launch {
            // Get stock added in period
            val stockInPeriod = _stocks.value.filter { it.addedDate >= startDate }
            
            // Get sales in period
            val salesInPeriod = _sales.value.filter { it.saleDate >= startDate && it.saleDate <= endDate }
            
            // Get all categories
            val allCategories = _categories.value
            
            // Calculate history for each category
            val historyList = allCategories.map { category ->
                val categoryStocks = stockInPeriod.filter { it.rubberId == category.rubberId }
                val categorySales = salesInPeriod.filter { it.rubberId == category.rubberId }
                
                CategoryHistory(
                    rubberName = category.rubberName,
                    rubberId = category.rubberId,
                    rollsAdded = categoryStocks.sumOf { it.numberOfRolls },
                    weightAdded = categoryStocks.sumOf { it.weightInKg },
                    rollsSold = categorySales.sumOf { it.numberOfRolls },
                    weightSold = categorySales.sumOf { it.weightInKg },
                    totalRevenue = categorySales.filter { it.paymentStatus == "PAID" }.sumOf { it.soldFor }
                )
            }.filter { it.rollsAdded > 0 || it.rollsSold > 0 } // Only show categories with activity
            
            _historyData.value = historyList
        }
    }
}
