package com.example.notepad.view.fragments

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
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
    private lateinit var repository: NoteRepository
    private var currentNote: Note? = null

    private lateinit var alarmHelper: AlarmHelper

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

    @RequiresApi(Build.VERSION_CODES.O)
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

        alarmHelper = AlarmHelper(requireContext())

        binding.alarmButton.setOnClickListener {
            // Проверяем разрешения
            if (checkPermissions()) {
                showDateTimePicker()
            } else {
                requestPermissions()
            }
        }

        // Показываем текущее время напоминания если оно есть
        if (noteId != -1L) {
            val alarmTime = alarmHelper.getAlarmTime(noteId)
            if (alarmTime > 0) {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                binding.alarmButton.tooltipText = "Напоминание: ${dateFormat.format(Date(alarmTime))}"
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

    private fun checkPermissions(): Boolean{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE)
            as AlarmManager
            alarmManager.canScheduleExactAlarms()
        }
        else{
            true
        }
    }

    private fun requestPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            requestPermissions(
                arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM),
                PERMISSION_REQUEST_CODE
            )
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDateTimePicker() {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        DatePickerDialog(requireContext(), { _, year, month, day ->
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val selectedDateTime = Calendar.getInstance().apply {
                    set(year, month, day, hour, minute)
                }

                // Устанавливаем напоминание
                val title = binding.noteTitleText.text.toString()
                val content = binding.noteContentText.text.toString()
                alarmHelper.setAlarm(noteId, title, content, selectedDateTime.timeInMillis)

                // Показываем информацию о напоминании
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                binding.alarmButton.tooltipText = "Напоминание: ${dateFormat.format(
                    Date(
                        selectedDateTime.timeInMillis
                    )
                )}"
                Toast.makeText(requireContext(), "Напоминание установлено", Toast.LENGTH_SHORT).show()

            }, startHour, startMinute, true).show()
        }, startYear, startMonth, startDay).show()
    }

    companion object {

        private const val PERMISSION_REQUEST_CODE = 123
        fun newInstance(noteId: Long = -1) = // Измените на Long
            NoteDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong("note_id", noteId) // Используйте putLong
                }
            }
    }
}