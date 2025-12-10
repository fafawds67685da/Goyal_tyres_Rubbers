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
            viewModelInstance?.exportDatabase()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    viewModelInstance?.exportDatabase()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        } else {
            viewModelInstance?.exportDatabase()
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
    val totalRolls by viewModel.totalRolls.collectAsState()
    val totalWeight by viewModel.totalWeight.collectAsState()
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
                StatCard(
                    title = "Total Rolls",
                    value = totalRolls.toString(),
                    icon = Icons.Default.Inventory,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )
                
                StatCard(
                    title = "Total Weight",
                    value = String.format("%.2f kg", totalWeight),
                    icon = Icons.Default.Scale,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            StatCard(
                title = "Total Revenue",
                value = "₹" + String.format("%.2f", totalRevenue),
                icon = Icons.Default.AttachMoney,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Pie Chart
        if (stockByType.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Stock Distribution by Type",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SimplePieChart(stockByType)
                    }
                }
            }
        }
        
        // Export Button
        item {
            Button(
                onClick = {
                    viewModel.exportDatabase()
                    onExportClick()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Database")
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
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun SimplePieChart(data: List<com.hellodev.app.data.StockByType>) {
    val total = data.sumOf { it.totalWeight }
    val colors = listOf(
        Color(0xFF6200EA),
        Color(0xFF03DAC5),
        Color(0xFFFF6F00),
        Color(0xFFE91E63),
        Color(0xFF4CAF50),
        Color(0xFF2196F3)
    )
    
    Column {
        // Legend
        data.forEachIndexed { index, item ->
            val percentage = (item.totalWeight / total * 100).toInt()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(colors[index % colors.size])
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${item.rubberName}: ${String.format("%.1f", item.totalWeight)}kg ($percentage%)",
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun StockCard(stock: com.hellodev.app.data.RubberStock) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.rubberName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Rolls: ${stock.numberOfRolls}", fontSize = 14.sp)
                Text("Weight: ${stock.weightInKg} kg", fontSize = 14.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${stock.costOfStock}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "#${stock.id}",
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
    val sales by viewModel.sales.collectAsState()
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sales Records (${sales.size})",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = { navController.navigate(Screen.AddSale.route) }) {
                Icon(Icons.Default.Add, "Add Sale", tint = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (sales.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No sales recorded yet", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sales) { sale ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = sale.rubberName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₹${sale.soldFor}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Rolls: ${sale.numberOfRolls}", fontSize = 14.sp)
                                Text("Weight: ${sale.weightInKg} kg", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
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
