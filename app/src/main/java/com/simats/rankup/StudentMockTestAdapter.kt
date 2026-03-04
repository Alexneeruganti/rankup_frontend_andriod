package com.simats.rankup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.rankup.network.MockTestResponse

class StudentMockTestAdapter(
    private val tests: List<MockTestResponse>,
    private val onStartClick: (MockTestResponse) -> Unit
) : RecyclerView.Adapter<StudentMockTestAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTestTitle: TextView = view.findViewById(R.id.tvTestTitle)
        val tvTestDuration: TextView = view.findViewById(R.id.tvTestDuration)
        val btnStartTest: Button = view.findViewById(R.id.btnStartTest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mock_test, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val test = tests[position]
        holder.tvTestTitle.text = test.title
        holder.tvTestDuration.text = "Duration: ${test.duration_minutes} mins"

        holder.btnStartTest.setOnClickListener {
            onStartClick(test)
        }
    }

    override fun getItemCount() = tests.size
}
