package com.hellodev.app.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.hellodev.app.data.ScheduledEvent
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    
    fun scheduleEventNotification(context: Context, event: ScheduledEvent) {
        val delay = calculateDelay(event.notificationTime)
        
        if (delay <= 0) {
            // Notification time has passed, don't schedule
            return
        }
        
        val workRequest = OneTimeWorkRequestBuilder<EventNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putInt(EventNotificationWorker.KEY_EVENT_ID, event.id)
                    .build()
            )
            .addTag("event_${event.id}")
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "event_notification_${event.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }
    
    fun cancelEventNotification(context: Context, eventId: Int) {
        WorkManager.getInstance(context)
            .cancelUniqueWork("event_notification_$eventId")
    }
    
    fun rescheduleAllPendingNotifications(context: Context, events: List<ScheduledEvent>) {
        events.forEach { event ->
            if (!event.isCompleted) {
                scheduleEventNotification(context, event)
            }
        }
    }
    
    private fun calculateDelay(notificationTime: Long): Long {
        return notificationTime - System.currentTimeMillis()
    }
}
