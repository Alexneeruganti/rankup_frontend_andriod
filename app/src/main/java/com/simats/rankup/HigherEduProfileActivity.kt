package com.simats.rankup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HigherEduProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_higher_edu_profile)

        // UI Components
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        
        // Initial Placeholder (Will be updated by API)
        findViewById<TextView>(R.id.tvName).text = "Loading..."

        // Back Navigation
        btnBack.setOnClickListener {
            finish()
        }

        // Logout Logic
        btnLogout.setOnClickListener {
            val intent = Intent(this, HigherEduLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupBottomNavigation()
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)

        if (userId == -1) return

        BackendApiService.api.getProfile(userId).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful && response.body()?.profile != null) {
                    val profile = response.body()?.profile!!
                    findViewById<TextView>(R.id.tvName).text = profile.name ?: "Higher Edu User"
                    findViewById<TextView>(R.id.tvRegNo).text = profile.register_number ?: "N/A"
                    findViewById<TextView>(R.id.tvEmail).text = profile.email ?: "No Email"
                    findViewById<TextView>(R.id.tvCourse).text = "HIGHER EDU • ${(profile.role ?: "STUDENT").uppercase()}"
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@HigherEduProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_profile

        bottomNav.setOnItemSelectedListener { item ->
            
             val color = when (item.itemId) {
                R.id.nav_home -> android.graphics.Color.parseColor("#1C52E6")
                R.id.nav_learn -> android.graphics.Color.parseColor("#00C853")
                R.id.nav_exam -> android.graphics.Color.parseColor("#D50000") // Red/Pink for Exam
                R.id.nav_ranks -> android.graphics.Color.parseColor("#FF6D00")
                R.id.nav_profile -> android.graphics.Color.parseColor("#6200EA")
                else -> android.graphics.Color.parseColor("#1C52E6")
            }

            val colorStateList = android.content.res.ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                ),
                intArrayOf(
                    color,
                    android.graphics.Color.parseColor("#757575")
                )
            )
            
            bottomNav.itemIconTintList = colorStateList
            bottomNav.itemTextColor = colorStateList

            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HigherEduHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_learn -> {
                    val intent = Intent(this, HigherEduLearningActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_ranks -> {
                    val intent = Intent(this, HigherEduRankingsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_exam -> {
                    val intent = Intent(this, HigherEduMockTestListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.nav_profile
    }
}
