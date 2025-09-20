package com.example.notepad.view.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.data.NoteRepository
import com.example.notepad.domain.Note
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository): ViewModel() {

    var notes = MutableLiveData<List<Note>>()


    fun getAllNotes(): LiveData<List<Note>> = repository.getAllNotes()

    suspend fun addNote(note: Note) {
        viewModelScope.launch {
            repository.insertNote(note)
        }
    }

    suspend fun deleteNote(note: Note){
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    suspend fun updateNote(note: Note){
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }
}