package com.simats.rankup

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAnalyticsAdapter(
    private val students: List<AnalyticsStudent>
) : RecyclerView.Adapter<StudentAnalyticsAdapter.ViewHolder>() {

    data class AnalyticsStudent(
        val name: String,
        val regNo: String,
        val status: String, // "In Progress", "Placed", "Searching"
        val trend: String, // "IMPROVING", "DROPPING"
        val aptitudeScore: Int,
        val codingScore: Int
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatar: TextView = view.findViewById(R.id.tvAvatar)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvRegNo: TextView = view.findViewById(R.id.tvRegNo)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvTrend: TextView = view.findViewById(R.id.tvTrend)
        val tvAptitude: TextView = view.findViewById(R.id.tvAptitudeScore)
        val progressAptitude: ProgressBar = view.findViewById(R.id.progressAptitude)
        val tvCoding: TextView = view.findViewById(R.id.tvCodingScore)
        val progressCoding: ProgressBar = view.findViewById(R.id.progressCoding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_analytics, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]
        
        // Basic Info
        holder.tvName.text = student.name
        holder.tvRegNo.text = student.regNo
        holder.tvAvatar.text = getInitials(student.name)

        // Status Styling
        holder.tvStatus.text = student.status
        when (student.status) {
            "In Progress" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#1E88E5"))
                holder.tvStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E3F2FD"))
            }
            "Placed" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#00C853"))
                holder.tvStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
            }
            "Searching" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#FF6D00"))
                holder.tvStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF3E0"))
            }
        }

        // Trend Styling
        if (student.trend == "IMPROVING") {
            holder.tvTrend.text = "↗ IMPROVING"
            holder.tvTrend.setTextColor(Color.parseColor("#00C853"))
        } else {
            holder.tvTrend.text = "↘ DROPPING"
            holder.tvTrend.setTextColor(Color.RED)
        }

        // Scores
        holder.tvAptitude.text = "${student.aptitudeScore}%"
        holder.progressAptitude.progress = student.aptitudeScore
        holder.progressAptitude.progressTintList = ColorStateList.valueOf(Color.parseColor("#2962FF"))

        holder.tvCoding.text = "${student.codingScore}%"
        holder.progressCoding.progress = student.codingScore
        holder.progressCoding.progressTintList = ColorStateList.valueOf(Color.parseColor("#00C853"))
    }

    override fun getItemCount() = students.size

    private fun getInitials(name: String): String {
        return name.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
    }
}
