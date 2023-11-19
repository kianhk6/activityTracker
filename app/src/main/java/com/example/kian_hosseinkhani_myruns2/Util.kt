package com.example.kian_hosseinkhani_myruns2

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
object Util {
    private val activityTypes = listOf("Running", "Walking", "Standing",  "Cycling", "Hiking",
        "Downhill Skiing", "Cross-Country Skiing", "Snowboarding", "Skating",
        "Swimming", "Mountain Biking", "Wheelchair", "Elliptical", "Unknown")
    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 0)
        }
    }

    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        var bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
        val matrix = Matrix()
//        matrix.setRotate(90f)
        var ret = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return ret
    }


    // Function to convert activity type to an integer ID
    fun activityTypeToId(activityType: String): Int {
        return activityTypes.indexOf(activityType)
    }

    // Function to convert an integer ID to its corresponding activity type
    fun idToActivityType(id: Int): String {
        if (id < 0 || id >= activityTypes.size) {
            return "Unknown"
        }
        return activityTypes[id]
    }

    fun idToInputType(inputType: Int): String {
        if(inputType == 0){
            return "Manual Entry"
        }
        else if(inputType == 1){
            return "GPS"
        }
        else{
            return "Automatic"
        }
    }

    fun milesToKilometers(miles: Double): Double {
        return miles * 1.60934
    }

    fun kilometersToMiles(kilometers: Double): Double {
        return kilometers * 0.621371
    }

    fun convertDistance(distance: Double, unit: String): Double {
        return when (unit) {
            "Miles" -> milesToKilometers(distance)
            "Kilometers" -> kilometersToMiles(distance)
            else -> throw IllegalArgumentException("Invalid unit. Expected 'Miles' or 'Kilometers'")
        }
    }

}
