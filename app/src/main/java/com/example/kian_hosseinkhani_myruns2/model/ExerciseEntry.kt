package com.example.kian_hosseinkhani_myruns2.model
import android.os.Parcelable
import androidx.preference.Preference
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
import java.util.*
@Entity(tableName = "exercise_table")
data class ExerciseEntry (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "inputType")
    var inputType: Int,

    @ColumnInfo(name = "activityType")
    var activityType: Int,

    @ColumnInfo(name = "dateTime")
    var dateTime: Calendar? = null,

    @ColumnInfo(name = "duration")
    var duration: Double,

    @ColumnInfo(name = "distance")
    var distance: Double,

    @ColumnInfo(name = "avg_pace")
    var avgPace: Double,

    @ColumnInfo(name = "avg_speed")
    var avgSpeed: Double,

    @ColumnInfo(name = "calorie")
    var calorie: Double,

    @ColumnInfo(name = "climb")
    var climb: Double,

    @ColumnInfo(name = "heart_rate")
    var heartRate: Double,

    @ColumnInfo(name = "comment")
    var comment: String,

    @ColumnInfo(name = "unit_preference")
    var unit_preference: String,

    @ColumnInfo(name = "location_list", typeAffinity = ColumnInfo.BLOB)
    var locationList: ArrayList<LatLng>? = null
)