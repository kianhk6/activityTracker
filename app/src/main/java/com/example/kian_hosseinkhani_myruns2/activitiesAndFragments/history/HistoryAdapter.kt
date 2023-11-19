package com.example.kian_hosseinkhani_myruns2.activitiesAndFragments.history

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.example.kian_hosseinkhani_myruns2.R
import com.example.kian_hosseinkhani_myruns2.Util
import com.example.kian_hosseinkhani_myruns2.model.ExerciseEntry
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(private val context: Context, private var dataList: List<ExerciseEntry>) : BaseAdapter(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun getCount(): Int = dataList.size
    override fun getItem(position: Int): Any = dataList[position]
    override fun getItemId(position: Int): Long = position.toLong()

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_history, parent, false)
        val primaryText: TextView = view.findViewById(R.id.primaryText)
        val secondaryText: TextView = view.findViewById(R.id.secondaryText)

        val entry = dataList[position]
        val sdf = SimpleDateFormat("HH:mm:ss MMM dd yyyy", Locale.getDefault())

        val unitPreferenceValue = sharedPreferences.getString("unitPreference", "Kilometers") ?: "Kilometers"
        var distance = entry.distance

        if (unitPreferenceValue != entry.unit_preference) {
            distance = Util.convertDistance(distance, entry.unit_preference)
        }

        primaryText.text = "${Util.idToInputType(entry.inputType)}: " +
                "${Util.idToActivityType(entry.activityType)}, " +
                sdf.format(entry.dateTime!!.time)

        if(entry.inputType == 0){
            secondaryText.text = "$distance $unitPreferenceValue, ${entry.duration} minutes, 0 secs"
        }
        else{
            val (minutes, seconds) = calculateMinutesAndSeconds(entry.duration)

            var timeString = ""
            timeString = if(minutes == 0){
                "$seconds secs"
            } else{
                "$minutes minutes, $seconds secs"
            }
            secondaryText.text = "$distance $unitPreferenceValue, $timeString"
        }
        return view
    }

    private fun calculateMinutesAndSeconds(durationInSeconds: Double): Pair<Int, Int> {
        val minutes = (durationInSeconds / 60).toInt()
        val seconds = (durationInSeconds % 60).toInt()
        return Pair(minutes, seconds)
    }


    fun replace(newDataList: List<ExerciseEntry>) {
        dataList = newDataList
        notifyDataSetChanged()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "unitPreference") {
            notifyDataSetChanged()
        }
    }

    fun cleanup() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}
