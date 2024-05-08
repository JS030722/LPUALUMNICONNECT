package com.example.lpualumniconnect.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lpualumniconnect.R
import com.example.lpualumniconnect.datamodal.Message
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(val context: Context, val messageList: ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val ITEM_RECEIVE = 1
    val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_RECEIVE) {
            val view: View = LayoutInflater.from(context).inflate(R.layout.message_recieve, parent, false)
            ReceivedViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(context).inflate(R.layout.message_sent, parent, false)
            SentViewHolder(view)
        }
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentMessage = messageList[position]
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateString = sdf.format(Date(currentMessage.timeStamp ?: System.currentTimeMillis()))
        if (holder is SentViewHolder) {
            holder.sentMessage.text = currentMessage.message
            holder.sentTimeStamp.text = dateString
        } else if (holder is ReceivedViewHolder) {
            holder.receivedMessage.text = currentMessage.message
            holder.receivedTimeStamp.text = dateString
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (FirebaseAuth.getInstance().currentUser?.uid == currentMessage.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage: TextView = itemView.findViewById(R.id.text_sent_message)
        val sentTimeStamp: TextView = itemView.findViewById(R.id.text_sent_time)

    }

    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedMessage: TextView = itemView.findViewById(R.id.text_receive_message)
        val receivedTimeStamp: TextView = itemView.findViewById(R.id.text_receive_time)

    }
}