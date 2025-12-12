package com.hellodev.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hellodev.app.navigation.Screen
import com.hellodev.app.ui.theme.HelloDevAppTheme
import com.hellodev.app.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        }
    }
    
    private var viewModelInstance: InventoryViewModel? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HelloDevAppTheme {
                val viewModel: InventoryViewModel = viewModel()
                viewModelInstance = viewModel
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        viewModel = viewModel,
                        onExportClick = { checkAndExport() }
                    )
                }
            }
        }
    }
    
    private fun checkAndExport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            viewModelInstance?.exportToExcel()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    viewModelInstance?.exportToExcel()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        } else {
            viewModelInstance?.exportToExcel()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: InventoryViewModel,
    onExportClick: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        scope.launch {
                            navController.navigate(route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = false }
                                launchSingleTop = true
                            }
                            drawerState.close()
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (currentRoute) {
                                Screen.Dashboard.route -> "Goyal Tyres & Rubbers"
                                Screen.AddStock.route -> "Add New Stock"
                                Screen.Sales.route -> "Sales Records"
                                Screen.AddSale.route -> "Record Sale"
                                else -> "Inventory"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(viewModel, navController, onExportClick)
                }
                composable(Screen.AddStock.route) {
                    AddStockScreen(viewModel, navController)
                }
                composable(Screen.Sales.route) {
                    SalesScreen(viewModel, navController)
                }
                composable(Screen.AddSale.route) {
                    AddSaleScreen(viewModel, navController)
                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Goyal Tyres",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Divider()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Dashboard, null) },
            label = { Text("Dashboard") },
            selected = currentRoute == Screen.Dashboard.route,
            onClick = { onNavigate(Screen.Dashboard.route) }
        )
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.AddBox, null) },
            label = { Text("Add Stock") },
            selected = currentRoute == Screen.AddStock.route,
            onClick = { onNavigate(Screen.AddStock.route) }
        )
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Receipt, null) },
            label = { Text("Sales Records") },
            selected = currentRoute == Screen.Sales.route,
            onClick = { onNavigate(Screen.Sales.route) }
        )
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.ShoppingCart, null) },
            label = { Text("Add Sale") },
            selected = currentRoute == Screen.AddSale.route,
            onClick = { onNavigate(Screen.AddSale.route) }
        )
    }
}

@Composable
fun DashboardScreen(
    viewModel: InventoryViewModel,
    navController: NavHostController,
    onExportClick: () -> Unit
) {
    val stocks by viewModel.stocks.collectAsState()
    val availableRolls by viewModel.availableRolls.collectAsState()
    val availableWeight by viewModel.availableWeight.collectAsState()
    val totalStockWorth by viewModel.totalStockWorth.collectAsState()
    val stockByType by viewModel.stockByType.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val exportPath by viewModel.exportPath.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Statistics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.hellodev.app.ui.StatCard(
                    title = "Available Rolls",
                    value = availableRolls.toString(),
                    subtitle = "After sales deduction",
                    icon = Icons.Default.Inventory,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )
                
                com.hellodev.app.ui.StatCard(
                    title = "Available Weight",
                    value = String.format("%.1f kg", availableWeight),
                    subtitle = "After sales deduction",
                    icon = Icons.Default.Scale,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.hellodev.app.ui.StatCard(
                    title = "Stock Worth",
                    value = "₹" + String.format("%.0f", totalStockWorth),
                    subtitle = "Total investment",
                    icon = Icons.Default.AccountBalance,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
                
                com.hellodev.app.ui.StatCard(
                    title = "Total Revenue",
                    value = "₹" + String.format("%.0f", totalRevenue),
                    subtitle = "All time sales",
                    icon = Icons.Default.AttachMoney,
                    color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Pie Chart
        if (stockByType.isNotEmpty()) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Stock Distribution by Type",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        com.hellodev.app.ui.EnhancedPieChart(stockByType)
                    }
                }
            }
        }
        
        // Export Button
        item {
            Button(
                onClick = {
                    viewModel.exportToExcel()
                    onExportClick()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.TableChart, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export to Excel")
            }
        }
        
        if (exportPath.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "✓ Exported to: $exportPath",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp
                    )
                }
            }
        }
        
        // Current Stock Table
        item {
            Text(
                text = "Current Stock (${stocks.size} items)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(stocks) { stock ->
            StockCard(stock)
        }
    }
}

@Composable
fun StockCard(stock: com.hellodev.app.data.RubberStock) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stock.rubberName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "#${stock.id}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Rolls: ${stock.numberOfRolls}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Weight: ${stock.weightInKg} kg",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Cost",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "₹${String.format("%.0f", stock.costOfStock)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Worth: ₹${String.format("%.0f", stock.stockWorth)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(stock.addedDate)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun AddStockScreen(
    viewModel: InventoryViewModel,
    navController: NavHostController
) {
    var rubberName by remember { mutableStateOf("") }
    var numberOfRolls by remember { mutableStateOf("") }
    var weightInKg by remember { mutableStateOf("") }
    var costOfStock by remember { mutableStateOf("") }
    
    val stocks by viewModel.stocks.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Add New Rubber Stock",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        OutlinedTextField(
            value = rubberName,
            onValueChange = { rubberName = it },
            label = { Text("Rubber Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = numberOfRolls,
            onValueChange = { numberOfRolls = it },
            label = { Text("Number of Rolls") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = weightInKg,
            onValueChange = { weightInKg = it },
            label = { Text("Weight (kg)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = costOfStock,
            onValueChange = { costOfStock = it },
            label = { Text("Cost of Stock (₹)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (rubberName.isNotBlank() && numberOfRolls.isNotBlank() &&
                        weightInKg.isNotBlank() && costOfStock.isNotBlank()) {
                        viewModel.insertStock(
                            rubberName,
                            numberOfRolls.toIntOrNull() ?: 0,
                            weightInKg.toDoubleOrNull() ?: 0.0,
                            costOfStock.toDoubleOrNull() ?: 0.0
                        )
                        rubberName = ""
                        numberOfRolls = ""
                        weightInKg = ""
                        costOfStock = ""
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Stock")
            }
            
            OutlinedButton(
                onClick = { viewModel.clearAllStock() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Delete, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear All")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Recent Stock (${stocks.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(stocks.take(5)) { stock ->
                StockCard(stock)
            }
        }
    }
}

@Composable
fun SalesScreen(
    viewModel: InventoryViewModel,
    navController: NavHostController
) {
    val filteredSales by viewModel.filteredSales.collectAsState()
    val filteredRevenue by viewModel.filteredRevenue.collectAsState()
    val selectedFilter by viewModel.selectedDateFilter.collectAsState()
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sales Records",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = { navController.navigate(Screen.AddSale.route) }) {
                    Icon(Icons.Default.Add, "Add Sale", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
            }
        }
        
        item {
            com.hellodev.app.ui.StatCard(
                title = "Revenue (${selectedFilter.name.replace("_", " ")})",
                value = "₹${String.format("%.0f", filteredRevenue)}",
                subtitle = "${filteredSales.size} sales",
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            ScrollableTabRow(
                selectedTabIndex = com.hellodev.app.viewmodel.DateFilter.values().indexOf(selectedFilter),
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 0.dp
            ) {
                com.hellodev.app.viewmodel.DateFilter.values().forEach { filter ->
                    Tab(
                        selected = selectedFilter == filter,
                        onClick = { viewModel.setDateFilter(filter) },
                        text = {
                            Text(
                                filter.name.replace("_", " "),
                                fontSize = 13.sp,
                                fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }
        
        if (filteredSales.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No sales recorded for this period", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            items(filteredSales) { sale ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sale.rubberName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "₹${String.format("%.0f", sale.soldFor)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Column {
                                Text(
                                    "Rolls",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    sale.numberOfRolls.toString(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Column {
                                Text(
                                    "Weight",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    "${sale.weightInKg} kg",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = dateFormat.format(Date(sale.saleDate)),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddSaleScreen(
    viewModel: InventoryViewModel,
    navController: NavHostController
) {
    var rubberName by remember { mutableStateOf("") }
    var numberOfRolls by remember { mutableStateOf("") }
    var weightInKg by remember { mutableStateOf("") }
    var soldFor by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Record New Sale",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        OutlinedTextField(
            value = rubberName,
            onValueChange = { rubberName = it },
            label = { Text("Rubber Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = numberOfRolls,
            onValueChange = { numberOfRolls = it },
            label = { Text("Number of Rolls") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = weightInKg,
            onValueChange = { weightInKg = it },
            label = { Text("Weight of Rubber (kg)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = soldFor,
            onValueChange = { soldFor = it },
            label = { Text("Amount Sold For (₹)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (rubberName.isNotBlank() && numberOfRolls.isNotBlank() &&
                    weightInKg.isNotBlank() && soldFor.isNotBlank()) {
                    viewModel.insertSale(
                        rubberName,
                        numberOfRolls.toIntOrNull() ?: 0,
                        weightInKg.toDoubleOrNull() ?: 0.0,
                        soldFor.toDoubleOrNull() ?: 0.0
                    )
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ShoppingCart, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Record Sale")
        }
    }
}
