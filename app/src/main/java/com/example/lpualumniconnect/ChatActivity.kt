package com.example.lpualumniconnect

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lpualumniconnect.adapter.MessageAdapter
import com.example.lpualumniconnect.datamodal.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class ChatActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox : EditText
    private lateinit var sendButton : ImageView
    private lateinit var imageProfile:ImageView
    private lateinit var nameProfile : TextView

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>

    var receiverRoom : String?= null
    var senderRoom : String ?= null

    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null
    private var alertShown = false

    private lateinit var mDbRef : DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        initSensor()

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            var newVisibility = visibility
            newVisibility = visibility or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
            window.decorView.systemUiVisibility = newVisibility
        }

        chatRecyclerView  =findViewById(R.id.chat_recycler_view)
        messageBox = findViewById(R.id.chat_box)
        sendButton = findViewById(R.id.chat_send_btn)
        mDbRef = FirebaseDatabase.getInstance().reference
        nameProfile = findViewById(R.id.name_chat)
        imageProfile = findViewById(R.id.profile_chat)

        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")

        nameProfile.text = name
        if (receiverUid != null) {
            fetchUserProfile(receiverUid)
        }

        val senderUid = FirebaseAuth.getInstance().currentUser?.uid

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        supportActionBar?.title = name

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()
                    for (postSnapshot in snapshot.children){
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                    chatRecyclerView.scrollToPosition(messageList.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })


        sendButton.setOnClickListener {
            val messageText = messageBox.text.toString()
            if (messageText.isNotEmpty()) {
                val timeStamp = System.currentTimeMillis()
                val messageObject = Message(messageText, senderUid, timeStamp)
                val ref = mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                ref.setValue(messageObject).addOnSuccessListener {
                    mDbRef.child("chats").child(receiverRoom!!).child("messages").push().setValue(messageObject)
                }
                messageBox.setText("")
            }
        }

    }

    private fun fetchUserProfile(receiverUid: String) {
        mDbRef.child("users").child(receiverUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val imageUrl = snapshot.child("photo").value.toString()
                    loadImageIntoView(imageUrl)
                } else {
                    imageProfile.setImageResource(R.drawable.profile)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun loadImageIntoView(imageUrl: String) {
        if (imageUrl.startsWith("http")) {
            Glide.with(this@ChatActivity).load(imageUrl).into(imageProfile)
        } else {

            val imageBytes = Base64.decode(imageUrl, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageProfile.setImageBitmap(decodedImage)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun initSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val distance = it.values[0]
            Log.d("Sensor", "Proximity sensor value: $distance")

            if (distance > 7) {
                if (!alertShown) {
                    showAlertDialog()
                    alertShown = true
                }
            } else {
                if (alertShown) {
                    alertShown = false
                }
            }
        }
    }



    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this)
            .setTitle("Too Close")
            .setMessage("Please move your device away from your face.")
            .setCancelable(false)
            .setNegativeButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
        alertShown = true
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        proximitySensor?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }
}

