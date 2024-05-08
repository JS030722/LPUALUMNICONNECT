import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lpualumniconnect.R
import com.example.lpualumniconnect.adapter.UserAdapter
import com.example.lpualumniconnect.datamodal.User

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ChatFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private val userList = mutableListOf<User>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recyclerView = view.findViewById(R.id.chat_list_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = UserAdapter(requireContext(), userList)
        recyclerView.adapter = adapter
        return view
    }

    override fun onResume() {
        super.onResume()
        loadChattedUsers()
    }

    override fun onPause() {
        super.onPause()
        FirebaseDatabase.getInstance().reference.child("chats").removeEventListener(listener)
    }

    private val listener = object : com.google.firebase.database.ValueEventListener {
        override fun onDataChange(dataSnapshot: com.google.firebase.database.DataSnapshot) {
            userList.clear()
            val tempUserSet = mutableSetOf<String>()
            dataSnapshot.children.forEach {
                if (it.key!!.contains(FirebaseAuth.getInstance().currentUser!!.uid)) {
                    val otherUserId = it.key!!.replace(FirebaseAuth.getInstance().currentUser!!.uid, "")
                    if (otherUserId !in tempUserSet) {
                        tempUserSet.add(otherUserId)
                        fetchUserDetails(otherUserId)
                    }
                }
            }
        }

        override fun onCancelled(databaseError: com.google.firebase.database.DatabaseError) {

        }
    }

    private fun loadChattedUsers() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("chats")
        databaseReference.addValueEventListener(listener)
    }

    private fun fetchUserDetails(uid: String) {
        FirebaseDatabase.getInstance().getReference("users").child(uid)
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        if (it !in userList) {
                            userList.add(it)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {

                }
            })
    }
}