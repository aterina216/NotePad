package com.example.notepad.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.notepad.R
import com.example.notepad.view.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm received!")

        val noteId = intent.getLongExtra("note_id", -1)
        val noteTitle = intent.getStringExtra("note_title") ?: ""
        val noteContent = intent.getStringExtra("note_content") ?: ""

        Log.d("AlarmReceiver", "Note ID: $noteId, Title: $noteTitle")

        // Создаем уведомление
        createNotification(context, noteId, noteTitle, noteContent)
    }

    private fun createNotification(context: Context, noteId: Long, title: String, content: String) {
        try {
            Log.d("AlarmReceiver", "Creating notification...")

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Создаем канал для уведомлений
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "note_reminders",
                    "Напоминания о заметках",
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.description = "Уведомления о напоминаниях для заметок"
                channel.enableVibration(true)
                channel.vibrationPattern = longArrayOf(0, 500, 200, 500)
                channel.setShowBadge(true)
                notificationManager.createNotificationChannel(channel)
            }

            // Intent для открытия приложения
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("note_id", noteId)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                noteId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Строим уведомление
            val notification = NotificationCompat.Builder(context, "note_reminders")
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Временная иконка
                .setContentTitle("Напоминание: $title")
                .setContentText(if (content.length > 50) content.substring(0, 50) + "..." else content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()

            // Показываем уведомление
            notificationManager.notify(noteId.toInt(), notification)
            Log.d("AlarmReceiver", "Notification shown with ID: ${noteId.toInt()}")

        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error creating notification", e)
        }
    }
}