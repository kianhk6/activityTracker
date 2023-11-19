package com.example.kian_hosseinkhani_myruns2.activitiesAndFragments.start.manualEntry

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.example.kian_hosseinkhani_myruns2.R
import java.util.Calendar

class ManualEntryDialog : DialogFragment(),
    DialogInterface.OnClickListener,
    DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    interface ManualEntryListener {
        fun onEntryReceived(type: String, value: Any)
    }

    private var listener: ManualEntryListener? = null

    companion object {
        const val DIALOG_KEY = "dialog_type"
        private const val INPUT_TEXT_KEY = "input_text_key"
    }

    private var userInput: String? = null
    private var dialogType: String? = null
    private val calendar = Calendar.getInstance()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogType = arguments?.getString(DIALOG_KEY)

        when (dialogType) {
            "Date" -> {
                return DatePickerDialog(
                    requireContext(), // Use requireContext() in a fragment to get the context
                    this as DatePickerDialog.OnDateSetListener,  // cast to OnDateSetListener
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }

            "Time" -> {
                return TimePickerDialog(
                    requireContext(), // Use requireContext() in a fragment to get the context
                    this as TimePickerDialog.OnTimeSetListener, // cast to OnTimeSetListener
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true // 24hr format
                )
            }

            "Duration", "Distance", "Calories", "Heart Rate", "Comment" -> {
                val builder = AlertDialog.Builder(requireActivity())
                val input = EditText(context)
                input.id = R.id.dialog_input

                val title = when (dialogType) {
                    "Duration" -> "Duration"
                    "Distance" -> "Distance"
                    "Calories" -> "Calories"
                    "Heart Rate" -> "Heart Rate"
                    else -> "Comment"
                }

                if (dialogType == "Comment") {
                    input.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    input.hint = "How did it go? Notes here."
                } else {
                    input.inputType = InputType.TYPE_CLASS_NUMBER
                }

                // Restore the userInput value if it exists
                if (savedInstanceState != null) {
                    userInput = savedInstanceState.getString(INPUT_TEXT_KEY)
                    input.setText(userInput)
                }
                builder.setView(input)
                builder.setTitle(title)



                builder.setView(input)
                builder.setTitle(title)

                // Create the dialog but don't show it yet
                val alertDialog = builder.create()

                // Set the positive and negative buttons
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", this)
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", this)

                // Once the dialog is shown, we can fetch the positive button and modify its OnClickListener
                alertDialog.setOnShowListener {
                    val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    positiveButton.setOnClickListener {
                        userInput = input.text.toString()
                        onClick(
                            alertDialog,
                            DialogInterface.BUTTON_POSITIVE
                        ) // Manually call onClick
                        alertDialog.dismiss()  // Close the modal
                    }
                }

                return alertDialog
            }

            else -> throw IllegalArgumentException("Unknown dialog type: $dialogType")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as ManualEntryListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement ManualEntryListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INPUT_TEXT_KEY, userInput)
    }


    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val calendarDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthOfYear)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        listener?.onEntryReceived("Date", calendarDate)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        val calendarTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }
        listener?.onEntryReceived("Time", calendarTime)
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                when (dialogType) {
                    "Duration" -> {
                        listener?.onEntryReceived("Duration", userInput ?: "")
                    }

                    "Distance" -> {
                        listener?.onEntryReceived("Distance", userInput ?: "")
                    }

                    "Calories" -> {
                        listener?.onEntryReceived("Calories", userInput ?: "")
                    }

                    "Heart Rate" -> {
                        listener?.onEntryReceived("Heart Rate", userInput ?: "")
                    }

                    "Comment" -> {
                        listener?.onEntryReceived("Comment", userInput ?: "")
                    }
                }
            }

            DialogInterface.BUTTON_NEGATIVE -> {
                println("User cancelled")
            }
        }
    }
}
