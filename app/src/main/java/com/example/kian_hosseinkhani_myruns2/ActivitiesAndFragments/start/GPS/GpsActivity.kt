package com.example.kian_hosseinkhani_myruns2.ActivitiesAndFragments.start.GPS

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.kian_hosseinkhani_myruns2.R
import com.example.kian_hosseinkhani_myruns2.Util
import com.example.kian_hosseinkhani_myruns2.model.ExerciseDatabase
import com.example.kian_hosseinkhani_myruns2.model.ExerciseDatabaseDao
import com.example.kian_hosseinkhani_myruns2.model.ExerciseEntry
import com.example.kian_hosseinkhani_myruns2.reprository.ExerciseRepository
import com.example.kian_hosseinkhani_myruns2.services.TrackingService
import com.example.kian_hosseinkhani_myruns2.viewModel.ExerciseViewModel
import com.example.kian_hosseinkhani_myruns2.viewModel.ExerciseViewModelFactory
import com.example.kian_hosseinkhani_myruns2.viewModel.TrackingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions


class GpsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap


    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var intValueLabel: TextView

    private lateinit var appContext: Context
    private var isBind = false

    // 2. wiring the view model and using its massage handler
    private lateinit var trackingViewModel: TrackingViewModel
    private val BIND_STATUS_KEY = "bind_status_key"
    private lateinit var backPressedCallback: OnBackPressedCallback

    // global variable intent: as this is used many time accross functions
    private lateinit var service_intent: Intent
    private val PERMISSION_REQUEST_CODE = 0


    // specify locations of the markers we want to drop (initiated onMapReady,
    // used in onLocationChanged)
    private lateinit var markerOptions: MarkerOptions


    // initialized in onMapReady
    private lateinit var polylineOptions: PolylineOptions

    // array list of poly lines getting gilles in onMapLongClickListener
    private lateinit var polylines: ArrayList<Polyline>

    private var startMarker: Marker? = null
    private var currentMarker: Marker? = null

    private val unitsInString: Triple<String, String, String>
        get() {

            val smallUnit = ""
            val largeUnitPerHour = ""
            val largeUnit = ""
            return Triple(smallUnit, largeUnitPerHour, largeUnit)
        }

    private var latestExerciseEntry: ExerciseEntry? = null

    // For the database
    private lateinit var database: ExerciseDatabase
    private lateinit var databaseDao: ExerciseDatabaseDao
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var exerciseViewModel: ExerciseViewModel
    private lateinit var selectedActivityType: String
    private lateinit var selectedInputType: String
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps) // Set the content view to your activity's layout

        appContext = this.applicationContext

        // Initialize UI components
        saveButton = findViewById(R.id.saveButton)
        intValueLabel = findViewById(R.id.exerciseStatus)
        cancelButton = findViewById(R.id.cancelButton)

        // Initialize ViewModel
        trackingViewModel = ViewModelProvider(this).get(TrackingViewModel::class.java)

        // Initialize the database components
        database = ExerciseDatabase.getInstance(this)
        databaseDao = database.exerciseDao
        repository = ExerciseRepository(databaseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        exerciseViewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseViewModel::class.java)



        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val unitPreferenceValue = sharedPreferences.getString("unitPreference", "Miles")

        var (smallUnit, largeUnitPerHour, largeUnit) = unitsInString
        val triple = setUnitStrings(unitPreferenceValue, smallUnit, largeUnitPerHour, largeUnit)
        largeUnit = triple.first
        largeUnitPerHour = triple.second
        smallUnit = triple.third
        selectedInputType = intent.getStringExtra("selectedInputType").toString()

        checkPermission()

        trackingViewModel.exerciseEntry.observe(this, Observer { it ->
            if (!it.locationList.isNullOrEmpty()) {
                onLocationChanged(latLngToLocation(it.locationList!!.last()))
            }


            val (minutes, seconds) = calculateMinutesAndSeconds(it.duration)
            var activityType = Util.idToActivityType(it.activityType)
            if(selectedInputType != "GPS"){
                println(selectedInputType)
                activityType = "Unknown"
            }
            var timeString = ""
            timeString = if(minutes == 0){
                "$seconds secs"
            } else{
                "$minutes minutes, $seconds secs"
            }
            val displayText = """
            Type: ${activityType}
            Avg Speed: ${String.format("%.2f", it.avgSpeed)} $largeUnitPerHour
            Cur Speed: ${String.format("%.2f", it.avgPace)} $largeUnitPerHour
            Climb: ${String.format("%.2f", it.climb)} $smallUnit
            Calorie: ${it.calorie.toInt()} 
            $timeString
            Distance: ${String.format("%.2f", it.distance)} $largeUnit
            """.trimIndent()

            intValueLabel.text = displayText
            latestExerciseEntry = it
        })


        // Restore state
        if (savedInstanceState != null) {
            isBind = savedInstanceState.getBoolean(BIND_STATUS_KEY)
            println(isBind)
        }

        // Set up button click listeners
        setupButtonListeners()

        // Handle back press
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("debug: back button pressed")
                unBindService()
                stopService(intent)
                isEnabled = false
                finish()
            }
        }
        this.onBackPressedDispatcher.addCallback(this, backPressedCallback)


        // getting a reference of the map fragment in activity_main.xml
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment


        // this function initializes google map and takes onMapReady call back
        // that is why we call this
        mapFragment.getMapAsync(this)

    }
    private fun calculateMinutesAndSeconds(durationInSeconds: Double): Pair<Int, Int> {
        val minutes = (durationInSeconds / 60).toInt()
        val seconds = (durationInSeconds % 60).toInt()
        return Pair(minutes, seconds)
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

    private fun latLngToLocation(latLng: LatLng): Location {
        val location = Location("") // Provider is empty or could be set to a relevant value
        location.latitude = latLng.latitude
        location.longitude = latLng.longitude
        return location
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)

        // Initial setup for the start marker and camera zoom on the first location update
        if (startMarker == null) {
            val startMarkerOptions = MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title("Start Position")
            startMarker = mMap.addMarker(startMarkerOptions)

            // Move and zoom the camera to the start position
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f) // Zoom level 17
            mMap.moveCamera(cameraUpdate)
        }

        // Update the current marker's position or create it if it doesn't exist
        if (currentMarker == null) {
            val currentMarkerOptions = MarkerOptions()
                .position(latLng)
                .icon(bitmapDescriptorFromVector(this, R.drawable.circle_marker))
            currentMarker = mMap.addMarker(currentMarkerOptions)
        } else {
            // Smoothly animate the current marker to the new position
            currentMarker?.position = latLng
        }

        // Add the new location to the polyline and draw it on the map
        polylineOptions.add(latLng)
        mMap.addPolyline(polylineOptions)

        // Smoothly animate the camera to follow the current marker
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, mMap.cameraPosition.zoom)
        mMap.animateCamera(cameraUpdate, 1000, null) // Animate over 1000 milliseconds
    }

    private fun setupButtonListeners() {
        saveButton.setOnClickListener { onClick(it) }
        cancelButton.setOnClickListener { onClick(it) }
    }


    override fun onDestroy() {
        super.onDestroy()
        backPressedCallback.remove()
    }

    // single function to handle all onClick listeners
    fun onClick(view: View) {
        if (view == saveButton) {
            latestExerciseEntry?.let { entry ->
                if(selectedInputType != "GPS"){
                    entry.activityType = -1
                    entry.inputType = 2
                }
                exerciseViewModel.insert(entry)
            }
            unBindService()
            this.stopService(service_intent)
            finish()
        } else if (view == cancelButton) {
            unBindService()
            // even when u use new activity to stop service since ur passing the intent
            // still works
            this.stopService(service_intent)
            finish()
        }
    }

    // you must make sure to not bind services when its already bound
    private fun bindService() {
        // check if service is already bind (global flag)
        if (!isBind) {
            appContext.bindService(service_intent, trackingViewModel, Context.BIND_AUTO_CREATE)
            isBind = true
            println(isBind)

        }
    }

    private fun unBindService() {
        // check if its already bound
        if (isBind) {
            appContext.unbindService(trackingViewModel)
            isBind = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BIND_STATUS_KEY, isBind)
    }

    // ask user permission to get gps data
    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        else {
            service_intent = Intent(this, TrackingService::class.java)
            val selectedActivityType = intent.getStringExtra("selectedActivityType").toString()
            service_intent.putExtra("selectedActivityType", selectedActivityType)
            service_intent.putExtra("selectedInputType", selectedInputType)
            this.startService(service_intent)
            bindService()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                service_intent = Intent(this, TrackingService::class.java)
                val selectedActivityType = intent.getStringExtra("selectedActivityType").toString()
                service_intent.putExtra("selectedActivityType", selectedActivityType)
                service_intent.putExtra("selectedInputType", selectedInputType)
                this.startService(service_intent)
                bindService()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this, R.raw.map_style_night
            )
        )

        polylineOptions = PolylineOptions()
            .color(ContextCompat.getColor(applicationContext, R.color.colorAccent))
            .width(10f)

        polylines = ArrayList()

        markerOptions = MarkerOptions()
    }
}