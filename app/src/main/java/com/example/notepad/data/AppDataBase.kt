package com.example.notepad.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.notepad.domain.Note

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class AppDataBase: RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object{
        fun getDataBase(context: Context): AppDataBase{
           return Room.databaseBuilder(context,
               AppDataBase::class.java,
               "notes_db")
               .fallbackToDestructiveMigration()
               .build()
        }
    }
}