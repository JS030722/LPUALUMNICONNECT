package com.example.lpualumniconnect

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.lpualumniconnect.datamodal.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class SchoolDomainActivity : AppCompatActivity() {

    private lateinit var schoolEdittext: com.google.android.material.textfield.TextInputEditText
    private lateinit var domainEditText: com.google.android.material.textfield.TextInputEditText
    private val auth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference("users")

    private var selectedSchool: String? = null
    private var selectedBatch: String? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_school_domain)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            window.decorView.systemUiVisibility = visibility or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        schoolEdittext = findViewById(R.id.schoolEdittext)
        domainEditText = findViewById(R.id.domainEditText)
        val changeButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.schoolDomainButton)

        val schools = arrayOf(
            "Mittal School of Business",
            "School of Computer Science and Engineering",
            "School of Civil Engineering",
            "School of Electronics and Electrical Engineering",
            "School of Mechanical Engineering",
            "School of Bioengineering and Biosciences",
            "School of Design",
            "School of Hotel Management and Tourism",
            "School of Architecture and Design",
            "School of Fashion Design",
            "School of Journalism and Mass Communication",
            "School of Pharmaceutical Sciences",
            "School of Physical Education",
            "School of Law",
            "School of Agriculture",
            "School of Fine Arts",
            "School of Liberal Arts and Sciences",
            "School of Education"
        )

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (2005..currentYear).map { it.toString() }.toTypedArray()

        loadData()

        schoolEdittext.setOnClickListener {
            showAlertDialog("Select School", schools, true)
        }

        domainEditText.setOnClickListener {
            showAlertDialog("Select Batch", years, false)
        }

        changeButton.setOnClickListener {
            saveData()
        }
    }

    private fun showAlertDialog(title: String, items: Array<String>, isSchool: Boolean) {
        val adapter = ColoredArrayAdapter(this, android.R.layout.simple_list_item_single_choice, items, R.color.chocolate)
        val listView = ListView(this).apply {
            setAdapter(adapter)
            choiceMode = ListView.CHOICE_MODE_SINGLE
        }

        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle(title)
            .setView(listView)
            .setPositiveButton("Select") { dialog, _ ->
                val selectedItem = listView.getItemAtPosition(listView.checkedItemPosition) as String
                if (isSchool) {
                    schoolEdittext.setText(selectedItem)
                    selectedSchool = selectedItem
                } else {
                    domainEditText.setText(selectedItem)
                    selectedBatch = selectedItem
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun saveData() {
        val userUID = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val tasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()

        selectedSchool?.let {
            tasks.add(databaseReference.child(userUID).child("school").setValue(it))
        }
        selectedBatch?.let {
            tasks.add(databaseReference.child(userUID).child("batch").setValue(it))
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, UserProfileActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadData() {
        val userUID = auth.currentUser?.uid ?: return
        databaseReference.child(userUID).get().addOnSuccessListener { dataSnapshot ->
            val user = dataSnapshot.getValue(User::class.java)
            user?.let {
                schoolEdittext.setText(it.school)
                domainEditText.setText(it.batch)
            }
        }
    }

    class ColoredArrayAdapter(context: Context, resource: Int, private val items: Array<String>, private val textColor: Int)
        : ArrayAdapter<String>(context, resource, items) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent) as TextView
            view.setTextColor(ContextCompat.getColor(context, textColor))
            return view
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
