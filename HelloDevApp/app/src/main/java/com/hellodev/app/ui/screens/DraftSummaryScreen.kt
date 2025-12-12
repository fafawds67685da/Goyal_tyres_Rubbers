package com.hellodev.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hellodev.app.viewmodel.InventoryViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftSummaryScreen(
    viewModel: InventoryViewModel,
    draftId: Int,
    onNavigateBack: () -> Unit,
    onCommitSuccess: () -> Unit
) {
    val drafts by viewModel.drafts.collectAsState()
    val draft = drafts.find { it.id == draftId }
    val summary = remember(draftId) { viewModel.getDraftSummary(draftId) }
    
    var showCommitDialog by remember { mutableStateOf(false) }
    
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    
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
                title = { Text("Draft Summary") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Total Cost",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            currencyFormat.format(summary.sumOf { it.totalCost }),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Button(
                        onClick = { showCommitDialog = true },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Commit Stock")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Draft Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SummaryStatCard(
                            label = "Total Items",
                            value = draft.items.size.toString()
                        )
                        SummaryStatCard(
                            label = "Categories",
                            value = summary.size.toString()
                        )
                        SummaryStatCard(
                            label = "Total Rolls",
                            value = summary.sumOf { it.totalRolls }.toString()
                        )
                        SummaryStatCard(
                            label = "Total Weight",
                            value = "%.2f kg".format(summary.sumOf { it.totalWeight })
                        )
                    }
                    
                    if (draft.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Notes:",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            draft.notes,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Summary by Category
            Text(
                text = "Summary by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(summary) { item ->
                    SummaryItemCard(item = item, currencyFormat = currencyFormat)
                }
            }
        }
    }
    
    if (showCommitDialog) {
        AlertDialog(
            onDismissRequest = { showCommitDialog = false },
            title = { Text("Commit Stock") },
            text = {
                Column {
                    Text("Are you sure you want to commit this draft to stock?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This will add the stock to your inventory and delete the draft.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.commitDraft(draftId)
                        showCommitDialog = false
                        onCommitSuccess()
                    }
                ) {
                    Text("Commit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SummaryStatCard(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun SummaryItemCard(
    item: InventoryViewModel.DraftSummary,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.rubberName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            "Rolls",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            item.totalRolls.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column {
                        Text(
                            "Weight",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "%.2f kg".format(item.totalWeight),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column {
                        Text(
                            "Cost",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            currencyFormat.format(item.totalCost),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
