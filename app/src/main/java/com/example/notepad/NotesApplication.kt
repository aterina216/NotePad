package com.example.notepad

import android.app.Application
import com.example.notepad.data.AppDataBase
import com.example.notepad.data.NoteRepository
import com.example.notepad.view.viewmodels.NoteViewModel

class NotesApplication: Application() {

    val viewModel: NoteViewModel by lazy {
        val database = AppDataBase.getDataBase(this)
        val noteDao = database.noteDao()
        val repository = NoteRepository(noteDao)
        NoteViewModel(repository)
    }
}