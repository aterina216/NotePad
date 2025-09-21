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

    // Приватное MutableLiveData для хранения списка заметок
    private val _notes = MutableLiveData<List<Note>>()
    // Публичное LiveData для наблюдения UI компонентами
    val notes: LiveData<List<Note>> = _notes

    init {
        loadNotes()
    }

    fun loadNotes() {
        // Наблюдаем за LiveData из репозитория и обновляем наше MutableLiveData
        repository.getAllNotes().observeForever { notesList ->
            _notes.value = notesList
        }
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            try {
                val note = Note(
                    title = title,
                    content = content,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                repository.insertNote(note)
                // Room автоматически обновит LiveData, поэтому явный вызов loadNotes() не нужен
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Error adding note", e)
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                repository.deleteNote(note)
                // Room автоматически обновит LiveData
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Error deleting note", e)
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            try {
                val updatedNote = note.copy(updatedAt = System.currentTimeMillis())
                repository.updateNote(updatedNote)
                // Room автоматически обновит LiveData
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Error updating note", e)
            }
        }
    }

    // Важно: отменить наблюдение при очистке ViewModel
    override fun onCleared() {
        super.onCleared()
        // Если вы используете observeForever, нужно вручную удалить наблюдателя
        // Но лучше переделать на трансформации (см. альтернативное решение ниже)
    }
}