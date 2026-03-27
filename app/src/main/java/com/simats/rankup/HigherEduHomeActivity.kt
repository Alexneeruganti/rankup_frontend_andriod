package com.simats.rankup

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class HigherEduHomeActivity : ComponentActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_higher_edu_home)

            drawerLayout = findViewById(R.id.drawerLayout)
            val navView = findViewById<NavigationView>(R.id.navView)
            val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
            val btnNotification = findViewById<ImageButton>(R.id.btnNotification)

            // Setup Navigation Drawer
            btnMenu.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }

            navView.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_dashboard -> {
                        // Already on dashboard, just close drawer
                    }
                    
                    // Sidebar Navigation Logic
                    R.id.nav_notifications -> startActivity(Intent(this, HigherEduNotificationsActivity::class.java))
                    R.id.nav_gate_ielts -> startActivity(Intent(this, HigherEduLearningActivity::class.java))
                    R.id.nav_academic_ranks -> startActivity(Intent(this, HigherEduRankingsActivity::class.java))
                    
                    R.id.nav_library -> Toast.makeText(this, "Library: Coming Soon", Toast.LENGTH_SHORT).show()
                    R.id.nav_profile -> startActivity(Intent(this, HigherEduProfileActivity::class.java))
                }
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }

            // Footer Logout Logic
            val btnTerminateSession = findViewById<android.widget.LinearLayout>(R.id.btnTerminateSession)
            btnTerminateSession.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                Toast.makeText(this, "Session Terminated", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HigherEduLoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

            // Cards
            val cardRankings = findViewById<CardView>(R.id.cardRankings)
            val cardLearn = findViewById<CardView>(R.id.cardLearn)
            val cardExam = findViewById<CardView>(R.id.cardExam)

            btnNotification.setOnClickListener {
                animateClick(btnNotification)
                startActivity(Intent(this, HigherEduNotificationsActivity::class.java))
            }

            // ... (Card Click Listeners remain same) ...
            cardRankings.setOnClickListener {
                animateClick(cardRankings)
                startActivity(Intent(this, HigherEduRankingsActivity::class.java))
            }

            cardLearn.setOnClickListener {
                animateClick(cardLearn)
                startActivity(Intent(this, HigherEduLearningActivity::class.java))
            }

            cardExam.setOnClickListener {
                animateClick(cardExam)
                startActivity(Intent(this, HigherEduMockTestListActivity::class.java))
            }
            
            // Bottom Navigation
            val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
            bottomNav.setOnItemSelectedListener { item ->
                animateNavItem(bottomNav, item.itemId)
                
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
                        // Already here
                    }
                    R.id.nav_learn -> startActivity(Intent(this, HigherEduLearningActivity::class.java))
                    R.id.nav_ranks -> startActivity(Intent(this, HigherEduRankingsActivity::class.java))
                    R.id.nav_exam -> startActivity(Intent(this, HigherEduMockTestListActivity::class.java))
                    R.id.nav_profile -> startActivity(Intent(this, HigherEduProfileActivity::class.java))
                }
                true
            }

            animateEntry(cardRankings, 200)
            animateEntry(cardLearn, 300)
            animateEntry(cardExam, 400)

            fetchUserProfile()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchUserProfile() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)
        if (userId == -1) return

        com.simats.rankup.network.BackendApiService.api.getProfile(userId).enqueue(object : retrofit2.Callback<com.simats.rankup.network.UserProfileResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.rankup.network.UserProfileResponse>, response: retrofit2.Response<com.simats.rankup.network.UserProfileResponse>) {
                if (response.isSuccessful && response.body()?.profile != null) {
                    val profile = response.body()!!.profile!!
                    findViewById<android.widget.TextView>(R.id.tvUserName).text = profile.name ?: "Higher Edu User"
                }
            }
            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.UserProfileResponse>, t: Throwable) {}
        })
    }

    private fun setupCard(view: View, message: String) {
        view.setOnClickListener {
            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
            animateClick(view)
            
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun animateClick(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f)
        scaleX.duration = 100
        scaleY.duration = 100
        scaleX.start()
        scaleY.start()
    }
    
    private fun animateNavItem(bottomNav: com.google.android.material.bottomnavigation.BottomNavigationView, itemId: Int) {
        // Animation logic same as StudentHome
    }

    private fun animateEntry(view: View, delay: Long) {
        view.alpha = 0f
        view.translationY = 100f
        val alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        val transY = ObjectAnimator.ofFloat(view, "translationY", 100f, 0f)
        alpha.duration = 600
        transY.duration = 600
        alpha.startDelay = delay
        transY.startDelay = delay
        transY.interpolator = AccelerateDecelerateInterpolator()
        alpha.start()
        transY.start()
    }
}
