package com.example.lpualumniconnect.adapter

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lpualumniconnect.ChatActivity
import com.example.lpualumniconnect.QuickLookProfileActivity
import com.example.lpualumniconnect.R
import com.example.lpualumniconnect.UserProfileActivity
import com.example.lpualumniconnect.datamodal.MessageUserCover
import com.example.lpualumniconnect.datamodal.User
import com.google.android.material.card.MaterialCardView


class UserAdapter(private val context: Context, var userList: List<User>) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        var bioTextView: TextView = itemView.findViewById(R.id.bioTextView)
        var locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        var profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        var materialCardView: MaterialCardView = itemView.findViewById(R.id.materialCardView)
        var viewProfile: TextView = itemView.findViewById(R.id.back_viewProfile)
        var messageProfile: TextView = itemView.findViewById(R.id.back_messageProfile)
        var flipCardBack: ImageView = itemView.findViewById(R.id.back_flipCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_list_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.nameTextView.text = user.name
        holder.bioTextView.text = user.bio
        holder.locationTextView.text = user.location


        user.photo?.let {
            if (it.isNotEmpty()) {
                val imageBytes = Base64.decode(it, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.profileImageView.setImageBitmap(decodedImage)
            }
        } ?: holder.profileImageView.setImageResource(R.drawable.profile)

        holder.materialCardView.setOnClickListener {
            flipCard(holder, true)
        }

        holder.viewProfile.setOnClickListener {
            val intent = Intent(context, QuickLookProfileActivity::class.java).apply {
                putExtra("userId", user.uid)
            }
            context.startActivity(intent)
        }


        holder.messageProfile.setOnClickListener {
            val intent = Intent(context,ChatActivity::class.java)
            intent.putExtra("name",user.name)
            intent.putExtra("uid",user.uid)
            context.startActivity(intent)
        }

        holder.flipCardBack.setOnClickListener {
            flipCard(holder, false)
        }
    }

    private fun flipCard(holder: ViewHolder, toBack: Boolean) {
        val flipOut = AnimatorInflater.loadAnimator(context, R.animator.flip_out) as AnimatorSet
        val flipIn = AnimatorInflater.loadAnimator(context, R.animator.flip_in) as AnimatorSet
        flipOut.setTarget(holder.materialCardView)
        flipIn.setTarget(holder.materialCardView)

        flipOut.start()
        flipOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (toBack) {
                    holder.viewProfile.visibility = View.VISIBLE
                    holder.messageProfile.visibility = View.VISIBLE
                    holder.flipCardBack.visibility = View.VISIBLE
                    holder.nameTextView.visibility = View.GONE
                    holder.bioTextView.visibility = View.GONE
                    holder.locationTextView.visibility = View.GONE
                    holder.profileImageView.visibility = View.GONE
                } else {
                    holder.viewProfile.visibility = View.GONE
                    holder.messageProfile.visibility = View.GONE
                    holder.flipCardBack.visibility = View.GONE
                    holder.nameTextView.visibility = View.VISIBLE
                    holder.bioTextView.visibility = View.VISIBLE
                    holder.locationTextView.visibility = View.VISIBLE
                    holder.profileImageView.visibility = View.VISIBLE
                }
                flipIn.start()
            }
        })
    }

    override fun getItemCount(): Int = userList.size
}