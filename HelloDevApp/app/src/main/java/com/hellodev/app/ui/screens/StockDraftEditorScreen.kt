package com.hellodev.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hellodev.app.data.DraftItem
import com.hellodev.app.data.StockCategory
import com.hellodev.app.viewmodel.InventoryViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDraftEditorScreen(
    viewModel: InventoryViewModel,
    draftId: Int,
    onNavigateBack: () -> Unit,
    onViewSummary: (Int) -> Unit
) {
    val drafts by viewModel.drafts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val draft = drafts.find { it.id == draftId }
    
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showEditDetailsDialog by remember { mutableStateOf(false) }
    
    if (draft == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warning, null, Modifier.size(64.dp))
                Text("Draft not found")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateBack) {
                    Text("Go Back")
                }
            }
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Draft") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDetailsDialog = true }) {
                        Icon(Icons.Default.Edit, "Edit Details")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (draft.items.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = { onViewSummary(draftId) },
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(Icons.Default.Check, "View Summary")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                FloatingActionButton(
                    onClick = { showAddItemDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "Add Item")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Supplier",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                draft.supplierName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Vehicle",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                draft.vehicleNumber,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (draft.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            draft.notes,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Items List
            if (draft.items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No items added yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(draft.items) { index, item ->
                        DraftItemCard(
                            item = item,
                            onRemove = { viewModel.removeItemFromDraft(draftId, index) }
                        )
                    }
                }
            }
        }
    }
    
    if (showAddItemDialog) {
        AddDraftItemDialog(
            categories = categories,
            onDismiss = { showAddItemDialog = false },
            onAdd = { item ->
                viewModel.addItemToDraft(draftId, item)
                showAddItemDialog = false
            }
        )
    }
    
    if (showEditDetailsDialog) {
        EditDraftDetailsDialog(
            draft = draft,
            onDismiss = { showEditDetailsDialog = false },
            onSave = { supplier, vehicle, notes ->
                viewModel.updateDraftDetails(draftId, supplier, vehicle, notes)
                showEditDetailsDialog = false
            }
        )
    }
}

@Composable
fun DraftItemCard(
    item: DraftItem,
    onRemove: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.rubberName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.numberOfRolls} rolls • ${item.weightInKg} kg • ${currencyFormat.format(item.costOfStock)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDraftItemDialog(
    categories: List<StockCategory>,
    onDismiss: () -> Unit,
    onAdd: (DraftItem) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<StockCategory?>(null) }
    var rolls by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item to Draft") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.rubberName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.rubberName) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = rolls,
                    onValueChange = { rolls = it },
                    label = { Text("Number of Rolls") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Cost (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val category = selectedCategory
                    val rollsInt = rolls.toIntOrNull()
                    val weightDouble = weight.toDoubleOrNull()
                    val costDouble = cost.toDoubleOrNull()
                    
                    if (category != null && rollsInt != null && rollsInt > 0 && 
                        weightDouble != null && weightDouble > 0 && 
                        costDouble != null && costDouble >= 0) {
                        onAdd(
                            DraftItem(
                                categoryId = category.id,
                                rubberName = category.rubberName,
                                rubberId = category.rubberId,
                                numberOfRolls = rollsInt,
                                weightInKg = weightDouble,
                                costOfStock = costDouble,
                                addedAt = System.currentTimeMillis()
                            )
                        )
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

@Composable
fun EditDraftDetailsDialog(
    draft: com.hellodev.app.data.StockDraft,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var supplier by remember { mutableStateOf(draft.supplierName) }
    var vehicle by remember { mutableStateOf(draft.vehicleNumber) }
    var notes by remember { mutableStateOf(draft.notes) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Draft Details") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = vehicle,
                    onValueChange = { vehicle = it },
                    label = { Text("Vehicle Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (supplier.isNotBlank() && vehicle.isNotBlank()) {
                        onSave(supplier, vehicle, notes)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
