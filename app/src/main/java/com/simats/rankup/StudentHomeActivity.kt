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

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.simats.rankup.network.AppSettingsRequest
import com.simats.rankup.network.AppSettingsResponse
import com.simats.rankup.network.BackendApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentHomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_student_home)

            drawerLayout = findViewById(R.id.drawerLayout)
            val navView = findViewById<NavigationView>(R.id.navView)
            val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
            val btnNotification = findViewById<ImageButton>(R.id.btnNotification)
            val headerBg = findViewById<View>(R.id.headerBg)

            // Setup Navigation Drawer
            btnMenu.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }

            // Setup Sidebar Header Close Button
            val headerView = navView.getHeaderView(0)
            headerView.findViewById<ImageButton>(R.id.btnCloseDrawer)?.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            navView.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_dashboard -> {
                        // Already on dashboard, just close drawer
                    }
                    R.id.nav_find_faculty -> {
                        startActivity(Intent(this, MentorsActivity::class.java))
                    }
                    R.id.nav_notifications -> {
                        val intent = Intent(this, NotificationsActivity::class.java)
                        intent.putExtra("USER_TYPE", "student")
                        startActivity(intent)
                    }
                    R.id.nav_coding -> {
                        startActivity(Intent(this, StudentCodingActivity::class.java))
                    }
                    R.id.nav_communication -> {
                        startActivity(Intent(this, CommunicationActivity::class.java))
                    }
                    R.id.nav_leaderboard -> {
                        startActivity(Intent(this, RankingsActivity::class.java))
                    }
                    R.id.nav_resources -> {
                        startActivity(Intent(this, LearningActivity::class.java))
                    }
                    R.id.nav_profile -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                    else -> Toast.makeText(this, "${menuItem.title} Clicked", Toast.LENGTH_SHORT).show()
                }
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            
            // Handle Footer Logout Button (Traverse footer layout)
            val footerView = navView.getChildAt(navView.childCount - 1) // Footer is usually the last child or loaded via getChildAt if indexed differently due to RecyclerView
            // Better way: navView.itemIconTintList = ...
            
            // Note: Footer view handling might need specific lookup if added via attribute
            // However, we didn't add an ID to the footer include in XML attribute essentially.
            // But we can find it via findViewById logic if we inflate it, OR just rely on menu items.
            // Since we used app:footerLayout, we need to find the view inside it.
            // The footer layout is inflated into the Navigation Menu.
            
            // Let's keep it simple. The user asked for the sidebar. 
            // We can just rely on standard menu clicks for now.
            
            
            val cardMentors = findViewById<CardView>(R.id.cardMentors)
            val cardRankings = findViewById<CardView>(R.id.cardRankings)
            val cardResume = findViewById<CardView>(R.id.cardResume)
            // val cardScore = findViewById<CardView>(R.id.cardScore)
            // val cardPhase1 = findViewById<CardView>(R.id.cardPhase1)
            val cardPhase4 = findViewById<CardView>(R.id.cardPhase4)
            val cardTechnical = findViewById<CardView>(R.id.cardTechnical)
            val cardCoding = findViewById<CardView>(R.id.cardCoding)
            val cardCompanyQues = findViewById<CardView>(R.id.cardCompanyQues)

            
            val cardCommunication = findViewById<CardView>(R.id.cardCommunication)
            val cardAnalytics = findViewById<CardView>(R.id.cardAnalytics)

            // Setup Buttons with bounce animation
            // setupButton(btnMenu, "Menu Clicked") // Removed to allow Drawer opening
            btnNotification.setOnClickListener {
                animateClick(btnNotification)
                val intent = Intent(this, NotificationsActivity::class.java)
                intent.putExtra("USER_TYPE", "student")
                startActivity(intent)
            }
            
            // Handle Logout Click from drawer footer
            val btnLogoutSidebar = findViewById<View>(R.id.btnTerminateSession)
            
            btnLogoutSidebar?.setOnClickListener {
                Toast.makeText(this, "Terminating Session...", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, PlacementLoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

            // Setup Cards with bounce animation and toasts
            setupCard(cardMentors, "Opening Mentors...")
            setupCard(cardRankings, "Opening Rankings...")
            setupCard(cardResume, "Opening Resume Builder...")
            // setupCard(cardScore, "Analyzing Score...")
            // setupCard(cardPhase1, "Starting Aptitude Training...")
            setupCard(cardPhase4, "Starting Mock Tests...")
            setupCard(cardTechnical, "Opening Technical Q&A...")
            setupCard(cardCompanyQues, "Opening Company Questions...")
            setupCard(cardCoding, "Opening Coding Drills...")
            setupCard(cardCommunication, "Opening Communication Round...")
            cardAnalytics.setOnClickListener {
                animateClick(cardAnalytics)
                startActivity(Intent(this, StudentAnalyticsActivity::class.java))
            }
            fetchStudentStats()


            // Bottom Navigation
            val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
            bottomNav.setOnItemSelectedListener { item ->
                animateNavItem(bottomNav, item.itemId)
                
                // Color mapping for active state
                val color = when (item.itemId) {
                    R.id.nav_home -> android.graphics.Color.parseColor("#1C52E6") // Blue
                    R.id.nav_ranks -> android.graphics.Color.parseColor("#FF6D00") // Orange
                    R.id.nav_learn -> android.graphics.Color.parseColor("#00C853") // Green
                    R.id.nav_profile -> android.graphics.Color.parseColor("#6200EA") // Deep Purple
                    else -> android.graphics.Color.parseColor("#1C52E6")
                }

                // Create state list for Active vs Default (Grey)
                val colorStateList = android.content.res.ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf(-android.R.attr.state_checked)
                    ),
                    intArrayOf(
                        color,
                        android.graphics.Color.parseColor("#757575") // Grey
                    )
                )
                
                bottomNav.itemIconTintList = colorStateList
                bottomNav.itemTextColor = colorStateList
                bottomNav.itemRippleColor = android.content.res.ColorStateList.valueOf(color).withAlpha(30)


                when (item.itemId) {
                    R.id.nav_home -> {
                        Toast.makeText(this, "Home Selected", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.nav_ranks -> {
                        Toast.makeText(this, "Ranks Selected", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, RankingsActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                        true
                    }
                    R.id.nav_learn -> {
                        Toast.makeText(this, "Learn Selected", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LearningActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                        true
                    }
                    R.id.nav_profile -> {
                        Toast.makeText(this, "Profile Selected", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, ProfileActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                        true
                    }
                    else -> false
                }
            }

            // Entry Animations
            animateEntry(cardMentors, 200)
            animateEntry(cardRankings, 200)
            animateEntry(cardResume, 300)
            // animateEntry(cardScore, 400)
            // animateEntry(cardPhase1, 500)
            animateEntry(cardPhase4, 600)
            animateEntry(cardTechnical, 700)
            animateEntry(cardCompanyQues, 750)
            animateEntry(cardCommunication, 800)
            animateEntry(cardAnalytics, 900)
            
            // Enforce admin constraints globally
            fetchAdminSettingsToEnforceConstraints()
            fetchUserProfile()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading Student Home: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }



    private fun fetchUserProfile() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)
        if (userId == -1) return

        BackendApiService.api.getProfile(userId).enqueue(object : Callback<com.simats.rankup.network.UserProfileResponse> {
            override fun onResponse(call: Call<com.simats.rankup.network.UserProfileResponse>, response: Response<com.simats.rankup.network.UserProfileResponse>) {
                if (response.isSuccessful && response.body()?.profile != null) {
                    val profile = response.body()!!.profile!!
                    findViewById<android.widget.TextView>(R.id.tvUserName).text = profile.name ?: "Student"
                }
            }
            override fun onFailure(call: Call<com.simats.rankup.network.UserProfileResponse>, t: Throwable) {}
        })
    }

    private fun fetchStudentStats() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)
        if (userId == -1) return

        BackendApiService.api.getStudentStats(userId).enqueue(object : Callback<com.simats.rankup.network.StudentStatsResponse> {
            override fun onResponse(call: Call<com.simats.rankup.network.StudentStatsResponse>, response: Response<com.simats.rankup.network.StudentStatsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!
                    findViewById<android.widget.TextView>(R.id.tvWeeklyGrowth).text = "+${stats.average_score.toInt()}% GROWTH"
                }
            }
            override fun onFailure(call: Call<com.simats.rankup.network.StudentStatsResponse>, t: Throwable) {}
        })
    }

    private fun setupButton(view: View, message: String) {
        view.setOnClickListener {
            playSoundEffect(view)
            animateClick(view)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCard(view: View, message: String) {
        view.setOnClickListener {
            playSoundEffect(view)
            animateClick(view)
            
            if (view.id == R.id.cardMentors) {
                val intent = Intent(this, MentorsActivity::class.java)
                startActivity(intent)
            } else if (view.id == R.id.cardCoding) {
                val intent = Intent(this, StudentCodingActivity::class.java)
                startActivity(intent)
            } else if (view.id == R.id.cardRankings) {
                val intent = Intent(this, RankingsActivity::class.java)
                startActivity(intent)
            } else if (view.id == R.id.cardResume) {
                val intent = Intent(this, StudentResumeBuilderActivity::class.java)
                startActivity(intent)
            } else if (view.id == R.id.cardTechnical) {
                val intent = Intent(this, TechnicalRoundActivity::class.java)
                startActivity(intent)
            } else if (view.id == R.id.cardCommunication) {
                val intent = Intent(this, CommunicationActivity::class.java)
                startActivity(intent)
            } else if (view.id == R.id.cardCompanyQues) {
                val intent = Intent(this, StudentCompanyQuestionsActivity::class.java)
                startActivity(intent)
            } else if (view.id == R.id.cardPhase4) {
                val intent = Intent(this, StudentMockTestListActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchAdminSettingsToEnforceConstraints() {
        BackendApiService.api.getAdminSettings().enqueue(object : Callback<AppSettingsResponse> {
            override fun onResponse(call: Call<AppSettingsResponse>, response: Response<AppSettingsResponse>) {
                val settings = response.body()?.settings
                if (response.isSuccessful && settings != null) {
                    applyConstraints(settings)
                }
            }

            override fun onFailure(call: Call<AppSettingsResponse>, t: Throwable) {
                // If network fails, allow default caching logic or fail silently to let cards be clickable
            }
        })
    }

    private fun applyConstraints(settings: AppSettingsRequest) {
        /*
        if (!settings.aptitude_tests) {
            findViewById<CardView>(R.id.cardPhase1).setOnClickListener {
                playSoundEffect(it)
                animateClick(it)
                Toast.makeText(this, "Aptitude Tests are currently disabled by Admin.", Toast.LENGTH_SHORT).show()
            }
        }
        */
        if (!settings.coding_practice) {
            findViewById<CardView>(R.id.cardCoding).setOnClickListener {
                playSoundEffect(it)
                animateClick(it)
                Toast.makeText(this, "Coding Practice is currently disabled by Admin.", Toast.LENGTH_SHORT).show()
            }
        }
        if (!settings.leaderboard) {
            findViewById<CardView>(R.id.cardRankings).setOnClickListener {
                playSoundEffect(it)
                animateClick(it)
                Toast.makeText(this, "Leaderboards are currently disabled by Admin.", Toast.LENGTH_SHORT).show()
            }
        }
        if (!settings.learning_resources) {
            findViewById<CardView>(R.id.cardMentors).setOnClickListener {
                playSoundEffect(it)
                animateClick(it)
                Toast.makeText(this, "Learning Resources are currently disabled by Admin.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playSoundEffect(view: View) {
        view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
    }

    private fun animateNavItem(bottomNav: com.google.android.material.bottomnavigation.BottomNavigationView, itemId: Int) {
        val menu = bottomNav.menu
        var index = -1
        for (i in 0 until menu.size()) {
            if (menu.getItem(i).itemId == itemId) {
                index = i
                break
            }
        }

        if (index != -1) {
            try {
                val menuView = bottomNav.getChildAt(0) as android.view.ViewGroup
                val itemView = menuView.getChildAt(index)
                animateClick(itemView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun animateClick(view: View) {
        // Zoom In/Out Bounce
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f)
        scaleX.duration = 100
        scaleY.duration = 100
        scaleX.start()
        scaleY.start()
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
