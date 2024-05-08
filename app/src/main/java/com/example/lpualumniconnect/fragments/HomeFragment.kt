package com.example.lpualumniconnect.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lpualumniconnect.R
import com.example.lpualumniconnect.adapter.EventAdapter
import com.example.lpualumniconnect.datamodal.Event
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventAdapter
    private val events = mutableListOf<Event>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragement_home, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = EventAdapter(events)
        recyclerView.adapter = adapter
        loadEvents()
        return view
    }

    private fun loadEvents() {
        val dbRef = FirebaseDatabase.getInstance().getReference("events")
        dbRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.mapNotNullTo(events) {
                    val event = it.getValue(Event::class.java)
                    Log.d("HomeFragment", "Loaded event: ${event?.description}, Photo URL: ${event?.photo}")
                    event
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "Failed to load events: ${databaseError.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
