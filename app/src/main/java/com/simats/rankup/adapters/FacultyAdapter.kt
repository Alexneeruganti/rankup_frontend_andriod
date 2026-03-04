package com.simats.rankup.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.simats.rankup.R
import com.simats.rankup.network.FacultyMember

class FacultyAdapter(
    private var facultyList: List<FacultyMember>,
    private val onRequestClick: (FacultyMember, String) -> Unit
) : RecyclerView.Adapter<FacultyAdapter.FacultyViewHolder>() {

    class FacultyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        val tvFacultyName: TextView = itemView.findViewById(R.id.tvFacultyName)
        val tvDepartment: TextView = itemView.findViewById(R.id.tvDepartment)
        val tvFacultyId: TextView = itemView.findViewById(R.id.tvFacultyId)
        val btnMentorship: AppCompatButton = itemView.findViewById(R.id.btnMentorship)
        val btnJoinClass: AppCompatButton = itemView.findViewById(R.id.btnJoinClass)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacultyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faculty, parent, false)
        return FacultyViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacultyViewHolder, position: Int) {
        val faculty = facultyList[position]

        // Set Text Data
        holder.tvFacultyName.text = faculty.name
        holder.tvDepartment.text = faculty.department
        holder.tvFacultyId.text = "ID: ${faculty.register_number ?: faculty.id}"

        // Create a basic 1-3 letter avatar
        val initials = faculty.name
            .split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it[0].uppercase() }
        holder.tvAvatar.text = if (initials.isNotEmpty()) initials else "F"

        // Interactions & Animations
        holder.btnMentorship.applyClickAnimation()
        holder.btnMentorship.setOnClickListener {
            onRequestClick(faculty, "MENTORSHIP")
        }

        holder.btnJoinClass.applyClickAnimation()
        holder.btnJoinClass.setOnClickListener {
            onRequestClick(faculty, "CLASS")
        }
    }

    override fun getItemCount(): Int {
        return facultyList.size
    }

    @android.annotation.SuppressLint("ClickableViewAccessibility")
    private fun View.applyClickAnimation() {
        this.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false // allow click listener to still trigger
        }
    }
    fun updateData(newList: List<FacultyMember>) {
        facultyList = newList
        notifyDataSetChanged()
    }
}
