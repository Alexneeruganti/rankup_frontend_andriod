package com.simats.rankup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.rankup.network.AnnouncementResponse

class NotificationAdapter(private var announcements: List<AnnouncementResponse>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvNotificationTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvNotificationMessage)
        val tvTime: TextView = view.findViewById(R.id.tvNotificationTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = announcements[position]
        holder.tvTitle.text = item.title
        holder.tvMessage.text = item.message
        holder.tvTime.text = item.date_string
    }

    override fun getItemCount() = announcements.size

    fun updateData(newAnnouncements: List<AnnouncementResponse>) {
        announcements = newAnnouncements
        notifyDataSetChanged()
    }
}
