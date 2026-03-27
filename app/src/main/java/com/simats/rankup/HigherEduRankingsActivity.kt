package com.simats.rankup

import android.content.Intent

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HigherEduRankingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_higher_edu_rankings)

        // Back Button
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
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
                R.id.nav_ranks -> true
                R.id.nav_exam -> {
                    val intent = Intent(this, HigherEduMockTestListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, HigherEduProfileActivity::class.java)
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
        findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.nav_ranks
        loadRankings()
    }

    private fun loadRankings() {
        // Clear previous podium data
        findViewById<android.widget.TextView>(R.id.tvRank1Name).text = ""
        findViewById<android.widget.TextView>(R.id.tvRank1Score).text = ""
        findViewById<android.widget.TextView>(R.id.tvRank2Name).text = ""
        findViewById<android.widget.TextView>(R.id.tvRank2Score).text = ""
        findViewById<android.widget.TextView>(R.id.tvRank3Name).text = ""
        findViewById<android.widget.TextView>(R.id.tvRank3Score).text = ""

        com.simats.rankup.network.BackendApiService.api.getTestLeaderboard("Higher Education").enqueue(object : retrofit2.Callback<com.simats.rankup.network.LeaderboardResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.rankup.network.LeaderboardResponse>, response: retrofit2.Response<com.simats.rankup.network.LeaderboardResponse>) {
                if (response.isSuccessful && response.body()?.error == null) {
                    val leaderboard = response.body()?.leaderboard ?: emptyList()
                    
                    // Bind Podium
                    if (leaderboard.isNotEmpty()) {
                        findViewById<android.widget.TextView>(R.id.tvRank1Name).text = leaderboard[0].name
                        findViewById<android.widget.TextView>(R.id.tvRank1Score).text = leaderboard[0].score.toString()
                    }
                    if (leaderboard.size > 1) {
                        findViewById<android.widget.TextView>(R.id.tvRank2Name).text = leaderboard[1].name
                        findViewById<android.widget.TextView>(R.id.tvRank2Score).text = leaderboard[1].score.toString()
                    }
                    if (leaderboard.size > 2) {
                        findViewById<android.widget.TextView>(R.id.tvRank3Name).text = leaderboard[2].name
                        findViewById<android.widget.TextView>(R.id.tvRank3Score).text = leaderboard[2].score.toString()
                    }

                    // Bind List
                    val listStudents = mutableListOf<RankingsAdapter.StudentRank>()
                    for (i in 3 until leaderboard.size) {
                        val entry = leaderboard[i]
                        listStudents.add(
                            RankingsAdapter.StudentRank(
                                rank = i + 1,
                                name = entry.name,
                                department = "Aspirant", 
                                points = entry.score,
                                profilePic = entry.profile_pic
                            )
                        )
                    }
                    
                    val rvRankings = findViewById<RecyclerView>(R.id.rvRankings)
                    rvRankings.adapter = RankingsAdapter(listStudents)

                } else {
                    android.widget.Toast.makeText(this@HigherEduRankingsActivity, "Failed to load Higher Ed Leaderboard.", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.LeaderboardResponse>, t: Throwable) {
                android.widget.Toast.makeText(this@HigherEduRankingsActivity, "Network Error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
    }
}
