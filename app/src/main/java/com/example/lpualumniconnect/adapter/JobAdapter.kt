package com.example.lpualumniconnect.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lpualumniconnect.QuickLookProfileActivity
import com.example.lpualumniconnect.R
import com.example.lpualumniconnect.datamodal.Job

class JobAdapter(private val jobs: MutableList<Job>, private val onDeleteClick: (Job) -> Unit)
    : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    var showDelete = false

    inner class JobViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var jobTitleView: TextView = view.findViewById(R.id.job_title)
        private var jobLevelView: TextView = view.findViewById(R.id.job_level)
        private var jobDescriptionView: TextView = view.findViewById(R.id.job_description)
        private var jobCreatorView: TextView = view.findViewById(R.id.uid_name)
        var deleteButton: Button = view.findViewById(R.id.delete_button)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val job = jobs[position]
                    val intent = Intent(view.context, QuickLookProfileActivity::class.java).apply {
                        putExtra("userId", job.userId)
                    }
                    view.context.startActivity(intent)
                }
            }
            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(jobs[position])
                }
            }
        }

        fun bind(job: Job, showDeleteButton: Boolean) {
            jobTitleView.text = job.title
            jobLevelView.text = job.level
            jobDescriptionView.text = job.description
            jobCreatorView.text = "Created by: ${job.userName}"
            deleteButton.visibility = if (showDeleteButton) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.job_item, parent, false)
        return JobViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(jobs[position], showDelete)
    }

    override fun getItemCount(): Int = jobs.size

    fun toggleDeleteButtons() {
        showDelete = !showDelete
        notifyDataSetChanged()
    }
}