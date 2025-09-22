package com.example.notepad.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.notepad.NotesApplication
import com.example.notepad.R
import com.example.notepad.data.AppDataBase
import com.example.notepad.data.NoteRepository
import com.example.notepad.databinding.FragmentNoteDetailBinding
import com.example.notepad.domain.Note
import com.example.notepad.view.viewmodels.NoteViewModel
import com.example.notepad.view.viewmodels.NoteViewModelFactory

/**
 * A simple [Fragment] subclass.
 * Use the [NoteDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NoteDetailFragment : Fragment() {

    private lateinit var binding: FragmentNoteDetailBinding
    private lateinit var viewModel: NoteViewModel
    private var noteId: Long = -1
    private lateinit var repository: NoteRepository
    private var currentNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            noteId = it.getLong("note_id", -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNoteDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (requireActivity().application as NotesApplication).viewModel

        if(noteId != -1L){
            viewModel.getNoteById(noteId).observe(viewLifecycleOwner){
                note ->
                note?.let{
                    currentNote = it
                    binding.noteTitleText.setText(it.title)
                    binding.noteContentText.setText(it.content)
                }
            }
        }

        binding.saveButton.setOnClickListener {
            val title = binding.noteTitleText.text.toString()
            val content = binding.noteContentText.text.toString()

            if(title.isBlank() && content.isBlank()){
                Toast.makeText(requireContext(), "Заметка не может быть пустой", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(noteId == -1L){
                viewModel.addNote(title, content)
                Toast.makeText(requireContext(), "Заметка создана", Toast.LENGTH_SHORT).show()
            }
            else{
                viewModel.getNoteById(noteId).observe(viewLifecycleOwner){
                    note ->
                    note?.let {
                        val newNote = it.copy(title = title,
                            content = content,
                            updatedAt = System.currentTimeMillis())
                        viewModel.updateNote(newNote)
                        Toast.makeText(requireContext(), "Заметка обновлена", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            parentFragmentManager.popBackStack()
        }

        binding.deleteButton.setOnClickListener {
            if(noteId != -1L){
                showDeleteDialog()
            }
            else {
                Toast.makeText(requireContext(), "Нельзя удалить несохраненную заметку", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun showDeleteDialog(){
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление заметки")
            .setMessage("Вы уверены, что хотите удалить эту заметку?")
            .setPositiveButton("Удалить"){
                dialog, which ->
                viewModel.deleteNoteById(noteId)
                Toast.makeText(requireContext(), "Заметка удалена", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    companion object {
        fun newInstance(noteId: Long = -1) = // Измените на Long
            NoteDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong("note_id", noteId) // Используйте putLong
                }
            }
    }
}