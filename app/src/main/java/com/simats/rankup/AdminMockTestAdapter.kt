package com.simats.rankup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.rankup.network.MockTestResponse

class AdminMockTestAdapter(
    private val testsList: MutableList<MockTestResponse>,
    private val onDeleteClicked: (MockTestResponse, Int) -> Unit
) : RecyclerView.Adapter<AdminMockTestAdapter.MockTestViewHolder>() {

    class MockTestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTestTitle: TextView = view.findViewById(R.id.tvTestTitle)
        val tvDurationInfo: TextView = view.findViewById(R.id.tvDurationInfo)
        val tvDateCreated: TextView = view.findViewById(R.id.tvDateCreated)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MockTestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_mock_test, parent, false)
        return MockTestViewHolder(view)
    }

    override fun onBindViewHolder(holder: MockTestViewHolder, position: Int) {
        val testDetails = testsList[position]
        holder.tvTestTitle.text = testDetails.title
        holder.tvDurationInfo.text = "${testDetails.duration_minutes} mins"
        
        val dateString = testDetails.created_at ?: "Unknown Date"
        holder.tvDateCreated.text = "Generated $dateString"

        holder.btnDelete.setOnClickListener {
            onDeleteClicked(testDetails, position)
        }
    }

    override fun getItemCount() = testsList.size
    
    fun removeAt(position: Int) {
        if (position in 0 until testsList.size) {
            testsList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, testsList.size)
        }
    }
}
