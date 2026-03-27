package com.simats.rankup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class RankingsAdapter(private val students: List<StudentRank>) :
    RecyclerView.Adapter<RankingsAdapter.ViewHolder>() {

    data class StudentRank(val rank: Int, val name: String, val department: String, val points: Int, val profilePic: String?)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tvRank)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDepartment: TextView = view.findViewById(R.id.tvDepartment)
        val tvPoints: TextView = view.findViewById(R.id.tvPoints)
        val imgAvatar: android.widget.ImageView = view.findViewById(R.id.imgAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]
        holder.tvRank.text = "#${student.rank}"
        holder.tvName.text = student.name
        holder.tvDepartment.text = student.department.uppercase(Locale.getDefault())
        holder.tvPoints.text = student.points.toString()
        
        if (!student.profilePic.isNullOrEmpty()) {
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(com.simats.rankup.network.BackendApiService.getFullUrl(student.profilePic))
                .placeholder(R.drawable.ic_medal)
                .circleCrop()
                .into(holder.imgAvatar)
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_medal)
        }
    }

    override fun getItemCount() = students.size
}
