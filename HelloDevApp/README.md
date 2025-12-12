# Goyal Tyres & Rubbers - Inventory Management System

A comprehensive Android inventory management application built with Kotlin and Jetpack Compose for managing rubber stock, sales tracking, payment monitoring, and business analytics.

## 🚀 Features

### 📊 **Sales Records** (Launch Page)
- View all sales with payment status tracking
- Date filtering (Today/Week/Month/Year/All)
- Total Revenue display (paid sales only)
- Dealer/Customer name tracking
- Payment status badges (Paid/Pending)
- Export sales to CSV
- Share via WhatsApp, Gmail, and other apps
- Clear sales history

### 🏠 **Dashboard**
- Total Rolls Available (auto-calculated: Total - Sold)
- Total Weight Available (auto-calculated: Total Weight - Sold Weight)
- Pie chart showing stock distribution by rubber type
- Quick navigation to Add Stock and Stock Details

### ➕ **Add Stock**
- Stock category dropdown selection
- Rubber Name and ID from categories
- Number of rolls
- Weight (kg)
- Cost of stock (₹)
- Auto-calculated stock worth preview
- Real-time validation

### 🛒 **Add Sale**
- Stock category dropdown selection
- Dealer/Customer name input
- Rolls and weight sold
- Sale amount (₹)
- Payment status selection (Paid/Pending)
- **If Paid**: Adds to revenue immediately + records payment date
- **If Pending**: Adds to Pending Payments page
- Auto-deducts from available stock

### 📦 **Stock Details**
- Total Stock Worth display (Indian number format: ₹12,34,567)
- Available stock breakdown by rubber type
- Shows: Rubber name, ID, Available rolls, Available weight
- Real-time updates as sales are made
- Export stock details to CSV
- Share via Android apps
- Clear stock records

### ⏳ **Pending Payments**
- Total Pending Due (formatted with comma separators)
- List of all unpaid sales
- Approve payment button for each sale
- Date picker: "When was payment received?"
- On approval: Moves to revenue + records payment date
- Export pending payments to CSV
- Share via Android apps
- Clear all pending payments

### 🏷️ **Stock Categories**
- Add new rubber categories with Name + ID (e.g., CBL - 120)
- View all categories with creation dates
- Used in dropdowns for Add Stock and Add Sale
- Clear all categories
- Floating Action Button (FAB) for quick add

---

## 💡 Key Functionality

### **Payment Tracking System**
- Sales can be marked as "PAID" or "PENDING" when created
- **Paid Sales**: Immediately add to Total Revenue
- **Pending Sales**: Show in Pending Payments page
- **Approval Process**: Click "Approve Payment" → Select date → Moves to revenue

### **Stock Deduction**
- **Available Stock = Total Stock - Sold Stock**
- **Available Weight = Total Weight - Sold Weight**
- Updates in real-time across all screens

### **Indian Number Formatting**
- All currency displays use Indian comma format
- Example: ₹12,34,567 (12 lakhs 34 thousand 567)
- Applied to: Stock Worth, Total Revenue, Pending Due, Sale amounts

### **Export & Share**
- **Export Formats**: CSV (easy to open in Excel/Sheets)
- **Pages with Export**: Sales Records, Stock Details, Pending Payments, Stock Categories
- **Share Options**: Android native share sheet
  - WhatsApp
  - Gmail
  - Google Drive
  - Other installed apps

### **Data Management**
- **Clear Functions** on each page with confirmation dialogs
- **Database Version 4** with 3 tables:
  1. `stock_categories` - Rubber types with IDs
  2. `rubber_stock` - Inventory with category linkage
  3. `sales` - Sales transactions with payment tracking

---

## 🗂️ App Navigation

### **7 Pages in Side Drawer:**

1. **Sales Records** (Opens First) 💰
   - Total Revenue (Paid)
   - Date filters
   - Sales list with status
   - Export & Clear buttons

2. **Dashboard** 🏠
   - Available rolls & weight
   - Pie chart visualization

3. **Add Stock** ➕
   - Category dropdown
   - Stock entry form

4. **Add Sale** 🛒
   - Category dropdown
   - Dealer name
   - Payment status selector

5. **Stock Details** 📦
   - Stock Worth display
   - Available stock breakdown
   - Export & Clear options

6. **Pending Payments** ⏳
   - Total Pending Due
   - Approve payment buttons
   - Export & Clear options

7. **Stock Categories** 🏷️
   - Category list
   - Add new category (FAB)
   - Clear categories

---

## 🛠️ Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design**: Material Design 3
- **Database**: Room Database (version 4)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Data Flow**: Kotlin Flow (reactive streams)
- **Navigation**: Navigation Component with Drawer
- **Export**: CSV file generation with FileProvider
- **Charts**: Custom Enhanced Pie Chart
- **Date Handling**: Calendar with DatePickerDialog

---

## 📱 Minimum Requirements

- **Android**: 8.0 (API 26) or higher
- **Reason**: Apache POI library requires API 26+ for Java 8 features
- **Permissions**: 
  - Storage (for Android 12 and below)
  - FileProvider for secure file sharing

---

## 📊 Database Schema

### **StockCategory Table**
```kotlin
id: Int (Primary Key)
rubberName: String
rubberId: Int
addedDate: Long (timestamp)
```

### **RubberStock Table**
```kotlin
id: Int (Primary Key)
rubberName: String
rubberId: Int
numberOfRolls: Int
weightInKg: Double
costOfStock: Double
addedDate: Long (timestamp)
stockWorth: Double (computed property)
```

### **Sale Table**
```kotlin
id: Int (Primary Key)
rubberName: String
rubberId: Int
dealerName: String
numberOfRolls: Int
weightInKg: Double
soldFor: Double
saleDate: Long (timestamp)
paymentStatus: String ("PAID" or "PENDING")
paymentReceivedDate: Long? (nullable, set when approved)
```

---

## 🎨 UI Highlights

- **Material Design 3** theming with elevated cards
- **Indian Rupee (₹)** symbol throughout
- **Color-coded badges**: Green (Paid), Orange (Pending)
- **Stat Cards** with icons and subtitles
- **Enhanced Pie Chart** with percentage labels
- **Responsive layouts** with LazyColumn scrolling
- **Dropdown menus** for category selection
- **Floating Action Button** for quick actions
- **Date Filter Tabs** for time-based analytics
- **Confirmation Dialogs** before destructive actions

---

## 🔄 Data Flow Example

### Complete Sales Workflow:

1. **Add Category**: "CBL" with ID "120"
2. **Add Stock**: Select "CBL (120)" → 100 rolls, 500kg, ₹50,000
3. **Add Sale**: 
   - Select "CBL (120)"
   - Dealer: "Ramesh Traders"
   - Sold: 20 rolls, 100kg
   - Amount: ₹12,000
   - Status: "Pending"
4. **Dashboard Updates**:
   - Available Stock: 80 rolls, 400kg
   - Total Revenue: ₹0 (not paid yet)
5. **Pending Payments**:
   - Shows: Ramesh Traders - ₹12,000
   - Total Pending Due: ₹12,000
6. **Approve Payment**:
   - Click "Approve Payment"
   - Select date: "December 15, 2025"
   - Payment moves to revenue
7. **Final State**:
   - Total Revenue: ₹12,000
   - Pending Due: ₹0
   - Sale shows "PAID" status with payment date

---

## 📤 Export Example

### CSV Export Structure:

**Sales.csv:**
```csv
Sale ID,Rubber Name,Rubber ID,Dealer Name,Rolls,Weight (kg),Amount (₹),Sale Date,Payment Status,Payment Received Date
1,CBL,120,Ramesh Traders,20,100.0,12000.0,12 Dec 2024 03:45 PM,PAID,15 Dec 2024 10:00 AM
```

**Stock.csv:**
```csv
Stock ID,Rubber Name,Rubber ID,Number of Rolls,Weight (kg),Cost of Stock (₹),Stock Worth (₹),Added Date
1,CBL,120,100,500.0,50000.0,50000.0,10 Dec 2024 09:30 AM
```

---

## 👨‍💼 Business Use Cases

### **For Inventory Manager:**
- Track all rubber stock in one place
- Know exact available inventory at any time
- Monitor stock worth and investment

### **For Sales Team:**
- Record sales quickly with customer names
- Track payment status per sale
- Filter sales by time period

### **For Accounts Team:**
- See total revenue (paid only)
- Monitor pending payments by dealer
- Approve payments with date tracking
- Export data for accounting software

### **For Management:**
- Dashboard overview of business metrics
- Stock distribution visualization
- Export reports for analysis
- Share data with stakeholders via WhatsApp/Email

---

## 🔒 Data Security

- **Local Storage**: All data stored in device's internal storage
- **No Cloud Sync**: Complete offline functionality
- **FileProvider**: Secure file sharing with temporary permissions
- **Confirmation Dialogs**: Prevents accidental data deletion

---

## 📝 Notes

- **Launching**: App opens to Sales Records page by default
- **Required Setup**: Add at least one Stock Category before adding stock/sales
- **Number Format**: Indian style (lakhs, crores) for readability
- **Date Format**: DD MMM YYYY, HH:MM AM/PM
- **Payment Logic**: Pending payments don't count towards revenue until approved
- **Stock Calculation**: Automatic deduction from available stock on each sale

---

## 🎯 Future Enhancements (Potential)

- PDF export with formatted reports
- Barcode scanning for quick stock entry
- Multi-user support with login
- Cloud backup and sync
- Analytics dashboard with graphs
- Low stock alerts
- Customer credit limit tracking
- GST invoice generation

---

## 📄 License

This project is created for Goyal Tyres & Rubbers for internal business use.

---

## 🤝 Support

For issues or feature requests, contact the development team.

---

**Built with ❤️ using Kotlin & Jetpack Compose**
