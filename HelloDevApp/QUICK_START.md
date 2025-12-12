# 🚀 Quick Start Guide - Goyal Tyres & Rubbers App

## Build & Run

### 1. Sync Gradle
```
Open Android Studio → File → Sync Project with Gradle Files
Wait for sync to complete (may take 2-3 minutes first time)
```

### 2. Build APK
```
Build → Build Bundle(s) / APK(s) → Build APK
Or press: Ctrl+Shift+F9 (Windows) / Cmd+Shift+F9 (Mac)
```

### 3. Run on Device
```
Connect Android device via USB
Enable USB Debugging on device
Click Run (Green Play button) or press Shift+F10
```

---

## First Time Setup (App Usage)

### Step 1: Add Stock Categories
1. Open app → Opens on **Sales Records** page
2. Open side menu (hamburger icon)
3. Go to **Stock Categories**
4. Click **➕ FAB button** at bottom right
5. Add categories:
   - Example 1: Name: "CBL", ID: 120
   - Example 2: Name: "MRF", ID: 101
   - Example 3: Name: "JK", ID: 205

### Step 2: Add Stock
1. Go to **Add Stock** from menu
2. Select category from dropdown
3. Enter:
   - Number of Rolls: 100
   - Weight (kg): 500
   - Cost of Stock: 50000
4. Click **Add Stock**

### Step 3: Record a Sale (PAID)
1. Go to **Add Sale** from menu
2. Select category from dropdown
3. Enter:
   - Dealer Name: "Ramesh Traders"
   - Rolls Sold: 20
   - Weight Sold: 100
   - Sold For: 12000
   - Payment Status: **PAID**
5. Click **Record Sale**
6. ✅ Amount added to Total Revenue immediately

### Step 4: Record a Sale (PENDING)
1. Go to **Add Sale**
2. Enter sale details
3. Select Payment Status: **PENDING**
4. Click **Record Sale**
5. ✅ Sale goes to **Pending Payments** page

### Step 5: Approve Pending Payment
1. Go to **Pending Payments** from menu
2. Find the pending sale
3. Click **Approve Payment** button
4. Select date when payment was received
5. ✅ Payment moves to revenue

### Step 6: View Dashboard
1. Go to **Dashboard** from menu
2. See:
   - Total Rolls Available (auto-calculated)
   - Total Weight Available (auto-calculated)
   - Pie chart of stock distribution

### Step 7: Export Data
1. Go to any page with Export button:
   - Sales Records
   - Stock Details
   - Pending Payments
2. Click **Export** button
3. Confirm CSV export
4. **Share** via:
   - WhatsApp
   - Gmail
   - Google Drive
   - Other apps

---

## 📱 Navigation Map

```
Side Menu (Drawer)
├── 💰 Sales Records (Launch Page)
│   ├── Total Revenue (Paid only)
│   ├── Date Filters
│   ├── Sales List
│   └── Export & Clear
│
├── 🏠 Dashboard
│   ├── Available Rolls
│   ├── Available Weight
│   └── Pie Chart
│
├── ➕ Add Stock
│   └── Category Dropdown + Form
│
├── 🛒 Add Sale
│   ├── Category Dropdown
│   ├── Dealer Name
│   └── Payment Status (Paid/Pending)
│
├── 📦 Stock Details
│   ├── Stock Worth
│   ├── Stock Breakdown
│   └── Export & Clear
│
├── ⏳ Pending Payments
│   ├── Total Pending Due
│   ├── Approve Buttons
│   └── Export & Clear
│
└── 🏷️ Stock Categories
    ├── Category List
    ├── Add Category (FAB)
    └── Clear
```

---

## 🎯 Key Features

### ✅ Indian Number Formatting
All amounts display as: **₹12,34,567**

### ✅ Payment Tracking
- **PAID** → Green badge, adds to revenue immediately
- **PENDING** → Orange badge, goes to Pending Payments

### ✅ Auto Stock Calculation
- **Available = Total - Sold** (both rolls and weight)

### ✅ Date Filtering
- **Today** | **This Week** | **This Month** | **This Year** | **All**

### ✅ Export & Share
- CSV format (Excel compatible)
- Share via WhatsApp, Gmail, etc.

---

## ⚠️ Important Notes

1. **Add Categories First**: Before adding stock or sales, create at least one category
2. **Payment Logic**: Pending payments don't count towards revenue until approved
3. **Stock Deduction**: Automatic on every sale (both rolls and weight)
4. **Clear Functions**: All have confirmation dialogs
5. **Export Location**: Files saved to cache, shared via Android share sheet

---

## 🐛 Troubleshooting

### App won't build?
```
1. Check internet connection (downloads dependencies)
2. Sync Gradle again
3. Clean project: Build → Clean Project
4. Rebuild: Build → Rebuild Project
```

### Export not working?
```
1. Grant storage permission when prompted
2. Check if other apps installed (WhatsApp, Gmail)
3. Try export again
```

### Categories not showing in dropdown?
```
1. Go to Stock Categories page
2. Verify categories exist
3. Add new category if empty
```

### Available stock showing negative?
```
This shouldn't happen, but if it does:
1. Go to Stock Details
2. Verify total stock entered correctly
3. Check if too many sales recorded
4. Add more stock if needed
```

---

## 📊 Sample Test Data

### Categories:
```
CBL - 120
MRF - 101
JK - 205
Apollo - 303
```

### Stock:
```
CBL: 100 rolls, 500kg, ₹50,000
MRF: 80 rolls, 400kg, ₹40,000
JK: 60 rolls, 300kg, ₹30,000
```

### Sales (Paid):
```
Ramesh Traders: CBL, 20 rolls, 100kg, ₹12,000
Suresh Motors: MRF, 15 rolls, 75kg, ₹9,500
```

### Sales (Pending):
```
Mukesh Enterprises: JK, 10 rolls, 50kg, ₹6,000
Dinesh Trading: CBL, 25 rolls, 125kg, ₹15,000
```

---

## 🎉 Success Indicators

After setup, you should see:

**Dashboard:**
- Available Rolls: 170 (total 240 - sold 70)
- Available Weight: 850kg (total 1200 - sold 350)
- Pie chart with 3 colors

**Sales Records:**
- Total Revenue: ₹21,500 (2 paid sales)
- 4 sales listed (2 paid, 2 pending)

**Pending Payments:**
- Total Pending Due: ₹21,000 (2 pending sales)
- 2 cards with Approve buttons

**Stock Details:**
- Stock Worth: ₹1,20,000
- 3 stock items listed

---

## 📱 Requirements

- **Android 8.0+** (API 26)
- **Storage**: 50MB minimum
- **RAM**: 2GB minimum recommended

---

## 🔧 Tech Stack

- Kotlin + Jetpack Compose
- Room Database (version 4)
- Material Design 3
- MVVM Architecture

---

**Ready to go! 🚀**

Build the app and start managing your inventory!
