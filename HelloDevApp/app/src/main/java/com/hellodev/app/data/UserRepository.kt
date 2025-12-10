package com.hellodev.app.data

import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    
    suspend fun insert(user: User) {
        userDao.insertUser(user)
    }
    
    suspend fun deleteAll() {
        userDao.deleteAllUsers()
    }
}
