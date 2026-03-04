package com.simats.rankup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LearningAdapter(private val modules: List<LearningModule>) :
    RecyclerView.Adapter<LearningAdapter.ViewHolder>() {

    data class LearningModule(
        val title: String,
        val type: String, // "Notes" or "Lecture"
        val author: String,
        val size: String,
        val iconResId: Int,
        val url: String? = null,
        val id: Int = -1
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgIcon: ImageView = view.findViewById(R.id.imgModuleIcon)
        val tvTitle: TextView = view.findViewById(R.id.tvModuleTitle)
        val tvType: TextView = view.findViewById(R.id.tvModuleType)
        val tvDetails: TextView = view.findViewById(R.id.tvModuleDetails)
        val btnDownload: android.widget.ImageButton = view.findViewById(R.id.btnDownload)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_learning_module, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val module = modules[position]
        holder.tvTitle.text = module.title
        holder.tvType.text = module.type
        
        if (module.type == "VIDEO") {
            holder.tvDetails.text = "${module.author} • ${module.size}"
            holder.tvDetails.setTextColor(android.graphics.Color.parseColor("#1C52E6"))
            holder.btnDownload.setImageResource(R.drawable.ic_play)
            holder.btnDownload.setOnClickListener {
                if (!module.url.isNullOrEmpty()) {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(module.url))
                        holder.itemView.context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(holder.itemView.context, "Invalid Video Link", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    android.widget.Toast.makeText(holder.itemView.context, "No Link Provided", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            holder.tvDetails.text = "${module.author} • ${module.size}"
            holder.tvDetails.setTextColor(android.graphics.Color.parseColor("#9E9E9E"))
            holder.btnDownload.setImageResource(R.drawable.ic_download)
            holder.btnDownload.setOnClickListener {
                android.widget.Toast.makeText(holder.itemView.context, "Downloading PDF...", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        holder.imgIcon.setImageResource(module.iconResId)
    }

    override fun getItemCount() = modules.size
}
