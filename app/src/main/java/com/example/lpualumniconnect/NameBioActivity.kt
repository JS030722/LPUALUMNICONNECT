package com.example.lpualumniconnect

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.Formatter
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.lpualumniconnect.datamodal.User

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Locale

class NameBioActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var profileImageView: ImageView
    private lateinit var github: EditText
    private lateinit var linkedIn: EditText
    private lateinit var ipTextView: TextView
    private lateinit var saveButton: Button
    private var isManualEntrySelected: Boolean = false

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: FirebaseDatabase
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val GALLERY_REQUEST_CODE = 100
        private const val CAMERA_REQUEST_CODE = 101
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_bio)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            var newVisibility = visibility
            newVisibility = visibility or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
            window.decorView.systemUiVisibility = newVisibility
        }

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        nameEditText = findViewById(R.id.nameEditText)
        bioEditText = findViewById(R.id.bioEditText)
        locationEditText = findViewById(R.id.locationEditText)
        profileImageView = findViewById(R.id.EditImageView)
        github = findViewById(R.id.githubEditText)
        linkedIn = findViewById(R.id.linkedInEditText)
        saveButton = findViewById(R.id.submit_button)
        ipTextView = findViewById(R.id.user_ip)

        displayWifiIpAddress()
        setupListeners()
        loadData()
    }

    private fun setupListeners() {
        profileImageView.setOnClickListener { chooseImageSource() }
        saveButton.setOnClickListener { updateUserData() }
        locationEditText.setOnClickListener {
            if (!isManualEntrySelected) {
                showLocationOptionsDialog()
            }
        }
    }

    private fun loadData() {
        mAuth.currentUser?.uid?.let { uid ->
            mDbRef.getReference("users/$uid").get().addOnSuccessListener { dataSnapshot ->
                val user = dataSnapshot.getValue(User::class.java)
                user?.let {
                    nameEditText.setText(user.name)
                    bioEditText.setText(user.bio)
                    locationEditText.setText(user.location)
                    github.setText(user.github)
                    linkedIn.setText(user.linkedIn)
                    user.photo?.let {
                        val bitmap = decodeBase64ToBitmap(it)
                        profileImageView.setImageBitmap(bitmap)
                    } ?: Glide.with(this).load(R.drawable.profile).into(profileImageView)  // Load a default image if no photo is available
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap {
        val imageBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun updateUserData() {
        val name = nameEditText.text.toString()
        val bio = bioEditText.text.toString()
        val location = locationEditText.text.toString()
        val github = github.text.toString()
        val linkedIn = linkedIn.text.toString()

        val updates = mapOf(
            "name" to name,
            "bio" to bio,
            "location" to location,
            "github" to github,
            "linkedIn" to linkedIn
        )

        mAuth.currentUser?.uid?.let { uid ->
            val userRef = mDbRef.getReference("users/$uid")
            userRef.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun chooseImageSource() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(this)
            .setTitle("Choose Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }.show()
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { cameraIntent ->
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        }
    }

    private fun openGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { galleryIntent ->
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> data?.data?.let { uri ->
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    updateProfileWithBase64Image(bitmap)
                }
                CAMERA_REQUEST_CODE -> data?.extras?.get("data")?.let { bitmap ->
                    updateProfileWithBase64Image(bitmap as Bitmap)
                }
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        ByteArrayOutputStream().apply {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
            return Base64.encodeToString(this.toByteArray(), Base64.DEFAULT)
        }
    }

    private fun updateProfileWithBase64Image(bitmap: Bitmap) {
        val base64Image = bitmapToBase64(bitmap)

        mAuth.currentUser?.uid?.let { uid ->
            val userRef = mDbRef.getReference("users/$uid")
            val updates = mapOf("photo" to base64Image)
            userRef.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Profile image updated successfully.", Toast.LENGTH_SHORT).show()
                    profileImageView.setImageBitmap(bitmap)
                } else {
                    Toast.makeText(this, "Failed to update profile image.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLocationOptionsDialog() {
        val options = arrayOf("Enter Live Location", "Enter Location Manually")
        AlertDialog.Builder(this)
            .setTitle("Choose Location Option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkLocationPermission()
                    1 -> manuallyEnterLocation()
                }
            }.show()
    }

    private fun manuallyEnterLocation() {
        isManualEntrySelected = true
        locationEditText.isFocusableInTouchMode = true
        locationEditText.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(locationEditText, InputMethodManager.SHOW_FORCED)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(this@NameBioActivity, Locale.getDefault())
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (addresses != null) {
                                if (addresses.isNotEmpty()) {
                                    val address = addresses[0].getAddressLine(0)
                                    withContext(Dispatchers.Main) {
                                        locationEditText.setText(address)
                                    }
                                }
                            }
                        } catch (e: IOException) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@NameBioActivity, "Failed to fetch address", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayWifiIpAddress() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        ipTextView.text = "IP Address: $ipAddress"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}