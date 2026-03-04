package com.simats.rankup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminContentAdapter(
    private var modules: List<LearningAdapter.LearningModule>,
    private val onDelete: (LearningAdapter.LearningModule) -> Unit
) : RecyclerView.Adapter<AdminContentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvModuleTitle)
        val tvType: TextView = view.findViewById(R.id.tvModuleType)
        val tvAuthor: TextView = view.findViewById(R.id.tvModuleAuthor)
        val tvSize: TextView = view.findViewById(R.id.tvModuleSize)
        val ivIcon: ImageView = view.findViewById(R.id.ivModuleIcon)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val module = modules[position]
        holder.tvTitle.text = module.title
        holder.tvType.text = module.type
        holder.tvAuthor.text = "By ${module.author}"
        holder.tvSize.text = module.size
        holder.ivIcon.setImageResource(module.iconResId)

        holder.btnDelete.setOnClickListener {
            onDelete(module)
        }
    }

    override fun getItemCount(): Int = modules.size
    
    fun updateData(newModules: List<LearningAdapter.LearningModule>) {
        modules = newModules
        notifyDataSetChanged()
    }
}
