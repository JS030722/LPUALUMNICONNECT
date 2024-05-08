package com.example.lpualumniconnect

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lpualumniconnect.adapter.ExperienceAdapter
import com.example.lpualumniconnect.datamodal.Experience
import com.example.lpualumniconnect.datamodal.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class QuickLookProfileActivity : AppCompatActivity() {

    private lateinit var name: TextView
    private lateinit var photo: ImageView
    private lateinit var bio: TextView
    private lateinit var location: TextView
    private lateinit var about: TextView
    private lateinit var skill1: TextView
    private lateinit var skill2: TextView
    private lateinit var skill3: TextView
    private lateinit var school: TextView
    private lateinit var year: TextView
    private lateinit var message : TextView
    private lateinit var githubImageView: ImageView
    private lateinit var linkedInImageView: ImageView

    private lateinit var mDbRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var experienceAdapter: ExperienceAdapter
    var githubUri : String? = null
    var linkedInUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_look_profile)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            var newVisibility = visibility
            newVisibility = visibility or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
            window.decorView.systemUiVisibility = newVisibility
        }

        initializeViews()
        val userId = intent.getStringExtra("userId") ?: return
        loadData(userId)
        loadExperienceData(userId)

        message.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("uid", userId)
            intent.putExtra("name", name.text.toString())
            startActivity(intent)
        }


    }

    private fun initializeViews() {
        name = findViewById(R.id.userprofile_name)
        photo = findViewById(R.id.userprofile_profileImage)
        bio = findViewById(R.id.userprofile_bio)
        location = findViewById(R.id.userprofile_location)
        about = findViewById(R.id.userprofile_about)
        skill1 = findViewById(R.id.userprofile_skills_tv1)
        skill2 = findViewById(R.id.userprofile_skills_tv2)
        skill3 = findViewById(R.id.userprofile_skills_tv3)
        school = findViewById(R.id.userprofile_school)
        year = findViewById(R.id.userprofile_batch)
        message = findViewById(R.id.userprofile_name_bio_edit)
        githubImageView = findViewById(R.id.github_iv)
        linkedInImageView = findViewById(R.id.linkedIn_iv)


        recyclerView = findViewById(R.id.recyclerView022)
        recyclerView.layoutManager = LinearLayoutManager(this)

        experienceAdapter = ExperienceAdapter(mutableListOf())
        recyclerView.adapter = experienceAdapter
    }

    private fun loadData(userId: String) {
        mDbRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        mDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                user?.let {
                    name.text = it.name
                    bio.text = it.bio
                    location.text = it.location
                    about.text = it.description
                    school.text = it.school
                    year.text = it.batch
                    skill1.text = it.skill1
                    skill2.text = it.skill2
                    skill3.text = it.skill3
                    it.photo?.let { encodedImage ->
                        decodeBase64ToBitmap(encodedImage)?.let { bitmap ->
                            photo.setImageBitmap(bitmap)
                        } ?: photo.setImageResource(R.drawable.profile)
                    } ?: photo.setImageResource(R.drawable.profile)

                    githubUri = it.github
                    linkedInUri = it.linkedIn

                    setupLongPressListener(githubImageView, githubUri)
                    setupLongPressListener(linkedInImageView, linkedInUri)
                }


            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadExperienceData(userId: String) {
        val experienceRef = FirebaseDatabase.getInstance().reference.child("experiences").child(userId)
        experienceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val experiences = mutableListOf<Experience>()
                for (experienceSnapshot in dataSnapshot.children) {
                    val experience = experienceSnapshot.getValue(Experience::class.java)
                    if (experience != null) {
                        experiences.add(experience)
                        Log.d("ExperienceData", "Added experience: ${experience.jobTitle}")
                    }
                }
                if (experiences.isNotEmpty()) {
                    Log.d("ExperienceData", "Experiences loaded, updating adapter.")
                    experienceAdapter.experiences.clear()
                    experienceAdapter.experiences.addAll(experiences)
                    experienceAdapter.notifyDataSetChanged()
                } else {
                    Log.d("ExperienceData", "No experiences found.")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("ExperienceData", "Error loading experiences: ${databaseError.message}")
            }
        })
    }

    private fun setupLongPressListener(imageView: ImageView, url: String?) {
        imageView.setOnLongClickListener {
            if (!url.isNullOrBlank()) {
                openWebPage(url)
            } else {
                Log.d("SocialLinkError", "Invalid or null URL for ${imageView.id}")
            }
            true
        }
    }

    private fun openWebPage(url: String) {
        val correctedUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "https://$url"
        }

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(correctedUrl)
        startActivity(intent)
    }


    private fun decodeBase64ToBitmap(encodedImage: String): Bitmap? {
        return try {
            val imageBytes = Base64.decode(encodedImage, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}