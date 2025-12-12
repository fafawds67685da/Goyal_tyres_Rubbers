package com.hellodev.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hellodev.app.MainActivity
import com.hellodev.app.R
import com.hellodev.app.data.AppDatabase
import com.hellodev.app.data.ScheduledEventRepository
import kotlinx.coroutines.flow.first

class EventNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val eventId = inputData.getInt(KEY_EVENT_ID, -1)
        if (eventId == -1) return Result.failure()

        val database = AppDatabase.getDatabase(context)
        val eventRepository = ScheduledEventRepository(database.eventDao())
        
        val event = eventRepository.getEventById(eventId) ?: return Result.failure()

        // Create notification channel
        createNotificationChannel()

        // Build notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(event.title)
            .setContentText(if (event.description.isNotEmpty()) event.description else "Scheduled event reminder")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Show notification if permission granted
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
            == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(eventId, notification)
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Event Reminders"
            val descriptionText = "Notifications for scheduled events"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "event_reminders"
        const val KEY_EVENT_ID = "event_id"
    }
}
