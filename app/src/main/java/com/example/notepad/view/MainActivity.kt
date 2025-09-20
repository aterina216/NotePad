package com.example.notepad.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notepad.R
import com.example.notepad.data.AppDataBase
import com.example.notepad.data.NoteRepository
import com.example.notepad.databinding.ActivityMainBinding
import com.example.notepad.domain.Note
import com.example.notepad.view.adapters.NoteAdapter
import com.example.notepad.view.viewmodels.NoteViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NoteAdapter
    private lateinit var viewModel: NoteViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = NoteAdapter(emptyList())

        binding.notesRecyclerView.adapter = adapter
        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)

        val db = AppDataBase.getDataBase(applicationContext)
        val noteDao = db.noteDao()
        val repository = NoteRepository(noteDao)

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return NoteViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        })[NoteViewModel::class.java]

        viewModel.getAllNotes().observe(this) {
            notes -> adapter.updateNotes(notes)
        }


        }
}