package com.simats.rankup

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.rankup.network.JoinRequest

class StudentRequestAdapter(
    private var requests: List<JoinRequest>,
    private val onAction: (JoinRequest, Boolean) -> Unit, // Boolean: true = Accept, false = Decline/Remove
    private val onProfileClick: (JoinRequest) -> Unit
) : RecyclerView.Adapter<StudentRequestAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvDetails: TextView = view.findViewById(R.id.tvDetails)
        val btnAccept: TextView = view.findViewById(R.id.btnAccept)
        val btnDecline: TextView = view.findViewById(R.id.btnDecline)
        val imgAvatar: View = view.findViewById(R.id.imgAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_request, parent, false)
        return ViewHolder(view)
    }

    private var isAcceptedList: Boolean = false

    fun updateData(newRequests: List<JoinRequest>, isAcceptedList: Boolean = false) {
        requests = newRequests
        this.isAcceptedList = isAcceptedList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val req = requests[position]
        holder.tvName.text = req.student_name
        
        // Show ID instead of generic year since we don't have year right now
        holder.tvDetails.text = "${req.department} • ID: ${req.student_id}"
        
        // Profile Click
        holder.itemView.setOnClickListener { onProfileClick(req) }
        holder.imgAvatar.setOnClickListener { onProfileClick(req) }

        val actionLayout = holder.btnAccept.parent as View
        actionLayout.visibility = View.VISIBLE

        if (isAcceptedList) {
            // Accepted List -> Show Remove Option
            holder.btnAccept.text = "Remove"
            holder.btnAccept.setBackgroundResource(R.drawable.bg_button_white) // Or red style
            holder.btnAccept.background.setTint(Color.parseColor("#FFEBEE")) // Light Red
            holder.btnAccept.setTextColor(Color.RED)
            
            holder.btnDecline.visibility = View.GONE
            
            holder.tvType.text = if (req.request_type == "MENTORSHIP") "MENTEE" else "CLASS MEMBER"
            holder.tvType.setTextColor(Color.parseColor("#4CAF50")) // Green for status
        } else {
            // Pending Request -> Show Accept/Decline
            holder.btnDecline.visibility = View.VISIBLE
            
            if (req.request_type == "MENTORSHIP") {
                holder.tvType.text = "MENTORSHIP REQUEST"
                holder.tvType.setTextColor(Color.parseColor("#1E88E5")) // Blue
                holder.btnAccept.text = "Accept Mentee"
                holder.btnAccept.setBackgroundResource(R.drawable.bg_button_faculty)
                holder.btnAccept.background.setTintList(null) // Reset tint
                holder.btnAccept.setTextColor(Color.WHITE)
            } else {
                holder.tvType.text = "CLASS ENROLLMENT"
                holder.tvType.setTextColor(Color.parseColor("#1565C0")) // Darker Blue
                holder.btnAccept.text = "Approve to Class"
                holder.btnAccept.setBackgroundResource(R.drawable.bg_button_faculty)
                holder.btnAccept.background.setTintList(null) // Reset tint
                holder.btnAccept.setTextColor(Color.WHITE)
            }
        }

        holder.btnAccept.setOnClickListener { 
            if (isAcceptedList) {
                onAction(req, false) // Treat Remove as negative action
            } else {
                onAction(req, true) // Accept
            }
        }
        
        holder.btnDecline.setOnClickListener { onAction(req, false) }
    }
    
    override fun getItemCount() = requests.size
}
