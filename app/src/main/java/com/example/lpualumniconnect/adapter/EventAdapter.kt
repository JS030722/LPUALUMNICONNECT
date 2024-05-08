package com.example.lpualumniconnect.adapter

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lpualumniconnect.R
import com.example.lpualumniconnect.datamodal.Event
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EventAdapter(private val events: List<Event>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageViewEvent: ImageView = view.findViewById(R.id.image_view_event)
        val textViewDescription: TextView = view.findViewById(R.id.description_event)
        val textViewDate: TextView = view.findViewById(R.id.date_event)
        val textViewTime: TextView = view.findViewById(R.id.time_event)
        val textViewUid: TextView = view.findViewById(R.id.uid_event)
        val frontCard: View = view.findViewById(R.id.front_card)
        val backCard: View = view.findViewById(R.id.back_card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.all_events_layout, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.textViewDescription.text = event.description
        holder.textViewDate.text = event.date
        holder.textViewTime.text = event.time
        holder.textViewUid.text = event.uid


        if (event.uid != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(event.uid!!)
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("name").getValue(String::class.java)
                    holder.textViewUid.text = userName ?: "Unknown User"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("EventAdapter", "Failed to read user name: ${error.toException()}")
                    holder.textViewUid.text = "Failed to load user"
                }
            })
        } else {
            holder.textViewUid.text = "No user ID"
        }


        if (event.photo != null) {
            val decodedByte = Base64.decode(event.photo, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
            holder.imageViewEvent.setImageBitmap(decodedImage)
        }


        holder.itemView.setOnLongClickListener {
            val showBack = holder.backCard.visibility == View.GONE
            val animatorOut = AnimatorInflater.loadAnimator(holder.itemView.context, if (showBack) R.animator.flip_in else R.animator.flip_out) as AnimatorSet
            val animatorIn = AnimatorInflater.loadAnimator(holder.itemView.context, if (showBack) R.animator.flip_out else R.animator.flip_in) as AnimatorSet

            animatorOut.setTarget(if (showBack) holder.frontCard else holder.backCard)
            animatorIn.setTarget(if (showBack) holder.backCard else holder.frontCard)

            animatorOut.start()
            animatorOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    holder.backCard.visibility = if (showBack) View.VISIBLE else View.GONE
                    holder.frontCard.visibility = if (showBack) View.GONE else View.VISIBLE
                    animatorIn.start()
                }
            })
            true
        }
    }

    override fun getItemCount(): Int = events.size
}