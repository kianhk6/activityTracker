package com.example.kian_hosseinkhani_myruns2.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.example.kian_hosseinkhani_myruns2.MainActivity
import com.example.kian_hosseinkhani_myruns2.R
import com.example.kian_hosseinkhani_myruns2.Util
import com.example.kian_hosseinkhani_myruns2.model.ExerciseEntry
import com.google.android.gms.maps.model.LatLng
import java.util.Calendar
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import java.text.DecimalFormat
import java.util.concurrent.ArrayBlockingQueue
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instance
import weka.core.Instances

class TrackingService : Service(), LocationListener, SensorEventListener {
    private var numOfZeros: Int = 0
    private var prevPace: Double = 0.0

    private var unitPreferenceValue: String? = null

    private lateinit var notificationManager: NotificationManager
    private val NOTIFICATION_ID = 777
    private val CHANNEL_ID = "notification channel"

    // this a variable we are returning at the end
    private lateinit var myBinder: MyBinder

    // 3. using handler to send a message from counter service to view model [in timerTask we are
    // sending the message]
    // upon binding the newly created handler object gets assigned
    private var msgHandler: Handler? = null

    companion object {
        val EXERCISE_UPDATE = 1
    }

    private lateinit var exerciseEntry: ExerciseEntry
    private lateinit var locationManager: LocationManager
//    private var selectedActivityType = ""
    private var selectedInputType = ""
    private var previousAltitude = Double.MIN_VALUE

    private lateinit var mSensorManager: SensorManager
    private lateinit var mAccelerometer: Sensor
    private var mServiceTaskType = 0
    private lateinit var mAsyncTask: OnSensorChangedTask
    private lateinit var mAccBuffer: ArrayBlockingQueue<Double>
    private lateinit var mClassAttribute: Attribute
    private val mFeatLen = Globals.ACCELEROMETER_BLOCK_CAPACITY + 1
    private var currentActivityType : Double = 0.0

    private val sensorDataChannel = Channel<Double>(Channel.UNLIMITED)
    private var processingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        mAccBuffer = ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BUFFER_CAPACITY)

        Log.d("debug", "Service onCreate() called")

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)


        unitPreferenceValue = sharedPreferences.getString("unitPreference", "Miles")

        exerciseEntry = ExerciseEntry(
            // assuming you have an id generation mechanism in your DAO or entity
            dateTime = Calendar.getInstance(),
            duration = 0.0,
            distance = 0.0,
            calorie = 0.0,
            heartRate = 0.0,
            comment = "",
            avgPace = 0.0,
            avgSpeed = 0.0,
            climb = 0.0,
            locationList = ArrayList<LatLng>(), // Empty ArrayList to store location data
            activityType = Util.activityTypeToId("Standing"),
            inputType = 1, // define according to where you get this information
            unit_preference = unitPreferenceValue.toString(),
        )

        initLocationManager()

        showNotification()
        myBinder = MyBinder()

        numOfZeros = 0


    }

    // similar to last demo
    // this gets called when service gets started
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!!
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        mServiceTaskType = Globals.SERVICE_TASK_TYPE_COLLECT

        // Create the container for attributes
        val allAttr = java.util.ArrayList<Attribute>()

        // Adding FFT coefficient attributes
        val df = DecimalFormat("0000")

        for (i in 0 until Globals.ACCELEROMETER_BLOCK_CAPACITY) {
            allAttr.add(Attribute(Globals.FEAT_FFT_COEF_LABEL + df.format(i.toLong())))
        }

        // Adding the max feature
        allAttr.add(Attribute(Globals.FEAT_MAX_LABEL))

        // Declare a nominal attribute along with its candidate values
        val labelItems = java.util.ArrayList<String>(3)
        labelItems.add(Globals.CLASS_LABEL_STANDING)
        labelItems.add(Globals.CLASS_LABEL_WALKING)
        labelItems.add(Globals.CLASS_LABEL_RUNNING)
        labelItems.add(Globals.CLASS_LABEL_OTHER)
        mClassAttribute = Attribute(Globals.CLASS_LABEL_KEY, labelItems)
        allAttr.add(mClassAttribute)

        // Construct the dataset with the attributes specified as allAttr and
        // capacity 10000

        mAsyncTask = OnSensorChangedTask()
        mAsyncTask.execute()


        println("debug: Service onStartCommand() called everytime startService() is called; startId: $startId flags: $flags")
        // if OS kills service let it remain dead
//        selectedActivityType = intent?.getStringExtra("selectedActivityType").toString()
        selectedInputType = intent?.getStringExtra("selectedInputType").toString()
        return START_NOT_STICKY
    }

    // XD:Multiple clients can connect to the service at once. However, the system calls your
    // service's onBind() method to retrieve the IBinder only when the first client binds.
    // The system then delivers the same IBinder to any additional clients that bind, without
    // calling onBind() again.
    // returns an object that is IBinder interface
    override fun onBind(intent: Intent?): IBinder? {
        println("debug: Service onBind() called")
        // object that implements IBinder
        return myBinder
    }

    // Binder is a class: gives access to variable here created in service
    inner class MyBinder : Binder() {
        // 4. passing  the message handler to counter service through binder
        fun setmsgHandler(msgHandler: Handler) {
            this@TrackingService.msgHandler = msgHandler
        }
    }

    //XD: return false will allow you to unbind only once. Play with it.
    //XD: Return true if you would like to have the service's onRebind(Intent) method later called
    // when new clients bind to it.
    // if you return false, it means u want to bind service only once/once it gets unbind you cannot rebound
    // but we want to bind many times
    override fun onUnbind(intent: Intent?): Boolean {
        println("debug: Service onUnBind() called~~~")
        // stops the updates in the view fragment [text stops changing]
        msgHandler = null
        return true
    }

    // clean up notifications, ....
    override fun onDestroy() {

        mAsyncTask.cancel(true)
        try {
            Thread.sleep(100)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        mSensorManager.unregisterListener(this)

        super.onDestroy()
        println("debug: Service onDestroy")
        cleanupTasks()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        println("debug: app removed from the application list")
        cleanupTasks()
        stopSelf()
    }

    private fun cleanupTasks() {
        // removing notification
        notificationManager.cancel(NOTIFICATION_ID)
        // Stopping location updates
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(this)
        }
    }


    private fun showNotification() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        ) //XD: see book p1019 why we do not use Notification.Builder
        notificationBuilder.setSmallIcon(R.drawable.notification)
        notificationBuilder.setContentTitle("Service has started")
        notificationBuilder.setContentText("Tap me to go back")
        notificationBuilder.setContentIntent(pendingIntent)
        val notification = notificationBuilder.build()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "channel name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }


    // Initialize LocationManager and start location updates
    private fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return

            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            location?.let { onLocationChanged(it) }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0f, this)
        } catch (e: SecurityException) {
            // Handle the security exception
        }
    }

    override fun onProviderEnabled(provider: String) {
        // Called when the GPS provider is enabled by the user
    }

    override fun onProviderDisabled(provider: String) {
        // Called when the GPS provider is disabled by the user
    }

    @Deprecated("Deprecated in Java", ReplaceWith("Unit"))
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) =
        // Called when the status of the GPS provider changes
        Unit


    override fun onLocationChanged(location: Location) {
        // Assuming unitPreferenceValue is a String that can be "Miles" or "Kilometers"
        if (exerciseEntry.avgPace != 0.0) {
            prevPace = exerciseEntry.avgPace
        }

        val isUnitMiles = unitPreferenceValue == "Miles"

        val newLatLng = LatLng(location.latitude, location.longitude)
        exerciseEntry.locationList?.add(newLatLng)

        // Speed in meters/second from location object
        val currentSpeedMetersPerSec = location.speed

        // Convert speed to the desired unit
        val currentSpeed = unitCorrectionForSpeed(isUnitMiles, currentSpeedMetersPerSec)


        val climb = calculateClimb(location)

        // Update the previous altitude for the next location update
        if (location.hasAltitude()) {
            previousAltitude = location.altitude
        }

        // Add the climb to your exercise entry

        exerciseEntry.climb = unitCorrectionForClimb(isUnitMiles, climb)


//        println("from location changed: " + selectedInputType);

        // Calculate distance
        val distanceMeters = calculateDistance(newLatLng)

        // Convert distance to the desired unit
        val distance = unitCorrectionForDistance(isUnitMiles, distanceMeters)

        // Update total distance
        exerciseEntry.distance += distance
        exerciseEntry.avgPace = currentSpeed


        val (weightInKg, metValue) = getWeightAndMet()
        val totalTimeInSeconds =
            (System.currentTimeMillis() - exerciseEntry.dateTime!!.timeInMillis) / 1000.0 // Time in seconds

        // Calculate the duration in hours
        val durationInHours = secondsToHour(totalTimeInSeconds)
        val caloriesBurned = calculateCalories(metValue, weightInKg, durationInHours)
        exerciseEntry.calorie = caloriesBurned

        // Calculate average speed in user's preferred unit
        val averageSpeed = calculateAverageSpeed(totalTimeInSeconds)
        exerciseEntry.avgSpeed = averageSpeed
        exerciseEntry.duration = totalTimeInSeconds
        exerciseEntry.unit_preference = unitPreferenceValue.toString()

        // Send updated exerciseEntry to the msgHandler
        if (msgHandler != null) {
            val message = msgHandler!!.obtainMessage()
            message.what = EXERCISE_UPDATE
            message.obj = exerciseEntry // Sending the entire ExerciseEntry object
            msgHandler!!.sendMessage(message)
        }
    }

    private fun calculateAverageSpeed(totalTimeInSeconds: Double) = if (totalTimeInSeconds > 0) {
        val totalTimeInHours = secondsToHour(totalTimeInSeconds)
        exerciseEntry.distance / totalTimeInHours
    } else 0.0

    private fun secondsToHour(totalTimeInSeconds: Double) = totalTimeInSeconds / 3600.0

    private fun calculateCalories(
        metValue: Double,
        weightInKg: Double,
        durationInHours: Double
    ) = metValue * weightInKg * durationInHours

    private fun unitCorrectionForClimb(isUnitMiles: Boolean, climb: Double) = if (isUnitMiles) {
        // Convert climb to feet for miles unit preference (1 meter = 3.28084 feet)
        climb * 3.28084
    } else {
        // Keep climb in meters for kilometers unit preference
        climb
    }

    private fun calculateClimb(location: Location) =
        if (previousAltitude > Double.MIN_VALUE && location.hasAltitude()) {
            val altitudeChange = location.altitude - previousAltitude
            if (altitudeChange > 0) altitudeChange else 0.0 // Only count positive changes as climb
        } else 0.0

    private fun calculateDistance(newLatLng: LatLng): Double {
        val distanceMeters = if (exerciseEntry.locationList?.size!! > 1) {
            val lastLatLng = exerciseEntry.locationList?.get(exerciseEntry.locationList!!.size - 2)
            SphericalUtil.computeDistanceBetween(lastLatLng, newLatLng)
        } else 0.0
        return distanceMeters
    }

    private fun unitCorrectionForDistance(isUnitMiles: Boolean, distanceMeters: Double): Double {
        val distance = if (isUnitMiles) {
            // Convert to miles
            distanceMeters / 1609.34
        } else {
            // Convert to kilometers
            distanceMeters / 1000.0
        }
        return distance
    }

    private fun getWeightAndMet(): Pair<Double, Double> {
        val metValues = mapOf(
            "Running" to 11.5,  // Example MET value for running
            "Walking" to 3.8,   // and so on for each activity
            "Standing" to 2.3,
            "Cycling" to 8.0,
            "Hiking" to 6.0,
            "Downhill Skiing" to 6.8,
            "Cross-Country Skiing" to 13.5,
            "Snowboarding" to 5.3,
            "Skating" to 7.0,
            "Swimming" to 8.3,
            "Mountain Biking" to 8.5,
            "Wheelchair" to 3.0,
            "Elliptical" to 5.0,
            "Other" to 8.0
        )

        val weightInKg = 70.3

        val metValue = metValues[Util.idToActivityType(exerciseEntry.activityType)] ?: metValues["Other"]!!
        return Pair(weightInKg, metValue)
    }

    private fun unitCorrectionForSpeed(
        isUnitMiles: Boolean,
        currentSpeedMetersPerSec: Float
    ): Double {
        var currentSpeed = if (isUnitMiles) {
            // Convert to miles/hour
            currentSpeedMetersPerSec * 2.23694
        } else {
            // Convert to kilometers/hour
            currentSpeedMetersPerSec * 3.6
        }
        return currentSpeed
    }

    inner class OnSensorChangedTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg p0: Void?): Void? {
            val inst: Instance = DenseInstance(mFeatLen)
            val fft = FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val accBlock = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val im = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            var max = Double.MIN_VALUE
            var blockSize = 0

            while (true) {
                try {
                    // need to check if the AsyncTask is cancelled or not in the while loop
                    if (isCancelled() == true) {
                        return null
                    }

                    // Dumping buffer
                    accBlock[blockSize++] = mAccBuffer.take().toDouble()
                    if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
                        blockSize = 0

                        // time = System.currentTimeMillis();
                        max = .0
                        for (`val` in accBlock) {
                            if (max < `val`) {
                                max = `val`
                            }
                        }
                        fft.fft(accBlock, im)
                        for (i in accBlock.indices) {
                            val mag = Math.sqrt(
                                accBlock[i] * accBlock[i] + im[i]
                                        * im[i]
                            )
                            inst.setValue(i, mag)
                            im[i] = .0 // Clear the field
                        }

                        // Append max after frequency component
                        inst.setValue(Globals.ACCELEROMETER_BLOCK_CAPACITY, max)

                        classifyActivity(inst)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun classifyActivity(instance: Instance) {
        try {
            // Convert DenseInstance to Object array
            val objectArray: Array<Object> = Array(instance.numAttributes()) { i ->
                instance.value(i) as Object
            }

            println("Instance Values: ${objectArray.contentToString()}")
            val result = WekaClassifier.classify(objectArray)

            currentActivityType = result
            println("Classification Result: $result")
            exerciseEntry.activityType = result.toInt()
            // Handle the classification result
        } catch (e: Exception) {
            // Handle any exceptions
            e.printStackTrace()
        }
    }
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            val m = Math.sqrt(
                (event.values[0] * event.values[0] + event.values[1] * event.values[1] + (event.values[2]
                        * event.values[2])).toDouble()
            )

            // Inserts the specified element into this queue if it is possible
            // to do so immediately without violating capacity restrictions,
            // returning true upon success and throwing an IllegalStateException
            // if no space is currently available. When using a
            // capacity-restricted queue, it is generally preferable to use
            // offer.
            try {
                mAccBuffer.add(m)
            } catch (e: IllegalStateException) {

                // Exception happens when reach the capacity.
                // Doubling the buffer. ListBlockingQueue has no such issue,
                // But generally has worse performance
                val newBuf = ArrayBlockingQueue<Double>(mAccBuffer.size * 2)
                mAccBuffer.drainTo(newBuf)
                mAccBuffer = newBuf
                mAccBuffer.add(m)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}


}