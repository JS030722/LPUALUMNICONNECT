package com.example.lpualumniconnect.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lpualumniconnect.R
import com.example.lpualumniconnect.datamodal.Chat

class ChatAdapter(
    private val context: Context,
    private val chats: List<Chat>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recent_messages_layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.bind(chat)
    }

    override fun getItemCount(): Int = chats.size

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageProfile: ImageView = itemView.findViewById(R.id.recent_iv)
        private val textName: TextView = itemView.findViewById(R.id.recent_name)
        private val textLastMessage: TextView = itemView.findViewById(R.id.recent_message)

        fun bind(chat: Chat) {
            textName.text = chat.userName ?: "Unknown User"
            textLastMessage.text = chat.lastMessage ?: "No messages"
            if (chat.profileImageUrl != null) {
                Glide.with(context).load(chat.profileImageUrl).into(imageProfile)
            } else {
                imageProfile.setImageResource(R.drawable.profile)
            }
        }
    }
}