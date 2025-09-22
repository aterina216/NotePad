package com.example.notepad.view.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.data.NoteRepository
import com.example.notepad.domain.Note
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    fun getAllNotes(): LiveData<List<Note>> = repository.getAllNotes()

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                content = content,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun getNoteById(id: Long): LiveData<Note?> {
        return repository.getNoteById(id)
    }

    fun deleteNoteById(id: Long){
        viewModelScope.launch {
            try {
                repository.deleteNoteById(id)
            }
            catch (e: Exception) {
                Log.e("NoteViewModel", "Error deleting note", e)
            }
        }
    }

}