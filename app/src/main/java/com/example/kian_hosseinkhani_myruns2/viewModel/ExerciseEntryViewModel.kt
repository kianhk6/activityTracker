package com.example.kian_hosseinkhani_myruns2.viewModel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kian_hosseinkhani_myruns2.model.ExerciseEntry
import com.google.android.gms.maps.model.LatLng

class ExerciseEntryViewModel : ViewModel() {
    val currentExerciseEntry = MutableLiveData<ExerciseEntry>()

    fun updateExerciseEntry(updatedEntry: ExerciseEntry) {
        currentExerciseEntry.value = updatedEntry
    }

    // Additional methods to update specific fields of ExerciseEntry if needed
    fun updateLocationList(newLocationList: ArrayList<LatLng>) {
        currentExerciseEntry.value?.let {
            it.locationList = newLocationList
            currentExerciseEntry.value = it // Trigger LiveData update
        }
    }

    // ... Other update methods for different fields
}
