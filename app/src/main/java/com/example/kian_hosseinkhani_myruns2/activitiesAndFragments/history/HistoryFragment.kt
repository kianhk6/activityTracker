package com.example.kian_hosseinkhani_myruns2.activitiesAndFragments.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.kian_hosseinkhani_myruns2.activitiesAndFragments.details.DetailsActivityManualEntry
import com.example.kian_hosseinkhani_myruns2.activitiesAndFragments.details.DetailsActivityMap
import com.example.kian_hosseinkhani_myruns2.R
import com.example.kian_hosseinkhani_myruns2.Util
import com.example.kian_hosseinkhani_myruns2.model.ExerciseDatabase
import com.example.kian_hosseinkhani_myruns2.model.ExerciseEntry
import com.example.kian_hosseinkhani_myruns2.reprository.ExerciseRepository
import com.example.kian_hosseinkhani_myruns2.viewModel.ExerciseViewModel
import com.example.kian_hosseinkhani_myruns2.viewModel.ExerciseViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {

    private lateinit var arrayAdapter: HistoryAdapter
    private lateinit var historyListView: ListView


    private lateinit var exerciseEntryViewModel: ExerciseViewModel
    private val unitsInString: Triple<String, String, String>
        get() {

            val smallUnit = ""
            val largeUnitPerHour = ""
            val largeUnit = ""
            return Triple(smallUnit, largeUnitPerHour, largeUnit)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        historyListView = view.findViewById(R.id.historyListView)

        // Initialize the database components
        val database = ExerciseDatabase.getInstance(requireActivity())
        val databaseDao = database.exerciseDao
        val repository = ExerciseRepository(databaseDao)
        val viewModelFactory = ExerciseViewModelFactory(repository)
        val exerciseViewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseViewModel::class.java)

        val arrayList = ArrayList<ExerciseEntry>()

        arrayAdapter = HistoryAdapter(requireActivity(), arrayList)
        historyListView.adapter = arrayAdapter

        exerciseViewModel.allEntriesLiveData.observe(requireActivity(), Observer { entries ->
            arrayAdapter.replace(entries)
            arrayAdapter.notifyDataSetChanged()
        })

        historyListView.setOnItemClickListener { _, _, position, _ ->
            val selectedEntry = arrayAdapter.getItem(position) as ExerciseEntry

            if(selectedEntry.inputType == 0){
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                val unitPreferenceValue = sharedPreferences.getString("unitPreference", "Kilometers")

                var distance = selectedEntry.distance

                if(unitPreferenceValue != selectedEntry.unit_preference){
                    distance = Util.convertDistance(distance, selectedEntry.unit_preference)
                }
                val details: List<Pair<String, String>> = listOfNotNull(
                    Pair("Input Type", Util.idToInputType(selectedEntry.inputType)),
                    Pair("Activity Type", Util.idToActivityType(selectedEntry.activityType)),
                    Pair("Date and Time", SimpleDateFormat("HH:mm:ss MMM dd yyyy", Locale.getDefault()).format(
                        selectedEntry.dateTime?.time ?: Date())),
                    Pair("Duration", "${selectedEntry.duration} mins"),
                    Pair("Distance", "$distance $unitPreferenceValue"),
                    Pair("Calories", "${selectedEntry.calorie} cals"),
                    Pair("Heart Rate", "${selectedEntry.heartRate} bpm"),
                    Pair("Comments", selectedEntry.comment )
                )

                val bundleList = ArrayList<Bundle>()
                details.forEach {
                    val bundle = Bundle()
                    bundle.putString("key", it.first)
                    bundle.putString("value", it.second)
                    bundleList.add(bundle)
                }

                val intent = Intent(requireContext(), DetailsActivityManualEntry::class.java)
                intent.putParcelableArrayListExtra("selectedEntry", bundleList)
                intent.putExtra("selectedEntryId", selectedEntry.id)

                startActivity(intent)
            }
            else{


                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                val unitPreferenceValue = sharedPreferences.getString("unitPreference", "Kilometers")

                var distance = selectedEntry.distance

                if(unitPreferenceValue != selectedEntry.unit_preference){
                    distance = Util.convertDistance(distance, selectedEntry.unit_preference)
                }
                val (minutes, seconds) = calculateMinutesAndSeconds(selectedEntry.duration)
                var timeString = ""
                timeString = if(minutes == 0){
                    "$seconds secs"
                } else{
                    "$minutes minutes, $seconds secs"
                }
                var (smallUnit, largeUnitPerHour, largeUnit) = unitsInString
                val triple = setUnitStrings(unitPreferenceValue, smallUnit, largeUnitPerHour, largeUnit)
                largeUnit = triple.first
                largeUnitPerHour = triple.second
                smallUnit = triple.third


                val details: List<Pair<String, String>> = listOfNotNull(
                    Pair("Input Type", Util.idToInputType(selectedEntry.inputType)),
                    Pair("Activity Type", Util.idToActivityType(selectedEntry.activityType)),
                    Pair("Date and Time", SimpleDateFormat("HH:mm:ss MMM dd yyyy", Locale.getDefault()).format(
                        selectedEntry.dateTime?.time ?: Date())),
                    Pair("Duration", "$timeString"),
                    Pair("Distance", "$distance $largeUnit"),
                    Pair("Calories", "${selectedEntry.calorie} cals"),
                    Pair("Average speed",  "${selectedEntry.avgSpeed} $largeUnitPerHour"),
                    Pair("Climb",  "${selectedEntry.climb} $smallUnit"),
                    Pair("Heart Rate", "${selectedEntry.heartRate} bpm"),
                    Pair("Comments", selectedEntry.comment )
                )

                val bundleList = ArrayList<Bundle>()
                details.forEach {
                    val bundle = Bundle()
                    bundle.putString("key", it.first)
                    bundle.putString("value", it.second)
                    bundleList.add(bundle)
                }

                val intent = Intent(requireContext(), DetailsActivityMap::class.java)
                intent.putParcelableArrayListExtra("selectedEntry", bundleList)
                intent.putParcelableArrayListExtra("locationListKey", selectedEntry.locationList)
                intent.putExtra("selectedEntryId", selectedEntry.id)
                startActivity(intent)
            }

        }

        return view
    }
    private fun setUnitStrings(
        unitPreferenceValue: String?,
        smallUnit: String,
        largeUnitPerHour: String,
        largeUnit: String
    ): Triple<String, String, String> {
        var smallUnit1 = smallUnit
        var largeUnitPerHour1 = largeUnitPerHour
        var largeUnit1 = largeUnit
        if (unitPreferenceValue == "Miles") {
            smallUnit1 = "Feet"
            largeUnitPerHour1 = "m/h"
            largeUnit1 = "Miles"
        } else if (unitPreferenceValue == "Kilometers") {
            smallUnit1 = "Meters"
            largeUnitPerHour1 = "km/h"
            largeUnit1 = "Kilometers"

        }
        return Triple(largeUnit1, largeUnitPerHour1, smallUnit1)
    }

    private fun calculateMinutesAndSeconds(durationInSeconds: Double): Pair<Int, Int> {
        val minutes = (durationInSeconds / 60).toInt()
        val seconds = (durationInSeconds % 60).toInt()
        return Pair(minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        arrayAdapter.cleanup()
    }

}