package com.example.kian_hosseinkhani_myruns2.ActivitiesAndFragments.details

import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.kian_hosseinkhani_myruns2.R
import com.example.kian_hosseinkhani_myruns2.model.ExerciseDatabase
import com.example.kian_hosseinkhani_myruns2.model.ExerciseDatabaseDao
import com.example.kian_hosseinkhani_myruns2.reprository.ExerciseRepository
import com.example.kian_hosseinkhani_myruns2.viewModel.ExerciseViewModel
import com.example.kian_hosseinkhani_myruns2.viewModel.ExerciseViewModelFactory

class DetailsActivityManualEntry : AppCompatActivity() {
    // For the database
    private lateinit var database: ExerciseDatabase
    private lateinit var databaseDao: ExerciseDatabaseDao
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var exerciseViewModel: ExerciseViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val detailsListView: ListView = findViewById(R.id.detailsListView)
        val bundleList = intent.getParcelableArrayListExtra<Bundle>("selectedEntry")
        val details: List<Pair<String, String>> = bundleList?.mapNotNull {
            val key = it.getString("key", "")
            val value = it.getString("value", "")
            if (key.isNotBlank() && value.isNotBlank()) Pair(key, value) else null
        } ?: emptyList()

        // Initialize the database components
        database = ExerciseDatabase.getInstance(this)
        databaseDao = database.exerciseDao
        repository = ExerciseRepository(databaseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        exerciseViewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseViewModel::class.java)

        val adapter = DetailsListAdapterManualEntry(this, details)
        detailsListView.adapter = adapter

        val selectedEntryId = intent.getLongExtra("selectedEntryId", -1) // or getIntExtra if it's an Int
        println(selectedEntryId)
        val deleteButton: Button = findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener {
            exerciseViewModel.getEntryById(selectedEntryId).observe(this, Observer { entry ->
                // This code will be executed once the entry data is available
                entry?.let {
                    // Now you have your ExerciseEntry object, and you can delete it
                    exerciseViewModel.deleteEntry(it)
                }
            })
            finish()
        }
    }
}
