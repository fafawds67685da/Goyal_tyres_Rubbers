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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.hellodev.app.data.PaymentType
import com.hellodev.app.navigation.Screen
import com.hellodev.app.screens.*
import com.hellodev.app.ui.*
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
                
                // Check for app updates
                UpdateChecker(context = this)
                
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
                Screen.Calendar.route -> Screen.Calendar
                Screen.Payments.route -> Screen.Payments
                Screen.Notes.route -> Screen.Notes
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
                
                // Draft screens
                composable(Screen.StockDrafts.route) { 
                    com.hellodev.app.ui.screens.StockDraftListScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onCreateDraft = { navController.navigate(Screen.CreateDraft.route) },
                        onEditDraft = { draftId -> navController.navigate(Screen.EditDraft.createRoute(draftId)) }
                    )
                }
                composable(Screen.CreateDraft.route) {
                    com.hellodev.app.ui.screens.CreateDraftScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onDraftCreated = { draftId -> 
                            navController.popBackStack()
                            navController.navigate(Screen.EditDraft.createRoute(draftId))
                        }
                    )
                }
                composable(Screen.EditDraft.route) { backStackEntry ->
                    val draftId = backStackEntry.arguments?.getString("draftId")?.toIntOrNull() ?: 0
                    com.hellodev.app.ui.screens.StockDraftEditorScreen(
                        viewModel = viewModel,
                        draftId = draftId,
                        onNavigateBack = { navController.popBackStack() },
                        onViewSummary = { id -> navController.navigate(Screen.DraftSummary.createRoute(id)) }
                    )
                }
                composable(Screen.DraftSummary.route) { backStackEntry ->
                    val draftId = backStackEntry.arguments?.getString("draftId")?.toIntOrNull() ?: 0
                    com.hellodev.app.ui.screens.DraftSummaryScreen(
                        viewModel = viewModel,
                        draftId = draftId,
                        onNavigateBack = { navController.popBackStack() },
                        onCommitSuccess = { 
                            navController.popBackStack(Screen.StockDrafts.route, inclusive = false)
                        }
                    )
                }
                
                // Calendar screens
                composable(Screen.Calendar.route) {
                    CalendarScreen(
                        onAddEvent = { navController.navigate(Screen.AddEvent.route) },
                        onEditEvent = { eventId -> navController.navigate(Screen.EditEvent.createRoute(eventId)) }
                    )
                }
                composable(Screen.AddEvent.route) {
                    AddEditEventScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.EditEvent.route) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId")?.toIntOrNull()
                    AddEditEventScreen(
                        eventId = eventId,
                        onBack = { navController.popBackStack() }
                    )
                }
                
                // Payment screens
                composable(Screen.Payments.route) {
                    PaymentsScreen(
                        onAddPayment = { type -> navController.navigate(Screen.AddPayment.createRoute(type.name)) },
                        onPaymentClick = { paymentId -> navController.navigate(Screen.PaymentDetails.createRoute(paymentId)) }
                    )
                }
                composable(Screen.AddPayment.route) { backStackEntry ->
                    val typeString = backStackEntry.arguments?.getString("type") ?: "INCOMING"
                    val paymentType = try {
                        PaymentType.valueOf(typeString)
                    } catch (e: Exception) {
                        PaymentType.INCOMING
                    }
                    AddPaymentScreen(
                        paymentType = paymentType,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.PaymentDetails.route) { backStackEntry ->
                    val paymentId = backStackEntry.arguments?.getString("paymentId")?.toIntOrNull() ?: 0
                    PaymentDetailsScreen(
                        paymentId = paymentId,
                        onBack = { navController.popBackStack() }
                    )
                }
                
                // Notes screens
                composable(Screen.Notes.route) {
                    NotesScreen(
                        onAddNote = { navController.navigate(Screen.AddNote.route) },
                        onEditNote = { noteId -> navController.navigate(Screen.EditNote.createRoute(noteId)) }
                    )
                }
                composable(Screen.AddNote.route) {
                    AddEditNoteScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.EditNote.route) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull()
                    AddEditNoteScreen(
                        noteId = noteId,
                        onBack = { navController.popBackStack() }
                    )
                }
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
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Goyal Tyres & Rubbers",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp).padding(vertical = 8.dp)
            )
            
            Divider()
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
            
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
                    label = "Stock Drafts",
                    isSelected = currentScreen == Screen.StockDrafts,
                    onClick = { onNavigate(Screen.StockDrafts) }
                )
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
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // History
            DrawerItem(
                icon = Icons.Default.DateRange,
                label = "History",
                isSelected = currentScreen == Screen.History,
                onClick = { onNavigate(Screen.History) }
            )
            
            // Calendar
            DrawerItem(
                icon = Icons.Default.CalendarToday,
                label = "Calendar",
                isSelected = currentScreen == Screen.Calendar,
                onClick = { onNavigate(Screen.Calendar) }
            )
            
            // Payments
            DrawerItem(
                icon = Icons.Default.Payment,
                label = "Payments",
                isSelected = currentScreen == Screen.Payments,
                onClick = { onNavigate(Screen.Payments) }
            )
            
            // Notes
            DrawerItem(
                icon = Icons.Default.Note,
                label = "Notes",
                isSelected = currentScreen == Screen.Notes,
                onClick = { onNavigate(Screen.Notes) }
            )
            }
        }
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

@Composable
fun UpdateChecker(context: android.content.Context) {
    val prefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    val currentVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (e: Exception) {
            1
        }
    }
    
    val lastKnownVersion = remember { prefs.getInt("last_version", 0) }
    var showUpdateDialog by remember { mutableStateOf(lastKnownVersion > 0 && currentVersion > lastKnownVersion) }
    
    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Update Available")
                }
            },
            text = { 
                Column {
                    Text(
                        "A new update has been injected into the app!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Your existing data will be preserved. Would you like to apply the update now?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "What's New:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("• Enhanced History with detailed records", style = MaterialTheme.typography.bodySmall)
                            Text("• View stock additions and sales details", style = MaterialTheme.typography.bodySmall)
                            Text("• Expandable transaction history", style = MaterialTheme.typography.bodySmall)
                            Text("• Bug fixes and improvements", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        prefs.edit().putInt("last_version", currentVersion).apply()
                        showUpdateDialog = false
                    }
                ) {
                    Text("Update Now")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        prefs.edit().putInt("last_version", currentVersion).apply()
                        showUpdateDialog = false
                    }
                ) {
                    Text("Later")
                }
            }
        )
    } else if (lastKnownVersion == 0) {
        // First launch - save current version
        LaunchedEffect(Unit) {
            prefs.edit().putInt("last_version", currentVersion).apply()
        }
    }
}
