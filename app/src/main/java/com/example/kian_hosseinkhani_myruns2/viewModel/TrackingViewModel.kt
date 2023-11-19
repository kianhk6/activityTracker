package com.example.kian_hosseinkhani_myruns2.viewModel

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kian_hosseinkhani_myruns2.services.TrackingService
import com.example.kian_hosseinkhani_myruns2.model.ExerciseEntry

class TrackingViewModel : ViewModel(), ServiceConnection {
    // 1. view model listens to messages coming from services (2. wired up in ui fragment next)
    private var myMessageHandler: MyMessageHandler = MyMessageHandler(Looper.getMainLooper())


    private val _counter = MutableLiveData<Int>()


    // MutableLiveData for ExerciseEntry
    private val _exerciseEntry = MutableLiveData<ExerciseEntry>()

    // Public LiveData to access the ExerciseEntry
    val exerciseEntry: LiveData<ExerciseEntry>
        get() = _exerciseEntry


    override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
        println("debug: ViewModel: onServiceConnected() called; ComponentName: $name")
        val tempBinder = iBinder as TrackingService.MyBinder
        tempBinder.setmsgHandler(myMessageHandler)
    }


    // never gets called other than OS kills service or app crashes
    // unbinding service does not make this re called
    override fun onServiceDisconnected(name: ComponentName) {
        println("debug: Activity: onServiceDisconnected() called~~~")
    }

    inner class MyMessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            val exerciseEntry = msg.obj as? ExerciseEntry
            exerciseEntry?.let {
                _exerciseEntry.value = it
            }
        }
    }

}