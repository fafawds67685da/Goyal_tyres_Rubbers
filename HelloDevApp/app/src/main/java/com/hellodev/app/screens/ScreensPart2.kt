package com.hellodev.app.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.hellodev.app.data.RubberStock
import com.hellodev.app.data.Sale
import com.hellodev.app.navigation.Screen
import com.hellodev.app.ui.StatCard
import com.hellodev.app.utils.NumberFormatter
import com.hellodev.app.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.*

private val dateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

// ========== ADD STOCK SCREEN ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockScreen(viewModel: InventoryViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    
    var selectedCategory by remember { mutableStateOf<Int?>(null) }
    var rubberName by remember { mutableStateOf("") }
    var rubberId by remember { mutableStateOf(0) }
    var numberOfRolls by remember { mutableStateOf("") }
    var weightInKg by remember { mutableStateOf("") }
    var costOfStock by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Add New Stock",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            if (categories.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "No categories available!",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "Please add stock categories first from the Stock Categories page.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { navController.navigate(Screen.StockCategories.route) }
                        ) {
                            Text("Go to Categories")
                        }
                    }
                }
            } else {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = if (selectedCategory != null) {
                            val cat = categories.find { it.id == selectedCategory }
                            "${cat?.rubberName} (ID: ${cat?.rubberId})"
                        } else "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Rubber Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text("${category.rubberName} (ID: ${category.rubberId})") },
                                onClick = {
                                    selectedCategory = category.id
                                    rubberName = category.rubberName
                                    rubberId = category.rubberId
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        
        item {
            OutlinedTextField(
                value = numberOfRolls,
                onValueChange = { 
                    numberOfRolls = it
                    // Clear weight if number of rolls changes
                    weightInKg = ""
                },
                label = { Text("Number of Rolls") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.List, contentDescription = null) }
            )
        }
        
        item {
            OutlinedTextField(
                value = weightInKg,
                onValueChange = { weightInKg = it },
                label = { Text("Weight (kg)") },
                supportingText = { 
                    Text(
                        "Enter weight per roll, or comma-separated weights for multiple rolls",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
            )
        }
        
        item {
            OutlinedTextField(
                value = costOfStock,
                onValueChange = { costOfStock = it },
                label = { Text("Cost of Stock (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) }
            )
        }
        
        item {
            if (costOfStock.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Stock Worth",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            NumberFormatter.formatIndianCurrency(costOfStock.toDoubleOrNull() ?: 0.0),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        if (selectedCategory == null || numberOfRolls.isEmpty() || 
                            weightInKg.isEmpty() || costOfStock.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val rolls = numberOfRolls.toIntOrNull() ?: 0
                        
                        // Parse weight - either single value or comma-separated
                        val weights = weightInKg.split(",").map { it.trim().toDoubleOrNull() ?: 0.0 }
                        
                        // Validate weight input
                        if (weights.isEmpty() || weights.all { it == 0.0 }) {
                            Toast.makeText(context, "Invalid weight format", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        // Calculate total weight
                        val totalWeight = if (weights.size == 1) {
                            // Single weight value - multiply by number of rolls
                            weights[0] * rolls
                        } else {
                            // Multiple weights - sum them all
                            if (weights.size != rolls) {
                                Toast.makeText(
                                    context, 
                                    "Number of weights (${weights.size}) must match number of rolls ($rolls)",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }
                            weights.sum()
                        }
                        
                        val stock = RubberStock(
                            rubberName = rubberName,
                            rubberId = rubberId,
                            numberOfRolls = rolls,
                            weightInKg = totalWeight,
                            costOfStock = costOfStock.toDoubleOrNull() ?: 0.0
                        )
                        
                        viewModel.insertStock(stock)
                        Toast.makeText(context, "Stock added successfully", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = categories.isNotEmpty()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Stock")
                }
            }
        }
    }
}

// ========== ADD SALE SCREEN ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleScreen(viewModel: InventoryViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    val allStock by viewModel.stocks.collectAsState()
    
    var selectedCategory by remember { mutableStateOf<Int?>(null) }
    var rubberName by remember { mutableStateOf("") }
    var rubberId by remember { mutableStateOf(0) }
    var dealerName by remember { mutableStateOf("") }
    var numberOfRolls by remember { mutableStateOf("") }
    var weightInKg by remember { mutableStateOf("") }
    var soldFor by remember { mutableStateOf("") }
    var paymentStatus by remember { mutableStateOf("PENDING") }
    var expanded by remember { mutableStateOf(false) }
    
    // Calculate available stock for selected category
    val availableStock = remember(selectedCategory, allStock) {
        if (selectedCategory == null) null
        else {
            val categoryRubberId = categories.find { it.id == selectedCategory }?.rubberId
            allStock.filter { it.rubberId == categoryRubberId }
                .let { stocks ->
                    val totalRolls = stocks.sumOf { it.numberOfRolls }
                    val totalWeight = stocks.sumOf { it.weightInKg }
                    Pair(totalRolls, totalWeight)
                }
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Record New Sale",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            if (categories.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "No categories available!",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text("Please add stock categories first.")
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { navController.navigate(Screen.StockCategories.route) }
                        ) {
                            Text("Go to Categories")
                        }
                    }
                }
            } else {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = if (selectedCategory != null) {
                            val cat = categories.find { it.id == selectedCategory }
                            "${cat?.rubberName} (ID: ${cat?.rubberId})"
                        } else "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Rubber Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text("${category.rubberName} (ID: ${category.rubberId})") },
                                onClick = {
                                    selectedCategory = category.id
                                    rubberName = category.rubberName
                                    rubberId = category.rubberId
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        
        item {
            if (availableStock != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Available Stock",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "${availableStock.first} rolls | ${String.format("%.2f", availableStock.second)} kg",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
        
        item {
            OutlinedTextField(
                value = dealerName,
                onValueChange = { dealerName = it },
                label = { Text("Dealer/Customer Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )
        }
        
        item {
            OutlinedTextField(
                value = numberOfRolls,
                onValueChange = { numberOfRolls = it },
                label = { Text("Number of Rolls Sold") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.List, contentDescription = null) }
            )
        }
        
        item {
            OutlinedTextField(
                value = weightInKg,
                onValueChange = { weightInKg = it },
                label = { Text("Weight Sold (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
            )
        }
        
        item {
            OutlinedTextField(
                value = soldFor,
                onValueChange = { soldFor = it },
                label = { Text("Sold For (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) }
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Payment Status",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = paymentStatus == "PAID",
                            onClick = { paymentStatus = "PAID" }
                        )
                        Text("Paid", modifier = Modifier.padding(start = 8.dp))
                        
                        Spacer(Modifier.width(32.dp))
                        
                        RadioButton(
                            selected = paymentStatus == "PENDING",
                            onClick = { paymentStatus = "PENDING" }
                        )
                        Text("Pending", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
        
        item {
            if (soldFor.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (paymentStatus == "PAID") 
                            MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Total Amount",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            NumberFormatter.formatIndianCurrency(soldFor.toDoubleOrNull() ?: 0.0),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (paymentStatus == "PAID") "Will be added to revenue" 
                            else "Will be added to pending payments",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        if (selectedCategory == null || dealerName.isEmpty() || 
                            numberOfRolls.isEmpty() || weightInKg.isEmpty() || soldFor.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val saleRolls = numberOfRolls.toIntOrNull() ?: 0
                        val saleWeight = weightInKg.toDoubleOrNull() ?: 0.0
                        
                        // Validate stock availability
                        if (availableStock != null) {
                            if (saleRolls > availableStock.first) {
                                Toast.makeText(
                                    context, 
                                    "Error: Only ${availableStock.first} rolls available in stock",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }
                            if (saleWeight > availableStock.second) {
                                Toast.makeText(
                                    context, 
                                    "Error: Only ${String.format("%.2f", availableStock.second)} kg available in stock",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }
                        }
                        
                        val sale = Sale(
                            rubberName = rubberName,
                            rubberId = rubberId,
                            dealerName = dealerName,
                            numberOfRolls = saleRolls,
                            weightInKg = saleWeight,
                            soldFor = soldFor.toDoubleOrNull() ?: 0.0,
                            paymentStatus = paymentStatus,
                            paymentReceivedDate = if (paymentStatus == "PAID") System.currentTimeMillis() else null
                        )
                        
                        viewModel.insertSale(sale)
                        Toast.makeText(
                            context, 
                            "Sale recorded ${if (paymentStatus == "PAID") "and payment received" else "as pending"}",
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = categories.isNotEmpty()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Record Sale")
                }
            }
        }
    }
}

// ========== STOCK DETAILS SCREEN ==========
@Composable
fun StockDetailsScreen(viewModel: InventoryViewModel) {
    val context = LocalContext.current
    val stocks by viewModel.stocks.collectAsState()
    val totalStockWorth by viewModel.totalStockWorth.collectAsState()
    val totalRollsSold by viewModel.totalRollsSold.collectAsState()
    val totalWeightSold by viewModel.totalWeightSold.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Stock Worth Card
        StatCard(
            title = "Total Stock Worth",
            value = NumberFormatter.formatIndianCurrency(totalStockWorth),
            subtitle = "Value of remaining inventory",
            icon = Icons.Default.AccountBox,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(16.dp)
        )
        
        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { showExportDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Export")
            }
            
            OutlinedButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Clear")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Stock List - Grouped by Category
        if (stocks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No stock available", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            // Group stocks by rubberId
            val groupedStocks = remember(stocks) {
                stocks.groupBy { it.rubberId }
                    .map { (rubberId, stockList) ->
                        val firstStock = stockList.first()
                        RubberStock(
                            id = 0, // Aggregated, no single ID
                            rubberName = firstStock.rubberName,
                            rubberId = rubberId,
                            numberOfRolls = stockList.sumOf { it.numberOfRolls },
                            weightInKg = stockList.sumOf { it.weightInKg },
                            costOfStock = stockList.sumOf { it.costOfStock },
                            addedDate = firstStock.addedDate
                        )
                    }
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(groupedStocks) { stock ->
                    StockCard(stock = stock)
                }
            }
        }
    }
    
    // Clear Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Stock?") },
            text = { Text("This will permanently delete all stock records. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllStock()
                        showClearDialog = false
                        Toast.makeText(context, "Stock cleared", Toast.LENGTH_SHORT).show()
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }
    
    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Stock Details") },
            text = { Text("Export stock data as CSV file") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = viewModel.exportStockToCSV()
                        if (intent != null) {
                            context.startActivity(intent)
                            Toast.makeText(context, "Exporting stock...", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                        }
                        showExportDialog = false
                    }
                ) { Text("Export CSV") }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun StockCard(stock: RubberStock) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stock.rubberName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${stock.rubberId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = NumberFormatter.formatIndianCurrency(stock.stockWorth),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total: ${stock.numberOfRolls} rolls", fontWeight = FontWeight.Medium)
                    Text("Available: ${stock.numberOfRolls} rolls", color = Color(0xFF4CAF50))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total: ${stock.weightInKg} kg", fontWeight = FontWeight.Medium)
                    Text("Available: ${stock.weightInKg} kg", color = Color(0xFF4CAF50))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Added: ${dateFormatter.format(Date(stock.addedDate))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
