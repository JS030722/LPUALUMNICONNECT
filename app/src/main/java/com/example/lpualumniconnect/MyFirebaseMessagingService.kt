package com.example.lpualumniconnect

import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "From: ${remoteMessage.from}")

            val chatIntent = Intent(this, ChatActivity::class.java)
            startActivity(chatIntent)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun handleNow(dataMap: Map<String, String>) {
        Log.d("FCM", "Data Payload: $dataMap")
    }

    private fun sendRegistrationToServer(token: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}/fcmToken")
            userRef.setValue(token)
                .addOnSuccessListener {
                    Log.d("FCM", "FCM token saved successfully for user: ${currentUser.uid}")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Error saving FCM token for user: ${currentUser.uid}, error: $e")
                }
        } else {
            Log.e("FCM", "Current user is null")
        }
    }
}