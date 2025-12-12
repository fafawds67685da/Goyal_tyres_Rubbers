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
import kotlinx.coroutines.flow.combine
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
    
    // Draft States
    private val draftRepository: StockDraftRepository
    private val _drafts = MutableStateFlow<List<StockDraft>>(emptyList())
    val drafts: StateFlow<List<StockDraft>> = _drafts.asStateFlow()
    
    // Dashboard Analytics States
    private val _lowStockCategories = MutableStateFlow<List<StockCategory>>(emptyList())
    val lowStockCategories: StateFlow<List<StockCategory>> = _lowStockCategories.asStateFlow()
    
    private val _recentActivity = MutableStateFlow<List<RecentActivity>>(emptyList())
    val recentActivity: StateFlow<List<RecentActivity>> = _recentActivity.asStateFlow()
    
    private val _topSellingCategory = MutableStateFlow<CategoryPerformance?>(null)
    val topSellingCategory: StateFlow<CategoryPerformance?> = _topSellingCategory.asStateFlow()
    
    private val _todayRevenue = MutableStateFlow(0.0)
    val todayRevenue: StateFlow<Double> = _todayRevenue.asStateFlow()
    
    private val _todayRollsSold = MutableStateFlow(0)
    val todayRollsSold: StateFlow<Int> = _todayRollsSold.asStateFlow()
    
    private val _todaySalesCount = MutableStateFlow(0)
    val todaySalesCount: StateFlow<Int> = _todaySalesCount.asStateFlow()
    
    val dbPath: String
    
    init {
        val database = AppDatabase.getDatabase(application)
        stockRepository = StockRepository(database.stockDao())
        saleRepository = SaleRepository(database.saleDao())
        categoryRepository = StockCategoryRepository(database.categoryDao())
        draftRepository = StockDraftRepository(database.draftDao())
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
        
        // Collect drafts
        viewModelScope.launch {
            draftRepository.allDrafts.collect { draftList ->
                _drafts.value = draftList
            }
        }
        
        // Initialize history with today's data
        calculateHistoryData(HistoryPeriod.TODAY)
        
        // Calculate dashboard analytics whenever data changes
        viewModelScope.launch {
            combine(
                _stocks,
                _categories,
                _sales
            ) { stocks, categories, sales ->
                Triple(stocks, categories, sales)
            }.collect {
                calculateDashboardAnalytics()
            }
        }
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
                
                // Create detailed stock additions list
                val stockAdditions = categoryStocks.map { stock ->
                    StockAddition(
                        date = stock.addedDate,
                        rolls = stock.numberOfRolls,
                        weight = stock.weightInKg,
                        cost = stock.costOfStock
                    )
                }.sortedByDescending { it.date }
                
                // Create detailed sales records list
                val salesRecords = categorySales.map { sale ->
                    SaleRecord(
                        date = sale.saleDate,
                        dealerName = sale.dealerName,
                        rolls = sale.numberOfRolls,
                        weight = sale.weightInKg,
                        amount = sale.soldFor,
                        paymentStatus = sale.paymentStatus
                    )
                }.sortedByDescending { it.date }
                
                CategoryHistory(
                    rubberName = category.rubberName,
                    rubberId = category.rubberId,
                    rollsAdded = categoryStocks.sumOf { it.numberOfRolls },
                    weightAdded = categoryStocks.sumOf { it.weightInKg },
                    rollsSold = categorySales.sumOf { it.numberOfRolls },
                    weightSold = categorySales.sumOf { it.weightInKg },
                    totalRevenue = categorySales.filter { it.paymentStatus == "PAID" }.sumOf { it.soldFor },
                    stockAdditions = stockAdditions,
                    salesRecords = salesRecords
                )
            }.filter { it.rollsAdded > 0 || it.rollsSold > 0 } // Only show categories with activity
            
            _historyData.value = historyList
        }
    }
    
    // Draft Operations
    fun createDraft(supplier: String, vehicle: String) {
        viewModelScope.launch {
            draftRepository.insertDraft(
                StockDraft(
                    supplierName = supplier,
                    vehicleNumber = vehicle,
                    items = emptyList(),
                    notes = "",
                    draftDate = System.currentTimeMillis()
                )
            )
        }
    }
    
    fun addItemToDraft(draftId: Int, item: DraftItem) {
        viewModelScope.launch {
            val draft = draftRepository.getDraftById(draftId)
            if (draft != null) {
                val updatedItems = draft.items.toMutableList()
                updatedItems.add(item)
                draftRepository.updateDraft(draft.copy(items = updatedItems))
            }
        }
    }
    
    fun removeItemFromDraft(draftId: Int, itemIndex: Int) {
        viewModelScope.launch {
            val draft = draftRepository.getDraftById(draftId)
            if (draft != null) {
                val updatedItems = draft.items.toMutableList()
                if (itemIndex in updatedItems.indices) {
                    updatedItems.removeAt(itemIndex)
                    draftRepository.updateDraft(draft.copy(items = updatedItems))
                }
            }
        }
    }
    
    fun updateDraftDetails(draftId: Int, supplier: String, vehicle: String, notes: String) {
        viewModelScope.launch {
            val draft = draftRepository.getDraftById(draftId)
            if (draft != null) {
                draftRepository.updateDraft(
                    draft.copy(
                        supplierName = supplier,
                        vehicleNumber = vehicle,
                        notes = notes
                    )
                )
            }
        }
    }
    
    data class DraftSummary(
        val categoryId: Int,
        val rubberName: String,
        val totalRolls: Int,
        val totalWeight: Double,
        val totalCost: Double
    )
    
    fun getDraftSummary(draftId: Int): List<DraftSummary> {
        val draft = drafts.value.find { it.id == draftId } ?: return emptyList()
        return draft.items
            .groupBy { it.categoryId }
            .map { (categoryId, items) ->
                DraftSummary(
                    categoryId = categoryId,
                    rubberName = items.first().rubberName,
                    totalRolls = items.sumOf { it.numberOfRolls },
                    totalWeight = items.sumOf { it.weightInKg },
                    totalCost = items.sumOf { it.costOfStock }
                )
            }
    }
    
    fun commitDraft(draftId: Int) {
        viewModelScope.launch {
            val draft = draftRepository.getDraftById(draftId)
            if (draft != null) {
                // Create stock entries from draft items
                draft.items.forEach { item ->
                    stockRepository.insert(
                        RubberStock(
                            rubberName = item.rubberName,
                            rubberId = item.rubberId,
                            numberOfRolls = item.numberOfRolls,
                            weightInKg = item.weightInKg,
                            costOfStock = item.costOfStock,
                            addedDate = System.currentTimeMillis()
                        )
                    )
                }
                
                // Delete the draft after committing
                draftRepository.deleteDraft(draft)
            }
        }
    }
    
    fun deleteDraft(draftId: Int) {
        viewModelScope.launch {
            val draft = draftRepository.getDraftById(draftId)
            if (draft != null) {
                draftRepository.deleteDraft(draft)
            }
        }
    }
    
    // Dashboard Analytics Functions
    private fun calculateDashboardAnalytics() {
        viewModelScope.launch {
            val currentStocks = _stocks.value
            val currentCategories = _categories.value
            val currentSales = _sales.value
            
            // Calculate low stock categories (less than 10 rolls)
            val lowStock = currentStocks
                .groupBy { it.rubberId }
                .mapNotNull { (rubberId, stocks) ->
                    val totalRolls = stocks.sumOf { it.numberOfRolls }
                    if (totalRolls < 10) {
                        currentCategories.find { it.rubberId == rubberId }
                    } else null
                }
            _lowStockCategories.value = lowStock
            
            // Calculate today's sales
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            val todaySales = currentSales.filter { it.saleDate >= todayStart }
            _todaySalesCount.value = todaySales.size
            _todayRollsSold.value = todaySales.sumOf { it.numberOfRolls }
            _todayRevenue.value = todaySales.filter { it.paymentStatus == "PAID" }.sumOf { it.soldFor }
            
            // Calculate top selling category (this month)
            val monthStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            val monthSales = currentSales.filter { it.saleDate >= monthStart }
            val categoryPerformance = monthSales
                .groupBy { it.rubberId }
                .map { (rubberId, sales) ->
                    CategoryPerformance(
                        categoryId = rubberId,
                        categoryName = sales.first().rubberName,
                        totalRollsSold = sales.sumOf { it.numberOfRolls },
                        totalRevenue = sales.filter { it.paymentStatus == "PAID" }.sumOf { it.soldFor },
                        salesCount = sales.size
                    )
                }
                .maxByOrNull { it.totalRevenue }
            _topSellingCategory.value = categoryPerformance
            
            // Calculate recent activity (last 10 transactions)
            val recentStocks = currentStocks
                .sortedByDescending { it.addedDate }
                .take(5)
                .map { stock ->
                    RecentActivity(
                        type = ActivityType.STOCK_ADDED,
                        description = "Added ${stock.numberOfRolls} rolls of ${stock.rubberName}",
                        timestamp = stock.addedDate,
                        amount = null
                    )
                }
            
            val recentSalesActivity = currentSales
                .sortedByDescending { it.saleDate }
                .take(5)
                .map { sale ->
                    RecentActivity(
                        type = ActivityType.SALE_MADE,
                        description = "Sold ${sale.numberOfRolls} rolls of ${sale.rubberName} to ${sale.dealerName}",
                        timestamp = sale.saleDate,
                        amount = sale.soldFor
                    )
                }
            
            _recentActivity.value = (recentStocks + recentSalesActivity)
                .sortedByDescending { it.timestamp }
                .take(10)
        }
    }
}

// Data classes for dashboard analytics
data class RecentActivity(
    val type: ActivityType,
    val description: String,
    val timestamp: Long,
    val amount: Double?
)

enum class ActivityType {
    STOCK_ADDED,
    SALE_MADE,
    PAYMENT_RECEIVED
}

data class CategoryPerformance(
    val categoryId: Int,
    val categoryName: String,
    val totalRollsSold: Int,
    val totalRevenue: Double,
    val salesCount: Int
)
