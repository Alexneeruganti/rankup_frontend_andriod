package com.simats.rankup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminRankingAdapter(private val students: List<StudentRank>) :
    RecyclerView.Adapter<AdminRankingAdapter.RankingViewHolder>() {

    data class StudentRank(
        val rank: Int,
        val name: String,
        val department: String,
        val points: Int
    )

    class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvDepartment: TextView = itemView.findViewById(R.id.tvDepartment)
        val tvPoints: TextView = itemView.findViewById(R.id.tvPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking_admin, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val student = students[position]
        holder.tvRank.text = student.rank.toString()
        holder.tvName.text = student.name
        holder.tvDepartment.text = student.department
        holder.tvPoints.text = student.points.toString()
        
        // Dynamic background tint for rank could be added here if needed
    }

    override fun getItemCount(): Int = students.size
}
