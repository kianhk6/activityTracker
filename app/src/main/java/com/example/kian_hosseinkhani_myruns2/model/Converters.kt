package com.example.kian_hosseinkhani_myruns2.model

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.Calendar

class Converters {

    @TypeConverter
    fun fromLatLngList(value: ArrayList<LatLng>?): ByteArray? {
        if (value == null) return null
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeInt(value.size)
        for (latLng in value) {
            objectOutputStream.writeDouble(latLng.latitude)
            objectOutputStream.writeDouble(latLng.longitude)
        }
        objectOutputStream.close()
        return byteArrayOutputStream.toByteArray()
    }

    @TypeConverter
    fun toLatLngList(value: ByteArray?): ArrayList<LatLng>? {
        if (value == null) return null
        val byteArrayInputStream = ByteArrayInputStream(value)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        val size = objectInputStream.readInt()
        val latLngList = ArrayList<LatLng>(size)
        repeat(size) {
            val latitude = objectInputStream.readDouble()
            val longitude = objectInputStream.readDouble()
            latLngList.add(LatLng(latitude, longitude))
        }
        objectInputStream.close()
        return latLngList
    }

    @TypeConverter
    fun calendarToTimestamp(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }

    @TypeConverter
    fun timestampToCalendar(value: Long?): Calendar? {
        return value?.let {
            Calendar.getInstance().apply { timeInMillis = it }
        }
    }
}
