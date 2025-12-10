package com.hellodev.app.viewmodel

import android.app.Application
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hellodev.app.data.AppDatabase
import com.hellodev.app.data.User
import com.hellodev.app.data.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class UserViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: UserRepository
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    private val _exportPath = MutableStateFlow<String>("")
    val exportPath: StateFlow<String> = _exportPath.asStateFlow()
    
    val dbPath: String
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = UserRepository(database.userDao())
        dbPath = application.getDatabasePath("user_database").absolutePath
        
        viewModelScope.launch {
            repository.allUsers.collect { userList ->
                _users.value = userList
            }
        }
    }
    
    fun insertUser(name: String, age: Int) {
        viewModelScope.launch {
            val user = User(name = name, age = age)
            repository.insert(user)
        }
    }
    
    fun clearAllUsers() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
    
    fun exportDatabase() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Get the Downloads directory
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val exportFile = File(downloadsDir, "user_database.db")
                    
                    // Copy database file
                    val dbFile = File(dbPath)
                    if (dbFile.exists()) {
                        FileInputStream(dbFile).use { input ->
                            FileOutputStream(exportFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        
                        _exportPath.value = exportFile.absolutePath
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                getApplication(),
                                "Database exported to Downloads folder!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "Export failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
