package com.example.kian_hosseinkhani_myruns2.activitiesAndFragments.start

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.example.kian_hosseinkhani_myruns2.activitiesAndFragments.start.GPS.GpsActivity
import com.example.kian_hosseinkhani_myruns2.activitiesAndFragments.start.manualEntry.ManualEntryActivity
import com.example.kian_hosseinkhani_myruns2.R

//class StartFragment : Fragment() {
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_start, container, false)
//    }
//}
class StartFragment : Fragment() {

    private val inputTypes = listOf("Manual Entry", "GPS", "Automatic")
    private val activityTypes = listOf("Running", "Walking", "Standing",  "Cycling", "Hiking",
        "Downhill Skiing", "Cross-Country Skiing", "Snowboarding", "Skating",
        "Swimming", "Mountain Biking", "Wheelchair", "Elliptical", "Other")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    // The onViewCreated method is called just after onCreateView,
    // and the view returned by onCreateView is passed to onViewCreated.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val inputTypeSpinner = view.findViewById<Spinner>(R.id.inputTypeSpinner)
        val activityTypeSpinner = view.findViewById<Spinner>(R.id.activityTypeSpinner)
        val startButton = view.findViewById<Button>(R.id.startButton)

        val inputTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, inputTypes)
        inputTypeSpinner.adapter = inputTypeAdapter

        val activityTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, activityTypes)
        activityTypeSpinner.adapter = activityTypeAdapter

        startButton.setOnClickListener {
            val selectedInputType = inputTypeSpinner.selectedItem.toString()
            val selectedActivityType = activityTypeSpinner.selectedItem.toString()

            when (selectedInputType) {
                "Manual Entry" -> {
                    val intent = Intent(activity, ManualEntryActivity::class.java)
                    intent.putExtra("selectedActivityType", selectedActivityType)
                    startActivity(intent)
                }

                "GPS" -> {
                    val intent = Intent(activity, GpsActivity::class.java)
                    intent.putExtra("selectedActivityType", selectedActivityType)
                    intent.putExtra("selectedInputType", selectedInputType)
                    println(selectedActivityType)
                    startActivity(intent)
                }

                "Automatic" -> {
                    val intent = Intent(activity, GpsActivity::class.java)
                    intent.putExtra("selectedActivityType", selectedActivityType)
                    intent.putExtra("selectedInputType", selectedInputType)

                    startActivity(intent)
                }
            }
        }
    }
}