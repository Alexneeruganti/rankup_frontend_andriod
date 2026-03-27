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

        fetchClassAnalytics()
    }

    private fun fetchClassAnalytics() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val facultyId = sharedPref.getInt("USER_ID", -1)
        if (facultyId == -1) {
             android.widget.Toast.makeText(this, "Session Expired", android.widget.Toast.LENGTH_SHORT).show()
             return
        }

        val rvAnalytics = findViewById<RecyclerView>(R.id.rvAnalytics)
        rvAnalytics.layoutManager = LinearLayoutManager(this)

        com.simats.rankup.network.BackendApiService.api.getClassStudents(facultyId).enqueue(object : retrofit2.Callback<com.simats.rankup.network.ClassStudentsResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.rankup.network.ClassStudentsResponse>, response: retrofit2.Response<com.simats.rankup.network.ClassStudentsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val students = response.body()!!.students ?: emptyList()
                    
                    // Map to AnalyticsStudent using real scores from backend
                    val analyticsData = students.map { student ->
                        StudentAnalyticsAdapter.AnalyticsStudent(
                            name = student.student_name,
                            regNo = student.register_number ?: "N/A",
                            status = if (student.overall_score >= 75) "Excellent" else if (student.overall_score >= 50) "Good" else "Needs Improvement",
                            trend = if (student.overall_score >= 60) "IMPROVING" else "STABLE",
                            aptitudeScore = student.aptitude_score.toInt(),
                            codingScore = student.coding_score.toInt()
                        )
                    }

                    val adapter = StudentAnalyticsAdapter(analyticsData)
                    rvAnalytics.adapter = adapter
                    
                    // Update header average if needed (optional enrichment)
                    if (analyticsData.isNotEmpty()) {
                        val avgOverall = analyticsData.map { it.aptitudeScore + it.codingScore }.average() / 2
                        val avgAptitude = analyticsData.map { it.aptitudeScore }.average()
                        val avgCoding = analyticsData.map { it.codingScore }.average()
                        
                        findViewById<android.widget.TextView>(R.id.tvOverallScore).text = "${avgOverall.toInt()}%"
                        findViewById<android.widget.TextView>(R.id.tvAptitudeScoreHeader).text = "${avgAptitude.toInt()}%"
                        findViewById<android.widget.TextView>(R.id.tvCodingScoreHeader).text = "${avgCoding.toInt()}%"
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.ClassStudentsResponse>, t: Throwable) {
                android.widget.Toast.makeText(this@StudentAnalyticsActivity, "Failed to load analytics", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
    }
}
