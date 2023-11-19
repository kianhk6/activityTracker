package com.example.kian_hosseinkhani_myruns2.reprository


import androidx.lifecycle.LiveData
import com.example.kian_hosseinkhani_myruns2.model.ExerciseDatabaseDao
import com.example.kian_hosseinkhani_myruns2.model.ExerciseEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ExerciseRepository(private val exerciseDao: ExerciseDatabaseDao) {

    // Get all exercise entries from the database in a flow format.
    val allEntries: Flow<List<ExerciseEntry>> = exerciseDao.getAllEntries()

    // Insert a new exercise entry into the database.
    fun insert(entry: ExerciseEntry) {
        CoroutineScope(IO).launch {
            exerciseDao.insert(entry)
        }
    }

    // Retrieve an exercise entry from the database by its ID.
    fun getEntryById(entryId: Long): Flow<ExerciseEntry?> {
        return exerciseDao.getEntryById(entryId)
    }

    // Delete an exercise entry from the database.
    fun delete(entry: ExerciseEntry) {
        CoroutineScope(IO).launch {
            exerciseDao.deleteEntry(entry)
        }
    }

    // Delete all exercise entries from the database.
    fun deleteAll() {
        CoroutineScope(IO).launch {
            exerciseDao.deleteAll()
        }
    }
    fun getLastEntry(): LiveData<ExerciseEntry?> = exerciseDao.getLastEntry()



}
