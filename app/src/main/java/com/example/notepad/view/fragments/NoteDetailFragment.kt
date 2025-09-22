package com.example.notepad.view.fragments

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.notepad.NotesApplication
import com.example.notepad.R
import com.example.notepad.data.AppDataBase
import com.example.notepad.data.NoteRepository
import com.example.notepad.databinding.FragmentNoteDetailBinding
import com.example.notepad.domain.Note
import com.example.notepad.utils.AlarmHelper
import com.example.notepad.utils.DateTimePickerDialogHelper
import com.example.notepad.utils.PermissionManager
import com.example.notepad.view.viewmodels.NoteViewModel
import com.example.notepad.view.viewmodels.NoteViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A simple [Fragment] subclass.
 * Use the [NoteDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NoteDetailFragment : Fragment() {

    private lateinit var binding: FragmentNoteDetailBinding
    private lateinit var viewModel: NoteViewModel
    private var noteId: Long = -1
    private var currentNote: Note? = null

    private lateinit var alarmHelper: AlarmHelper
    private lateinit var permissionManager: PermissionManager
    private lateinit var dateTimePicker: DateTimePickerDialogHelper

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
        binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDependencies()
        setupViewModel()
        setupUI()
        loadNoteData()
        loadExistingAlarm()
    }

    private fun initDependencies() {
        viewModel = (requireActivity().application as NotesApplication).viewModel
        alarmHelper = AlarmHelper(requireContext())
        permissionManager = PermissionManager(requireContext())
        dateTimePicker = DateTimePickerDialogHelper(requireContext()) { selectedTime ->
            onDateTimeSelected(selectedTime)
        }
    }

    private fun setupViewModel() {
        // Наблюдаем за изменениями заметки
        if (noteId != -1L) {
            viewModel.getNoteById(noteId).observe(viewLifecycleOwner) { note ->
                note?.let {
                    currentNote = it
                    binding.noteTitleText.setText(it.title)
                    binding.noteContentText.setText(it.content)
                }
            }
        }
    }

    private fun setupUI() {
        setupSaveButton()
        setupDeleteButton()
        setupAlarmButton()
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val title = binding.noteTitleText.text.toString()
            val content = binding.noteContentText.text.toString()

            if (title.isBlank() && content.isBlank()) {
                Toast.makeText(requireContext(), "Заметка не может быть пустой", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (noteId == -1L) {
                viewModel.addNote(title, content)
                Toast.makeText(requireContext(), "Заметка создана", Toast.LENGTH_SHORT).show()
            } else {
                currentNote?.let { note ->
                    val updatedNote = note.copy(
                        title = title,
                        content = content,
                        updatedAt = System.currentTimeMillis()
                    )
                    viewModel.updateNote(updatedNote)
                    Toast.makeText(requireContext(), "Заметка обновлена", Toast.LENGTH_SHORT).show()
                }
            }
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupDeleteButton() {
        binding.deleteButton.setOnClickListener {
            if (noteId != -1L) {
                showDeleteDialog()
            } else {
                Toast.makeText(requireContext(), "Нельзя удалить несохраненную заметку", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAlarmButton() {
        binding.alarmButton.setOnClickListener {
            if (permissionManager.hasAllPermissions()) {
                dateTimePicker.show()
            } else {
                requestNeededPermissions()
            }
        }
    }

    private fun loadNoteData() {
        if (noteId != -1L) {
            viewModel.getNoteById(noteId).observe(viewLifecycleOwner) { note ->
                note?.let {
                    currentNote = it
                    binding.noteTitleText.setText(it.title)
                    binding.noteContentText.setText(it.content)
                }
            }
        }
    }

    private fun loadExistingAlarm() {
        if (noteId != -1L) {
            val alarmTime = alarmHelper.getAlarmTime(noteId)
            if (alarmTime > 0) {
                updateAlarmButtonTooltip(alarmTime)
            }
        }
    }

    private fun onDateTimeSelected(alarmTime: Long) {
        val title = binding.noteTitleText.text.toString()
        val content = binding.noteContentText.text.toString()

        if (alarmHelper.setAlarm(noteId, title, content, alarmTime)) {
            updateAlarmButtonTooltip(alarmTime)
            Toast.makeText(requireContext(), "Напоминание установлено", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Ошибка установки напоминания", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAlarmButtonTooltip(alarmTime: Long) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.alarmButton.tooltipText = "Напоминание: ${dateFormat.format(Date(alarmTime))}"
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление заметки")
            .setMessage("Вы уверены, что хотите удалить эту заметку?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteNoteById(noteId)
                alarmHelper.cancelAlarm(noteId) // Отменяем напоминание при удалении
                Toast.makeText(requireContext(), "Заметка удалена", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun requestNeededPermissions() {
        val permissions = permissionManager.getRequiredPermissions()
        if (permissions.isNotEmpty()) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE)
        } else {
            permissionManager.openExactAlarmSettings()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (permissionManager.areAllPermissionsGranted(grantResults)) {
                dateTimePicker.show()
            } else {
                Toast.makeText(requireContext(), "Разрешения необходимы для работы напоминаний", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123

        fun newInstance(noteId: Long = -1) = NoteDetailFragment().apply {
            arguments = Bundle().apply {
                putLong("note_id", noteId)
            }
        }
    }
}