package com.example.lpualumniconnect

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.lpualumniconnect.datamodal.Event
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EventsActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var descriptionBox: TextInputEditText
    private lateinit var dateEditText: TextInputEditText
    private lateinit var timeEditText: TextInputEditText
    private var imageBitmap: Bitmap? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            var newVisibility = visibility
            newVisibility = visibility or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
            window.decorView.systemUiVisibility = newVisibility
        }


        dateEditText = findViewById(R.id.textInputEditText)
        timeEditText = findViewById(R.id.textInputEditText02)
        imageView = findViewById(R.id.event_imageView)
        descriptionBox = findViewById(R.id.event_descriptionBox)
        val postButton = findViewById<Button>(R.id.post_button_event)

        dateEditText.setOnClickListener { showDatePicker() }
        timeEditText.setOnClickListener { showTimePicker() }
        imageView.setOnLongClickListener {
            openGallery()
            true
        }
        postButton.setOnClickListener { uploadEvent()
        Toast.makeText(this,"Event Posted", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this,NavigationActivity::class.java))}
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            imageView.setImageBitmap(imageBitmap)
        }
    }

    private fun uploadEvent() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val description = descriptionBox.text.toString()
        val date = dateEditText.text.toString()
        val time = timeEditText.text.toString()

        if (imageBitmap != null) {
            val baos = ByteArrayOutputStream()
            imageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageData = baos.toByteArray()
            val encodedImage = Base64.encodeToString(imageData, Base64.DEFAULT)
            saveEventInfo(uid, encodedImage, description, date, time)
        } else {
            Toast.makeText(this, "Image is not selected!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveEventInfo(uid: String, encodedImage: String, description: String, date: String, time: String) {
        Log.d("EventsActivity", "Saving event info")
        val dbRef = FirebaseDatabase.getInstance().getReference("events")
        val eventId = dbRef.push().key ?: run {
            Log.e("EventsActivity", "Failed to get a new event key")
            return
        }
        val eventMap = mapOf(
            "uid" to uid,
            "photo" to encodedImage,
            "description" to description,
            "date" to date,
            "time" to time
        )
        Log.d("EventsActivity", "Event Map: $eventMap")
        dbRef.child(eventId).setValue(eventMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("EventsActivity", "Event posted successfully!")
            } else {
                Log.e("EventsActivity", "Failed to post event: ${task.exception?.message}")
            }
        }
    }


    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Select Date")
        val datePicker = builder.build()
        datePicker.addOnPositiveButtonClickListener {
            dateEditText.setText(datePicker.headerText)
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun showTimePicker() {
        val builder = MaterialTimePicker.Builder()
        builder.setTitleText("Select Time")
        builder.setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
        val timePicker = builder.build()
        timePicker.addOnPositiveButtonClickListener {
            val hour = if (timePicker.hour < 10) "0${timePicker.hour}" else "${timePicker.hour}"
            val minute = if (timePicker.minute < 10) "0${timePicker.minute}" else "${timePicker.minute}"
            timeEditText.setText("$hour:$minute")
        }
        timePicker.show(supportFragmentManager, "TIME_PICKER")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}