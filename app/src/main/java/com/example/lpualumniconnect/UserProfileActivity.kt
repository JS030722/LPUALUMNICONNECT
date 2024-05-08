package com.example.lpualumniconnect

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lpualumniconnect.adapter.ExperienceAdapter
import com.example.lpualumniconnect.datamodal.Experience
import com.example.lpualumniconnect.datamodal.User

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import org.w3c.dom.Text
import java.util.Calendar

class UserProfileActivity : AppCompatActivity() {
    private lateinit var name_bio_edit: TextView
    private lateinit var name: TextView
    private lateinit var photo : ImageView
    private lateinit var bio: TextView
    private lateinit var location: TextView
    private lateinit var about: TextView
    private lateinit var skill1: TextView
    private lateinit var skill2: TextView
    private lateinit var skill3: TextView
    private lateinit var arrow : ImageView
    private lateinit var linkedIn_iv : ImageView
    private lateinit var github_iv : ImageView
    private lateinit var schoolDomainEdit : TextView
    private lateinit var school : TextView
    private lateinit var year : TextView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    private lateinit var experienceAdapter: ExperienceAdapter
    private var experienceList = mutableListOf<Experience>()
    private lateinit var recyclerView: RecyclerView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            var newVisibility = visibility
            newVisibility = visibility or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
            window.decorView.systemUiVisibility = newVisibility
        }

        initializeViews()
        setupFirebase()
        loadUserData()

        name_bio_edit.setOnClickListener {
            startActivity(Intent(this, NameBioActivity::class.java))
        }

        val addExperienceIcon = findViewById<ImageView>(R.id.user_profile_experience_add_iv)
        addExperienceIcon.setOnClickListener {
            openAddExperienceDialog()
        }

        linkedIn_iv.setOnLongClickListener {
            retrieveSocialLink("LinkedIn")
            true
        }

        github_iv.setOnLongClickListener {
            retrieveSocialLink("GitHub")
            true
        }

        arrow.setOnClickListener {
            startActivity(Intent(this, DescriptionSkillsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        }

        schoolDomainEdit.setOnClickListener {
            startActivity(Intent(this, SchoolDomainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        }
    }

    private fun initializeViews() {
        name_bio_edit = findViewById(R.id.userprofile_name_bio_edit)
        name = findViewById(R.id.userprofile_name)
        photo = findViewById(R.id.userprofile_profileImage)
        bio = findViewById(R.id.userprofile_bio)
        location = findViewById(R.id.userprofile_location)
        about = findViewById(R.id.userprofile_about)
        skill1 = findViewById(R.id.userprofile_skills_tv1)
        skill2 = findViewById(R.id.userprofile_skills_tv2)
        skill3 = findViewById(R.id.userprofile_skills_tv3)
        linkedIn_iv = findViewById(R.id.linkedIn_iv)
        github_iv = findViewById(R.id.github_iv)
        arrow = findViewById(R.id.userprofile_skills_edit_arrow)
        schoolDomainEdit = findViewById(R.id.userprofile_domain_edit)
        school = findViewById(R.id.userprofile_school)
        year = findViewById(R.id.userprofile_batch)

        recyclerView = findViewById(R.id.recyclerView022)
        recyclerView.layoutManager = LinearLayoutManager(this)
        experienceAdapter = ExperienceAdapter(experienceList)
        recyclerView.adapter = experienceAdapter

    }

    private fun setupFirebase() {
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser?.uid ?: "")
    }

    private fun loadUserData() {
        loadUserDetails()
        loadExperiences()
    }

    private fun loadUserDetails() {
        mDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    updateUI(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserProfileActivity, "Failed to load user data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadExperiences() {
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            val experiencesRef = FirebaseDatabase.getInstance().reference.child("experiences").child(userId)
            experiencesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val experiences = mutableListOf<Experience>()
                    snapshot.children.forEach { experienceSnapshot ->
                        val experience = experienceSnapshot.getValue(Experience::class.java)
                        experience?.let {
                            experiences.add(it)
                        }
                    }
                    updateExperiencesList(experiences)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UserProfileActivity, "Failed to load experiences: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }



    private fun updateUI(user: User) {
        name.text = user.name ?: ""
        bio.text = user.bio ?: ""
        location.text = user.location ?: ""
        about.text = user.description ?: ""
        skill1.text = user.skill1 ?: ""
        skill2.text = user.skill2 ?: ""
        skill3.text = user.skill3 ?: ""
        school.text = user.school ?: ""
        year.text = user.batch ?: ""

        user.photo?.let {
            val bitmap = decodeBase64ToBitmap(it)
            if (bitmap != null) {
                photo.setImageBitmap(bitmap)
            } else {
                photo.setImageResource(R.drawable.profile)
            }
        } ?: photo.setImageResource(R.drawable.profile)
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun updateExperiencesList(experiences: List<Experience>?) {
        experiences?.let {
            experienceList.clear()
            experienceList.addAll(it)
            experienceAdapter.notifyDataSetChanged()
        }
    }

    private fun openAddExperienceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_experience, null)
        val jobTitle = dialogView.findViewById<EditText>(R.id.editJobTitle)
        val company = dialogView.findViewById<EditText>(R.id.editCompany)
        val description = dialogView.findViewById<EditText>(R.id.editDescription)
        val startDateView = dialogView.findViewById<TextView>(R.id.editStartDate)
        val endDateView = dialogView.findViewById<TextView>(R.id.editEndDate)
        setupDatePicker(startDateView)
        setupDatePicker(endDateView)

        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle("Add New Experience")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val newExperience = Experience(
                    jobTitle.text.toString(),
                    company.text.toString(),
                    startDateView.text.toString(),
                    endDateView.text.toString(),
                    description.text.toString()
                )
                addExperience(newExperience)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun setupDatePicker(textView: TextView) {
        textView.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                textView.text = "${year}-${monthOfYear + 1}-${dayOfMonth}"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun addExperience(experience: Experience) {
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            val experiencesRef = FirebaseDatabase.getInstance().reference.child("experiences").child(userId)
            experiencesRef.push().setValue(experience)
                .addOnSuccessListener {
                    Toast.makeText(this, "Experience added successfully", Toast.LENGTH_SHORT).show()
                    loadExperiences()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add experience: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun retrieveSocialLink(socialMedia: String) {
        mDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    val link = if (socialMedia == "LinkedIn") {
                        it.linkedIn
                    } else {
                        it.github
                    }
                    link?.let { socialLink ->
                        if (socialLink.isNotEmpty()) {
                            openProfile(socialLink)
                        } else {
                            Toast.makeText(this@UserProfileActivity, "$socialMedia link not available", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Toast.makeText(this@UserProfileActivity, "$socialMedia link not available", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserProfileActivity, "Failed to retrieve $socialMedia link: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openProfile(profileLink: String) {
        val link = if (!profileLink.startsWith("https://")) {
            "https://$profileLink"
        } else {
            profileLink
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No app available to handle this action", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val imageBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: IllegalArgumentException) {
            null
        }
    }


}