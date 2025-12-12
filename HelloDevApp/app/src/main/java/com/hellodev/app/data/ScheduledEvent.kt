package com.hellodev.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_events")
data class ScheduledEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val eventDate: Long, // Date and time of the event
    val notificationTime: Long, // When to send notification (can be before event)
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)
