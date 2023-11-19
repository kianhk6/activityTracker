package com.example.kian_hosseinkhani_myruns2.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


// this class is inspired by the class lecture notes for managing
// the observer
class MyViewModel: ViewModel() {
    val userImage = MutableLiveData<Bitmap>()
}