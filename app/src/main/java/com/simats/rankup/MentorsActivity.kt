package com.simats.rankup

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.rankup.adapters.FacultyAdapter
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.FacultyResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MentorsActivity : ComponentActivity() {

    private lateinit var rvMentors: RecyclerView
    private lateinit var facultyAdapter: FacultyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentors)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        rvMentors = findViewById(R.id.rvMentors)
        rvMentors.layoutManager = LinearLayoutManager(this)
        facultyAdapter = FacultyAdapter(emptyList()) { faculty, requestType ->
            sendJoinRequest(faculty, requestType)
        }
        rvMentors.adapter = facultyAdapter

        // Fetch Faculty
        fetchFacultyList()
    }

    private fun fetchFacultyList() {
        BackendApiService.api.getFaculty().enqueue(object : Callback<FacultyResponse> {
            override fun onResponse(call: Call<FacultyResponse>, response: Response<FacultyResponse>) {
                if (response.isSuccessful) {
                    val facultyList = response.body()?.faculty ?: emptyList()
                    facultyAdapter.updateData(facultyList)
                } else {
                    Toast.makeText(this@MentorsActivity, "Failed to load faculty", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FacultyResponse>, t: Throwable) {
                Toast.makeText(this@MentorsActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendJoinRequest(faculty: com.simats.rankup.network.FacultyMember, requestType: String) {
        // Simulating student ID 5 for now until global session state is built. 
        // Real logic will grab logged-in user ID from SharedPreferences.
        val studentId = 5
        val facultyId = faculty.id.toIntOrNull() ?: 0

        val request = com.simats.rankup.network.SendJoinRequestPayload(studentId, facultyId, requestType)
        BackendApiService.api.sendJoinRequest(request).enqueue(object : Callback<com.simats.rankup.network.ApiResponse> {
            override fun onResponse(call: Call<com.simats.rankup.network.ApiResponse>, response: Response<com.simats.rankup.network.ApiResponse>) {
                if (response.isSuccessful) {
                    val statusText = if (requestType == "MENTORSHIP") "Mentorship Request Sent!" else "Class Request Sent!"
                    Toast.makeText(this@MentorsActivity, statusText, Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        if (errorBody != null && errorBody.contains("already have a pending request")) {
                            Toast.makeText(this@MentorsActivity, "Already Requested!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MentorsActivity, "Failed to send request", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@MentorsActivity, "Failed to send request", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<com.simats.rankup.network.ApiResponse>, t: Throwable) {
                Toast.makeText(this@MentorsActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
