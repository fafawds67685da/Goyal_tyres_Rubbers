package com.hellodev.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val color: Int = 0xFFFFF59D.toInt() // Default: Light Yellow
)

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): Note?
    
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchNotes(query: String): Flow<List<Note>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long
    
    @Update
    suspend fun updateNote(note: Note)
    
    @Delete
    suspend fun deleteNote(note: Note)
    
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Int)
    
    @Query("SELECT COUNT(*) FROM notes")
    fun getTotalNotesCount(): Flow<Int>
}

class NoteRepository(private val noteDao: NoteDao) {
    
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    
    suspend fun getNoteById(noteId: Int): Note? = noteDao.getNoteById(noteId)
    
    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)
    
    fun getTotalNotesCount(): Flow<Int> = noteDao.getTotalNotesCount()
    
    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)
    
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
    
    suspend fun deleteNoteById(noteId: Int) = noteDao.deleteNoteById(noteId)
}
