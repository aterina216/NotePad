package com.example.notepad.view

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notepad.NotesApplication
import com.example.notepad.R
import com.example.notepad.data.AppDataBase
import com.example.notepad.data.NoteRepository
import com.example.notepad.databinding.ActivityMainBinding
import com.example.notepad.domain.Note
import com.example.notepad.utils.PermissionManager
import com.example.notepad.view.adapters.NoteAdapter
import com.example.notepad.view.fragments.NoteDetailFragment
import com.example.notepad.view.viewmodels.NoteViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NoteAdapter
    private lateinit var viewModel: NoteViewModel
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        initDependencies()
        setupUI()
        checkPermissions()
    }

    private fun initDependencies() {
        viewModel = (application as NotesApplication).viewModel
        permissionManager = PermissionManager(this)
    }

    private fun setupUI() {
        setupRecyclerView()
        setupFab()
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(emptyList()) { note ->
            openDetailFragment(note.id)
        }

        binding.notesRecyclerView.apply {
            adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        setupSwipeToDelete()


        viewModel.getAllNotes().observe(this) { notes ->
            adapter.updateNotes(notes)
        }
    }

    private fun setupFab() {
        binding.floatingActionButton.setOnClickListener {
            openDetailFragment()
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, // направления для drag & drop (0 = отключено)
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT // направления свайпа
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // Не поддерживаем перетаскивание
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val noteToDelete =
                        adapter.getNoteAt(position) // Нужно добавить этот метод в адаптер
                    noteToDelete?.let { note ->
                        // Удаляем из ViewModel
                        viewModel.deleteNote(note)
                    }
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.notesRecyclerView)
    }

    private fun openDetailFragment(noteId: Long = -1) {
        val fragment = NoteDetailFragment.newInstance(noteId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun checkPermissions() {
        val permissions = permissionManager.getRequiredPermissions()
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions, 123)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (!permissionManager.areAllPermissionsGranted(grantResults)) {
                Toast.makeText(this, "Некоторые функции могут не работать", Toast.LENGTH_LONG).show()
            }
        }
    }
}