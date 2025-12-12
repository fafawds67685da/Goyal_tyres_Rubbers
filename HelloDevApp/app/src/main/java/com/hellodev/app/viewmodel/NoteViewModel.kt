package com.hellodev.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hellodev.app.data.AppDatabase
import com.hellodev.app.data.Note
import com.hellodev.app.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    
    private val noteRepository: NoteRepository
    
    // Note States
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<Note>>(emptyList())
    val searchResults: StateFlow<List<Note>> = _searchResults.asStateFlow()
    
    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()
    
    private val _totalNotesCount = MutableStateFlow(0)
    val totalNotesCount: StateFlow<Int> = _totalNotesCount.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    init {
        val database = AppDatabase.getDatabase(application)
        noteRepository = NoteRepository(database.noteDao())
        
        // Collect all notes
        viewModelScope.launch {
            noteRepository.getAllNotes().collect { noteList ->
                _notes.value = noteList
            }
        }
        
        // Collect total count
        viewModelScope.launch {
            noteRepository.getTotalNotesCount().collect { count ->
                _totalNotesCount.value = count
            }
        }
    }
    
    fun searchNotes(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            noteRepository.searchNotes(query).collect { results ->
                _searchResults.value = results
            }
        }
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }
    
    fun selectNote(noteId: Int) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId)
            _selectedNote.value = note
        }
    }
    
    fun clearSelectedNote() {
        _selectedNote.value = null
    }
    
    fun addNote(title: String, content: String, color: Int = 0xFFFFF59D.toInt()) {
        viewModelScope.launch {
            _isLoading.value = true
            val note = Note(
                title = title,
                content = content,
                color = color,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            noteRepository.insertNote(note)
            _isLoading.value = false
        }
    }
    
    fun updateNote(note: Note) {
        viewModelScope.launch {
            _isLoading.value = true
            val updatedNote = note.copy(updatedAt = System.currentTimeMillis())
            noteRepository.updateNote(updatedNote)
            _isLoading.value = false
        }
    }
    
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            _isLoading.value = true
            noteRepository.deleteNote(note)
            _isLoading.value = false
        }
    }
    
    fun deleteNoteById(noteId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            noteRepository.deleteNoteById(noteId)
            _isLoading.value = false
        }
    }
}
