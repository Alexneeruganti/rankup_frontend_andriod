package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.ResumeReviewsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentResumeStatusActivity : AppCompatActivity() {

    private lateinit var rvResumeStatus: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var adapter: StudentResumeStatusAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_resume_status)

        rvResumeStatus = findViewById(R.id.rvResumeStatus)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadRequests()
    }

    private fun setupRecyclerView() {
        adapter = StudentResumeStatusAdapter(emptyList()) { _ ->
            // If they need to edit, launch the builder form again
            val intent = Intent(this, StudentResumeFormActivity::class.java)
            // intent.putExtra("TEMPLATE_ID", request.templateId) // Not available in backend response
            startActivity(intent)
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true // Show newest first
        layoutManager.stackFromEnd = true
        rvResumeStatus.layoutManager = layoutManager
        rvResumeStatus.adapter = adapter
    }

    private fun loadRequests() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val studentId = sharedPref.getInt("USER_ID", -1)
        
        if (studentId == -1) {
            Toast.makeText(this, "Error: Student ID not found. Please log in.", Toast.LENGTH_SHORT).show()
            rvResumeStatus.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
            return
        }

        BackendApiService.api.getStudentResumes(studentId).enqueue(object : Callback<ResumeReviewsResponse> {
            override fun onResponse(call: Call<ResumeReviewsResponse>, response: Response<ResumeReviewsResponse>) {
                if (response.isSuccessful && response.body()?.error == null) {
                    val resumes = response.body()?.resumes ?: emptyList()
                    if (resumes.isEmpty()) {
                        rvResumeStatus.visibility = View.GONE
                        layoutEmptyState.visibility = View.VISIBLE
                    } else {
                        rvResumeStatus.visibility = View.VISIBLE
                        layoutEmptyState.visibility = View.GONE
                        adapter.updateList(resumes)
                    }
                } else {
                    Toast.makeText(this@StudentResumeStatusActivity, "Failed to load resumes", Toast.LENGTH_SHORT).show()
                    rvResumeStatus.visibility = View.GONE
                    layoutEmptyState.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<ResumeReviewsResponse>, t: Throwable) {
                Toast.makeText(this@StudentResumeStatusActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                rvResumeStatus.visibility = View.GONE
                layoutEmptyState.visibility = View.VISIBLE
            }
        })
    }
}
