package com.hellodev.app.viewmodel

import android.app.Application
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hellodev.app.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

enum class DateFilter {
    TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR, ALL
}

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val stockRepository: StockRepository
    private val saleRepository: SaleRepository
    
    private val _stocks = MutableStateFlow<List<RubberStock>>(emptyList())
    val stocks: StateFlow<List<RubberStock>> = _stocks.asStateFlow()
    
    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales.asStateFlow()
    
    private val _filteredSales = MutableStateFlow<List<Sale>>(emptyList())
    val filteredSales: StateFlow<List<Sale>> = _filteredSales.asStateFlow()
    
    private val _totalRolls = MutableStateFlow(0)
    val totalRolls: StateFlow<Int> = _totalRolls.asStateFlow()
    
    private val _totalWeight = MutableStateFlow(0.0)
    val totalWeight: StateFlow<Double> = _totalWeight.asStateFlow()
    
    private val _totalStockWorth = MutableStateFlow(0.0)
    val totalStockWorth: StateFlow<Double> = _totalStockWorth.asStateFlow()
    
    private val _totalRollsSold = MutableStateFlow(0)
    val totalRollsSold: StateFlow<Int> = _totalRollsSold.asStateFlow()
    
    private val _totalWeightSold = MutableStateFlow(0.0)
    val totalWeightSold: StateFlow<Double> = _totalWeightSold.asStateFlow()
    
    private val _availableRolls = MutableStateFlow(0)
    val availableRolls: StateFlow<Int> = _availableRolls.asStateFlow()
    
    private val _availableWeight = MutableStateFlow(0.0)
    val availableWeight: StateFlow<Double> = _availableWeight.asStateFlow()
    
    private val _stockByType = MutableStateFlow<List<StockByType>>(emptyList())
    val stockByType: StateFlow<List<StockByType>> = _stockByType.asStateFlow()
    
    private val _totalRevenue = MutableStateFlow(0.0)
    val totalRevenue: StateFlow<Double> = _totalRevenue.asStateFlow()
    
    private val _filteredRevenue = MutableStateFlow(0.0)
    val filteredRevenue: StateFlow<Double> = _filteredRevenue.asStateFlow()
    
    private val _exportPath = MutableStateFlow<String>("")
    val exportPath: StateFlow<String> = _exportPath.asStateFlow()
    
    private val _selectedDateFilter = MutableStateFlow(DateFilter.ALL)
    val selectedDateFilter: StateFlow<DateFilter> = _selectedDateFilter.asStateFlow()
    
    val dbPath: String
    
    init {
        val database = AppDatabase.getDatabase(application)
        stockRepository = StockRepository(database.stockDao())
        saleRepository = SaleRepository(database.saleDao())
        dbPath = application.getDatabasePath("rubber_inventory_database").absolutePath
        
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
                updateAvailableStock()
            }
        }
        
        viewModelScope.launch {
            stockRepository.totalStockWorth.collect { worth ->
                _totalStockWorth.value = worth ?: 0.0
            }
        }
        
        viewModelScope.launch {
            stockRepository.stockByType.collect { types ->
                _stockByType.value = types
            }
        }
        
        viewModelScope.launch {
            saleRepository.allSales.collect { salesList ->
                _sales.value = salesList
                applySalesFilter(_selectedDateFilter.value)
            }
        }
        
        viewModelScope.launch {
            saleRepository.totalRevenue.collect { revenue ->
                _totalRevenue.value = revenue ?: 0.0
            }
        }
        
        viewModelScope.launch {
            saleRepository.totalRollsSold.collect { rolls ->
                _totalRollsSold.value = rolls ?: 0
                updateAvailableStock()
            }
        }
        
        viewModelScope.launch {
            saleRepository.totalWeightSold.collect { weight ->
                _totalWeightSold.value = weight ?: 0.0
                updateAvailableStock()
            }
        }
    }
    
    private fun updateAvailableStock() {
        _availableRolls.value = (_totalRolls.value - _totalRollsSold.value).coerceAtLeast(0)
        _availableWeight.value = (_totalWeight.value - _totalWeightSold.value).coerceAtLeast(0.0)
    }
    
    fun insertStock(rubberName: String, numberOfRolls: Int, weightInKg: Double, costOfStock: Double) {
        viewModelScope.launch {
            val stock = RubberStock(
                rubberName = rubberName,
                numberOfRolls = numberOfRolls,
                weightInKg = weightInKg,
                costOfStock = costOfStock
            )
            stockRepository.insert(stock)
        }
    }
    
    fun insertSale(rubberName: String, numberOfRolls: Int, weightInKg: Double, soldFor: Double) {
        viewModelScope.launch {
            val sale = Sale(
                rubberName = rubberName,
                numberOfRolls = numberOfRolls,
                weightInKg = weightInKg,
                soldFor = soldFor,
                saleDate = System.currentTimeMillis()
            )
            saleRepository.insert(sale)
        }
    }
    
    fun setDateFilter(filter: DateFilter) {
        _selectedDateFilter.value = filter
        applySalesFilter(filter)
    }
    
    private fun applySalesFilter(filter: DateFilter) {
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
                _filteredRevenue.value = _totalRevenue.value
                return
            }
        }
        
        val filtered = _sales.value.filter { it.saleDate in startDate..endDate }
        _filteredSales.value = filtered
        _filteredRevenue.value = filtered.sumOf { it.soldFor }
    }
    
    fun clearAllStock() {
        viewModelScope.launch {
            stockRepository.deleteAll()
        }
    }
    
    fun clearAllSales() {
        viewModelScope.launch {
            saleRepository.deleteAll()
        }
    }
    
    fun exportToExcel() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    val timestamp = dateFormat.format(Date())
                    val exportFile = File(downloadsDir, "Goyal_Inventory_$timestamp.xlsx")
                    
                    val workbook = XSSFWorkbook()
                    
                    // Stock Sheet
                    val stockSheet = workbook.createSheet("Stock")
                    val stockHeader = stockSheet.createRow(0)
                    val stockHeaders = listOf("ID", "Rubber Name", "Rolls", "Weight (kg)", "Cost (₹)", "Worth (₹)", "Date Added")
                    stockHeaders.forEachIndexed { index, header ->
                        stockHeader.createCell(index).setCellValue(header)
                    }
                    
                    _stocks.value.forEachIndexed { index, stock ->
                        val row = stockSheet.createRow(index + 1)
                        row.createCell(0).setCellValue(stock.id.toDouble())
                        row.createCell(1).setCellValue(stock.rubberName)
                        row.createCell(2).setCellValue(stock.numberOfRolls.toDouble())
                        row.createCell(3).setCellValue(stock.weightInKg)
                        row.createCell(4).setCellValue(stock.costOfStock)
                        row.createCell(5).setCellValue(stock.stockWorth)
                        row.createCell(6).setCellValue(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(stock.addedDate)))
                    }
                    
                    // Sales Sheet
                    val salesSheet = workbook.createSheet("Sales")
                    val salesHeader = salesSheet.createRow(0)
                    val salesHeaders = listOf("ID", "Rubber Name", "Rolls", "Weight (kg)", "Sold For (₹)", "Sale Date")
                    salesHeaders.forEachIndexed { index, header ->
                        salesHeader.createCell(index).setCellValue(header)
                    }
                    
                    _sales.value.forEachIndexed { index, sale ->
                        val row = salesSheet.createRow(index + 1)
                        row.createCell(0).setCellValue(sale.id.toDouble())
                        row.createCell(1).setCellValue(sale.rubberName)
                        row.createCell(2).setCellValue(sale.numberOfRolls.toDouble())
                        row.createCell(3).setCellValue(sale.weightInKg)
                        row.createCell(4).setCellValue(sale.soldFor)
                        row.createCell(5).setCellValue(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(sale.saleDate)))
                    }
                    
                    // Summary Sheet
                    val summarySheet = workbook.createSheet("Summary")
                    var rowNum = 0
                    summarySheet.createRow(rowNum++).createCell(0).setCellValue("Goyal Tyres & Rubbers - Inventory Summary")
                    summarySheet.createRow(rowNum++).createCell(0).setCellValue("Generated: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}")
                    rowNum++
                    
                    summarySheet.createRow(rowNum++).createCell(0).setCellValue("Total Stock:")
                    summarySheet.createRow(rowNum++).apply {
                        createCell(0).setCellValue("Total Rolls")
                        createCell(1).setCellValue(_totalRolls.value.toDouble())
                    }
                    summarySheet.createRow(rowNum++).apply {
                        createCell(0).setCellValue("Total Weight (kg)")
                        createCell(1).setCellValue(_totalWeight.value)
                    }
                    summarySheet.createRow(rowNum++).apply {
                        createCell(0).setCellValue("Total Stock Worth (₹)")
                        createCell(1).setCellValue(_totalStockWorth.value)
                    }
                    rowNum++
                    
                    summarySheet.createRow(rowNum++).createCell(0).setCellValue("Total Sales:")
                    summarySheet.createRow(rowNum++).apply {
                        createCell(0).setCellValue("Rolls Sold")
                        createCell(1).setCellValue(_totalRollsSold.value.toDouble())
                    }
                    summarySheet.createRow(rowNum++).apply {
                        createCell(0).setCellValue("Weight Sold (kg)")
                        createCell(1).setCellValue(_totalWeightSold.value)
                    }
                    summarySheet.createRow(rowNum++).apply {
                        createCell(0).setCellValue("Total Revenue (₹)")
                        createCell(1).setCellValue(_totalRevenue.value)
                    }
                    rowNum++
                    
                    summarySheet.createRow(rowNum++).createCell(0).setCellValue("Available Stock:")
                    summarySheet.createRow(rowNum++).apply {
                        createCell(0).setCellValue("Available Rolls")
                        createCell(1).setCellValue(_availableRolls.value.toDouble())
                    }
                    summarySheet.createRow(rowNum++).apply {
                        createCell(0).setCellValue("Available Weight (kg)")
                        createCell(1).setCellValue(_availableWeight.value)
                    }
                    
                    FileOutputStream(exportFile).use { fileOut ->
                        workbook.write(fileOut)
                    }
                    workbook.close()
                    
                    _exportPath.value = exportFile.absolutePath
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            getApplication(),
                            "Excel file exported to Downloads!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "Export failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
