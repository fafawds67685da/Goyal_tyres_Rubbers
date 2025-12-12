package com.hellodev.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hellodev.app.data.AppDatabase
import com.hellodev.app.data.ScheduledEvent
import com.hellodev.app.data.ScheduledEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    
    private val eventRepository: ScheduledEventRepository
    
    // Event States
    private val _events = MutableStateFlow<List<ScheduledEvent>>(emptyList())
    val events: StateFlow<List<ScheduledEvent>> = _events.asStateFlow()
    
    private val _upcomingEvents = MutableStateFlow<List<ScheduledEvent>>(emptyList())
    val upcomingEvents: StateFlow<List<ScheduledEvent>> = _upcomingEvents.asStateFlow()
    
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        val database = AppDatabase.getDatabase(application)
        eventRepository = ScheduledEventRepository(database.eventDao())
        
        // Collect all events
        viewModelScope.launch {
            eventRepository.allEvents.collect { eventList ->
                _events.value = eventList
            }
        }
        
        // Collect upcoming events
        viewModelScope.launch {
            eventRepository.upcomingEvents.collect { eventList ->
                _upcomingEvents.value = eventList
            }
        }
    }
    
    fun setSelectedDate(date: Long) {
        _selectedDate.value = date
    }
    
    fun getEventsForDate(date: Long): List<ScheduledEvent> {
        val startOfDay = getStartOfDay(date)
        val endOfDay = getEndOfDay(date)
        return _events.value.filter { event ->
            event.eventDate in startOfDay..endOfDay
        }
    }
    
    fun getEventsForDateRange(startDate: Long, endDate: Long): List<ScheduledEvent> {
        return _events.value.filter { event ->
            event.eventDate in startDate..endDate
        }
    }
    
    fun addEvent(event: ScheduledEvent) {
        viewModelScope.launch {
            _isLoading.value = true
            eventRepository.insertEvent(event)
            _isLoading.value = false
        }
    }
    
    fun updateEvent(event: ScheduledEvent) {
        viewModelScope.launch {
            _isLoading.value = true
            eventRepository.updateEvent(event)
            _isLoading.value = false
        }
    }
    
    fun deleteEvent(event: ScheduledEvent) {
        viewModelScope.launch {
            _isLoading.value = true
            eventRepository.deleteEvent(event)
            _isLoading.value = false
        }
    }
    
    fun markEventCompleted(eventId: Int) {
        viewModelScope.launch {
            eventRepository.markCompleted(eventId, true)
        }
    }
    
    fun selectEvent(eventId: Int) {
        viewModelScope.launch {
            eventRepository.getEventById(eventId)
        }
    }
    
    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun getEndOfDay(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
