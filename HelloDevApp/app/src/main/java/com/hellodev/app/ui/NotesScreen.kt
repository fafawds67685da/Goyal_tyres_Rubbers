package com.hellodev.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hellodev.app.data.Note
import com.hellodev.app.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NoteViewModel = viewModel(),
    onAddNote: () -> Unit = {},
    onEditNote: (Int) -> Unit = {}
) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var isSearching by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }
    
    val displayNotes = if (isSearching && searchQuery.isNotEmpty()) searchResults else notes
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes") },
                actions = {
                    IconButton(onClick = { viewMode = if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID }) {
                        Icon(
                            imageVector = if (viewMode == ViewMode.GRID) Icons.Default.ViewList else Icons.Default.ViewModule,
                            contentDescription = "Toggle View"
                        )
                    }
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNote,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isSearching) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchNotes(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search notes...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearSearch() }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )
            }
            
            if (displayNotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isSearching) "No notes found" else "No notes yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                if (viewMode == ViewMode.GRID) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp
                    ) {
                        items(displayNotes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onEditNote(note.id) },
                                onDelete = {
                                    noteToDelete = note
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayNotes, key = { it.id }) { note ->
                            NoteListItem(
                                note = note,
                                onClick = { onEditNote(note.id) },
                                onDelete = {
                                    noteToDelete = note
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showDeleteDialog && noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete '${noteToDelete?.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        noteToDelete?.let { viewModel.deleteNote(it) }
                        showDeleteDialog = false
                        noteToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(note.color)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.Black.copy(alpha = 0.6f))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        )
                    }
                }
            }
            
            if (note.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatDate(note.updatedAt),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun NoteListItem(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(note.color)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.content.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(note.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.5f)
                )
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.Black.copy(alpha = 0.6f))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    noteId: Int? = null,
    viewModel: NoteViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(0xFFFFF59D.toInt()) }
    var isLoading by remember { mutableStateOf(false) }
    
    val noteColors = listOf(
        0xFFFFF59D.toInt(), // Yellow
        0xFFB2DFDB.toInt(), // Teal
        0xFFF8BBD0.toInt(), // Pink
        0xFFCE93D8.toInt(), // Purple
        0xFF90CAF9.toInt(), // Blue
        0xFFA5D6A7.toInt(), // Green
        0xFFFFCCBC.toInt(), // Orange
        0xFFE0E0E0.toInt()  // Gray
    )
    
    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.selectNote(noteId)
        }
    }
    
    val selectedNote by viewModel.selectedNote.collectAsState()
    
    LaunchedEffect(selectedNote) {
        selectedNote?.let { note ->
            title = note.title
            content = note.content
            selectedColor = note.color
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "Add Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotEmpty()) {
                                isLoading = true
                                if (noteId == null) {
                                    viewModel.addNote(title, content, selectedColor)
                                } else {
                                    selectedNote?.let { note ->
                                        viewModel.updateNote(note.copy(title = title, content = content, color = selectedColor))
                                    }
                                }
                                onBack()
                            }
                        },
                        enabled = title.isNotEmpty() && !isLoading
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(selectedColor))
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Title") },
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("Note content...") },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Color",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                noteColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(if (selectedColor == color) 48.dp else 40.dp)
                            .background(Color(color), RoundedCornerShape(24.dp))
                            .clickable { selectedColor = color },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == color) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.Black.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class ViewMode {
    GRID, LIST
}

private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
