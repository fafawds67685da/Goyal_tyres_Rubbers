package com.hellodev.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.hellodev.app.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDraftScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit,
    onDraftCreated: (Int) -> Unit
) {
    var supplier by remember { mutableStateOf("") }
    var vehicle by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val drafts by viewModel.drafts.collectAsState()
    
    // Watch for new draft creation
    LaunchedEffect(drafts.size, isCreating) {
        if (isCreating && drafts.isNotEmpty()) {
            val latestDraft = drafts.first()
            if (latestDraft.supplierName == supplier && latestDraft.vehicleNumber == vehicle) {
                isCreating = false
                onDraftCreated(latestDraft.id)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Draft") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Draft Information",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Text(
                            "Create a draft to add stock items incrementally. " +
                            "You can add multiple items from different categories, " +
                            "review the summary, and commit when ready.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier Name *") },
                    placeholder = { Text("Enter supplier company name") },
                    leadingIcon = {
                        Icon(Icons.Default.Business, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = vehicle,
                    onValueChange = { vehicle = it.uppercase() },
                    label = { Text("Vehicle Number *") },
                    placeholder = { Text("Enter vehicle registration number") },
                    leadingIcon = {
                        Icon(Icons.Default.LocalShipping, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Add any additional notes") },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    minLines = 3,
                    maxLines = 5
                )
                
                Button(
                    onClick = {
                        if (supplier.isNotBlank() && vehicle.isNotBlank()) {
                            viewModel.createDraft(supplier.trim(), vehicle.trim())
                            isCreating = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = supplier.isNotBlank() && vehicle.isNotBlank() && !isCreating
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Draft & Add Items")
                    }
                }
                
                if (supplier.isBlank() || vehicle.isBlank()) {
                    Text(
                        "* Required fields",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}
