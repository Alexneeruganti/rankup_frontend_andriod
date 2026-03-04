package com.simats.rankup

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class AdminLeaderboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_leaderboard)

        // Back Button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Quick Actions
        findViewById<Button>(R.id.btnResetAll).setOnClickListener {
            LeaderboardManager.resetRankings(this)
            updateTopStudentsList()
            Toast.makeText(this, "All rankings reset!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnResetMonthly).setOnClickListener {
            LeaderboardManager.resetMonthlyRankings(this)
            updateTopStudentsList()
            Toast.makeText(this, "Monthly rankings reset!", Toast.LENGTH_SHORT).show()
        }


        // RecyclerView Setup
        val rvTopStudents = findViewById<RecyclerView>(R.id.rvTopStudents)
        rvTopStudents.layoutManager = LinearLayoutManager(this)
        
        // Initial Data Load
        updateTopStudentsList()

        // Settings - Enable Public View
        val switchPublicView = findViewById<android.widget.Switch>(R.id.switchPublicView)
        // Initialize state
        switchPublicView.isChecked = LeaderboardManager.isPublicViewEnabled(this)
        
        switchPublicView.setOnCheckedChangeListener { _, isChecked ->
            LeaderboardManager.setPublicViewEnabled(this, isChecked)
            val status = if (isChecked) "visible" else "hidden"
            Toast.makeText(this, "Leaderboard is now $status", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTopStudentsList() {
        val rvTopStudents = findViewById<RecyclerView>(R.id.rvTopStudents)
        // Get data from Manager
        val allStudents = LeaderboardManager.getRankings(this)
        // Map to Adapter Model (Top 5)
        val adapterData = allStudents.take(5).map { 
            AdminRankingAdapter.StudentRank(it.rank, it.name, it.department, it.points)
        }
        rvTopStudents.adapter = AdminRankingAdapter(adapterData)
    }
}


