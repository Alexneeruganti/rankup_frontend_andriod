package com.simats.rankup

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserManagementAdapter(
    private var userList: List<User>,
    private val onResetPasswordClick: (User) -> Unit,
    private val onBlockUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserManagementAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProfileInitial: TextView = itemView.findViewById(R.id.tvProfileInitial)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvUserId: TextView = itemView.findViewById(R.id.tvUserId)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvPlacementStatus: TextView = itemView.findViewById(R.id.tvPlacementStatus)

        // Student Actions
        val layoutStudentActions: LinearLayout = itemView.findViewById(R.id.layoutStudentActions)
        val btnResetPasswordStudent: LinearLayout = itemView.findViewById(R.id.btnResetPasswordStudent)
        val btnBlockUserStudent: LinearLayout = itemView.findViewById(R.id.btnBlockUserStudent)
        val tvBlockStudent: TextView = itemView.findViewById(R.id.tvBlockStudent)

        // Faculty Actions
        val layoutFacultyActions: LinearLayout = itemView.findViewById(R.id.layoutFacultyActions)
        val btnProfileFaculty: LinearLayout = itemView.findViewById(R.id.btnProfileFaculty)
        val btnResetPasswordFaculty: LinearLayout = itemView.findViewById(R.id.btnResetPasswordFaculty)
        val btnBlockUserFaculty: LinearLayout = itemView.findViewById(R.id.btnBlockUserFaculty)
        val tvBlockFaculty: TextView = itemView.findViewById(R.id.tvBlockFaculty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_management, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.tvUserName.text = user.name
        holder.tvUserEmail.text = user.email

        // Initials
        if (user.name.isNotEmpty()) {
            holder.tvProfileInitial.text = user.name.first().uppercase()
        }

        // Status Binding
        if (user.status == "Active") {
            holder.tvStatus.text = "ACTIVE"
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")) // Green
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_active)
            
            holder.tvBlockStudent.text = "Block User"
            holder.tvBlockStudent.setTextColor(Color.parseColor("#D32F2F")) // Red
            holder.btnBlockUserStudent.setBackgroundResource(R.drawable.bg_action_block)

            holder.tvBlockFaculty.text = "Block"
            holder.tvBlockFaculty.setTextColor(Color.parseColor("#D32F2F")) // Red
            holder.btnBlockUserFaculty.setBackgroundResource(R.drawable.bg_action_block)
        } else {
            holder.tvStatus.text = "BLOCKED"
            holder.tvStatus.setTextColor(Color.parseColor("#D32F2F")) // Red
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_blocked)
            
            holder.tvBlockStudent.text = "Unblock User"
            holder.tvBlockStudent.setTextColor(Color.parseColor("#2E7D32")) // Green
            holder.btnBlockUserStudent.setBackgroundResource(R.drawable.bg_status_active)

            holder.tvBlockFaculty.text = "Unblock"
            holder.tvBlockFaculty.setTextColor(Color.parseColor("#2E7D32")) // Green
            holder.btnBlockUserFaculty.setBackgroundResource(R.drawable.bg_status_active)
        }

        // Placement Status & Role specific UI
        if (user.role.equals("Student", ignoreCase = true)) {
            holder.tvUserId.text = user.registerNumber ?: user.id
            holder.layoutStudentActions.visibility = View.VISIBLE
            holder.layoutFacultyActions.visibility = View.GONE
            
            if (user.placementStatus != null) {
                holder.tvPlacementStatus.visibility = View.VISIBLE
                holder.tvPlacementStatus.text = user.placementStatus
                
                when (user.placementStatus) {
                    "Placed" -> holder.tvPlacementStatus.setBackgroundResource(R.drawable.bg_circular_light_purple)
                    "In Progress" -> holder.tvPlacementStatus.setBackgroundResource(R.drawable.bg_circular_light_blue)
                    else -> holder.tvPlacementStatus.setBackgroundResource(R.drawable.bg_circular_light_grey)
                }
            } else {
                holder.tvPlacementStatus.visibility = View.GONE
            }
        } else {
            holder.tvUserId.text = user.registerNumber ?: (if (user.department.isNotEmpty()) user.department else user.id)
            holder.tvPlacementStatus.visibility = View.GONE
            holder.layoutStudentActions.visibility = View.GONE
            holder.layoutFacultyActions.visibility = View.VISIBLE
        }

        holder.btnResetPasswordStudent.setOnClickListener { onResetPasswordClick(user) }
        holder.btnBlockUserStudent.setOnClickListener { onBlockUserClick(user) }

        holder.btnResetPasswordFaculty.setOnClickListener { onResetPasswordClick(user) }
        holder.btnBlockUserFaculty.setOnClickListener { onBlockUserClick(user) }
        
        holder.btnProfileFaculty.setOnClickListener {
            // Optional: Handle faculty profile click
        }
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }
}
