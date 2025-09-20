package com.example.notepad.data

import androidx.lifecycle.LiveData
import com.example.notepad.domain.Note

class NoteRepository(private val dao: NoteDao) {

    fun getAllNotes() : LiveData<List<Note>> = dao.getAllNotes()

    suspend fun insertNote(note: Note) = dao.insertNote(note)

    suspend fun updateNote(note: Note) = dao.updateNote(note)

    suspend fun deleteNote(note: Note) = dao.deleteNote(note)
}