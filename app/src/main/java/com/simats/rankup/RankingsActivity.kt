package com.simats.rankup

import android.graphics.Color
import android.view.ViewGroup
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.LeaderboardResponse

class RankingsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_rankings)

        // Menu Button & Drawer
        drawerLayout = findViewById(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navView)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, StudentHomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    finish()
                }
                R.id.nav_find_faculty -> {
                    startActivity(Intent(this, MentorsActivity::class.java))
                }
                else -> Toast.makeText(this, "${menuItem.title} Clicked", Toast.LENGTH_SHORT).show()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Setup Sidebar Header Close Button
        val headerView = navView.getHeaderView(0)
        headerView.findViewById<ImageButton>(R.id.btnCloseDrawer)?.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        
        // Handle Sidebar Logout
        val navFooter = findViewById<View>(R.id.navFooter)
        val btnTerminateSession = navFooter?.findViewById<View>(R.id.btnTerminateSession) ?: findViewById(R.id.btnTerminateSession)
        
        btnTerminateSession?.setOnClickListener {
            Toast.makeText(this, "Terminating Session...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PlacementLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Setup RecyclerView
        val rvRankings = findViewById<RecyclerView>(R.id.rvRankings)
        rvRankings.layoutManager = LinearLayoutManager(this)
        
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_ranks

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, StudentHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_ranks -> true
                R.id.nav_learn -> {
                    val intent = Intent(this, LearningActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_ranks
        
        checkLeaderboardStatus()
    }

    private fun checkLeaderboardStatus() {
        val isPublic = LeaderboardManager.isPublicViewEnabled(this)
        
        val scrollContent = findViewById<android.view.View>(R.id.scrollContent)
        val layoutLocked = findViewById<android.view.View>(R.id.layoutLocked)
        val cardMyRank = findViewById<android.view.View>(R.id.cardMyRank)
        val podium1 = findViewById<android.view.View>(R.id.podium1) // Part of scroll but good to check
        
        if (isPublic) {
            scrollContent.visibility = android.view.View.VISIBLE
            cardMyRank.visibility = android.view.View.VISIBLE
            layoutLocked.visibility = android.view.View.GONE
            loadRankings()
        } else {
            scrollContent.visibility = android.view.View.GONE
            cardMyRank.visibility = android.view.View.GONE
            layoutLocked.visibility = android.view.View.VISIBLE
        }
    }

    private fun loadRankings() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("USER_ID", -1)
        val currentUserName = sharedPref.getString("USER_NAME", "User Name") ?: "User Name"

        // Clear previous podium data
        findViewById<TextView>(R.id.tvRank1Name).text = ""
        findViewById<TextView>(R.id.tvRank1Score).text = ""
        findViewById<TextView>(R.id.tvRank2Name).text = ""
        findViewById<TextView>(R.id.tvRank2Score).text = ""
        findViewById<TextView>(R.id.tvRank3Name).text = ""
        findViewById<TextView>(R.id.tvRank3Score).text = ""

        BackendApiService.api.getTestLeaderboard("Placement").enqueue(object : Callback<LeaderboardResponse> {
            override fun onResponse(call: Call<LeaderboardResponse>, response: Response<LeaderboardResponse>) {
                if (response.isSuccessful && response.body()?.error == null) {
                    val leaderboard = response.body()?.leaderboard ?: emptyList()
                    
                    // Bind Podium
                    if (leaderboard.isNotEmpty()) {
                        findViewById<TextView>(R.id.tvRank1Name).text = leaderboard[0].name
                        findViewById<TextView>(R.id.tvRank1Score).text = leaderboard[0].score.toString()
                        val iv1 = findViewById<android.widget.ImageView>(R.id.ivRank1Avatar)
                        if (!leaderboard[0].profile_pic.isNullOrEmpty()) {
                            com.bumptech.glide.Glide.with(this@RankingsActivity).load(BackendApiService.getFullUrl(leaderboard[0].profile_pic)).placeholder(R.drawable.ic_medal).circleCrop().into(iv1)
                        } else iv1.setImageResource(R.drawable.ic_medal)
                    }
                    if (leaderboard.size > 1) {
                        findViewById<TextView>(R.id.tvRank2Name).text = leaderboard[1].name
                        findViewById<TextView>(R.id.tvRank2Score).text = leaderboard[1].score.toString()
                        val iv2 = findViewById<android.widget.ImageView>(R.id.ivRank2Avatar)
                        if (!leaderboard[1].profile_pic.isNullOrEmpty()) {
                            com.bumptech.glide.Glide.with(this@RankingsActivity).load(BackendApiService.getFullUrl(leaderboard[1].profile_pic)).placeholder(R.drawable.ic_medal).circleCrop().into(iv2)
                        } else iv2.setImageResource(R.drawable.ic_medal)
                    }
                    if (leaderboard.size > 2) {
                        findViewById<TextView>(R.id.tvRank3Name).text = leaderboard[2].name
                        findViewById<TextView>(R.id.tvRank3Score).text = leaderboard[2].score.toString()
                        val iv3 = findViewById<android.widget.ImageView>(R.id.ivRank3Avatar)
                        if (!leaderboard[2].profile_pic.isNullOrEmpty()) {
                            com.bumptech.glide.Glide.with(this@RankingsActivity).load(BackendApiService.getFullUrl(leaderboard[2].profile_pic)).placeholder(R.drawable.ic_medal).circleCrop().into(iv3)
                        } else iv3.setImageResource(R.drawable.ic_medal)
                    }

                    // Bind List
                    val listStudents = mutableListOf<RankingsAdapter.StudentRank>()
                    for (i in 3 until leaderboard.size) {
                        val entry = leaderboard[i]
                        listStudents.add(
                            RankingsAdapter.StudentRank(
                                rank = i + 1,
                                name = entry.name,
                                department = "Engineering", // DB currently doesn't sync department in the bare response
                                points = entry.score,
                                profilePic = entry.profile_pic
                            )
                        )
                    }
                    
                    val rvRankings = findViewById<RecyclerView>(R.id.rvRankings)
                    rvRankings.adapter = RankingsAdapter(listStudents)

                    // Bind Current User
                    var myEntry: com.simats.rankup.network.LeaderboardEntry? = null
                    var myRank = -1
                    for (i in leaderboard.indices) {
                        if (leaderboard[i].student_id == currentUserId) {
                            myEntry = leaderboard[i]
                            myRank = i + 1
                            break
                        }
                    }

                    val tvMyRank = findViewById<TextView>(R.id.tvMyRank)
                    val tvMyName = findViewById<TextView>(R.id.tvMyName)
                    val tvMyScore = findViewById<TextView>(R.id.tvMyScore)
                    val ivMyProfilePic = findViewById<android.widget.ImageView>(R.id.ivMyProfilePic)

                    if (myEntry != null) {
                        tvMyRank.text = "#$myRank"
                        tvMyName.text = myEntry.name
                        tvMyScore.text = myEntry.score.toString()
                        if (!myEntry.profile_pic.isNullOrEmpty()) {
                            com.bumptech.glide.Glide.with(this@RankingsActivity)
                                .load(BackendApiService.getFullUrl(myEntry.profile_pic))
                                .placeholder(R.drawable.ic_medal)
                                .circleCrop()
                                .into(ivMyProfilePic)
                        } else {
                            ivMyProfilePic.setImageResource(R.drawable.ic_medal)
                        }
                    } else {
                        tvMyRank.text = "NR"
                        tvMyName.text = currentUserName
                        tvMyScore.text = "0"
                        ivMyProfilePic.setImageResource(R.drawable.ic_medal)
                    }

                } else {
                    Toast.makeText(this@RankingsActivity, "Failed to load Leaderboard.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LeaderboardResponse>, t: Throwable) {
                Toast.makeText(this@RankingsActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
