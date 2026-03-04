package com.simats.rankup

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.rankup.network.AnnouncementListResponse
import com.simats.rankup.network.BackendApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsActivity : AppCompatActivity() {

    private lateinit var adapter: NotificationAdapter
    private lateinit var rvNotifications: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // Back Button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // RecyclerView Setup
        rvNotifications = findViewById(R.id.rvNotifications)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)

        rvNotifications.layoutManager = LinearLayoutManager(this)
        
        // Get User Type (default to student for now if not passed)
        // In a real app, you might store this in a SessionManager
        val userType = intent.getStringExtra("USER_TYPE") ?: "student" 

        fetchAnnouncements(userType)
    }

    private fun fetchAnnouncements(userType: String) {
        val category = when(userType) {
            "student" -> "Students Only"
            "faculty" -> "Faculty Only"
            "higher_edu" -> "Higher Education Students"
            else -> "All Users"
        }

        BackendApiService.api.getAnnouncements(category).enqueue(object : Callback<AnnouncementListResponse> {
            override fun onResponse(call: Call<AnnouncementListResponse>, response: Response<AnnouncementListResponse>) {
                if (response.isSuccessful && response.body()?.announcements != null) {
                    val announcements = response.body()!!.announcements!!
                    if (announcements.isEmpty()) {
                        rvNotifications.visibility = View.GONE
                        layoutEmptyState.visibility = View.VISIBLE
                    } else {
                        rvNotifications.visibility = View.VISIBLE
                        layoutEmptyState.visibility = View.GONE
                        adapter = NotificationAdapter(announcements)
                        rvNotifications.adapter = adapter
                    }
                } else {
                    Toast.makeText(this@NotificationsActivity, "Failed to load announcements", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AnnouncementListResponse>, t: Throwable) {
                Toast.makeText(this@NotificationsActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                rvNotifications.visibility = View.GONE
                layoutEmptyState.visibility = View.VISIBLE
            }
        })
    }
}
