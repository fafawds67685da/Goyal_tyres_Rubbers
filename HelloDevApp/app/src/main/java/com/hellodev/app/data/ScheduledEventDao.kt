package com.hellodev.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledEventDao {
    @Insert
    suspend fun insertEvent(event: ScheduledEvent): Long
    
    @Update
    suspend fun updateEvent(event: ScheduledEvent)
    
    @Delete
    suspend fun deleteEvent(event: ScheduledEvent)
    
    @Query("SELECT * FROM scheduled_events ORDER BY eventDate ASC")
    fun getAllEvents(): Flow<List<ScheduledEvent>>
    
    @Query("SELECT * FROM scheduled_events WHERE isCompleted = 0 ORDER BY eventDate ASC")
    fun getUpcomingEvents(): Flow<List<ScheduledEvent>>
    
    @Query("SELECT * FROM scheduled_events WHERE eventDate >= :startDate AND eventDate <= :endDate ORDER BY eventDate ASC")
    fun getEventsByDateRange(startDate: Long, endDate: Long): Flow<List<ScheduledEvent>>
    
    @Query("SELECT * FROM scheduled_events WHERE id = :eventId")
    suspend fun getEventById(eventId: Int): ScheduledEvent?
    
    @Query("UPDATE scheduled_events SET isCompleted = :completed WHERE id = :eventId")
    suspend fun markEventCompleted(eventId: Int, completed: Boolean)
}

class ScheduledEventRepository(private val eventDao: ScheduledEventDao) {
    val allEvents: Flow<List<ScheduledEvent>> = eventDao.getAllEvents()
    val upcomingEvents: Flow<List<ScheduledEvent>> = eventDao.getUpcomingEvents()
    
    suspend fun insertEvent(event: ScheduledEvent): Long {
        return eventDao.insertEvent(event)
    }
    
    suspend fun updateEvent(event: ScheduledEvent) {
        eventDao.updateEvent(event)
    }
    
    suspend fun deleteEvent(event: ScheduledEvent) {
        eventDao.deleteEvent(event)
    }
    
    suspend fun getEventById(eventId: Int): ScheduledEvent? {
        return eventDao.getEventById(eventId)
    }
    
    fun getEventsByDateRange(startDate: Long, endDate: Long): Flow<List<ScheduledEvent>> {
        return eventDao.getEventsByDateRange(startDate, endDate)
    }
    
    suspend fun markCompleted(eventId: Int, completed: Boolean) {
        eventDao.markEventCompleted(eventId, completed)
    }
}
