package com.example.kian_hosseinkhani_myruns2.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDatabaseDao {

    @Insert
    suspend fun insert(entry: ExerciseEntry): Long

    // Get all exercise entries in a flow. This means every time the data changes,
    // any active collectors of this flow will get the updated list.
    @Query("SELECT * FROM exercise_table ORDER BY dateTime DESC")
    fun getAllEntries(): Flow<List<ExerciseEntry>>

    @Query("SELECT * FROM exercise_table WHERE id = :entryId")
    fun getEntryById(entryId: Long): Flow<ExerciseEntry?>

    @Delete
    suspend fun deleteEntry(entry: ExerciseEntry)

    @Query("DELETE FROM exercise_table")
    suspend fun deleteAll()

    // for debugging purposes
    @Query("SELECT * FROM exercise_table ORDER BY id DESC LIMIT 1")
    fun getLastEntry(): LiveData<ExerciseEntry?>
}
