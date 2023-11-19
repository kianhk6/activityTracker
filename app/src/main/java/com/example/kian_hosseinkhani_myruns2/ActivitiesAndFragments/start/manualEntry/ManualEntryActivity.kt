package com.example.kian_hosseinkhani_myruns2.ActivitiesAndFragments.start.manualEntry

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.kian_hosseinkhani_myruns2.R
import com.example.kian_hosseinkhani_myruns2.Util
import com.example.kian_hosseinkhani_myruns2.model.ExerciseDatabase
import com.example.kian_hosseinkhani_myruns2.model.ExerciseDatabaseDao
import com.example.kian_hosseinkhani_myruns2.model.ExerciseEntry
import com.example.kian_hosseinkhani_myruns2.reprository.ExerciseRepository
import com.example.kian_hosseinkhani_myruns2.viewModel.ExerciseViewModel
import com.example.kian_hosseinkhani_myruns2.viewModel.ExerciseViewModelFactory
import java.util.Calendar

class ManualEntryActivity : AppCompatActivity(), ManualEntryDialog.ManualEntryListener {
    private var dateTime: Calendar? = null
    private var duration: Double? = null
    private var distance: Double? = null
    private var calorie: Double? = null
    private var heartRate: Double? = null
    private var comment: String? = null
    private var tempDate: Calendar? = null
    private var tempTime: Calendar? = null

    private val listItems =
        listOf("Date", "Time", "Duration", "Distance", "Calories", "Heart Rate", "Comment")
    private val calendar = Calendar.getInstance()

    // For the database
    private lateinit var database: ExerciseDatabase
    private lateinit var databaseDao: ExerciseDatabaseDao
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var exerciseViewModel: ExerciseViewModel
    private lateinit var selectedActivityType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry) // make sure the layout file is correctly named
        selectedActivityType = intent.getStringExtra("selectedActivityType").toString()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Manual Entry"

        val listView: ListView = findViewById(R.id.listView)
        val saveButton: Button = findViewById(R.id.saveButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)
        // the selected Activity
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val myDialog = ManualEntryDialog()
            val bundle = Bundle()

            // setting the dialog type
            bundle.putString(ManualEntryDialog.DIALOG_KEY, listItems[position])

            myDialog.arguments = bundle
            myDialog.show(supportFragmentManager, "manual_entry_dialog")
        }

        // Initialize the database components
        database = ExerciseDatabase.getInstance(this)
        databaseDao = database.exerciseDao
        repository = ExerciseRepository(databaseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        exerciseViewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseViewModel::class.java)

        saveButton.setOnClickListener {
            if (dateTime == null || duration == 0.0 || distance == 0.0 ||
                calorie == 0.0 || heartRate == 0.0) {

                // Using AlertDialog to show the error message
                AlertDialog.Builder(this).apply {
                    setTitle("Missing Information")
                    setMessage("Please fill out all the required fields before saving.")
                    setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    show()
                }
            }
            else{
                // put a check for everything being filled out
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                val unitPreferenceValue = sharedPreferences.getString("unitPreference", "Kilometers")  // "metric" is the default value if not set

                val entry = ExerciseEntry(
                    // assuming you have an id generation mechanism in your DAO or entity
                    dateTime = checkDateTime(dateTime),
                    duration = duration ?: 0.0,
                    distance = distance ?: 0.0,
                    calorie = calorie ?: 0.0,
                    heartRate = heartRate ?: 0.0,
                    comment = comment ?: "",
                    avgPace = 0.0,
                    avgSpeed = 0.0,
                    climb = 0.0,
                    locationList = null,
                    activityType = Util.activityTypeToId(selectedActivityType),
                    inputType = 0,
                    unit_preference = unitPreferenceValue.toString()
                )
                exerciseViewModel.insert(entry)

                Toast.makeText(this, "Activity Added!", Toast.LENGTH_SHORT).show()
                finish()
            }

        }

        cancelButton.setOnClickListener {
            // Show a toast message
            Toast.makeText(this, "Entry discarded.", Toast.LENGTH_SHORT).show()

            // Finish ManualEntryActivity
            finish()
        }
    }

    private fun checkDateTime(dateTime: Calendar?): Calendar? = if(dateTime is Calendar){
        dateTime
    } else{
        Calendar.getInstance()
    }


    override fun onEntryReceived(type: String, value: Any) {
        when (type) {
            "Date" -> {
                tempDate = value as? Calendar
                combineDateAndTime()
            }
            "Time" -> {
                tempTime = value as? Calendar
                combineDateAndTime()
            }
            "Duration" -> duration = (value as? String)?.toDoubleOrNull()
            "Distance" -> distance = (value as? String)?.toDoubleOrNull()
            "Calories" -> calorie = (value as? String)?.toDoubleOrNull()
            "Heart Rate" -> heartRate = (value as? String)?.toDoubleOrNull()
            "Comment" -> comment = value as? String
        }
    }

    private fun combineDateAndTime() {
        if (tempDate != null && tempTime != null) {
            dateTime = Calendar.getInstance().apply {
                timeInMillis = tempDate!!.timeInMillis
                set(Calendar.HOUR_OF_DAY, tempTime!!.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, tempTime!!.get(Calendar.MINUTE))
            }
            // Save dateTime to the database here.

            tempDate = null
            tempTime = null
        }
    }
}