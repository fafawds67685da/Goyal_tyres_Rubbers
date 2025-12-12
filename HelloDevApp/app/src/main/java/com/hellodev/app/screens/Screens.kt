package com.hellodev.app.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.hellodev.app.data.StockCategory
import com.hellodev.app.navigation.Screen
import com.hellodev.app.ui.EnhancedPieChart
import com.hellodev.app.ui.StatCard
import com.hellodev.app.utils.NumberFormatter
import com.hellodev.app.viewmodel.DateFilter
import com.hellodev.app.viewmodel.InventoryViewModel
import androidx.compose.foundation.text.KeyboardOptions
import java.text.SimpleDateFormat
import java.util.*

private val dateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

// ========== SALES RECORDS SCREEN ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesRecordsScreen(viewModel: InventoryViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val filteredSales by viewModel.filteredSales.collectAsState()
    val filteredRevenue by viewModel.filteredRevenue.collectAsState()
    val selectedFilter by viewModel.selectedDateFilter.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var isRevenueVisible by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Revenue Card with visibility toggle
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccountBox, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Total Revenue (Paid)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (isRevenueVisible) NumberFormatter.formatIndianCurrency(filteredRevenue) else "••••••",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "From ${selectedFilter.name.lowercase().replace('_', ' ')} sales",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = { isRevenueVisible = !isRevenueVisible }) {
                    Icon(
                        if (isRevenueVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isRevenueVisible) "Hide revenue" else "Show revenue",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // Date Filter Tabs
        ScrollableTabRow(
            selectedTabIndex = DateFilter.values().indexOf(selectedFilter),
            modifier = Modifier.fillMaxWidth()
        ) {
            DateFilter.values().forEach { filter ->
                Tab(
                    selected = selectedFilter == filter,
                    onClick = { viewModel.setDateFilter(filter) },
                    text = { 
                        Text(
                            filter.name.replace('_', ' ').lowercase()
                                .replaceFirstChar { it.uppercase() }
                        ) 
                    }
                )
            }
        }
        
        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { navController.navigate(Screen.AddSale.route) },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add Sale")
            }
            
            OutlinedButton(
                onClick = { showExportDialog = true },
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Export")
            }
            
            OutlinedButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Clear")
            }
        }
        
        // Sales List
        if (filteredSales.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No sales recorded yet", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredSales) { sale ->
                    SaleCard(sale = sale, onDelete = { viewModel.deleteSale(sale) })
                }
            }
        }
    }
    
    // Clear Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Sales?") },
            text = { Text("This will permanently delete all sales records. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllSales()
                        showClearDialog = false
                        Toast.makeText(context, "Sales cleared", Toast.LENGTH_SHORT).show()
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
            title = { Text("Export Sales") },
            text = { Text("Export sales data as CSV file") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = viewModel.exportSalesToCSV()
                        if (intent != null) {
                            context.startActivity(intent)
                            Toast.makeText(context, "Exporting sales...", Toast.LENGTH_SHORT).show()
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
fun SaleCard(sale: Sale, onDelete: () -> Unit = {}) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sale.rubberName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${sale.rubberId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (sale.paymentStatus == "PAID") 
                            Color(0xFF4CAF50) else Color(0xFFFF9800)
                    ) {
                        Text(
                            text = sale.paymentStatus,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Dealer: ${sale.dealerName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${sale.numberOfRolls} rolls • ${sale.weightInKg} kg",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = NumberFormatter.formatIndianCurrency(sale.soldFor),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = dateFormatter.format(Date(sale.saleDate)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (sale.paymentReceivedDate != null) {
                Text(
                    text = "Payment received: ${dateFormatter.format(Date(sale.paymentReceivedDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Sale?") },
            text = { Text("Are you sure you want to delete this sale record? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ========== DASHBOARD SCREEN ==========
@Composable
fun DashboardScreen(viewModel: InventoryViewModel, navController: NavHostController) {
    // Collect all state
    val availableRolls by viewModel.availableRolls.collectAsState()
    val availableWeight by viewModel.availableWeight.collectAsState()
    val totalStockWorth by viewModel.totalStockWorth.collectAsState()
    val pendingDue by viewModel.totalPendingDue.collectAsState()
    val activeDrafts by viewModel.drafts.collectAsState()
    val lowStockCategories by viewModel.lowStockCategories.collectAsState()
    val recentActivity by viewModel.recentActivity.collectAsState()
    val topSellingCategory by viewModel.topSellingCategory.collectAsState()
    val todayRevenue by viewModel.todayRevenue.collectAsState()
    val todayRollsSold by viewModel.todayRollsSold.collectAsState()
    val todaySalesCount by viewModel.todaySalesCount.collectAsState()
    val stockByType by viewModel.stockByType.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    val currencyFormat = remember { java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "IN")) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Business Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Low Stock Alert Banner
        if (lowStockCategories.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Low Stock Alert!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "${lowStockCategories.size} categories need restocking",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        // Key Metrics Grid (2x3)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardStatCard(
                        title = "Available Stock",
                        value = NumberFormatter.formatNumber(availableRolls),
                        subtitle = "${NumberFormatter.formatNumber(availableWeight)} kg",
                        icon = Icons.Default.List,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        healthStatus = if (lowStockCategories.isEmpty()) HealthStatus.HEALTHY else HealthStatus.WARNING,
                        modifier = Modifier.weight(1f)
                    )
                    
                    DashboardStatCard(
                        title = "Stock Value",
                        value = currencyFormat.format(totalStockWorth),
                        subtitle = "Inventory worth",
                        icon = Icons.Default.Face,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardStatCard(
                        title = "Pending Dues",
                        value = currencyFormat.format(pendingDue),
                        subtitle = "Awaiting payment",
                        icon = Icons.Default.DateRange,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        healthStatus = if (pendingDue > 10000) HealthStatus.WARNING else HealthStatus.HEALTHY,
                        modifier = Modifier.weight(1f)
                    )
                    
                    DashboardStatCard(
                        title = "Active Drafts",
                        value = activeDrafts.size.toString(),
                        subtitle = "Pending commits",
                        icon = Icons.Default.Edit,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { navController.navigate(Screen.StockDrafts.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardStatCard(
                        title = "Today's Sales",
                        value = todaySalesCount.toString(),
                        subtitle = "$todayRollsSold rolls sold",
                        icon = Icons.Default.ShoppingCart,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    
                    DashboardStatCard(
                        title = "Today's Revenue",
                        value = currencyFormat.format(todayRevenue),
                        subtitle = "Paid today",
                        icon = Icons.Default.Check,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Top Selling Category
        topSellingCategory?.let { top ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Top Performer This Month",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = top.categoryName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${top.totalRollsSold} rolls • ${currencyFormat.format(top.totalRevenue)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        // Stock Distribution Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Stock Distribution by Category",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${categories.size} categories • $availableRolls total rolls",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (stockByType.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No stock data available",
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        EnhancedPieChart(data = stockByType)
                    }
                }
            }
        }
        
        // Recent Activity
        if (recentActivity.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Recent Activity",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        recentActivity.take(5).forEach { activity ->
                            RecentActivityItem(activity = activity, currencyFormat = currencyFormat)
                            if (activity != recentActivity.take(5).last()) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }
        
        // Quick Actions
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.Add,
                    label = "New Sale",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    onClick = { navController.navigate(Screen.AddSale.route) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Default.Create,
                    label = "Stock Drafts",
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = { navController.navigate(Screen.StockDrafts.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.Face,
                    label = "Pending Payments",
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    onClick = { navController.navigate(Screen.PendingPayments.route) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Default.DateRange,
                    label = "History",
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { navController.navigate(Screen.History.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Footer spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ========== DASHBOARD COMPONENTS ==========
enum class HealthStatus {
    HEALTHY, WARNING, CRITICAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    healthStatus: HealthStatus? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                healthStatus?.let { status ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = when (status) {
                                    HealthStatus.HEALTHY -> Color(0xFF4CAF50)
                                    HealthStatus.WARNING -> Color(0xFFFF9800)
                                    HealthStatus.CRITICAL -> Color(0xFFF44336)
                                },
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecentActivityItem(
    activity: com.hellodev.app.viewmodel.RecentActivity,
    currencyFormat: java.text.NumberFormat
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when (activity.type) {
                            com.hellodev.app.viewmodel.ActivityType.STOCK_ADDED -> 
                                MaterialTheme.colorScheme.primaryContainer
                            com.hellodev.app.viewmodel.ActivityType.SALE_MADE -> 
                                MaterialTheme.colorScheme.secondaryContainer
                            com.hellodev.app.viewmodel.ActivityType.PAYMENT_RECEIVED -> 
                                MaterialTheme.colorScheme.tertiaryContainer
                        },
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (activity.type) {
                        com.hellodev.app.viewmodel.ActivityType.STOCK_ADDED -> Icons.Default.Add
                        com.hellodev.app.viewmodel.ActivityType.SALE_MADE -> Icons.Default.ShoppingCart
                        com.hellodev.app.viewmodel.ActivityType.PAYMENT_RECEIVED -> Icons.Default.Check
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
                Text(
                    text = "${dateFormat.format(Date(activity.timestamp))} • ${timeFormat.format(Date(activity.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        activity.amount?.let { amount ->
            Text(
                text = currencyFormat.format(amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (activity.type == com.hellodev.app.viewmodel.ActivityType.STOCK_ADDED) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}
