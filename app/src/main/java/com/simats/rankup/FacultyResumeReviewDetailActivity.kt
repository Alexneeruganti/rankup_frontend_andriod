package com.simats.rankup

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

import android.net.Uri
import android.content.Intent
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.ResumeReview
import com.simats.rankup.network.ReviewResumeRequest
import com.simats.rankup.network.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FacultyResumeReviewDetailActivity : AppCompatActivity() {

    private lateinit var request: ResumeReview
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_resume_review_detail)

        val requestJson = intent.getStringExtra("REQUEST_DATA")
        if (requestJson == null) {
            Toast.makeText(this, "Error loading request details.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        request = gson.fromJson(requestJson, ResumeReview::class.java)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        populateData()
        setupButtons()
    }

    private fun populateData() {
        val tvStudentNameDetail = findViewById<TextView>(R.id.tvStudentNameDetail)
        val tvDepartmentDetail = findViewById<TextView>(R.id.tvDepartmentDetail)
        val btnViewPdf = findViewById<Button>(R.id.btnViewPdf)

        tvStudentNameDetail.text = request.student_name ?: "Student Name"
        tvDepartmentDetail.text = request.department ?: "Department"

        btnViewPdf.setOnClickListener {
            val url = BackendApiService.getFullUrl(request.resume_url)
            if (!url.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } else {
                Toast.makeText(this, "No valid resume URL found", Toast.LENGTH_SHORT).show()
            }
        }

        val etFeedback = findViewById<EditText>(R.id.etFeedback)
        etFeedback.setText(request.feedback)
    }

    private fun setupButtons() {
        val btnAllow = findViewById<Button>(R.id.btnAllow)
        val btnChange = findViewById<Button>(R.id.btnChange)
        val etFeedback = findViewById<EditText>(R.id.etFeedback)

        btnAllow.setOnClickListener {
            val feedback = etFeedback.text.toString().trim().ifEmpty { "Looks great! Approved." }
            submitReview("Approved", feedback)
        }

        btnChange.setOnClickListener {
            val feedback = etFeedback.text.toString().trim()
            if (feedback.isEmpty()) {
                Toast.makeText(this, "Please provide feedback for required changes.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            submitReview("Rejected", feedback) // 'Rejected' implies changes needed
        }
    }

    private fun submitReview(status: String, feedback: String) {
        val reviewRequest = ReviewResumeRequest(
            review_id = request.review_id,
            status = status,
            feedback = feedback
        )

        BackendApiService.api.reviewResume(reviewRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.error == null) {
                    Toast.makeText(this@FacultyResumeReviewDetailActivity, "Review submitted successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@FacultyResumeReviewDetailActivity, "Failed to submit review: ${response.body()?.error}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@FacultyResumeReviewDetailActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
