package com.hellodev.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hellodev.app.data.RubberStock
import com.hellodev.app.data.Sale
import com.hellodev.app.data.StockCategory
import com.hellodev.app.navigation.Screen
import com.hellodev.app.screens.*
import com.hellodev.app.ui.EnhancedPieChart
import com.hellodev.app.ui.StatCard
import com.hellodev.app.ui.theme.HelloDevAppTheme
import com.hellodev.app.utils.NumberFormatter
import com.hellodev.app.viewmodel.DateFilter
import com.hellodev.app.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request storage permission for Android 12 and below
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        
        setContent {
            HelloDevAppTheme {
                val navController = rememberNavController()
                val viewModel: InventoryViewModel = viewModel()
                
                MainScreen(navController = navController, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController, viewModel: InventoryViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    
    val currentScreen = when (currentRoute) {
        Screen.AIAssistant.route -> Screen.AIAssistant
        Screen.Dashboard.route -> Screen.Dashboard
                Screen.SalesRecords.route -> Screen.SalesRecords
                Screen.AddSale.route -> Screen.AddSale
                Screen.MakeBill.route -> Screen.MakeBill
                Screen.PendingPayments.route -> Screen.PendingPayments
                Screen.AddStock.route -> Screen.AddStock
                Screen.StockCategories.route -> Screen.StockCategories
                Screen.History.route -> Screen.History
                else -> Screen.Dashboard
            }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentScreen = currentScreen,
                onNavigate = { screen ->
                    scope.launch {
                        drawerState.close()
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen.title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.AIAssistant.route) { AIAssistantScreen() }
                composable(Screen.Dashboard.route) { DashboardScreen(viewModel, navController) }
                composable(Screen.SalesRecords.route) { SalesRecordsScreen(viewModel, navController) }
                composable(Screen.AddSale.route) { AddSaleScreen(viewModel, navController) }
                composable(Screen.MakeBill.route) { MakeBillScreen() }
                composable(Screen.PendingPayments.route) { PendingPaymentsScreen(viewModel) }
                composable(Screen.AddStock.route) { AddStockScreen(viewModel, navController) }
                composable(Screen.StockCategories.route) { StockCategoriesScreen(viewModel) }
                composable(Screen.History.route) { HistoryScreen(viewModel) }
            }
        }
    }
}

@Composable
fun DrawerContent(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    var salesExpanded by remember { mutableStateOf(false) }
    var stockExpanded by remember { mutableStateOf(false) }
    
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Goyal Tyres & Rubbers",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 24.dp)
            )
            
            Divider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // AI Assistant
            DrawerItem(
                icon = Icons.Default.Star,
                label = "AI Assistant",
                isSelected = currentScreen == Screen.AIAssistant,
                onClick = { onNavigate(Screen.AIAssistant) }
            )
            
            // Dashboard
            DrawerItem(
                icon = Icons.Default.Home,
                label = "Dashboard",
                isSelected = currentScreen == Screen.Dashboard,
                onClick = { onNavigate(Screen.Dashboard) }
            )
            
            // Sales Section (Expandable)
            DrawerItem(
                icon = Icons.Default.ShoppingCart,
                label = "Sales",
                isSelected = false,
                onClick = { salesExpanded = !salesExpanded },
                trailingIcon = if (salesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
            )
            
            if (salesExpanded) {
                DrawerSubItem(
                    label = "Sales Records",
                    isSelected = currentScreen == Screen.SalesRecords,
                    onClick = { onNavigate(Screen.SalesRecords) }
                )
                DrawerSubItem(
                    label = "Add Sale",
                    isSelected = currentScreen == Screen.AddSale,
                    onClick = { onNavigate(Screen.AddSale) }
                )
                DrawerSubItem(
                    label = "Make Bill",
                    isSelected = currentScreen == Screen.MakeBill,
                    onClick = { onNavigate(Screen.MakeBill) }
                )
                DrawerSubItem(
                    label = "Pending Payments",
                    isSelected = currentScreen == Screen.PendingPayments,
                    onClick = { onNavigate(Screen.PendingPayments) }
                )
            }
            
            // Stock Section (Expandable)
            DrawerItem(
                icon = Icons.Default.List,
                label = "Stock",
                isSelected = false,
                onClick = { stockExpanded = !stockExpanded },
                trailingIcon = if (stockExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
            )
            
            if (stockExpanded) {
                DrawerSubItem(
                    label = "Add Stock",
                    isSelected = currentScreen == Screen.AddStock,
                    onClick = { onNavigate(Screen.AddStock) }
                )
                DrawerSubItem(
                    label = "Stock Categories",
                    isSelected = currentScreen == Screen.StockCategories,
                    onClick = { onNavigate(Screen.StockCategories) }
                )
            }
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // History
        DrawerItem(
            icon = Icons.Default.DateRange,
            label = "History",
            isSelected = currentScreen == Screen.History,
            onClick = { onNavigate(Screen.History) }
        )
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector, 
    label: String, 
    isSelected: Boolean, 
    onClick: () -> Unit,
    trailingIcon: ImageVector? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DrawerSubItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 2.dp, bottom = 2.dp, end = 4.dp),
        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "• $label",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
