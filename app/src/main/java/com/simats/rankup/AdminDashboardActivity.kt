package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        drawerLayout = findViewById(R.id.drawerLayout)

        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.btnUserManagement).setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.btnRankings).setOnClickListener {
            startActivity(Intent(this, AdminLeaderboardActivity::class.java))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.btnBroadcast).setOnClickListener {
            startActivity(Intent(this, AdminAnnouncementActivity::class.java))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.btnContentHub).setOnClickListener {
            startActivity(Intent(this, AdminContentHubActivity::class.java))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.btnTestPortal).setOnClickListener {
            startActivity(Intent(this, AdminTestPortalActivity::class.java))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.btnConfig).setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
        }

        val navigationView: NavigationView = findViewById(R.id.navViewAdmin)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    // Already on dashboard, just close drawer
                }
                R.id.nav_students -> {
                    startActivity(Intent(this, UserManagementActivity::class.java))
                }
                R.id.nav_content -> {
                    startActivity(Intent(this, AdminContentHubActivity::class.java))
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, AdminProfileActivity::class.java))
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, AdminSettingsActivity::class.java))
                }
                R.id.nav_logout -> {
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        fetchAdminStats()
        fetchAdminProfile()
    }

    override fun onResume() {
        super.onResume()
        fetchAdminStats()
        fetchAdminProfile()
    }

    private fun fetchAdminProfile() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)
        if (userId == -1) return

        com.simats.rankup.network.BackendApiService.api.getProfile(userId).enqueue(object : retrofit2.Callback<com.simats.rankup.network.UserProfileResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.rankup.network.UserProfileResponse>, response: retrofit2.Response<com.simats.rankup.network.UserProfileResponse>) {
                if (response.isSuccessful && response.body()?.profile != null) {
                    val profile = response.body()!!.profile!!
                    findViewById<android.widget.TextView>(R.id.tvAdminName)?.text = profile.name ?: "System Admin"
                    
                    if (!profile.profile_pic.isNullOrEmpty()) {
                        val fullUrl = com.simats.rankup.network.BackendApiService.getFullUrl(profile.profile_pic)
                        com.bumptech.glide.Glide.with(this@AdminDashboardActivity)
                            .load(fullUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(findViewById<android.widget.ImageView>(R.id.imgAdminProfile))
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.UserProfileResponse>, t: Throwable) {}
        })
    }

    private fun fetchAdminStats() {
        com.simats.rankup.network.BackendApiService.api.getAdminStats().enqueue(object : retrofit2.Callback<com.simats.rankup.network.AdminStatsResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.rankup.network.AdminStatsResponse>, response: retrofit2.Response<com.simats.rankup.network.AdminStatsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!
                    findViewById<android.widget.TextView>(R.id.tvTotalUsers).text = stats.total_users.toString()
                    findViewById<android.widget.TextView>(R.id.tvPlacementPerc).text = "${stats.placement_percentage}%"
                    findViewById<android.widget.TextView>(R.id.tvActiveTasks).text = stats.active_tasks.toString()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.AdminStatsResponse>, t: Throwable) {
                // Silently fail or log
            }
        })
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
