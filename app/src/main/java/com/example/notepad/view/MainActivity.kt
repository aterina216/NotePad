package com.example.notepad.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notepad.NotesApplication
import com.example.notepad.R
import com.example.notepad.data.AppDataBase
import com.example.notepad.data.NoteRepository
import com.example.notepad.databinding.ActivityMainBinding
import com.example.notepad.domain.Note
import com.example.notepad.view.adapters.NoteAdapter
import com.example.notepad.view.fragments.NoteDetailFragment
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

        viewModel = (application as NotesApplication).viewModel
        // Инициализация адаптера
        adapter = NoteAdapter(emptyList()) { note ->
            openDetailFragment(note.id)
        }

        binding.notesRecyclerView.adapter = adapter
        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)


        // Наблюдение за изменениями в списке заметок
        viewModel.getAllNotes().observe(this) { notes ->
            adapter.updateNotes(notes)
        }

        binding.floatingActionButton.setOnClickListener {
            openDetailFragment()
        }
    }

    private fun openDetailFragment(noteId: Long = -1) {
        val fragment = NoteDetailFragment.newInstance(noteId)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}