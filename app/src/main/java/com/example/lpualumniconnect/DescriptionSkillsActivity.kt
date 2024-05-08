package com.example.lpualumniconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.lpualumniconnect.datamodal.User

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DescriptionSkillsActivity : AppCompatActivity() {

    private lateinit var description : EditText
    private lateinit var skill1 : EditText
    private lateinit var skill2 : EditText
    private lateinit var skill3 : EditText
    private lateinit var btn : Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description_skills)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            var newVisibility = visibility
            newVisibility = visibility or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
            window.decorView.systemUiVisibility = newVisibility
        }

        description = findViewById(R.id.edit_description)
        skill1 = findViewById(R.id.edit_skill1)
        skill2 = findViewById(R.id.edit_skill2)
        skill3 = findViewById(R.id.edit_skill3)
        btn = findViewById(R.id.edit_descriptionSkillsBtn)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance()

        loadData()

        btn.setOnClickListener {
            saveData()
        }
    }

    private fun loadData() {
        val uid = mAuth.currentUser?.uid
        if (uid != null) {
            mDbRef.getReference("users").child(uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        description.setText(user?.description ?: "")
                        skill1.setText(user?.skill1 ?: "")
                        skill2.setText(user?.skill2 ?: "")
                        skill3.setText(user?.skill3 ?: "")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(applicationContext, "Failed to load data: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                })
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveData() {
        val desc = description.text.toString()
        val sk1 = skill1.text.toString()
        val sk2 = skill2.text.toString()
        val sk3 = skill3.text.toString()

        val uid = mAuth.currentUser?.uid

        if (uid != null) {
            val updates = mapOf(
                "description" to desc,
                "skill1" to sk1,
                "skill2" to sk2,
                "skill3" to sk3
            )
            mDbRef.getReference("users").child(uid).updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data updated successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this,UserProfileActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()

                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update data: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}