package com.example.notepad.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import java.util.Calendar

class DateTimePickerDialogHelper(
    private val context: Context,
    private val onDateTimeSelected: (Long) -> Unit
) {

    fun show() {
        val currentDateTime = Calendar.getInstance()

        // Сначала выбираем дату
        DatePickerDialog(
            context, { _, year, month, day ->
                // Затем выбираем время
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val selectedDateTime = Calendar.getInstance().apply {
                            set(year, month, day, hour, minute, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        // Проверяем, что выбранное время в будущем
                        if (selectedDateTime.timeInMillis > System.currentTimeMillis()) {
                            onDateTimeSelected(selectedDateTime.timeInMillis)
                        } else {
                            Toast.makeText(context, "Выберите время в будущем", Toast.LENGTH_SHORT)
                                .show()
                        }

                    },
                    currentDateTime.get(Calendar.HOUR_OF_DAY),
                    currentDateTime.get(Calendar.MINUTE),
                    true
                ).show()

            }, currentDateTime.get(Calendar.YEAR), currentDateTime.get(Calendar.MONTH),
            currentDateTime.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showWithMinDate(minDate: Long) {
        val currentDateTime = Calendar.getInstance()
        val minCalendar = Calendar.getInstance().apply { timeInMillis = minDate }

        val datePickerDialog = DatePickerDialog(
            context, { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val selectedDateTime = Calendar.getInstance().apply {
                            set(year, month, day, hour, minute, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        if (selectedDateTime.timeInMillis > System.currentTimeMillis()) {
                            onDateTimeSelected(selectedDateTime.timeInMillis)
                        } else {
                            Toast.makeText(context, "Выберите время в будущем", Toast.LENGTH_SHORT)
                                .show()
                        }

                    },
                    currentDateTime.get(Calendar.HOUR_OF_DAY),
                    currentDateTime.get(Calendar.MINUTE),
                    true
                ).show()

            }, currentDateTime.get(Calendar.YEAR), currentDateTime.get(Calendar.MONTH),
            currentDateTime.get(Calendar.DAY_OF_MONTH)
        )

        // Устанавливаем минимальную дату (например, текущую)
        datePickerDialog.datePicker.minDate = minDate
        datePickerDialog.show()
    }
}