package com.simats.rankup

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.rankup.network.ResumeReview
import java.util.Locale

class FacultyResumeReviewAdapter(
    private var requestList: List<ResumeReview>,
    private val onItemClick: (ResumeReview) -> Unit
) : RecyclerView.Adapter<FacultyResumeReviewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStudentName: TextView = view.findViewById(R.id.tvStudentName)
        val tvJobTitle: TextView = view.findViewById(R.id.tvJobTitle)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resume_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requestList[position]
        
        holder.tvStudentName.text = request.student_name ?: "Student"
        holder.tvJobTitle.text = request.department ?: "Department"
        
        holder.tvStatus.text = request.status
        when (request.status.uppercase(Locale.getDefault())) {
            "PENDING" -> holder.tvStatus.setTextColor(Color.parseColor("#FF9800"))
            "APPROVED" -> holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            "REJECTED" -> holder.tvStatus.setTextColor(Color.parseColor("#F44336"))
        }

        holder.itemView.setOnClickListener {
            onItemClick(request)
        }
    }

    override fun getItemCount() = requestList.size

    fun updateList(newList: List<ResumeReview>) {
        requestList = newList
        notifyDataSetChanged()
    }
}
