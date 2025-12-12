package com.hellodev.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.hellodev.app.data.RubberStock
import com.hellodev.app.data.Sale
import com.hellodev.app.data.StockCategory
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {
    
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val fileNameFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    /**
     * Exports sales data to CSV and returns share intent
     */
    fun exportSalesToCSV(
        context: Context,
        sales: List<Sale>,
        includeAll: Boolean = true
    ): Intent? {
        return try {
            val fileName = "Sales_${fileNameFormatter.format(Date())}.csv"
            val file = createCsvFile(context, fileName)
            
            FileWriter(file).use { writer ->
                // Header
                writer.append("Sale ID,Rubber Name,Rubber ID,Dealer Name,Rolls,Weight (kg),Amount (₹),Sale Date,Payment Status,Payment Received Date\n")
                
                // Data
                sales.forEach { sale ->
                    writer.append("${sale.id},")
                    writer.append("${sale.rubberName},")
                    writer.append("${sale.rubberId},")
                    writer.append("${sale.dealerName},")
                    writer.append("${sale.numberOfRolls},")
                    writer.append("${sale.weightInKg},")
                    writer.append("${sale.soldFor},")
                    writer.append("${dateFormatter.format(Date(sale.saleDate))},")
                    writer.append("${sale.paymentStatus},")
                    writer.append("${sale.paymentReceivedDate?.let { dateFormatter.format(Date(it)) } ?: "N/A"}\n")
                }
            }
            
            createShareIntent(context, file, "text/csv")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Exports stock data to CSV
     */
    fun exportStockToCSV(
        context: Context,
        stock: List<RubberStock>
    ): Intent? {
        return try {
            val fileName = "Stock_${fileNameFormatter.format(Date())}.csv"
            val file = createCsvFile(context, fileName)
            
            FileWriter(file).use { writer ->
                // Header
                writer.append("Stock ID,Rubber Name,Rubber ID,Number of Rolls,Weight (kg),Cost of Stock (₹),Stock Worth (₹),Added Date\n")
                
                // Data
                stock.forEach { item ->
                    writer.append("${item.id},")
                    writer.append("${item.rubberName},")
                    writer.append("${item.rubberId},")
                    writer.append("${item.numberOfRolls},")
                    writer.append("${item.weightInKg},")
                    writer.append("${item.costOfStock},")
                    writer.append("${item.stockWorth},")
                    writer.append("${dateFormatter.format(Date(item.addedDate))}\n")
                }
            }
            
            createShareIntent(context, file, "text/csv")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Exports pending payments to CSV
     */
    fun exportPendingPaymentsToCSV(
        context: Context,
        pendingSales: List<Sale>
    ): Intent? {
        return try {
            val fileName = "PendingPayments_${fileNameFormatter.format(Date())}.csv"
            val file = createCsvFile(context, fileName)
            
            FileWriter(file).use { writer ->
                // Header
                writer.append("Sale ID,Dealer Name,Rubber Type,Rubber ID,Amount (₹),Sale Date\n")
                
                // Data
                pendingSales.forEach { sale ->
                    writer.append("${sale.id},")
                    writer.append("${sale.dealerName},")
                    writer.append("${sale.rubberName},")
                    writer.append("${sale.rubberId},")
                    writer.append("${sale.soldFor},")
                    writer.append("${dateFormatter.format(Date(sale.saleDate))}\n")
                }
                
                // Total
                val total = pendingSales.sumOf { it.soldFor }
                writer.append("\n")
                writer.append("TOTAL PENDING DUE,,,,,${total}\n")
            }
            
            createShareIntent(context, file, "text/csv")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Exports categories to CSV
     */
    fun exportCategoriesToCSV(
        context: Context,
        categories: List<StockCategory>
    ): Intent? {
        return try {
            val fileName = "StockCategories_${fileNameFormatter.format(Date())}.csv"
            val file = createCsvFile(context, fileName)
            
            FileWriter(file).use { writer ->
                // Header
                writer.append("Category ID,Rubber Name,Rubber ID,Added Date\n")
                
                // Data
                categories.forEach { category ->
                    writer.append("${category.id},")
                    writer.append("${category.rubberName},")
                    writer.append("${category.rubberId},")
                    writer.append("${dateFormatter.format(Date(category.addedDate))}\n")
                }
            }
            
            createShareIntent(context, file, "text/csv")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Creates a CSV file in the cache directory
     */
    private fun createCsvFile(context: Context, fileName: String): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        return File(exportDir, fileName)
    }
    
    /**
     * Creates a share intent for the file
     */
    private fun createShareIntent(context: Context, file: File, mimeType: String): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        return Intent.createChooser(shareIntent, "Share via")
    }
}
