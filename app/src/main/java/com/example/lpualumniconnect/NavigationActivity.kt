package com.example.lpualumniconnect

import ChatFragment
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.lpualumniconnect.fragments.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.log

class NavigationActivity : AppCompatActivity() {

    private lateinit var drawer: DrawerLayout
    private lateinit var btn: ImageButton
    private lateinit var nav_view: NavigationView
    private lateinit var name: TextView
    private lateinit var view_profile: TextView
    private lateinit var iv: ImageView

    private lateinit var progressDialog: AlertDialog

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        setupProgressDialog()

        Handler().postDelayed({
            progressDialog.dismiss()
        }, 3000)


        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            var newVisibility = visibility
            newVisibility = visibility or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
            window.decorView.systemUiVisibility = newVisibility
        }

        drawer = findViewById(R.id.drawer)
        btn = findViewById(R.id.btn)
        nav_view = findViewById(R.id.nav_view)

        val headerView = nav_view.getHeaderView(0)

        name = headerView.findViewById(R.id.userNameNavigationHeader)
        view_profile = headerView.findViewById(R.id.userViewProfileNavigationHeader)

        iv = headerView.findViewById(R.id.userProfileNavigationHeader)

        val events = nav_view.menu.findItem(R.id.Event)
        events.setOnMenuItemClickListener {
            startActivity(Intent(this, EventsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            true
        }

        val users =nav_view.menu.findItem(R.id.users)
        users.setOnMenuItemClickListener {
            startActivity(Intent(this, ChatHomeActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            true
        }

        val logout = nav_view.menu.findItem(R.id.Logout)
        logout.setOnMenuItemClickListener {
            val firebaseAuth = FirebaseAuth.getInstance()
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null)
            {
                FirebaseAuth.getInstance().signOut()
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                removeTokenFromDatabase(userId)
                    }
            startActivity(Intent(this,SignInActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            true

        }

        val news = nav_view.menu.findItem(R.id.news)
        news.setOnMenuItemClickListener {
            startActivity(Intent(this,NewsAndUpdateActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            true
        }

        val findJobs = nav_view.menu.findItem(R.id.job)
        findJobs.setOnMenuItemClickListener {
            startActivity(Intent(this, JobsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            true
        }

        val funds = nav_view.menu.findItem(R.id.PostFund)
        funds.setOnMenuItemClickListener {
            startActivity(Intent(this, FundsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            true
        }


        btn.setOnClickListener {
            drawer.open()
        }

        view_profile.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        }


        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.selectedItemId = R.id.navigation_home

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val fragment = HomeFragment()
                    openFragment(fragment)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.navigation_chats -> {
                    val fragment = ChatFragment()
                    openFragment(fragment)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_home
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users/$userId")
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userName = dataSnapshot.child("name").getValue(String::class.java)
                        name.text = userName ?: "No Name"

                        val imageBase64 = dataSnapshot.child("photo").getValue(String::class.java)
                        imageBase64?.let {
                            val imageBytes = Base64.decode(it, Base64.DEFAULT)
                            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            iv.setImageBitmap(decodedImage)
                        } ?: run {
                            iv.setImageResource(R.drawable.lpu)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("DB_ERROR", "loadPost:onCancelled", databaseError.toException())
                }
            })
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.main_content, fragment)
            commit()
        }
    }

    private fun setupProgressDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(50, 50, 50, 50)
        }

        val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge).apply {
            isIndeterminate = true
        }
        val textView = TextView(this).apply {
            text = "Loading..."
            textSize = 18f
            gravity = Gravity.CENTER
        }

        layout.addView(progressBar)
        layout.addView(textView)

        val builder = AlertDialog.Builder(this)
        builder.setView(layout)
        builder.setCancelable(false)
        progressDialog = builder.create()
        progressDialog.show()
    }



    private fun removeTokenFromDatabase(userId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users/$userId/fcmToken")
        userRef.removeValue()
            .addOnSuccessListener {
                Log.d("FCM", "FCM token removed successfully for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error removing FCM token for user: $userId, error: $e")
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}