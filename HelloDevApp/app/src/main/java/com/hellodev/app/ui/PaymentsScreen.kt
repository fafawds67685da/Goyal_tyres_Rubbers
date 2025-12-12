package com.hellodev.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hellodev.app.data.Payment
import com.hellodev.app.data.PaymentTransaction
import com.hellodev.app.data.PaymentType
import com.hellodev.app.viewmodel.PaymentViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    viewModel: PaymentViewModel = viewModel(),
    onAddPayment: (PaymentType) -> Unit = {},
    onPaymentClick: (Int) -> Unit = {}
) {
    val incomingPayments by viewModel.incomingPayments.collectAsState()
    val outgoingPayments by viewModel.outgoingPayments.collectAsState()
    val pendingIncoming by viewModel.pendingIncoming.collectAsState()
    val pendingOutgoing by viewModel.pendingOutgoing.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payments") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    onAddPayment(if (selectedTab == 0) PaymentType.INCOMING else PaymentType.OUTGOING)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Payment")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "To Receive",
                    amount = pendingIncoming,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "To Pay",
                    amount = pendingOutgoing,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Incoming") },
                    icon = { Icon(Icons.Default.ArrowDownward, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Outgoing") },
                    icon = { Icon(Icons.Default.ArrowUpward, contentDescription = null) }
                )
            }
            
            // Payment Lists
            when (selectedTab) {
                0 -> PaymentList(
                    payments = incomingPayments,
                    onPaymentClick = onPaymentClick
                )
                1 -> PaymentList(
                    payments = outgoingPayments,
                    onPaymentClick = onPaymentClick
                )
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹${formatAmount(amount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PaymentList(
    payments: List<Payment>,
    onPaymentClick: (Int) -> Unit
) {
    if (payments.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.AccountBalance,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No payments found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(payments) { payment ->
                PaymentCard(
                    payment = payment,
                    onClick = { onPaymentClick(payment.id) }
                )
            }
        }
    }
}

@Composable
fun PaymentCard(
    payment: Payment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (payment.isFullyPaid)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = payment.partyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (payment.remark.isNotEmpty()) {
                        Text(
                            text = payment.remark,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (payment.isFullyPaid) {
                    Chip(
                        label = "Paid",
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Chip(
                        label = "Pending",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            if (!payment.isFullyPaid) {
                Column {
                    LinearProgressIndicator(
                        progress = payment.paymentProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "₹${formatAmount(payment.totalAmount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Column {
                    Text(
                        text = "Paid",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "₹${formatAmount(payment.paidAmount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column {
                    Text(
                        text = "Pending",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "₹${formatAmount(payment.pendingAmount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (payment.isFullyPaid) 
                            MaterialTheme.colorScheme.outline 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due: ${formatDate(payment.dueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun Chip(
    label: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDetailsScreen(
    paymentId: Int,
    viewModel: PaymentViewModel = viewModel(),
    onBack: () -> Unit = {},
    onAddTransaction: () -> Unit = {}
) {
    LaunchedEffect(paymentId) {
        viewModel.selectPayment(paymentId)
    }
    
    val selectedPayment by viewModel.selectedPayment.collectAsState()
    val transactions by viewModel.paymentTransactions.collectAsState()
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedPayment?.isFullyPaid == false) {
                FloatingActionButton(
                    onClick = { showAddTransactionDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Payment")
                }
            }
        }
    ) { padding ->
        selectedPayment?.let { payment ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (payment.type == PaymentType.INCOMING)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = payment.partyName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (payment.type == PaymentType.INCOMING) "To Receive" else "To Pay",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            PaymentDetailRow("Total Amount", "₹${formatAmount(payment.totalAmount)}")
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            PaymentDetailRow("Paid Amount", "₹${formatAmount(payment.paidAmount)}", MaterialTheme.colorScheme.primary)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            PaymentDetailRow("Pending Amount", "₹${formatAmount(payment.pendingAmount)}", MaterialTheme.colorScheme.error)
                            
                            if (!payment.isFullyPaid) {
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = payment.paymentProgress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${(payment.paymentProgress * 100).toInt()}% paid",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
                
                if (payment.remark.isNotEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Remark",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = payment.remark)
                            }
                        }
                    }
                }
                
                if (payment.notes.isNotEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Notes",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = payment.notes)
                            }
                        }
                    }
                }
                
                item {
                    Text(
                        text = "Payment History (${transactions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (transactions.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No transactions yet",
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                } else {
                    items(transactions) { transaction ->
                        TransactionCard(transaction)
                    }
                }
            }
        }
    }
    
    if (showAddTransactionDialog && selectedPayment != null) {
        AddTransactionDialog(
            payment = selectedPayment!!,
            onDismiss = { showAddTransactionDialog = false },
            onConfirm = { amount, remark ->
                viewModel.addPaymentTransaction(paymentId, amount, remark)
                showAddTransactionDialog = false
            }
        )
    }
}

@Composable
fun PaymentDetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun TransactionCard(transaction: PaymentTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
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
                    text = "₹${formatAmount(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDateTime(transaction.transactionDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transaction.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = transaction.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Paid",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    payment: Payment,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Payment") },
        text = {
            Column {
                Text("Pending: ₹${formatAmount(payment.pendingAmount)}")
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₹") },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("Remark (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0) {
                        onConfirm(amountValue, remark)
                    }
                },
                enabled = amount.toDoubleOrNull()?.let { it > 0 && it <= payment.pendingAmount } == true
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentScreen(
    paymentType: PaymentType,
    viewModel: PaymentViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    var partyName by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var paidAmount by remember { mutableStateOf("0") }
    var dueDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var remark by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (paymentType == PaymentType.INCOMING) "Add Incoming Payment" else "Add Outgoing Payment") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val total = totalAmount.toDoubleOrNull() ?: 0.0
                            val paid = paidAmount.toDoubleOrNull() ?: 0.0
                            
                            if (partyName.isNotEmpty() && total > 0) {
                                val payment = Payment(
                                    type = paymentType,
                                    partyName = partyName,
                                    totalAmount = total,
                                    paidAmount = paid,
                                    dueDate = dueDate,
                                    remark = remark,
                                    notes = notes,
                                    isFullyPaid = paid >= total
                                )
                                viewModel.addPayment(payment)
                                onBack()
                            }
                        },
                        enabled = partyName.isNotEmpty() && (totalAmount.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (paymentType == PaymentType.INCOMING)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (paymentType == PaymentType.INCOMING) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (paymentType == PaymentType.INCOMING) "Money to Receive" else "Money to Pay",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (paymentType == PaymentType.INCOMING) "From Customer" else "To Factory/Supplier",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            item {
                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text(if (paymentType == PaymentType.INCOMING) "Customer Name*" else "Factory/Supplier Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
            }
            
            item {
                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = { totalAmount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Total Amount*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    prefix = { Text("₹") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) }
                )
            }
            
            item {
                OutlinedTextField(
                    value = paidAmount,
                    onValueChange = { paidAmount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Paid Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    prefix = { Text("₹") },
                    leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null) },
                    supportingText = { Text("Amount already paid (if any)") }
                )
            }
            
            item {
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("Remark") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Comment, contentDescription = null) }
                )
            }
            
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
                )
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return NumberFormat.getNumberInstance(Locale("en", "IN")).format(amount)
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
}

private fun formatDateTime(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(timestamp))
}
