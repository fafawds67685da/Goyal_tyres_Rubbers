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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val stockRepository: StockRepository
    private val saleRepository: SaleRepository
    
    private val _stocks = MutableStateFlow<List<RubberStock>>(emptyList())
    val stocks: StateFlow<List<RubberStock>> = _stocks.asStateFlow()
    
    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales.asStateFlow()
    
    private val _totalRolls = MutableStateFlow(0)
    val totalRolls: StateFlow<Int> = _totalRolls.asStateFlow()
    
    private val _totalWeight = MutableStateFlow(0.0)
    val totalWeight: StateFlow<Double> = _totalWeight.asStateFlow()
    
    private val _stockByType = MutableStateFlow<List<StockByType>>(emptyList())
    val stockByType: StateFlow<List<StockByType>> = _stockByType.asStateFlow()
    
    private val _totalRevenue = MutableStateFlow(0.0)
    val totalRevenue: StateFlow<Double> = _totalRevenue.asStateFlow()
    
    private val _exportPath = MutableStateFlow<String>("")
    val exportPath: StateFlow<String> = _exportPath.asStateFlow()
    
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
            }
        }
        
        viewModelScope.launch {
            stockRepository.totalWeight.collect { total ->
                _totalWeight.value = total ?: 0.0
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
            }
        }
        
        viewModelScope.launch {
            saleRepository.totalRevenue.collect { revenue ->
                _totalRevenue.value = revenue ?: 0.0
            }
        }
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
                soldFor = soldFor
            )
            saleRepository.insert(sale)
        }
    }
    
    fun clearAllStock() {
        viewModelScope.launch {
            // This will delete all records from the table
            stockRepository.deleteAll()
        }
    }
    
    fun clearAllSales() {
        viewModelScope.launch {
            saleRepository.deleteAll()
        }
    }
    
    fun exportDatabase() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val exportFile = File(downloadsDir, "rubber_inventory.db")
                    
                    val dbFile = File(dbPath)
                    if (dbFile.exists()) {
                        FileInputStream(dbFile).use { input ->
                            FileOutputStream(exportFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        
                        _exportPath.value = exportFile.absolutePath
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                getApplication(),
                                "Database exported to Downloads folder!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
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
