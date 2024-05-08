package com.example.lpualumniconnect.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lpualumniconnect.R
import com.example.lpualumniconnect.adapter.JobAdapter
import com.example.lpualumniconnect.datamodal.Job
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentCreateJob : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JobAdapter
    private val jobs = mutableListOf<Job>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var fabMain: FloatingActionButton
    private lateinit var fabCreate: FloatingActionButton
    private lateinit var fabDelete: FloatingActionButton

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_create_job, container, false)
        setupUI(view)
        setupRecyclerView(view)
        setupDatabase()
        auth = FirebaseAuth.getInstance()
        return view
    }

    private fun setupUI(view: View) {
        fabMain = view.findViewById(R.id.fab_main)
        fabCreate = view.findViewById(R.id.fab_create_job)
        fabDelete = view.findViewById(R.id.fab_delete_jobs)

        fabMain.setOnClickListener { toggleFabMenu() }
        fabCreate.setOnClickListener { fetchUserNameAndShowCreateJobDialog() }
        fabDelete.setOnClickListener { adapter.toggleDeleteButtons() }
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = JobAdapter(jobs) { job -> databaseReference.child(job.id).removeValue() }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun setupDatabase() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        databaseReference = FirebaseDatabase.getInstance().getReference("jobs")

        databaseReference.orderByChild("userId").equalTo(currentUserUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    jobs.clear()
                    dataSnapshot.children.mapNotNullTo(jobs) { it.getValue(Job::class.java) }
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Database error", databaseError.toException())
                }
            })
    }


    private fun toggleFabMenu() {
        if (fabCreate.visibility == View.GONE) {
            fabCreate.visibility = View.VISIBLE
            fabDelete.visibility = View.VISIBLE
        } else {
            fabCreate.visibility = View.GONE
            fabDelete.visibility = View.GONE
        }
    }

    private fun fetchUserNameAndShowCreateJobDialog() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users/$userId")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("name").value.toString()
                showCreateJobDialog(userName)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to fetch user name", error.toException())
            }
        })
    }

    private fun showCreateJobDialog(userName: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_job, null)
        val jobNameInput = dialogView.findViewById<EditText>(R.id.editTextJobName)
        val jobLevelInput = dialogView.findViewById<EditText>(R.id.editTextJobLevel)
        val jobDescriptionInput = dialogView.findViewById<EditText>(R.id.editTextJobDescription)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Create Job")
            .setPositiveButton("Create") { dialog, which ->
                val jobName = jobNameInput.text.toString()
                val jobLevel = jobLevelInput.text.toString()
                val jobDescription = jobDescriptionInput.text.toString()
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                createJob(jobName, jobLevel, jobDescription, userId, userName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createJob(jobName: String, jobLevel: String, jobDescription: String, userId: String, userName: String) {
        val key = databaseReference.push().key ?: return
        val newJob = Job(id = key, title = jobName, level = jobLevel, description = jobDescription, userId = userId, userName = userName)
        databaseReference.child(key).setValue(newJob)
    }
}