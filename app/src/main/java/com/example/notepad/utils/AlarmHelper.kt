package com.example.notepad.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmHelper(private val context: Context) {

    @SuppressLint("NewApi")
    fun setAlarm(noteId: Long, noteTitle: String, noteContent: String, alarmTime: Long){

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("note_id", noteId)
            putExtra("note_title", noteTitle)
            putExtra("note_content", noteContent)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(alarmManager.canScheduleExactAlarms()){
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    pendingIntent
                )
            }
        }
        else{
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        }
        saveAlarmInfo(noteId, alarmTime)
    }

    private fun saveAlarmInfo(noteId: Long, alarmTime: Long){

        val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("alarm_$noteId", alarmTime).apply()
    }

    fun getAlarmTime(noteId: Long): Long{
        val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("alarm_$noteId", 0)
    }

    fun cancelAlarm(noteId: Long){
        val intent = Intent(
            context, AlarmReceiver::class.java
        )

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel (pendingIntent)

        val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("alarm_$noteId").apply()
    }
}