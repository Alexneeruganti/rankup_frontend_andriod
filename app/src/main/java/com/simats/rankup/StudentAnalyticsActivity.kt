package com.simats.rankup

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StudentAnalyticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_analytics)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        fetchStudentStats()
        setupList()
    }

    private fun fetchStudentStats() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)
        if (userId == -1) return

        com.simats.rankup.network.BackendApiService.api.getStudentStats(userId).enqueue(object : retrofit2.Callback<com.simats.rankup.network.StudentStatsResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.rankup.network.StudentStatsResponse>, response: retrofit2.Response<com.simats.rankup.network.StudentStatsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!
                    findViewById<android.widget.TextView>(R.id.tvOverallScore).text = "${stats.average_score.toInt()}%"
                    findViewById<android.widget.TextView>(R.id.tvAptitudeScoreHeader).text = "${stats.aptitude_score.toInt()}%"
                    findViewById<android.widget.TextView>(R.id.tvCodingScoreHeader).text = "${stats.coding_score.toInt()}%"
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.StudentStatsResponse>, t: Throwable) {
                // Ignore for now
            }
        })
    }

    private fun setupList() {
        val rvAnalytics = findViewById<RecyclerView>(R.id.rvAnalytics)
        rvAnalytics.layoutManager = LinearLayoutManager(this)

        // Get accepted class students from StudentManager
        val acceptedStudents = StudentManager.myClass
        
        // Map to AnalyticsStudent with mock stats (since StudentRequest doesn't have scores yet)
        val analyticsData = acceptedStudents.map { student ->
            StudentAnalyticsAdapter.AnalyticsStudent(
                name = student.name,
                regNo = student.regNo,
                status = "In Progress", // Default status
                trend = "IMPROVING",   // Default trend
                aptitudeScore = (60..95).random(), // Mock score
                codingScore = (50..98).random()    // Mock score
            )
        }

        val adapter = StudentAnalyticsAdapter(analyticsData)
        rvAnalytics.adapter = adapter
    }
}
