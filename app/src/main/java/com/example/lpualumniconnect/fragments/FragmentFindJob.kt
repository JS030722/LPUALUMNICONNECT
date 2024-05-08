package com.example.lpualumniconnect.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lpualumniconnect.R
import com.example.lpualumniconnect.adapter.JobAdapter
import com.example.lpualumniconnect.datamodal.Job
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentFindJob : Fragment(){

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JobAdapter
    private var jobs = mutableListOf<Job>()
    private var filteredJobs = mutableListOf<Job>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_find_job, container, false)
        setupRecyclerView(view)
        setupSearchView(view)
        return view
    }

        private fun setupRecyclerView(view: View) {
            recyclerView = view.findViewById(R.id.recyclerView_find_jobs)
            adapter = JobAdapter(filteredJobs) {}
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            fetchJobs()
        }

        private fun setupSearchView(view: View) {
            searchView = view.findViewById(R.id.search_view)

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    filterJobs(newText ?: "")
                    return true
                }
            })
        }

        private fun fetchJobs() {
            val databaseReference = FirebaseDatabase.getInstance().getReference("jobs")
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    jobs.clear()
                    dataSnapshot.children.mapNotNullTo(jobs) { it.getValue(Job::class.java) }
                    filteredJobs.clear()
                    filteredJobs.addAll(jobs)
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }

        private fun filterJobs(query: String) {
            filteredJobs.clear()
            if (query.isEmpty()) {
                filteredJobs.addAll(jobs)
            } else {
                filteredJobs.addAll(jobs.filter {
                    it.title.contains(query, ignoreCase = true)
                })
            }
            adapter.notifyDataSetChanged()
        }
    }
