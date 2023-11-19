package com.example.kian_hosseinkhani_myruns2.ActivitiesAndFragments.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.kian_hosseinkhani_myruns2.R
import com.example.kian_hosseinkhani_myruns2.Util
import com.example.kian_hosseinkhani_myruns2.viewModel.MyViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserProfileSettings : AppCompatActivity() {
    private var isImageCaptured: Boolean = false
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var radioGender: RadioGroup
    private lateinit var etClass: EditText
    private lateinit var etMajor: EditText
    private lateinit var changePhotoBtn: Button
    private lateinit var profilePic: ImageView
    private lateinit var tempImgUri: Uri
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var myViewModel: MyViewModel
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private lateinit var galleryImageUri: Uri
    private var isCamera: Boolean = false
    private var isGallery: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure the app always uses the dark theme
        setContentView(R.layout.activity_user_profile_settings)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "User Profile Settings"

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        radioGender = findViewById(R.id.radioGender)
        etClass = findViewById(R.id.etClass)
        etMajor = findViewById(R.id.etMajor)

        profilePic = findViewById(R.id.imageProfile)
        changePhotoBtn = findViewById(R.id.btnChangePhoto)

        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)




        Util.checkPermissions(this)


        loadProfile()


        isImageCaptured = savedInstanceState?.getBoolean("isImageCaptured") ?: false
        val tempImgUriString = savedInstanceState?.getString("tempImgUri")
        tempImgUri = if (tempImgUriString != null) Uri.parse(tempImgUriString) else {
            // If tempImgUri is not restored, initialize it
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
                Date()
            )
            val imageFileName = "JPEG_" + timeStamp + "_"
            val tempImgFile = File(getExternalFilesDir(null), imageFileName)
            FileProvider.getUriForFile(this, "com.example.kian_hosseinkhani_myruns2", tempImgFile)
        }



        changePhotoBtn.setOnClickListener {
            // Create an AlertDialog for the user to choose
            AlertDialog.Builder(this)
                .setTitle("Pick Profile Picture")
                .setItems(arrayOf("Open Camera", "Select from Gallery")) { _, which ->
                    when (which) {
                        0 -> openCamera()
                        1 -> openGallery()
                    }
                }
                .show()
        }




        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    // Attempt to retrieve the captured bitmap
                    val bitmap = Util.getBitmap(this, tempImgUri)
                    // Update the ViewModel with the captured bitmap
                    myViewModel.userImage.value = bitmap
                    isImageCaptured = true
                    isGallery = false
                    isCamera = true
                } catch (e: Exception) {
                    // Log or handle the exception, e.g., show an error message to the user
                    Log.e("CaptureImage", "Error retrieving captured image", e)
                }
            } else {
                // Handle the case where the image capture was not successful, e.g., show an error message to the user
                Log.w("CaptureImage", "Image capture failed with resultCode: ${result.resultCode}")
            }
        }



        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    // Copy image to app's storage
                    val copiedUri = copyImageToAppStorage(selectedImageUri, this)

                    if (copiedUri != null) {
                        try {
                            val bitmap = Util.getBitmap(this, copiedUri)
                            myViewModel.userImage.value = bitmap
                            galleryImageUri = copiedUri // Use the copied URI
                            isImageCaptured = true
                            isGallery = true
                            isCamera = false
                        } catch (e: Exception) {
                            Log.e("GalleryImage", "Error retrieving selected image", e)
                        }
                    }
                }
            }
        }


        // Initialize ViewModel to make sure observer is always alive
        myViewModel = ViewModelProvider(this).get(MyViewModel::class.java)



        // Observe changes in userImage and update the ImageView accordingly
        myViewModel.userImage.observe(this) { bitmap ->
            bitmap?.let {
                profilePic.setImageBitmap(it)
            } ?: run {
                // Set default image if the bitmap is null
                profilePic.setImageResource(R.drawable.default_profile)
            }
        }


        // Load the saved image URI from SharedPreferences if it exists
        val prefs: SharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val savedImageUriString = prefs.getString("savedImageUri", null)
        if (savedImageUriString != null) {
            val oldImgUri = Uri.parse(savedImageUriString)
            try {
                val bitmap = Util.getBitmap(this, oldImgUri)
                profilePic.setImageBitmap(bitmap)
            } catch (e: Exception) {
                // Log the exception for debugging
                Log.e("LoadImage", "Error loading saved image", e)
            }
        } else {
            // Load the default image if there is no saved image URI
            profilePic.setImageResource(R.drawable.default_profile)
        }

        findViewById<View>(R.id.btnSave).setOnClickListener {
            saveProfile()
        }

        findViewById<View>(R.id.btnCancel).setOnClickListener {
             finish()


        }
    }

    private fun openCamera() {
        // Existing code to open the camera
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            // Specify the URI where the captured image should be saved
            putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
        }
        cameraResult.launch(captureImageIntent)
    }

    private fun openGallery() {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResult.launch(pickImageIntent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the state of isImageCaptured and tempImgUri
        outState.putBoolean("isImageCaptured", isImageCaptured)
        outState.putString("tempImgUri", tempImgUri.toString())
    }


    private fun validatePhoneNumber(phone: String): Boolean {
        // Define a regex pattern for a valid phone number.
        // This pattern allows only digits, +, -, (), and spaces
        val pattern = Regex("^[+\\-()\\d\\s]*$")

        // Check if the phone number matches the pattern
        return pattern.matches(phone)
    }



    private fun saveProfile() {
        // Check if all the fields are filled out
        if (etName.text.isNullOrEmpty() ||
            etEmail.text.isNullOrEmpty() ||
            etPhone.text.isNullOrEmpty() ||
            radioGender.checkedRadioButtonId == -1 ||
            etClass.text.isNullOrEmpty() ||
            etMajor.text.isNullOrEmpty()) {

            // Show an AlertDialog asking the user to fill out all the information
            AlertDialog.Builder(this)
                .setTitle("Missing Information")
                .setMessage("Please fill out all the information before saving.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            return
        }

        val prefs: SharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = prefs.edit()

        editor.putString("name", etName.text.toString())

        val email = etEmail.text.toString()
        if (!email.contains("@") || !email.contains(".")) {
            // Show an AlertDialog informing the user to enter a valid email
            AlertDialog.Builder(this)
                .setTitle("Invalid Email")
                .setMessage("Please enter a valid email address.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            return
        }

        editor.putString("email", etEmail.text.toString())


        val phone = etPhone.text.toString()

        // Validate the phone number
        if (!validatePhoneNumber(phone)) {
            // Show an error dialog or message if the phone number is invalid
            AlertDialog.Builder(this)
                .setTitle("Invalid Phone Number")
                .setMessage("Please enter a valid phone number with only digits, +, -, () and spaces.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        editor.putString("phone", phone)

        editor.putInt("gender", radioGender.checkedRadioButtonId)
        editor.putString("class", etClass.text.toString())
        editor.putString("major", etMajor.text.toString())
        editor.apply()

        // Save the current image URI as a string to SharedPreferences

        if(isImageCaptured){
            if(!isCamera && isGallery){
                println("hello")
                println("saved gallery address: $galleryImageUri")
                editor.putString("savedImageUri", galleryImageUri.toString())
            }
            else if(isCamera && !isGallery){
                editor.putString("savedImageUri", tempImgUri.toString())
            }
        }

        editor.apply()


        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun loadProfile() {

        val prefs: SharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE)

        etName.setText(prefs.getString("name", ""))
        etEmail.setText(prefs.getString("email", ""))
        etPhone.setText(prefs.getString("phone", ""))
        radioGender.check(prefs.getInt("gender", -1))
        etClass.setText(prefs.getString("class", ""))
        etMajor.setText(prefs.getString("major", ""))
    }
    private fun copyImageToAppStorage(sourceUri: Uri, context: Context): Uri? {
        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_" + timeStamp + "_"
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val destinationFile = File(storageDir, imageFileName + ".jpg")

            inputStream?.use { input ->
                val outputStream = FileOutputStream(destinationFile)
                outputStream.use { output ->
                    val buffer = ByteArray(4 * 1024) // or other buffer size
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }

            return Uri.fromFile(destinationFile)
        } catch (e: Exception) {
            Log.e("AppStorage", "Error copying image", e)
            return null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_profile_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.main_menu, menu)
//        return true
//    }
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_settings -> {
//                // Handle settings click
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}
