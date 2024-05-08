package com.example.lpualumniconnect

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lpualumniconnect.adapter.UserAdapter
import com.example.lpualumniconnect.datamodal.FilterType
import com.example.lpualumniconnect.datamodal.MessageUserCover
import com.example.lpualumniconnect.datamodal.User

import com.example.lpualumniconnect.fragments.FilterDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatHomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private val userList = mutableListOf<User>()

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private var userListener: ValueEventListener? = null

    @SuppressLint("MissingInflatedId", "DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_home)
        recyclerView = findViewById(R.id.all_chat_recycler_view)


        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            var newVisibility = visibility
            newVisibility = visibility or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
            window.decorView.systemUiVisibility = newVisibility
        }


        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        findViewById<MaterialButton>(R.id.batch_btn).setOnClickListener {
            showFilter(FilterType.BATCH)
        }

        findViewById<MaterialButton>(R.id.school_btn).setOnClickListener {
            showFilter(FilterType.SCHOOL)
        }

        findViewById<MaterialButton>(R.id.location_btn).setOnClickListener {
            showFilter(FilterType.LOCATION)
        }

        findViewById<MaterialButton>(R.id.skill_btn).setOnClickListener {
            showFilter(FilterType.SKILLS)
        }

        findViewById<MaterialButton>(R.id.company_btn).setOnClickListener {
            showFilter(FilterType.POSITION)
        }


        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(this, userList)
        recyclerView.adapter = adapter

        val searchView = findViewById<SearchView>(R.id.search_View)
        val id = searchView.context.resources.getIdentifier("android:id/search_src_text", null, null)
        val textView = searchView.findViewById(id) as TextView
        textView.setTextColor(Color.BLACK)


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val selectedFilters = emptySet<String>()
                filterUsers(newText, selectedFilters)
                return true
            }
        })


        fetchUsers()
    }

    private fun filterUsers(query: String?, selectedFilters: Set<String>) {

        val filteredUsers = if (!query.isNullOrBlank()) {
            userList.filter { it.name?.contains(query, ignoreCase = true) ?: false }
        } else {
            applyFilters(FilterType.BATCH, selectedFilters)
            if (selectedFilters.isEmpty()) userList.toList() else emptyList()
        }
        adapter.userList = ArrayList(filteredUsers)
        adapter.notifyDataSetChanged()
    }

    private fun fetchUsers() {
        val currentUserUid = mAuth.currentUser?.uid
        val databaseReference = mDbRef.child("users")

        userListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                dataSnapshot.children.forEach {
                    val user = it.getValue(User::class.java)
                    if (user?.uid != currentUserUid) {
                        userList.add(user!!)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        databaseReference.addValueEventListener(userListener!!)
    }


    private fun showFilter(filterType: FilterType) {
        CoroutineScope(Dispatchers.IO).launch {
            val attributes = when (filterType) {
                FilterType.BATCH -> listOf("batch")
                FilterType.SCHOOL -> listOf("school")
                FilterType.LOCATION -> listOf("location")
                FilterType.SKILLS -> listOf("skill1", "skill2", "skill3")
                FilterType.POSITION -> listOf("position")
                else -> listOf()
            }

            val uniqueAttributes = mutableSetOf<String>()
            for (attribute in attributes) {
                val snapshot = mDbRef.child("users").get().await()
                uniqueAttributes.addAll(
                    snapshot.children.mapNotNull { it.child(attribute).getValue(String::class.java) }
                )
            }

            if (uniqueAttributes.isNotEmpty()) {
                launch(Dispatchers.Main) {
                    val dialog = FilterDialogFragment(filterType, uniqueAttributes.toList()) { selectedFilters ->
                        applyFilters(filterType, selectedFilters)
                    }
                    dialog.show(supportFragmentManager, "FilterDialogFragment")
                }
            }
        }
    }


    private fun applyFilters(filterType: FilterType, selectedFilters: Set<String>) {
        val filteredUsers = userList.filter {
            when (filterType) {
                FilterType.BATCH -> selectedFilters.contains(it.batch)
                FilterType.SCHOOL -> selectedFilters.contains((it.school))
                FilterType.LOCATION -> selectedFilters.contains(it.location)
                FilterType.SKILLS -> selectedFilters.contains(it.skill1)
                FilterType.NAME -> selectedFilters.contains(it.name)
                FilterType.POSITION -> selectedFilters.contains(it.position)

            }
        }

        adapter.userList = filteredUsers as ArrayList<User>
        adapter.notifyDataSetChanged()
    }

    override fun onStart() {
        super.onStart()
        fetchUsers()
    }

    override fun onStop() {
        super.onStop()
        userListener?.let { mDbRef.removeEventListener(it) }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}
