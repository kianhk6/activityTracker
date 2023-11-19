package com.example.kian_hosseinkhani_myruns2.activitiesAndFragments.details

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.kian_hosseinkhani_myruns2.R
import com.example.kian_hosseinkhani_myruns2.model.ExerciseDatabase
import com.example.kian_hosseinkhani_myruns2.model.ExerciseDatabaseDao
import com.example.kian_hosseinkhani_myruns2.reprository.ExerciseRepository
import com.example.kian_hosseinkhani_myruns2.viewModel.ExerciseViewModel
import com.example.kian_hosseinkhani_myruns2.viewModel.ExerciseViewModelFactory
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
import java.util.Locale

class DetailsActivityMap : AppCompatActivity(), OnMapReadyCallback {
    // For the database
    private lateinit var database: ExerciseDatabase
    private lateinit var databaseDao: ExerciseDatabaseDao
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var exerciseViewModel: ExerciseViewModel
    private lateinit var locationList: ArrayList<LatLng>

    // For the map
    // specify locations of the markers we want to drop (initiated onMapReady,
    // used in onLocationChanged)
    private lateinit var markerOptions: MarkerOptions
    // initialized in onMapReady
    private lateinit var polylineOptions: PolylineOptions
    // array list of poly lines getting gilles in onMapLongClickListener
    private lateinit var polylines: ArrayList<Polyline>
    private var startMarker: Marker? = null
    private var currentMarker: Marker? = null
    private lateinit var mMap: GoogleMap

    private lateinit var deleteButton: Button
    private lateinit var details: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_detail)
        locationList = intent.getParcelableArrayListExtra("locationListKey")!!
        val bundleList = intent.getParcelableArrayListExtra<Bundle>("selectedEntry")
        val details: List<Pair<String, String>> = bundleList?.mapNotNull {
            val key = it.getString("key", "")
            val value = it.getString("value", "")
            if (key.isNotBlank() && value.isNotBlank()) Pair(key, value) else null
        } ?: emptyList()
        this.details = findViewById(R.id.exerciseStatus)


        val displayText = StringBuilder()


        details.forEach { (key, value) ->
            val label = when (key) {
                "activityType" -> "Type"
                "avgSpeed" -> "Avg Speed"
                "curSpeed" -> "Cur Speed"
                "climb" -> "Climb"
                "calorie" -> "Calorie"
                "time" -> "Time" // assuming 'time' is a separate key
                "distance" -> "Distance"
                else -> key.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } // Default label for unrecognized keys
            }
            displayText.append("$label: $value\n")
        }
        this.details.text = displayText.toString().trimIndent()



        // getting a reference of the map fragment in activity_main.xml
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment


        // this function initializes google map and takes onMapReady call back
        // that is why we call this
        mapFragment.getMapAsync(this)



        initializeDatabase()
        val selectedEntryId = intent.getLongExtra("selectedEntryId", -1) // or getIntExtra if it's an Int
        deleteButton = findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener {
            exerciseViewModel.getEntryById(selectedEntryId).observe(this, Observer { entry ->
                // This code will be executed once the entry data is available
                entry?.let {
                    // Now you have your ExerciseEntry object, and you can delete it
                    exerciseViewModel.deleteEntry(it)
                }
            })
            finish()
        }
    }

    private fun initializeDatabase() {
        // Initialize the database components
        database = ExerciseDatabase.getInstance(this)
        databaseDao = database.exerciseDao
        repository = ExerciseRepository(databaseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        exerciseViewModel =
            ViewModelProvider(this, viewModelFactory).get(ExerciseViewModel::class.java)
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set map style
        mMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this, R.raw.map_style_night
            )
        )

        // Initialize PolylineOptions
        polylineOptions = PolylineOptions()
            .color(ContextCompat.getColor(applicationContext, R.color.colorAccent))
            .width(10f)

        // Initialize start and current markers
        var startMarker: Marker? = null
        var currentMarker: Marker? = null

        // Check if locationList has at least one element
        if (locationList.isNotEmpty()) {
            // Set the initial marker
            val startLatLng = locationList.first()
            val startMarkerOptions = MarkerOptions().position(startLatLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title("Start Position")
            startMarker = mMap.addMarker(startMarkerOptions)

            // Move and zoom the camera to the start position
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(startLatLng, 15f) // Adjust zoom level as needed
            mMap.moveCamera(cameraUpdate)
        }

        // Add all LatLng points to the polyline
        locationList.forEach { latLng ->
            polylineOptions.add(latLng)
        }

        // Add polyline to the map
        mMap.addPolyline(polylineOptions)

        // Set the current marker
        if (locationList.size > 1) {
            val currentLatLng = locationList.last()
            val currentMarkerOptions = MarkerOptions()
                .position(currentLatLng)
                .icon(bitmapDescriptorFromVector(this, R.drawable.circle_marker)) // Make sure to have this drawable
            currentMarker = mMap.addMarker(currentMarkerOptions)
        }
    }

}
