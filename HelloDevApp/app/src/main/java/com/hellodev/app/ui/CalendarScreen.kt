package com.hellodev.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hellodev.app.data.ScheduledEvent
import com.hellodev.app.viewmodel.CalendarViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(),
    onAddEvent: () -> Unit = {},
    onEditEvent: (Int) -> Unit = {}
) {
    val events by viewModel.events.collectAsState()
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    val eventsForSelectedDate = viewModel.getEventsForDate(selectedDate)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar & Schedule") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEvent,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Calendar") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Upcoming") }
                )
            }
            
            when (selectedTab) {
                0 -> CalendarView(
                    selectedDate = selectedDate,
                    events = eventsForSelectedDate,
                    onDateSelected = { viewModel.setSelectedDate(it) },
                    onEventClick = { onEditEvent(it.id) }
                )
                1 -> UpcomingEventsView(
                    events = upcomingEvents,
                    onEventClick = { onEditEvent(it.id) },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun CalendarView(
    selectedDate: Long,
    events: List<ScheduledEvent>,
    onDateSelected: (Long) -> Unit,
    onEventClick: (ScheduledEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Mini Calendar
        MiniCalendar(
            selectedDate = selectedDate,
            onDateSelected = onDateSelected
        )
        
        Divider()
        
        // Events for selected date
        Text(
            text = "Events for ${formatDateHeader(selectedDate)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        if (events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.EventBusy,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No events for this date",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events) { event ->
                    EventCard(event = event, onClick = { onEventClick(event) })
                }
            }
        }
    }
}

@Composable
fun MiniCalendar(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = selectedDate
    
    var currentMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (currentMonth == 0) {
                    currentMonth = 11
                    currentYear--
                } else {
                    currentMonth--
                }
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }
            
            Text(
                text = "${getMonthName(currentMonth)} $currentYear",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = {
                if (currentMonth == 11) {
                    currentMonth = 0
                    currentYear++
                } else {
                    currentMonth++
                }
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Day headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar grid
        val daysInMonth = getDaysInMonth(currentYear, currentMonth)
        val firstDayOfMonth = getFirstDayOfMonth(currentYear, currentMonth)
        
        Column {
            var dayCounter = 1 - firstDayOfMonth
            
            repeat(6) { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { day ->
                        val currentDay = dayCounter
                        val isValidDay = currentDay in 1..daysInMonth
                        
                        val dayCalendar = Calendar.getInstance()
                        dayCalendar.set(currentYear, currentMonth, if (isValidDay) currentDay else 1)
                        val dayTimestamp = dayCalendar.timeInMillis
                        
                        val isSelected = isValidDay && isSameDay(dayTimestamp, selectedDate)
                        val isToday = isValidDay && isSameDay(dayTimestamp, System.currentTimeMillis())
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable(enabled = isValidDay) {
                                    if (isValidDay) {
                                        onDateSelected(dayTimestamp)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isValidDay) {
                                Text(
                                    text = currentDay.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                        dayCounter++
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingEventsView(
    events: List<ScheduledEvent>,
    onEventClick: (ScheduledEvent) -> Unit,
    viewModel: CalendarViewModel
) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.EventAvailable,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No upcoming events",
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
            items(events) { event ->
                EventCard(
                    event = event,
                    onClick = { onEventClick(event) },
                    showDate = true,
                    onMarkComplete = { viewModel.markEventCompleted(event.id) }
                )
            }
        }
    }
}

@Composable
fun EventCard(
    event: ScheduledEvent,
    onClick: () -> Unit,
    showDate: Boolean = false,
    onMarkComplete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (event.isCompleted) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (event.isCompleted) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (event.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showDate) formatDateTime(event.eventDate) else formatTime(event.eventDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            if (onMarkComplete != null && !event.isCompleted) {
                IconButton(onClick = onMarkComplete) {
                    Icon(
                        Icons.Default.CheckCircleOutline,
                        contentDescription = "Mark Complete",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventScreen(
    eventId: Int? = null,
    viewModel: CalendarViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var notificationTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    LaunchedEffect(eventId) {
        if (eventId != null) {
            viewModel.selectEvent(eventId)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (eventId == null) "Add Event" else "Edit Event") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotEmpty()) {
                                val event = ScheduledEvent(
                                    id = eventId ?: 0,
                                    title = title,
                                    description = description,
                                    eventDate = eventDate,
                                    notificationTime = notificationTime,
                                    notes = notes
                                )
                                if (eventId == null) {
                                    viewModel.addEvent(event)
                                } else {
                                    viewModel.updateEvent(event)
                                }
                                onBack()
                            }
                        },
                        enabled = title.isNotEmpty()
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
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    ListItem(
                        headlineContent = { Text("Event Date & Time") },
                        supportingContent = { Text(formatDateTime(eventDate)) },
                        leadingContent = {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        }
                    )
                }
            }
            
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }
        }
    }
}

// Helper functions
private fun getDaysInMonth(year: Int, month: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

private fun getFirstDayOfMonth(year: Int, month: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    return calendar.get(Calendar.DAY_OF_WEEK) - 1
}

private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun getMonthName(month: Int): String {
    return listOf("January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December")[month]
}

private fun formatDateHeader(timestamp: Long): String {
    return SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
}

private fun formatDateTime(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
}
