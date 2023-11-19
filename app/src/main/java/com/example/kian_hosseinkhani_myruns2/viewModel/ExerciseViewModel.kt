package com.example.kian_hosseinkhani_myruns2.viewModel


import androidx.lifecycle.*
import com.example.kian_hosseinkhani_myruns2.model.ExerciseEntry
import com.example.kian_hosseinkhani_myruns2.reprository.ExerciseRepository
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class ExerciseViewModel(private val repository: ExerciseRepository) : ViewModel() {

    val allEntriesLiveData: LiveData<List<ExerciseEntry>> = repository.allEntries.asLiveData()

    fun insert(entry: ExerciseEntry) {
        viewModelScope.launch {  // Using viewModelScope to avoid creating a new scope. It automatically cancels the coroutine when the ViewModel is destroyed.
            repository.insert(entry)
        }
    }

    fun deleteEntry(entry: ExerciseEntry) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    fun getEntryById(entryId: Long): LiveData<ExerciseEntry?> {
        return repository.getEntryById(entryId).asLiveData()
    }

    fun getLastEntry(): LiveData<ExerciseEntry?> = repository.getLastEntry()

}

class ExerciseViewModelFactory(private val repository: ExerciseRepository) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            return ExerciseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
