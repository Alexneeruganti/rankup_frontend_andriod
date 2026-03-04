package com.simats.rankup

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.simats.rankup.network.ResumeReview

class StudentResumeStatusAdapter(
    private var requestList: List<ResumeReview>,
    private val onEditClick: (ResumeReview) -> Unit
) : RecyclerView.Adapter<StudentResumeStatusAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFacultyName: TextView = view.findViewById(R.id.tvFacultyName)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvFeedback: TextView = view.findViewById(R.id.tvFeedback)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_resume_status, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requestList[position]
        
        holder.tvFacultyName.text = "Sent to: ${request.mentor_name ?: "Unknown"}"
        
        // created_at is a string like "2023-10-25 10:00:00" from the backend
        holder.tvDate.text = request.created_at
        
        holder.tvStatus.text = request.status
        when (request.status.uppercase(Locale.getDefault())) {
            "PENDING" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800"))
                holder.tvFeedback.text = "Awaiting review from your mentor..."
                holder.btnEdit.visibility = View.GONE
            }
            "APPROVED" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                holder.tvFeedback.text = request.feedback?.takeIf { it.isNotEmpty() } ?: "Approved!"
                holder.btnEdit.visibility = View.GONE
            }
            "REJECTED" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#F44336"))
                holder.tvFeedback.text = request.feedback?.takeIf { it.isNotEmpty() } ?: "Changes requested."
                holder.btnEdit.visibility = View.VISIBLE
            }
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(request)
        }
    }

    override fun getItemCount() = requestList.size

    fun updateList(newList: List<ResumeReview>) {
        requestList = newList
        notifyDataSetChanged()
    }
}
