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
import com.google.gson.Gson

class FacultyResumeReviewActivity : AppCompatActivity() {

    private lateinit var rvResumeRequests: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var adapter: FacultyResumeReviewAdapter
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_resume_review)

        rvResumeRequests = findViewById(R.id.rvResumeRequests)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadRequests()
    }

    private fun setupRecyclerView() {
        adapter = FacultyResumeReviewAdapter(emptyList()) { request ->
            val intent = Intent(this, FacultyResumeReviewDetailActivity::class.java)
            intent.putExtra("REQUEST_DATA", gson.toJson(request))
            startActivity(intent)
        }
        rvResumeRequests.layoutManager = LinearLayoutManager(this)
        rvResumeRequests.adapter = adapter
    }

    private fun loadRequests() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val facultyId = sharedPref.getInt("USER_ID", -1)

        if (facultyId == -1) {
            Toast.makeText(this, "Error: Faculty ID not found. Please log in.", Toast.LENGTH_SHORT).show()
            rvResumeRequests.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
            return
        }

        BackendApiService.api.getMentorResumes(facultyId).enqueue(object : Callback<ResumeReviewsResponse> {
            override fun onResponse(call: Call<ResumeReviewsResponse>, response: Response<ResumeReviewsResponse>) {
                if (response.isSuccessful && response.body()?.error == null) {
                    val requests = response.body()?.resumes ?: emptyList()
                    if (requests.isEmpty()) {
                        rvResumeRequests.visibility = View.GONE
                        layoutEmptyState.visibility = View.VISIBLE
                    } else {
                        rvResumeRequests.visibility = View.VISIBLE
                        layoutEmptyState.visibility = View.GONE
                        adapter.updateList(requests)
                    }
                } else {
                    Toast.makeText(this@FacultyResumeReviewActivity, "Failed to load requests", Toast.LENGTH_SHORT).show()
                    rvResumeRequests.visibility = View.GONE
                    layoutEmptyState.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<ResumeReviewsResponse>, t: Throwable) {
                Toast.makeText(this@FacultyResumeReviewActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                rvResumeRequests.visibility = View.GONE
                layoutEmptyState.visibility = View.VISIBLE
            }
        })
    }
}
