package com.example.lpualumniconnect.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lpualumniconnect.R
import com.example.lpualumniconnect.datamodal.Experience

class ExperienceAdapter(val experiences: MutableList<Experience>) : RecyclerView.Adapter<ExperienceAdapter.ExperienceViewHolder>() {

    class ExperienceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val jobTitle: TextView = itemView.findViewById(R.id.jobTitle)
        private val company: TextView = itemView.findViewById(R.id.company)
        private val period: TextView = itemView.findViewById(R.id.period)
        private val description: TextView = itemView.findViewById(R.id.description)

        @SuppressLint("SetTextI18n")
        fun bind(experience: Experience) {
            jobTitle.text = experience.jobTitle
            company.text = experience.company
            period.text = "${experience.startDate} - ${experience.endDate}"
            description.text = experience.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperienceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_experience, parent, false)
        return ExperienceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExperienceViewHolder, position: Int) {
        holder.bind(experiences[position])
    }

    override fun getItemCount() = experiences.size
}