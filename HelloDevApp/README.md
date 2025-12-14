# 🏢 Goyal Tyres & Rubbers - Complete Business Management System

A professional-grade Android business management application built with **Kotlin** and **Jetpack Compose**. This comprehensive solution manages inventory, sales, payments, scheduling, and analytics for rubber trading businesses.

---

## 📑 Table of Contents
1. [Overview](#-overview)
2. [Complete Architecture](#-complete-architecture)
3. [Database Schema](#-database-schema)
4. [Feature Breakdown](#-feature-breakdown)
5. [Technical Implementation](#-technical-implementation)
6. [UI Components](#-ui-components)
7. [Business Logic](#-business-logic)
8. [Installation & Setup](#-installation--setup)
9. [API Reference](#-api-reference)

---

## ✨ Overview

### **Purpose**
Enterprise-level mobile solution for rubber trading businesses providing end-to-end management of:
- **Inventory Control** - Real-time stock tracking with multi-category support
- **Sales Operations** - Complete sales lifecycle from quotation to payment
- **Financial Management** - Receivables, payables with partial payment tracking
- **Event Scheduling** - Google Calendar-style scheduling with push notifications
- **Knowledge Management** - Searchable note-taking system
- **Business Intelligence** - Visual analytics and performance metrics
- **Data Portability** - CSV export with native Android sharing

### **Technology Highlights**
- 🎨 **Material Design 3** - Modern, adaptive UI components
- 🏗️ **MVVM Architecture** - Clean separation of concerns
- 🔄 **Reactive Programming** - Flow-based real-time updates
- 💾 **Room Database** - Type-safe SQLite with migrations
- 📊 **Vico Charts** - Beautiful data visualizations
- ⚡ **Jetpack Compose** - Declarative UI with no XML
- 🔔 **WorkManager** - Reliable background task scheduling

---

## 🏛️ Complete Architecture

### **System Architecture Diagram**
```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │  Jetpack    │  │   Material   │  │  Navigation  │       │
│  │  Compose    │  │   Design 3   │  │   Compose    │       │
│  │    UI       │  │  Components  │  │              │       │
│  └─────────────┘  └──────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      VIEWMODEL LAYER                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Inventory   │  │   Calendar   │  │   Payment    │      │
│  │  ViewModel   │  │   ViewModel  │  │   ViewModel  │      │
│  │              │  │              │  │              │      │
│  │  (State +    │  │  (State +    │  │  (State +    │      │
│  │   Flow)      │  │   Flow)      │  │   Flow)      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                     REPOSITORY LAYER                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Stock      │  │   Event      │  │   Payment    │      │
│  │  Repository  │  │  Repository  │  │  Repository  │      │
│  │              │  │              │  │              │      │
│  │ (Business    │  │ (Business    │  │ (Business    │      │
│  │  Logic)      │  │  Logic)      │  │  Logic)      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Room DAOs  │  │  Type        │  │  Work        │      │
│  │              │  │  Converters  │  │  Manager     │      │
│  │ - StockDao   │  │              │  │              │      │
│  │ - SaleDao    │  │ - Gson       │  │ - Notif      │      │
│  │ - EventDao   │  │ - Date       │  │ - Scheduler  │      │
│  │ - PaymentDao │  │ - Enum       │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    DATABASE (SQLite)                         │
│                   Room Database v6                           │
│    8 Tables | Type Converters | Auto-Migration              │
└─────────────────────────────────────────────────────────────┘
```

### **Architectural Patterns**

#### **1. MVVM (Model-View-ViewModel)**
```kotlin
// View Layer (Composable)
@Composable
fun DashboardScreen(viewModel: InventoryViewModel) {
    val stockList by viewModel.allStock.collectAsState(initial = emptyList())
    val totalRevenue by viewModel.totalRevenue.collectAsState(initial = 0.0)
    
    // UI rendering based on state
}

// ViewModel Layer
class InventoryViewModel(private val repository: RubberStockRepository) : ViewModel() {
    val allStock: Flow<List<RubberStock>> = repository.getAllStock()
    val totalRevenue: Flow<Double> = repository.getTotalRevenue()
    
    fun addStock(stock: RubberStock) {
        viewModelScope.launch {
            repository.insert(stock)
        }
    }
}

// Repository Layer
class RubberStockRepository(private val dao: RubberStockDao) {
    fun getAllStock(): Flow<List<RubberStock>> = dao.getAllStock()
    suspend fun insert(stock: RubberStock) = dao.insert(stock)
}

// DAO Layer
@Dao
interface RubberStockDao {
    @Query("SELECT * FROM rubber_stock ORDER BY id DESC")
    fun getAllStock(): Flow<List<RubberStock>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stock: RubberStock)
}
```

#### **2. Dependency Injection Pattern**
```kotlin
// Manual DI using Singleton Pattern
object AppContainer {
    private lateinit var database: AppDatabase
    
    fun initialize(context: Context) {
        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "goyal_tyres_db"
        ).addMigrations(MIGRATION_5_6).build()
    }
    
    fun provideInventoryViewModel(): InventoryViewModel {
        return InventoryViewModel(
            RubberStockRepository(database.rubberStockDao()),
            SaleRepository(database.saleDao()),
            StockCategoryRepository(database.stockCategoryDao())
        )
    }
}
```

#### **3. Repository Pattern**
- Abstracts data sources from business logic
- Single source of truth for data operations
- Handles data transformations and caching
- Provides clean API for ViewModels

#### **4. Observer Pattern (Flows)**
```kotlin
// Reactive data streams
val allStock: Flow<List<RubberStock>> = dao.getAllStock()
    .map { stocks -> 
        stocks.filter { it.numberOfRolls > 0 }
    }
    .distinctUntilChanged()
```

---

## 📊 Database Schema

### **Entity Relationship Diagram**
```
┌─────────────────────┐
│  stock_categories   │
│─────────────────────│
│  id (PK)            │
│  rubberName         │
│  rubberId           │
└─────────────────────┘
          │ 1
          │
          │ N
┌─────────────────────┐        ┌─────────────────────┐
│   rubber_stock      │   N    │      sales          │
│─────────────────────│────────│─────────────────────│
│  id (PK)            │        │  id (PK)            │
│  rubberId           │        │  rubberId           │
│  rubberName         │        │  rubberName         │
│  numberOfRolls      │        │  dealerName         │
│  weightInKg         │        │  rollsSold          │
│  costOfStock        │        │  weightSold         │
│  stockWorth         │        │  saleAmount         │
│  addedDate          │        │  saleDate           │
└─────────────────────┘        │  paymentStatus      │
                               │  paymentReceivedDate│
                               └─────────────────────┘

┌─────────────────────┐        ┌─────────────────────┐
│  stock_drafts       │        │  scheduled_events   │
│─────────────────────│        │─────────────────────│
│  id (PK)            │        │  id (PK)            │
│  supplierName       │        │  title              │
│  vehicleNumber      │        │  description        │
│  items (JSON)       │        │  eventDate          │
│  createdAt          │        │  notificationTime   │
│  isCommitted        │        │  isCompleted        │
└─────────────────────┘        │  createdAt          │
                               │  notes              │
                               └─────────────────────┘

┌─────────────────────┐        ┌─────────────────────┐
│     payments        │   1    │ payment_transactions│
│─────────────────────│────────│─────────────────────│
│  id (PK)            │        │  id (PK)            │
│  type (ENUM)        │        │  paymentId (FK)     │
│  partyName          │        │  amount             │
│  totalAmount        │   N    │  transactionDate    │
│  paidAmount         │        │  notes              │
│  dueDate            │        └─────────────────────┘
│  notes              │
│  isFullyPaid        │
└─────────────────────┘

┌─────────────────────┐
│       notes         │
│─────────────────────│
│  id (PK)            │
│  title              │
│  content            │
│  createdAt          │
│  updatedAt          │
│  color              │
└─────────────────────┘
```

### **Complete Entity Definitions**

#### **1. StockCategory Entity**
```kotlin
@Entity(tableName = "stock_categories")
data class StockCategory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rubberName: String,    // e.g., "CBL"
    val rubberId: String       // e.g., "120"
)
```

#### **2. RubberStock Entity**
```kotlin
@Entity(tableName = "rubber_stock")
data class RubberStock(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rubberId: String,           // Foreign key reference
    val rubberName: String,         // Denormalized for performance
    val numberOfRolls: Int,
    val weightInKg: Double,
    val costOfStock: Double,
    val stockWorth: Double,         // Calculated: numberOfRolls * costOfStock
    val addedDate: Date
)
```

#### **3. Sale Entity**
```kotlin
@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rubberId: String,
    val rubberName: String,
    val dealerName: String,
    val rollsSold: Int,
    val weightSold: Double,
    val saleAmount: Double,
    val saleDate: Date,
    val paymentStatus: String,      // "PAID" or "PENDING"
    val paymentReceivedDate: Date?  // Null if pending
)
```

#### **4. StockDraft Entity**
```kotlin
@Entity(tableName = "stock_drafts")
data class StockDraft(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val supplierName: String,
    val vehicleNumber: String,
    val items: List<DraftItem>,     // Stored as JSON
    val createdAt: Date,
    val isCommitted: Boolean = false
)

data class DraftItem(
    val rubberName: String,
    val rubberId: String,
    val numberOfRolls: Int,
    val weightInKg: Double,
    val costOfStock: Double
)

// Type Converter for List<DraftItem>
class DraftItemsConverter {
    private val gson = Gson()
    
    @TypeConverter
    fun fromDraftItemsList(items: List<DraftItem>): String {
        return gson.toJson(items)
    }
    
    @TypeConverter
    fun toDraftItemsList(json: String): List<DraftItem> {
        return gson.fromJson(json, object : TypeToken<List<DraftItem>>() {}.type)
    }
}
```

#### **5. ScheduledEvent Entity**
```kotlin
@Entity(tableName = "scheduled_events")
data class ScheduledEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val eventDate: Date,            // Event date and time
    val notificationTime: Date?,    // When to send notification
    val isCompleted: Boolean = false,
    val createdAt: Date,
    val notes: String = ""
)
```

#### **6. Payment Entity**
```kotlin
@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: PaymentType,          // INCOMING or OUTGOING
    val partyName: String,          // Customer/Supplier name
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val dueDate: Date?,
    val notes: String = "",
    val isFullyPaid: Boolean = false
)

enum class PaymentType {
    INCOMING,  // Money to receive
    OUTGOING   // Money to pay
}
```

#### **7. PaymentTransaction Entity**
```kotlin
@Entity(
    tableName = "payment_transactions",
    foreignKeys = [ForeignKey(
        entity = Payment::class,
        parentColumns = ["id"],
        childColumns = ["paymentId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PaymentTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val paymentId: Int,             // Foreign key
    val amount: Double,
    val transactionDate: Date,
    val notes: String = ""
)
```

#### **8. Note Entity**
```kotlin
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val createdAt: Date,
    val updatedAt: Date,
    val color: String = "Blue"      // 8 color options
)
```

### **Database Migrations**

#### **Migration 5 → 6**
```kotlin
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create new tables without affecting existing data
        
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS scheduled_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                eventDate INTEGER NOT NULL,
                notificationTime INTEGER,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                notes TEXT NOT NULL DEFAULT ''
            )
        """)
        
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS payments (
                id INTEGER PRIMARY KEY AUTOGENERATE NOT NULL,
                type TEXT NOT NULL,
                partyName TEXT NOT NULL,
                totalAmount REAL NOT NULL,
                paidAmount REAL NOT NULL DEFAULT 0.0,
                dueDate INTEGER,
                notes TEXT NOT NULL DEFAULT '',
                isFullyPaid INTEGER NOT NULL DEFAULT 0
            )
        """)
        
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS payment_transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                paymentId INTEGER NOT NULL,
                amount REAL NOT NULL,
                transactionDate INTEGER NOT NULL,
                notes TEXT NOT NULL DEFAULT '',
                FOREIGN KEY(paymentId) REFERENCES payments(id) 
                    ON DELETE CASCADE
            )
        """)
        
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                color TEXT NOT NULL DEFAULT 'Blue'
            )
        """)
    }
}
```

---

## 🚀 Feature Breakdown

### 📊 **1. Inventory Management**

**Dashboard Analytics** 
- Real-time KPI cards with icons
- Stock distribution pie chart (Vico library)
- Low stock alerts (configurable threshold)
- Recent activity timeline
- Top performing categories
- Quick action buttons

**Implementation:**
```kotlin
@Composable
fun DashboardScreen(viewModel: InventoryViewModel) {
    val allStock by viewModel.allStock.collectAsState(initial = emptyList())
    val totalRevenue by viewModel.totalRevenue.collectAsState(initial = 0.0)
    val totalPending by viewModel.totalPending.collectAsState(initial = 0.0)
    
    LazyColumn {
        // KPI Cards
        item {
            Row {
                StatCard(
                    title = "Available Stock",
                    value = "${allStock.sumOf { it.numberOfRolls }} Rolls",
                    subtitle = "${allStock.sumOf { it.weightInKg }} kg"
                )
                StatCard(
                    title = "Total Revenue",
                    value = formatIndianCurrency(totalRevenue)
                )
            }
        }
        
        // Pie Chart
        item {
            StockDistributionChart(stockList = allStock)
        }
        
        // Low Stock Alerts
        item {
            val lowStockItems = allStock.filter { it.numberOfRolls < 50 }
            LowStockAlerts(items = lowStockItems)
        }
    }
}
```

**Stock Management**
- Category-based organization
- Real-time stock worth calculation
- Automatic deduction on sales
- Bulk operations support
- CSV export with Android Sharing

**Code:**
```kotlin
class RubberStockRepository(
    private val stockDao: RubberStockDao,
    private val saleDao: SaleDao
) {
    // Get available stock (Total - Sold)
    fun getAvailableStock(): Flow<List<StockWithAvailability>> {
        return stockDao.getAllStock().combine(saleDao.getAllSales()) { stocks, sales ->
            stocks.map { stock ->
                val sold = sales
                    .filter { it.rubberId == stock.rubberId }
                    .sumOf { it.rollsSold }
                    
                StockWithAvailability(
                    stock = stock,
                    availableRolls = stock.numberOfRolls - sold,
                    soldRolls = sold
                )
            }
        }
    }
}
```

**Stock Drafts System**
- Multi-item draft creation
- Supplier and vehicle tracking
- Preview before commit
- Rollback capability

**Workflow:**
```kotlin
// 1. Create Draft
val draft = StockDraft(
    supplierName = "ABC Traders",
    vehicleNumber = "DL-1234",
    items = emptyList(),
    createdAt = Date()
)

// 2. Add Items Incrementally
draft.items += DraftItem(
    rubberName = "CBL",
    rubberId = "120",
    numberOfRolls = 100,
    weightInKg = 500.0,
    costOfStock = 50000.0
)

// 3. Preview Summary
DraftSummaryScreen(draft)

// 4. Commit to Inventory
viewModel.commitDraft(draft.id) // Converts to RubberStock entries
```

### 💰 **2. Sales & Revenue Tracking**

**Sales Records Screen**
```kotlin
@Composable
fun SalesRecordsScreen(viewModel: InventoryViewModel) {
    var filterPeriod by remember { mutableStateOf(FilterPeriod.ALL) }
    val allSales by viewModel.allSales.collectAsState(initial = emptyList())
    
    val filteredSales = when (filterPeriod) {
        FilterPeriod.TODAY -> allSales.filter { isToday(it.saleDate) }
        FilterPeriod.WEEK -> allSales.filter { isThisWeek(it.saleDate) }
        FilterPeriod.MONTH -> allSales.filter { isThisMonth(it.saleDate) }
        FilterPeriod.YEAR -> allSales.filter { isThisYear(it.saleDate) }
        FilterPeriod.ALL -> allSales
    }
    
    LazyColumn {
        // Filter Tabs
        item {
            ScrollableTabRow(selectedTabIndex = filterPeriod.ordinal) {
                FilterPeriod.values().forEach { period ->
                    Tab(
                        selected = filterPeriod == period,
                        onClick = { filterPeriod = period },
                        text = { Text(period.name) }
                    )
                }
            }
        }
        
        // Sales List
        items(filteredSales) { sale ->
            SaleCard(
                sale = sale,
                onPaymentCollect = { saleId, date ->
                    viewModel.markPaymentReceived(saleId, date)
                }
            )
        }
    }
}
```

**Revenue Calculation**
```kotlin
class SaleRepository(private val saleDao: SaleDao) {
    // Only paid sales count as revenue
    fun getTotalRevenue(): Flow<Double> {
        return saleDao.getAllSales().map { sales ->
            sales.filter { it.paymentStatus == "PAID" }
                 .sumOf { it.saleAmount }
        }
    }
    
    // Track pending payments
    fun getTotalPending(): Flow<Double> {
        return saleDao.getAllSales().map { sales ->
            sales.filter { it.paymentStatus == "PENDING" }
                 .sumOf { it.saleAmount }
        }
    }
    
    // Payment collection
    suspend fun markPaymentReceived(saleId: Int, date: Date) {
        saleDao.updatePaymentStatus(saleId, "PAID", date)
    }
}
```

**Pending Payments Screen**
```kotlin
@Composable
fun PendingPaymentsScreen(viewModel: InventoryViewModel) {
    val pendingSales by viewModel.pendingSales.collectAsState(initial = emptyList())
    val totalPending = pendingSales.sumOf { it.saleAmount }
    
    Column {
        // Total Pending Card
        Card {
            Text("Total Pending Due", style = MaterialTheme.typography.titleMedium)
            Text(
                formatIndianCurrency(totalPending),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        // Pending Sales List
        LazyColumn {
            items(pendingSales) { sale ->
                PendingPaymentCard(
                    sale = sale,
                    onApprovePayment = { saleId ->
                        showDatePicker { selectedDate ->
                            viewModel.markPaymentReceived(saleId, selectedDate)
                        }
                    }
                )
            }
        }
    }
}
```

### 💸 **3. Payment Management System**

**Two-Tab System: Incoming vs Outgoing**
```kotlin
@Composable
fun PaymentsScreen(viewModel: PaymentViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val incomingPayments by viewModel.incomingPayments.collectAsState(initial = emptyList())
    val outgoingPayments by viewModel.outgoingPayments.collectAsState(initial = emptyList())
    
    Column {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Incoming (Receivables)") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Outgoing (Payables)") }
            )
        }
        
        when (selectedTab) {
            0 -> PaymentList(incomingPayments, PaymentType.INCOMING)
            1 -> PaymentList(outgoingPayments, PaymentType.OUTGOING)
        }
    }
}
```

**Partial Payment Tracking**
```kotlin
class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val transactionDao: PaymentTransactionDao
) {
    // Add a partial payment
    suspend fun addPaymentTransaction(paymentId: Int, amount: Double, notes: String) {
        // 1. Add transaction record
        val transaction = PaymentTransaction(
            paymentId = paymentId,
            amount = amount,
            transactionDate = Date(),
            notes = notes
        )
        transactionDao.insert(transaction)
        
        // 2. Update payment's paid amount
        val payment = paymentDao.getPaymentById(paymentId)
        val newPaidAmount = payment.paidAmount + amount
        val isFullyPaid = newPaidAmount >= payment.totalAmount
        
        paymentDao.updatePaymentAmount(paymentId, newPaidAmount, isFullyPaid)
    }
    
    // Get payment with transaction history
    fun getPaymentWithTransactions(paymentId: Int): Flow<PaymentWithTransactions> {
        return paymentDao.getPaymentById(paymentId).combine(
            transactionDao.getTransactionsForPayment(paymentId)
        ) { payment, transactions ->
            PaymentWithTransactions(payment, transactions)
        }
    }
}
```

**Payment Details with Progress Visualization**
```kotlin
@Composable
fun PaymentDetailsScreen(paymentId: Int, viewModel: PaymentViewModel) {
    val paymentWithTransactions by viewModel
        .getPaymentWithTransactions(paymentId)
        .collectAsState(initial = null)
    
    paymentWithTransactions?.let { data ->
        val payment = data.payment
        val transactions = data.transactions
        val progress = payment.paidAmount / payment.totalAmount
        
        Column {
            // Payment Summary Card
            Card {
                Text(payment.partyName, style = MaterialTheme.typography.headlineSmall)
                Text("Total: ${formatCurrency(payment.totalAmount)}")
                Text("Paid: ${formatCurrency(payment.paidAmount)}")
                Text("Remaining: ${formatCurrency(payment.totalAmount - payment.paidAmount)}")
                
                // Progress Bar
                LinearProgressIndicator(
                    progress = progress.toFloat(),
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
                Text("${(progress * 100).toInt()}% Complete")
            }
            
            // Transaction History
            Text("Transaction History", style = MaterialTheme.typography.titleLarge)
            LazyColumn {
                items(transactions) { transaction ->
                    TransactionCard(
                        amount = transaction.amount,
                        date = transaction.transactionDate,
                        notes = transaction.notes
                    )
                }
            }
            
            // Add Transaction Button
            Button(onClick = { showAddTransactionDialog = true }) {
                Text("Add Payment")
            }
        }
    }
}
```

**Add Transaction Dialog**
```kotlin
@Composable
fun AddTransactionDialog(
    payment: Payment,
    onConfirm: (amount: Double, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    val remainingAmount = payment.totalAmount - payment.paidAmount
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Payment") },
        text = {
            Column {
                Text("Remaining: ${formatCurrency(remainingAmount)}")
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    if (amountDouble > 0 && amountDouble <= remainingAmount) {
                        onConfirm(amountDouble, notes)
                        onDismiss()
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```

### 📅 **4. Calendar & Scheduling System**

**Calendar UI Implementation**
```kotlin
@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val allEvents by viewModel.allEvents.collectAsState(initial = emptyList())
    
    Column {
        // Mini Calendar
        MiniCalendar(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            eventDates = allEvents.map { it.eventDate.toLocalDate() }
        )
        
        // Upcoming Events
        val upcomingEvents = allEvents.filter { 
            it.eventDate.after(Date()) && !it.isCompleted 
        }
        
        Text("Upcoming Events", style = MaterialTheme.typography.titleLarge)
        LazyColumn {
            items(upcomingEvents) { event ->
                EventCard(
                    event = event,
                    onEdit = { navController.navigate("edit_event/${event.id}") },
                    onComplete = { viewModel.markCompleted(event.id) }
                )
            }
        }
    }
}
```

**WorkManager Notification System**
```kotlin
class EventNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        val eventId = inputData.getInt("EVENT_ID", -1)
        val eventTitle = inputData.getString("EVENT_TITLE") ?: return Result.failure()
        val eventDescription = inputData.getString("EVENT_DESCRIPTION") ?: ""
        
        // Create notification
        val notification = NotificationCompat.Builder(applicationContext, "event_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(eventTitle)
            .setContentText(eventDescription)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(eventId, notification)
        
        return Result.success()
    }
}
```

**Notification Scheduling**
```kotlin
object NotificationScheduler {
    fun scheduleEventNotification(context: Context, event: ScheduledEvent) {
        val delay = event.notificationTime!!.time - System.currentTimeMillis()
        
        if (delay > 0) {
            val data = workDataOf(
                "EVENT_ID" to event.id,
                "EVENT_TITLE" to event.title,
                "EVENT_DESCRIPTION" to event.description
            )
            
            val workRequest = OneTimeWorkRequestBuilder<EventNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("event_${event.id}")
                .build()
            
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
    
    fun cancelEventNotification(context: Context, eventId: Int) {
        WorkManager.getInstance(context).cancelAllWorkByTag("event_$eventId")
    }
}
```

**Add/Edit Event Screen**
```kotlin
@Composable
fun AddEditEventScreen(
    eventId: Int? = null,
    viewModel: CalendarViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf(Date()) }
    var notificationTime by remember { mutableStateOf<Date?>(null) }
    var notes by remember { mutableStateOf("") }
    
    // Load existing event if editing
    LaunchedEffect(eventId) {
        eventId?.let {
            viewModel.getEvent(it)?.let { event ->
                title = event.title
                description = event.description
                eventDate = event.eventDate
                notificationTime = event.notificationTime
                notes = event.notes
            }
        }
    }
    
    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Event Title") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Date & Time Pickers
        DateTimePicker(
            label = "Event Date & Time",
            value = eventDate,
            onValueChange = { eventDate = it }
        )
        
        DateTimePicker(
            label = "Notification Time (Optional)",
            value = notificationTime,
            onValueChange = { notificationTime = it }
        )
        
        Button(
            onClick = {
                val event = ScheduledEvent(
                    id = eventId ?: 0,
                    title = title,
                    description = description,
                    eventDate = eventDate,
                    notificationTime = notificationTime,
                    createdAt = Date(),
                    notes = notes
                )
                
                viewModel.saveEvent(event)
                onBack()
            }
        ) {
            Text(if (eventId == null) "Create Event" else "Update Event")
        }
    }
}
```

### 📝 **5. Notes System**

**Grid/List View Toggle**
```kotlin
@Composable
fun NotesScreen(viewModel: NoteViewModel) {
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }
    var searchQuery by remember { mutableStateOf("") }
    
    val allNotes by viewModel.allNotes.collectAsState(initial = emptyList())
    val filteredNotes = if (searchQuery.isBlank()) {
        allNotes
    } else {
        allNotes.filter { 
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.content.contains(searchQuery, ignoreCase = true)
        }
    }
    
    Column {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search notes...") },
            leadingIcon = { Icon(Icons.Default.Search, "Search") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // View Mode Toggle
        Row {
            IconButton(onClick = { viewMode = ViewMode.GRID }) {
                Icon(Icons.Default.GridView, "Grid View")
            }
            IconButton(onClick = { viewMode = ViewMode.LIST }) {
                Icon(Icons.Default.List, "List View")
            }
        }
        
        // Notes Display
        when (viewMode) {
            ViewMode.GRID -> NotesGrid(filteredNotes)
            ViewMode.LIST -> NotesList(filteredNotes)
        }
    }
}
```

**8 Color Themes**
```kotlin
object NoteColors {
    val colors = listOf(
        "Blue" to Color(0xFFBBDEFB),
        "Green" to Color(0xFFC8E6C9),
        "Yellow" to Color(0xFFFFF9C4),
        "Orange" to Color(0xFFFFE0B2),
        "Pink" to Color(0xFFF8BBD0),
        "Purple" to Color(0xFFE1BEE7),
        "Red" to Color(0xFFFFCDD2),
        "Gray" to Color(0xFFEEEEEE)
    )
    
    fun getColor(colorName: String): Color {
        return colors.find { it.first == colorName }?.second ?: colors[0].second
    }
}
```

**Note Card with Smart Timestamps**
```kotlin
@Composable
fun NoteCard(note: Note, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = NoteColors.getColor(note.color)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = formatTimeAgo(note.updatedAt),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

fun formatTimeAgo(date: Date): String {
    val now = System.currentTimeMillis()
    val diff = now - date.time
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
    }
}
```

**Full-Text Search Implementation**
```kotlin
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>
    
    @Query("""
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
        OR content LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun searchNotes(query: String): Flow<List<Note>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)
    
    @Delete
    suspend fun delete(note: Note)
}
```

**Add/Edit Note Screen**
```kotlin
@Composable
fun AddEditNoteScreen(
    noteId: Int? = null,
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("Blue") }
    
    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            maxLines = Int.MAX_VALUE
        )
        
        // Color Picker
        Text("Choose Color")
        LazyRow {
            items(NoteColors.colors) { (colorName, color) ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selectedColor == colorName) 4.dp else 0.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .clickable { selectedColor = colorName }
                )
            }
        }
        
        Button(
            onClick = {
                val note = Note(
                    id = noteId ?: 0,
                    title = title,
                    content = content,
                    createdAt = Date(),
                    updatedAt = Date(),
                    color = selectedColor
                )
                viewModel.saveNote(note)
                onBack()
            }
        ) {
            Text(if (noteId == null) "Create Note" else "Update Note")
        }
    }
}
```

### 📈 **6. History & Analytics**

**Transaction Timeline**
```kotlin
@Composable
fun HistoryScreen(viewModel: InventoryViewModel) {
    val allStock by viewModel.allStock.collectAsState(initial = emptyList())
    val allSales by viewModel.allSales.collectAsState(initial = emptyList())
    
    // Combine and sort by date
    val timeline = buildList {
        allStock.forEach { stock ->
            add(TimelineEvent(
                type = "Stock Added",
                rubberName = stock.rubberName,
                quantity = "${stock.numberOfRolls} rolls",
                date = stock.addedDate
            ))
        }
        allSales.forEach { sale ->
            add(TimelineEvent(
                type = "Sale",
                rubberName = sale.rubberName,
                quantity = "${sale.rollsSold} rolls",
                date = sale.saleDate
            ))
        }
    }.sortedByDescending { it.date }
    
    LazyColumn {
        items(timeline) { event ->
            TimelineCard(event)
        }
    }
}
```

---

## 🛠️ Technical Implementation

### **Dependency Management**


**build.gradle.kts (Module: app)**
```kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // WorkManager for Notifications
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Vico Charts
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
    implementation("com.patrykandpatrick.vico:core:1.13.1")
    
    // Gson for JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Apache POI for Excel
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
}
```

### **Type Converters**
```kotlin
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

class PaymentTypeConverter {
    @TypeConverter
    fun fromPaymentType(type: PaymentType): String {
        return type.name
    }
    
    @TypeConverter
    fun toPaymentType(value: String): PaymentType {
        return PaymentType.valueOf(value)
    }
}

class DraftItemsConverter {
    private val gson = Gson()
    
    @TypeConverter
    fun fromDraftItemsList(items: List<DraftItem>): String {
        return gson.toJson(items)
    }
    
    @TypeConverter
    fun toDraftItemsList(json: String): List<DraftItem> {
        val type = object : TypeToken<List<DraftItem>>() {}.type
        return gson.fromJson(json, type)
    }
}
```

### **AppDatabase Configuration**
```kotlin
@Database(
    entities = [
        RubberStock::class,
        Sale::class,
        StockCategory::class,
        StockDraft::class,
        ScheduledEvent::class,
        Payment::class,
        PaymentTransaction::class,
        Note::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(DateConverter::class, PaymentTypeConverter::class, DraftItemsConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rubberStockDao(): RubberStockDao
    abstract fun saleDao(): SaleDao
    abstract fun stockCategoryDao(): StockCategoryDao
    abstract fun stockDraftDao(): StockDraftDao
    abstract fun eventDao(): ScheduledEventDao
    abstract fun paymentDao(): PaymentDao
    abstract fun paymentTransactionDao(): PaymentTransactionDao
    abstract fun noteDao(): NoteDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "goyal_tyres_database"
                )
                .addMigrations(MIGRATION_5_6)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

---

## 🎨 UI Components

### **Material Design 3 Components Used**

#### **1. Cards & Containers**
```kotlin
@Composable
fun StatCard(title: String, value: String, subtitle: String = "", icon: ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

#### **2. Navigation Drawer**
```kotlin
@Composable
fun AppNavigationDrawer(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Text(
                        text = "Goyal Tyres & Rubbers",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    
                    Divider()
                    
                    // Menu Items
                    DrawerMenuItem(
                        icon = Icons.Default.Home,
                        label = "Dashboard",
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("dashboard")
                        }
                    )
                    // ... more items
                }
            }
        }
    ) {
        // Main content
    }
}
```

#### **3. Date & Time Pickers**
```kotlin
@Composable
fun DateTimePicker(
    label: String,
    value: Date?,
    onValueChange: (Date) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    OutlinedButton(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.CalendarToday, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value?.let { 
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(it)
            } ?: label
        )
    }
    
    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            onDateSelected = { date ->
                TimePickerDialog(
                    onTimeSelected = { hour, minute ->
                        val calendar = Calendar.getInstance()
                        calendar.time = date
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        onValueChange(calendar.time)
                        showDialog = false
                    }
                )
            }
        )
    }
}
```

#### **4. Confirmation Dialogs**
```kotlin
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```

#### **5. Vico Charts Integration**
```kotlin
@Composable
fun StockDistributionPieChart(stockList: List<RubberStock>) {
    val chartData = stockList.groupBy { it.rubberName }
        .mapValues { (_, stocks) -> stocks.sumOf { it.numberOfRolls } }
    
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Stock Distribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            if (chartData.isNotEmpty()) {
                Chart(
                    chart = pieChart(),
                    model = entryModelOf(*chartData.values.map { it.toFloat() }.toTypedArray()),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis()
                )
                
                // Legend
                LazyColumn {
                    items(chartData.entries.toList()) { (name, count) ->
                        Row(
                            modifier = Modifier.padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(getColorForCategory(name), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$name: $count rolls")
                        }
                    }
                }
            } else {
                Text("No stock data available")
            }
        }
    }
}
```

---

## 💼 Business Logic


### **Stock Calculations**
```kotlin
class StockCalculations {
    // Available stock after sales
    fun calculateAvailableStock(
        totalRolls: Int,
        soldRolls: Int
    ): Int = maxOf(0, totalRolls - soldRolls)
    
    // Stock worth calculation
    fun calculateStockWorth(
        numberOfRolls: Int,
        costPerRoll: Double
    ): Double = numberOfRolls * costPerRoll
    
    // Low stock detection
    fun isLowStock(
        availableRolls: Int,
        threshold: Int = 50
    ): Boolean = availableRolls < threshold
    
    // Total investment
    fun calculateTotalInvestment(stockList: List<RubberStock>): Double {
        return stockList.sumOf { it.costOfStock }
    }
}
```

### **Revenue Calculations**
```kotlin
class RevenueCalculations {
    // Only paid sales count as revenue
    fun calculateTotalRevenue(sales: List<Sale>): Double {
        return sales
            .filter { it.paymentStatus == "PAID" }
            .sumOf { it.saleAmount }
    }
    
    // Pending payments
    fun calculatePendingDue(sales: List<Sale>): Double {
        return sales
            .filter { it.paymentStatus == "PENDING" }
            .sumOf { it.saleAmount }
    }
    
    // Average sale value
    fun calculateAverageSaleValue(sales: List<Sale>): Double {
        return if (sales.isEmpty()) 0.0 
        else sales.sumOf { it.saleAmount } / sales.size
    }
    
    // Best performing category
    fun getBestPerformingCategory(sales: List<Sale>): Pair<String, Double>? {
        return sales
            .groupBy { it.rubberName }
            .mapValues { (_, categorySales) -> categorySales.sumOf { it.saleAmount } }
            .maxByOrNull { it.value }
            ?.toPair()
    }
}
```

### **Payment Progress Tracking**
```kotlin
class PaymentTracking {
    // Calculate payment progress
    fun calculateProgress(totalAmount: Double, paidAmount: Double): Float {
        return if (totalAmount > 0) {
            (paidAmount / totalAmount).coerceIn(0.0, 1.0).toFloat()
        } else 0f
    }
    
    // Check if payment is fully paid
    fun isFullyPaid(totalAmount: Double, paidAmount: Double): Boolean {
        return paidAmount >= totalAmount
    }
    
    // Calculate remaining amount
    fun calculateRemaining(totalAmount: Double, paidAmount: Double): Double {
        return maxOf(0.0, totalAmount - paidAmount)
    }
    
    // Validate transaction amount
    fun isValidTransaction(
        totalAmount: Double,
        paidAmount: Double,
        newTransaction: Double
    ): Boolean {
        return newTransaction > 0 && (paidAmount + newTransaction) <= totalAmount
    }
}
```

### **Indian Currency Formatting**
```kotlin
fun formatIndianCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount)
        .replace("₹", "₹ ")  // Add space after symbol
}

// Alternative: Custom formatter for lakhs/crores
fun formatIndianNumber(amount: Double): String {
    val parts = amount.toLong().toString().reversed().chunked(3).toMutableList()
    
    if (parts.size > 1) {
        val first = parts.removeAt(0)
        val remaining = parts.joinToString(",") { it.reversed() }
        return "₹ ${remaining.reversed()},${first.reversed()}"
    }
    
    return "₹ ${amount.toLong()}"
}

// Examples:
// 50000 → ₹ 50,000
// 123456 → ₹ 1,23,456
// 1234567 → ₹ 12,34,567
```

### **CSV Export Implementation**
```kotlin
class CsvExporter(private val context: Context) {
    fun exportSales(sales: List<Sale>): Uri? {
        try {
            val csvContent = buildString {
                // Header
                appendLine("Sale ID,Rubber Name,Rubber ID,Dealer Name,Rolls,Weight (kg),Amount (₹),Sale Date,Payment Status,Payment Date")
                
                // Data rows
                sales.forEach { sale ->
                    appendLine(
                        "${sale.id},${sale.rubberName},${sale.rubberId},${sale.dealerName}," +
                        "${sale.rollsSold},${sale.weightSold},${sale.saleAmount}," +
                        "${formatDate(sale.saleDate)},${sale.paymentStatus}," +
                        "${sale.paymentReceivedDate?.let { formatDate(it) } ?: "N/A"}"
                    )
                }
            }
            
            // Save to cache directory
            val file = File(context.cacheDir, "sales_export_${System.currentTimeMillis()}.csv")
            file.writeText(csvContent)
            
            // Create shareable URI
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    fun shareFile(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share Sales Data"))
    }
}
```

### **Notification System**
```kotlin
class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "event_notifications"
        const val CHANNEL_NAME = "Event Reminders"
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled events"
                enableLights(true)
                enableVibration(true)
            }
            
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    fun showEventNotification(eventId: Int, title: String, description: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        NotificationManagerCompat.from(context).notify(eventId, notification)
    }
}
```

---

## 📱 Navigation Structure

### **Screen Routes**
```kotlin
sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object SalesRecords : Screen("sales_records", "Sales Records")
    object AddSale : Screen("add_sale", "Add Sale")
    object MakeBill : Screen("make_bill", "Make Bill")
    object PendingPayments : Screen("pending_payments", "Pending Payments")
    object AddStock : Screen("add_stock", "Add Stock")
    object StockCategories : Screen("stock_categories", "Stock Categories")
    object StockDrafts : Screen("stock_drafts", "Stock Drafts")
    object DraftSummary : Screen("draft_summary/{draftId}", "Draft Summary") {
        fun createRoute(draftId: Int) = "draft_summary/$draftId"
    }
    object History : Screen("history", "History")
    object Calendar : Screen("calendar", "Calendar")
    object AddEvent : Screen("add_event", "Add Event")
    object EditEvent : Screen("edit_event/{eventId}", "Edit Event") {
        fun createRoute(eventId: Int) = "edit_event/$eventId"
    }
    object Payments : Screen("payments", "Payments")
    object AddPayment : Screen("add_payment/{type}", "Add Payment") {
        fun createRoute(type: String) = "add_payment/$type"
    }
    object PaymentDetails : Screen("payment_details/{paymentId}", "Payment Details") {
        fun createRoute(paymentId: Int) = "payment_details/$paymentId"
    }
    object Notes : Screen("notes", "Notes")
    object AddNote : Screen("add_note", "Add Note")
    object EditNote : Screen("edit_note/{noteId}", "Edit Note") {
        fun createRoute(noteId: Int) = "edit_note/$noteId"
    }
}
```

### **Navigation Graph**
```kotlin
@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: InventoryViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(viewModel)
        }
        
        composable(Screen.SalesRecords.route) {
            SalesRecordsScreen(viewModel)
        }
        
        // ... all other routes
        
        // Parameterized routes
        composable(
            route = Screen.EditEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            AddEditEventScreen(eventId = eventId, onBack = { navController.popBackStack() })
        }
    }
}
```

---

## 📚 API Reference

### **ViewModel Methods**

#### **InventoryViewModel**
```kotlin
class InventoryViewModel(
    private val stockRepository: RubberStockRepository,
    private val saleRepository: SaleRepository,
    private val categoryRepository: StockCategoryRepository,
    private val draftRepository: StockDraftRepository
) : ViewModel() {
    
    // Flows for reactive UI
    val allStock: Flow<List<RubberStock>> = stockRepository.getAllStock()
    val allSales: Flow<List<Sale>> = saleRepository.getAllSales()
    val allCategories: Flow<List<StockCategory>> = categoryRepository.getAllCategories()
    val totalRevenue: Flow<Double> = saleRepository.getTotalRevenue()
    val totalPending: Flow<Double> = saleRepository.getTotalPending()
    
    // Stock operations
    fun addStock(stock: RubberStock) = viewModelScope.launch {
        stockRepository.insert(stock)
    }
    
    fun deleteStock(stock: RubberStock) = viewModelScope.launch {
        stockRepository.delete(stock)
    }
    
    fun clearAllStock() = viewModelScope.launch {
        stockRepository.deleteAll()
    }
    
    // Sale operations
    fun addSale(sale: Sale) = viewModelScope.launch {
        saleRepository.insert(sale)
    }
    
    fun markPaymentReceived(saleId: Int, date: Date) = viewModelScope.launch {
        saleRepository.markPaymentReceived(saleId, date)
    }
    
    // Category operations
    fun addCategory(category: StockCategory) = viewModelScope.launch {
        categoryRepository.insert(category)
    }
    
    // Draft operations
    fun saveDraft(draft: StockDraft) = viewModelScope.launch {
        draftRepository.insert(draft)
    }
    
    fun commitDraft(draftId: Int) = viewModelScope.launch {
        draftRepository.commitDraft(draftId)
    }
    
    // Export
    fun exportSalesToCsv(context: Context): Uri? {
        return CsvExporter(context).exportSales(allSales.value ?: emptyList())
    }
}
```

#### **CalendarViewModel**
```kotlin
class CalendarViewModel(
    private val eventRepository: ScheduledEventRepository
) : ViewModel() {
    
    val allEvents: Flow<List<ScheduledEvent>> = eventRepository.getAllEvents()
    val upcomingEvents: Flow<List<ScheduledEvent>> = eventRepository.getUpcomingEvents()
    
    fun saveEvent(event: ScheduledEvent) = viewModelScope.launch {
        eventRepository.insert(event)
        
        // Schedule notification
        event.notificationTime?.let {
            NotificationScheduler.scheduleEventNotification(context, event)
        }
    }
    
    fun markCompleted(eventId: Int) = viewModelScope.launch {
        eventRepository.markCompleted(eventId)
        NotificationScheduler.cancelEventNotification(context, eventId)
    }
    
    fun deleteEvent(event: ScheduledEvent) = viewModelScope.launch {
        eventRepository.delete(event)
        NotificationScheduler.cancelEventNotification(context, event.id)
    }
}
```

#### **PaymentViewModel**
```kotlin
class PaymentViewModel(
    private val paymentRepository: PaymentRepository
) : ViewModel() {
    
    val incomingPayments: Flow<List<Payment>> = paymentRepository.getIncomingPayments()
    val outgoingPayments: Flow<List<Payment>> = paymentRepository.getOutgoingPayments()
    val totalPendingIncoming: Flow<Double> = paymentRepository.getTotalPendingIncoming()
    val totalPendingOutgoing: Flow<Double> = paymentRepository.getTotalPendingOutgoing()
    
    fun addPayment(payment: Payment) = viewModelScope.launch {
        paymentRepository.insert(payment)
    }
    
    fun addTransaction(paymentId: Int, amount: Double, notes: String) = viewModelScope.launch {
        paymentRepository.addPaymentTransaction(paymentId, amount, notes)
    }
    
    fun getPaymentWithTransactions(paymentId: Int): Flow<PaymentWithTransactions> {
        return paymentRepository.getPaymentWithTransactions(paymentId)
    }
}
```

---

## 🚀 Installation & Setup


### **Prerequisites**
- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 17 or higher
- **Minimum SDK**: API 26 (Android 8.0 Oreo)
- **Target SDK**: API 34 (Android 14)
- **Kotlin**: 1.9.20+
- **Gradle**: 8.2+

### **Development Environment Setup**

**1. Clone Repository**
```bash
git clone https://github.com/fafawds67685da/Goyal_tyres_Rubbers.git
cd Goyal_tyres_Rubbers/HelloDevApp
```

**2. Open in Android Studio**
- File → Open → Select `HelloDevApp` folder
- Wait for Gradle sync to complete
- Resolve any dependency issues

**3. Configure Build**
```bash
# PowerShell (Windows)
.\gradlew assembleDebug

# Unix/Mac
./gradlew assembleDebug
```

**4. Install on Device**
```bash
# PowerShell
.\gradlew installDebug

# Or use Android Studio
Run → Run 'app' (Shift+F10)
```

### **Project Structure**
```
HelloDevApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/hellodev/app/
│   │   │   │   ├── data/
│   │   │   │   │   ├── entities/
│   │   │   │   │   │   ├── RubberStock.kt
│   │   │   │   │   │   ├── Sale.kt
│   │   │   │   │   │   ├── StockCategory.kt
│   │   │   │   │   │   ├── StockDraft.kt
│   │   │   │   │   │   ├── ScheduledEvent.kt
│   │   │   │   │   │   ├── Payment.kt
│   │   │   │   │   │   ├── PaymentTransaction.kt
│   │   │   │   │   │   └── Note.kt
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── RubberStockDao.kt
│   │   │   │   │   │   ├── SaleDao.kt
│   │   │   │   │   │   ├── StockCategoryDao.kt
│   │   │   │   │   │   ├── StockDraftDao.kt
│   │   │   │   │   │   ├── ScheduledEventDao.kt
│   │   │   │   │   │   ├── PaymentDao.kt
│   │   │   │   │   │   ├── PaymentTransactionDao.kt
│   │   │   │   │   │   └── NoteDao.kt
│   │   │   │   │   ├── database/
│   │   │   │   │   │   └── AppDatabase.kt
│   │   │   │   │   └── converters/
│   │   │   │   │       ├── DateConverter.kt
│   │   │   │   │       ├── PaymentTypeConverter.kt
│   │   │   │   │       └── DraftItemsConverter.kt
│   │   │   │   ├── viewmodel/
│   │   │   │   │   ├── InventoryViewModel.kt
│   │   │   │   │   ├── CalendarViewModel.kt
│   │   │   │   │   ├── PaymentViewModel.kt
│   │   │   │   │   └── NoteViewModel.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── DashboardScreen.kt
│   │   │   │   │   │   ├── SalesRecordsScreen.kt
│   │   │   │   │   │   ├── AddStockScreen.kt
│   │   │   │   │   │   ├── CalendarScreen.kt
│   │   │   │   │   │   ├── PaymentsScreen.kt
│   │   │   │   │   │   ├── NotesScreen.kt
│   │   │   │   │   │   └── HistoryScreen.kt
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── StatCard.kt
│   │   │   │   │   │   ├── ChartComponents.kt
│   │   │   │   │   │   └── DialogComponents.kt
│   │   │   │   │   └── theme/
│   │   │   │   │       ├── Color.kt
│   │   │   │   │       ├── Theme.kt
│   │   │   │   │       └── Type.kt
│   │   │   │   ├── notification/
│   │   │   │   │   ├── EventNotificationWorker.kt
│   │   │   │   │   └── NotificationScheduler.kt
│   │   │   │   ├── utils/
│   │   │   │   │   ├── CsvExporter.kt
│   │   │   │   │   ├── FormatUtils.kt
│   │   │   │   │   └── DateUtils.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── Screen.kt
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── xml/
│   │   │   │       └── file_paths.xml
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

### **First Run Configuration**

**1. Grant Permissions**
On first launch, the app will request:
- 📱 **Post Notifications** - For event reminders
- ⏰ **Schedule Exact Alarms** - For precise event notifications
- 📂 **Storage Access** (Android 12 and below) - For CSV exports

**2. Initial Data Setup**
```kotlin
// Step 1: Add Stock Categories
1. Open Navigation Drawer → Stock → Stock Categories
2. Click "+" button
3. Add categories:
   - Rubber Name: "CBL", Rubber ID: "120"
   - Rubber Name: "NBR", Rubber ID: "200"
   - etc.

// Step 2: Add Initial Stock
1. Navigate to Add Stock
2. Select category from dropdown
3. Enter: Number of Rolls, Weight (kg), Cost
4. Click "Add Stock"

// Step 3: Record First Sale
1. Navigate to Add Sale
2. Select rubber category
3. Enter dealer name, rolls sold, weight, amount
4. Click "Add Sale"
```

### **Configuration Files**

**AndroidManifest.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Permissions -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
        android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
        android:maxSdkVersion="32" />
    
    <application
        android:name=".MyApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.HelloDevApp">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.HelloDevApp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <!-- FileProvider for sharing -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
        
    </application>
    
</manifest>
```

**file_paths.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="shared_files" path="." />
</paths>
```

---

## 🔄 Database Migration Guide

### **Migration from v5 to v6**

**What Changed:**
- ✅ **Preserved**: All existing data (stock, sales, categories, drafts)
- ➕ **Added**: 4 new tables (scheduled_events, payments, payment_transactions, notes)
- 🔧 **Modified**: TypeConverter registration

**Migration Code:**
```kotlin
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // No data loss - only adds new tables
        
        // 1. Scheduled Events Table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS scheduled_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                eventDate INTEGER NOT NULL,
                notificationTime INTEGER,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                notes TEXT NOT NULL DEFAULT ''
            )
        """)
        
        // 2. Payments Table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS payments (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL,
                partyName TEXT NOT NULL,
                totalAmount REAL NOT NULL,
                paidAmount REAL NOT NULL DEFAULT 0.0,
                dueDate INTEGER,
                notes TEXT NOT NULL DEFAULT '',
                isFullyPaid INTEGER NOT NULL DEFAULT 0
            )
        """)
        
        // 3. Payment Transactions Table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS payment_transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                paymentId INTEGER NOT NULL,
                amount REAL NOT NULL,
                transactionDate INTEGER NOT NULL,
                notes TEXT NOT NULL DEFAULT '',
                FOREIGN KEY(paymentId) REFERENCES payments(id) ON DELETE CASCADE
            )
        """)
        
        // 4. Notes Table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                color TEXT NOT NULL DEFAULT 'Blue'
            )
        """)
    }
}
```

**Update Process:**
1. User updates app from Play Store or installs new APK
2. App launches → Room detects version change (5→6)
3. MIGRATION_5_6 executes automatically
4. All existing data remains intact
5. New features become available immediately

**Rollback Plan:**
```kotlin
// If migration fails, implement fallback
Room.databaseBuilder(context, AppDatabase::class.java, "goyal_tyres_database")
    .addMigrations(MIGRATION_5_6)
    .fallbackToDestructiveMigrationOnDowngrade()  // Only on downgrade
    .build()
```

### **Testing Migration**
```kotlin
@Test
fun testMigration_5_to_6() {
    // Insert data in v5
    val db = helper.createDatabase(TEST_DB, 5)
    db.execSQL("INSERT INTO rubber_stock VALUES (1, 'CBL', '120', 100, 500.0, 50000.0, 50000.0, ${Date().time})")
    db.close()
    
    // Migrate to v6
    val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)
    
    // Verify data preserved
    val cursor = migratedDb.query("SELECT * FROM rubber_stock WHERE id = 1")
    assert(cursor.count == 1)
    
    // Verify new tables exist
    val eventsCursor = migratedDb.query("SELECT * FROM scheduled_events")
    assert(eventsCursor != null)
}
```

---

## 🔐 Security & Permissions

### **Required Permissions Breakdown**

| Permission | Purpose | API Level | Request Time |
|-----------|---------|-----------|--------------|
| `POST_NOTIFICATIONS` | Show event reminders | 33+ (Android 13+) | Runtime |
| `SCHEDULE_EXACT_ALARM` | Precise notification timing | 31+ (Android 12+) | Runtime |
| `USE_EXACT_ALARM` | Alternative exact alarm API | 33+ (Android 13+) | Automatic |
| `READ_EXTERNAL_STORAGE` | Read files for export (legacy) | ≤32 (Android 12 and below) | Runtime |
| `WRITE_EXTERNAL_STORAGE` | Write CSV files (legacy) | ≤32 (Android 12 and below) | Runtime |

### **Permission Request Implementation**
```kotlin
class MainActivity : ComponentActivity() {
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please enable notifications in settings", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Request exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }
}
```

### **Data Security**
- **Local Storage Only**: All data stored in device's internal storage
- **No Cloud Sync**: Complete offline functionality (no internet required)
- **Encrypted Backups**: Android's backup service encrypts data automatically
- **FileProvider**: Secure file sharing with temporary URI permissions
- **SQL Injection Protection**: Room's parameterized queries prevent SQL injection

---

## 📈 Performance Optimizations

### **Database Optimization**
```kotlin
// Indexed queries for faster lookups
@Query("SELECT * FROM rubber_stock WHERE rubberId = :id")
@Index(value = ["rubberId"])
fun getStockByRubberId(id: String): Flow<List<RubberStock>>

// Limit results for large datasets
@Query("SELECT * FROM sales ORDER BY saleDate DESC LIMIT :limit")
fun getRecentSales(limit: Int = 100): Flow<List<Sale>>

// Pagination for infinite scroll
@Query("SELECT * FROM notes ORDER BY updatedAt DESC LIMIT :limit OFFSET :offset")
suspend fun getNotesPaged(limit: Int, offset: Int): List<Note>
```

### **Memory Management**
```kotlin
// Use Flows instead of LiveData for better backpressure handling
val allStock: Flow<List<RubberStock>> = dao.getAllStock()
    .flowOn(Dispatchers.IO)  // Execute on IO thread
    .distinctUntilChanged()  // Only emit on changes
    .shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),  // Keep active for 5s
        replay = 1  // Cache last emission
    )
```

### **UI Performance**
```kotlin
// LazyColumn with keys for efficient recomposition
LazyColumn {
    items(
        items = stockList,
        key = { stock -> stock.id }  // Stable key
    ) { stock ->
        StockCard(stock)
    }
}

// Remember expensive calculations
@Composable
fun DashboardScreen(stockList: List<RubberStock>) {
    val totalValue = remember(stockList) {
        stockList.sumOf { it.stockWorth }
    }
}
```

---

## 🧪 Testing

### **Unit Tests**
```kotlin
class RepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var stockDao: RubberStockDao
    private lateinit var repository: RubberStockRepository
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        stockDao = database.rubberStockDao()
        repository = RubberStockRepository(stockDao)
    }
    
    @Test
    fun testInsertStock() = runTest {
        val stock = RubberStock(
            rubberId = "120",
            rubberName = "CBL",
            numberOfRolls = 100,
            weightInKg = 500.0,
            costOfStock = 50000.0,
            stockWorth = 50000.0,
            addedDate = Date()
        )
        
        repository.insert(stock)
        
        val allStock = repository.getAllStock().first()
        assertEquals(1, allStock.size)
        assertEquals("CBL", allStock[0].rubberName)
    }
}
```

### **UI Tests**
```kotlin
@Test
fun testDashboardDisplaysData() {
    composeTestRule.setContent {
        val viewModel = InventoryViewModel(/* ... */)
        DashboardScreen(viewModel)
    }
    
    composeTestRule
        .onNodeWithText("Dashboard")
        .assertIsDisplayed()
    
    composeTestRule
        .onNodeWithText("Available Stock")
        .assertIsDisplayed()
}
```

---

## 🚀 Deployment

### **Build Variants**
```kotlin
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isMinifyEnabled = false
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### **ProGuard Rules**
```proguard
# Keep Room entities
-keep class com.hellodev.app.data.entities.** { *; }

# Keep Gson models
-keepclassmembers class com.hellodev.app.** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
```

### **Signing Configuration**
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
}
```

---

## 🛣️ Roadmap & Future Enhancements

### **Phase 1: Q1 2026**
- [ ] 🌙 **Dark Mode Theme** - Complete dark theme with automatic switching
- [ ] 🌐 **Multi-language Support** - Hindi, English, regional languages
- [ ] 📊 **Advanced Analytics** - Profit margins, trend analysis, forecasting
- [ ] 📧 **Email Reports** - Automated daily/weekly/monthly reports

### **Phase 2: Q2 2026**
- [ ] ☁️ **Cloud Backup & Sync** - Firebase integration for data backup
- [ ] 👥 **Multi-user Support** - User roles (Admin, Manager, Staff)
- [ ] 📱 **QR Code Scanning** - Quick stock entry via barcode/QR
- [ ] 🖨️ **PDF Invoice Generation** - Professional invoice with company logo

### **Phase 3: Q3 2026**
- [ ] 💬 **WhatsApp Integration** - Send invoices directly via WhatsApp Business API
- [ ] 📈 **Advanced Dashboard** - Customizable widgets and KPIs
- [ ] 🔔 **Smart Notifications** - Low stock alerts, payment reminders
- [ ] 🗺️ **Location Tracking** - Delivery tracking with Google Maps

### **Phase 4: Q4 2026**
- [ ] 🤖 **AI-Powered Insights** - Sales predictions, inventory optimization
- [ ] 📲 **Customer Portal** - Web portal for customers to view orders
- [ ] 💳 **Payment Gateway** - Integrate UPI, cards for online payments
- [ ] 🔐 **Biometric Authentication** - Fingerprint/face unlock for security

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

### **Code Style**
- Follow Kotlin coding conventions
- Use meaningful variable/function names
- Add KDoc comments for public APIs
- Keep functions small and focused

### **Pull Request Process**
1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open Pull Request with detailed description

### **Commit Message Format**
```
type(scope): subject

body (optional)

footer (optional)
```

Examples:
- `feat(payments): add partial payment tracking`
- `fix(dashboard): correct revenue calculation`
- `docs(readme): update installation instructions`

---

## 📄 License

This project is proprietary software developed for **Goyal Tyres & Rubbers**.

```
Copyright (c) 2024-2025 Goyal Tyres & Rubbers
All rights reserved.

This software is proprietary and confidential. Unauthorized copying,
modification, distribution, or use of this software, via any medium,
is strictly prohibited without explicit written permission from the
copyright holder.
```

---

## 👨‍💻 Credits

### **Development Team**
- **Lead Developer**: [Your Name]
- **Architecture**: MVVM with Jetpack Compose
- **Database Design**: Room Database v6
- **UI/UX Design**: Material Design 3

### **Technologies Used**
- Kotlin 1.9.20
- Jetpack Compose (BOM 2024.01.00)
- Room 2.6.1
- WorkManager 2.9.0
- Vico Charts 1.13.1
- Gson 2.10.1

### **Special Thanks**
- Android Jetpack team for excellent libraries
- Material Design team for beautiful components
- Open source community for inspiration

---

## 📞 Support & Contact

### **Technical Support**
- 📧 **Email**: support@goyaltyres.com
- 📱 **Phone**: +91-XXXXXXXXXX
- 🌐 **Website**: www.goyaltyres.com

### **Bug Reports**
Found a bug? Please report it:
1. Open GitHub Issues
2. Provide detailed description
3. Include screenshots if applicable
4. Mention device and Android version

### **Feature Requests**
Have an idea? We'd love to hear:
- Open a GitHub Issue with `[Feature Request]` tag
- Describe the feature and use case
- Explain expected behavior

---

## 📚 Additional Resources

### **Documentation**
- [Android Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Material Design 3](https://m3.material.io/)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)

### **Tutorials**
- [MVVM Architecture](https://developer.android.com/topic/architecture)
- [Kotlin Flows](https://kotlinlang.org/docs/flow.html)
- [Jetpack Compose Navigation](https://developer.android.com/jetpack/compose/navigation)

### **Community**
- [Stack Overflow - Android](https://stackoverflow.com/questions/tagged/android)
- [Kotlin Slack](https://kotlinlang.slack.com)
- [r/androiddev](https://reddit.com/r/androiddev)

---

## 📊 Project Statistics

| Metric | Count |
|--------|-------|
| **Total Files** | 50+ |
| **Lines of Code** | ~8,000 |
| **Entities** | 8 |
| **DAOs** | 8 |
| **ViewModels** | 4 |
| **Screens** | 15+ |
| **Features** | 10 major |
| **Database Version** | 6 |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 34 (Android 14) |

---

## 🔖 Changelog

### **Version 7.0** (December 2025) - Current
- ➕ Added Calendar & Scheduling system with notifications
- ➕ Added Payment tracking with partial payments
- ➕ Added Notes system with search
- ➕ Implemented WorkManager for background tasks
- 🔄 Database migration v5→v6 (data preserved)
- 📊 Enhanced dashboard with more metrics
- 🎨 Material Design 3 updates
- 🐛 Bug fixes and performance improvements

### **Version 1.1** (November 2024)
- ➕ Added Stock Drafts system
- ➕ Added incremental item addition
- ➕ Supplier and vehicle tracking
- 🔄 Navigation drawer improvements
- 📊 Dashboard enhancements
- 🐛 Fixed field name mismatches

### **Version 1.0** (October 2024)
- 🎉 Initial release
- ✅ Inventory management
- ✅ Sales tracking
- ✅ Payment status
- ✅ CSV export
- ✅ Dashboard analytics

---

## ⚠️ Known Issues

### **Current Issues**
1. **Large Dataset Performance**: Scrolling may lag with 1000+ items
   - **Workaround**: Use pagination or date filters
   - **Fix**: Planned for v7.1

2. **Notification Timing**: May delay by 1-2 minutes on some devices
   - **Workaround**: Enable "Unrestricted" battery usage
   - **Fix**: Investigating alternative scheduling

3. **CSV Export on Android 14**: Occasional permission issues
   - **Workaround**: Grant storage permissions manually
   - **Fix**: Migrate to Storage Access Framework (v7.2)

### **Fixed in v7.0**
- ✅ App crash on launch (missing TypeConverter)
- ✅ Duplicate method errors in PaymentRepository
- ✅ Field name mismatches in entities
- ✅ Navigation drawer scroll issues

---

## 🎯 Quick Reference

### **Common Tasks**

#### Add Stock
```
Drawer → Stock → Add Stock
→ Select Category → Enter Details → Save
```

#### Record Sale
```
Drawer → Sales → Add Sale
→ Select Rubber → Enter Dealer & Quantity → Save
```

#### Schedule Event
```
Drawer → Calendar → "+" Button
→ Enter Title, Date, Time → Set Notification → Save
```

#### Track Payment
```
Drawer → Payments → "+" Button
→ Choose Type (Incoming/Outgoing)
→ Enter Party, Amount, Due Date → Save
→ Add Partial Payments as Received
```

#### Export Data
```
Any Screen → Export Button → Select Format (CSV)
→ Choose Share Method (WhatsApp, Email, etc.)
```

### **Keyboard Shortcuts** (External Keyboard)
- `Ctrl + N` - New item (context-aware)
- `Ctrl + S` - Save current form
- `Ctrl + F` - Search/Filter
- `Ctrl + E` - Export data
- `Ctrl + M` - Open menu/drawer

---

## 🏆 Best Practices

### **Data Entry**
1. ✅ Add all stock categories before adding stock
2. ✅ Use consistent naming conventions
3. ✅ Double-check quantities before saving
4. ✅ Mark payments received immediately
5. ✅ Export data weekly for backup

### **Performance**
1. ⚡ Clear old data periodically (archive before deleting)
2. ⚡ Use filters to limit displayed data
3. ⚡ Close app when not in use to save battery
4. ⚡ Enable "Don't keep activities" for testing only

### **Security**
1. 🔒 Enable screen lock on device
2. 🔒 Don't share exported files publicly
3. 🔒 Regular backups to external storage
4. 🔒 Update app when new versions available

---

**Version**: 7.0 (Database v6)  
**Last Updated**: December 12, 2025  
**Build**: Production Ready  
**Build Number**: 7  
**Package**: com.hellodev.app

---

**Built with ❤️ using Kotlin & Jetpack Compose**

*Empowering rubber trading businesses with modern technology*

---

## 📱 Download

**Current Version**: 7.0  
**Release Date**: December 12, 2025  
**File Size**: ~15 MB  
**Compatible**: Android 8.0 and above

[Download APK](#) | [View on Play Store](#) | [GitHub Releases](https://github.com/fafawds67685da/Goyal_tyres_Rubbers/releases)

---

*For any questions, issues, or feedback, please don't hesitate to reach out. We're committed to making this the best inventory management solution for your business!*
