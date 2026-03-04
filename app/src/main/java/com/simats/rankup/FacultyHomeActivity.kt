package com.simats.rankup

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView

class FacultyHomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_home)

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navViewFaculty)
        bottomNav = findViewById(R.id.bottomNavFaculty)

        // Setup Buttons
        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        findViewById<ImageButton>(R.id.btnNotification).setOnClickListener {
             val intent = Intent(this, NotificationsActivity::class.java)
             intent.putExtra("USER_TYPE", "faculty")
             startActivity(intent)
        }

        // Setup Sidebar Header Close Button
        val headerView = navView.getHeaderView(0)
        headerView.findViewById<ImageButton>(R.id.btnCloseDrawer).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Setup Sidebar Navigation Items
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    Toast.makeText(this, "Dashboard Selected", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_upload_content -> {
                    startActivity(Intent(this, FacultyUploadContentActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_create_test -> {
                    startActivity(Intent(this, FacultyCreateTestActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_requests -> {
                    startActivity(Intent(this, FacultyStudentRequestsActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_alerts -> {
                    val intent = Intent(this, NotificationsActivity::class.java)
                    intent.putExtra("USER_TYPE", "faculty")
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_analytics -> {
                    startActivity(Intent(this, StudentAnalyticsActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                else -> {
                    Toast.makeText(this, "${menuItem.title} Selected", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
            true
        }

        // Setup Sidebar Logout Button (Footer)
        val btnTerminateSession = navView.findViewById<View>(R.id.btnTerminateSession)
        btnTerminateSession?.setOnClickListener {
             Toast.makeText(this, "Logging Out...", Toast.LENGTH_SHORT).show()
             val intent = Intent(this, PlacementLoginActivity::class.java)
             intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
             startActivity(intent)
             finish()
        }

        // Setup Bottom Navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_bottom_home -> {
                    updateBottomNavColor(Color.parseColor("#1C52E6")) // Blue
                    true
                }
                R.id.nav_bottom_analytics -> {
                    updateBottomNavColor(Color.parseColor("#00C853")) // Green
                    startActivity(Intent(this, StudentAnalyticsActivity::class.java))
                    true
                }
                R.id.nav_bottom_resources -> {
                    updateBottomNavColor(Color.parseColor("#FF6D00")) // Orange
                    startActivity(Intent(this, FacultyUploadContentActivity::class.java))
                    true
                }
                R.id.nav_bottom_profile -> {
                    updateBottomNavColor(Color.parseColor("#6200EA")) // Purple
                    startActivity(Intent(this, FacultyProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Setup Cards with interactions
        findViewById<View>(R.id.cardAcademic).setOnClickListener {
            startActivity(Intent(this, FacultyAcademicForgeActivity::class.java))
        }
        
        findViewById<View>(R.id.cardFacultyPlacement).setOnClickListener {
            Toast.makeText(this, "Opening Mock Architect...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, FacultyCreateTestActivity::class.java))
        }

        // Company Questions Button (Assuming we key off an ID or add it dynamically, 
        // but for now let's map it if we added it to XML, or add a temporary floating button / menu item
        // The user asked for a button. I will assume I added a Card/Button with ID btnCompanyQues in XML.
        // Waiting for XML update to be valid, but I can add the code speculatively or after XML update.
        // Let's look for an existing suitable place or add it to XML first.
        
        findViewById<View>(R.id.cardResources).setOnClickListener {
            startActivity(Intent(this, FacultyUploadContentActivity::class.java))
        }

        findViewById<View>(R.id.cardJoinRequests).setOnClickListener {
            startActivity(Intent(this, FacultyStudentRequestsActivity::class.java))
        }

        findViewById<View>(R.id.btnCompanyQues).setOnClickListener {
            startActivity(Intent(this, FacultyCompanyQuestionsActivity::class.java))
        }

        findViewById<View>(R.id.cardResumeReview).setOnClickListener {
            startActivity(Intent(this, FacultyResumeReviewActivity::class.java))
        }

        fetchUserProfile()
        fetchFacultyStats()
    }

    override fun onResume() {
        super.onResume()
        fetchUserProfile()
        fetchFacultyStats()
    }


    private fun fetchUserProfile() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)
        if (userId == -1) return

        com.simats.rankup.network.BackendApiService.api.getProfile(userId).enqueue(object : retrofit2.Callback<com.simats.rankup.network.UserProfileResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.rankup.network.UserProfileResponse>, response: retrofit2.Response<com.simats.rankup.network.UserProfileResponse>) {
                if (response.isSuccessful && response.body()?.profile != null) {
                    val profile = response.body()!!.profile!!
                    findViewById<android.widget.TextView>(R.id.tvName).text = profile.name ?: "Faculty"
                    findViewById<android.widget.TextView>(R.id.tvRole).text = (profile.role ?: "FACULTY").uppercase()
                    findViewById<android.widget.TextView>(R.id.tvFacultyDept).text = profile.department?.uppercase() ?: "DEPT OF COMP SCIENCE"
                    
                    if (!profile.profile_pic.isNullOrEmpty()) {
                        val fullUrl = if (profile.profile_pic!!.startsWith("http")) profile.profile_pic else "${com.simats.rankup.network.BackendApiService.BASE_URL.removeSuffix("/")}${profile.profile_pic}"
                        com.bumptech.glide.Glide.with(this@FacultyHomeActivity)
                            .load(fullUrl)
                            .circleCrop()
                            .into(findViewById<android.widget.ImageView>(R.id.imgProfile))
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.UserProfileResponse>, t: Throwable) {}
        })
    }

    private fun fetchFacultyStats() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)
        if (userId == -1) return

        com.simats.rankup.network.BackendApiService.api.getFacultyStats(userId).enqueue(object : retrofit2.Callback<com.simats.rankup.network.FacultyStatsResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.rankup.network.FacultyStatsResponse>, response: retrofit2.Response<com.simats.rankup.network.FacultyStatsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!
                    findViewById<android.widget.TextView>(R.id.tvClassesCount).text = String.format("%02d", stats.classes)
                    findViewById<android.widget.TextView>(R.id.tvActiveTestsCount).text = stats.active_tests.toString()
                    findViewById<android.widget.TextView>(R.id.tvSubmissionsCount).text = stats.submissions.toString()
                    
                    findViewById<android.widget.TextView>(R.id.tvJoinRequestsCount).text = "${stats.pending_requests} CANDIDATES AWAITING VERIFICATION"
                    findViewById<android.widget.ProgressBar>(R.id.progressAcad).progress = stats.academic_progress
                    findViewById<android.widget.TextView>(R.id.tvSyllabusProgress).text = "${stats.academic_progress}% SYLLABUS COMPLETE"
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.FacultyStatsResponse>, t: Throwable) {}
        })
    }

    private fun updateBottomNavColor(activeColor: Int) {
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                activeColor,
                Color.parseColor("#9E9E9E") // Default Gray
            )
        )
        bottomNav.itemIconTintList = colorStateList
        bottomNav.itemTextColor = colorStateList
    }

    private fun setupCardAction(view: View, message: String) {
        view.setOnClickListener {
            view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }.start()
        }
    }
    
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
