package com.simats.rankup

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class HigherEduLearningActivity : AppCompatActivity() {

    private lateinit var tabNotes: LinearLayout
    private lateinit var tabLectures: LinearLayout
    private lateinit var textNotes: TextView
    private lateinit var textLectures: TextView
    private lateinit var iconNotes: ImageView
    private lateinit var iconLectures: ImageView
    private lateinit var rvModules: RecyclerView
    
    // Defaulting global state variable equivalent to checkLearningEnabled for Higher Ed
    private var isLearningEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_higher_edu_learning)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        if(btnBack != null) {
            btnBack.setOnClickListener {
                finish()
            }
        }

        // Initialize Views
        tabNotes = findViewById(R.id.tabNotes)
        tabLectures = findViewById(R.id.tabLectures)
        textNotes = findViewById(R.id.textNotes)
        textLectures = findViewById(R.id.textLectures)
        iconNotes = findViewById(R.id.iconNotes)
        iconLectures = findViewById(R.id.iconLectures)
        rvModules = findViewById(R.id.rvModules)

        rvModules.layoutManager = LinearLayoutManager(this)

        setupTabLogic()
        checkLearningEnabledAndLoad()
        setupBottomNavigation()
    }

    private fun checkLearningEnabledAndLoad() {
        val progressBar = android.widget.ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
        }
        val emptyMessage = TextView(this).apply {
            text = "Learning Resources are currently disabled by the Admin."
            textSize = 16f
            setTextColor(Color.GRAY)
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
                setMargins(0, 100, 0, 0)
            }
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }

        val contentContainer = rvModules.parent as android.view.ViewGroup
        contentContainer.addView(progressBar)
        contentContainer.addView(emptyMessage)
        rvModules.visibility = View.GONE

        com.simats.rankup.network.BackendApiService.api.getAdminSettings().enqueue(object : retrofit2.Callback<com.simats.rankup.network.AppSettingsResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.rankup.network.AppSettingsResponse>, response: retrofit2.Response<com.simats.rankup.network.AppSettingsResponse>) {
                progressBar.visibility = View.GONE
                val settings = response.body()?.settings
                if (response.isSuccessful && settings != null && settings.learning_resources) {
                    isLearningEnabled = true
                    rvModules.visibility = View.VISIBLE
                    loadNotes()
                } else {
                    isLearningEnabled = false
                    emptyMessage.visibility = View.VISIBLE
                    Toast.makeText(this@HigherEduLearningActivity, "Learning resources are currently disabled.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.AppSettingsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                emptyMessage.text = "Error connecting to server."
                emptyMessage.visibility = View.VISIBLE
            }
        })
    }

    private fun setupTabLogic() {
        tabNotes.setOnClickListener {
            updateTabVisuals(isNotes = true)
            if(isLearningEnabled) loadNotes()
        }

        tabLectures.setOnClickListener {
            updateTabVisuals(isNotes = false)
            if(isLearningEnabled) loadLectures()
        }
    }

    private fun updateTabVisuals(isNotes: Boolean) {
        if (isNotes) {
            tabNotes.setBackgroundResource(R.drawable.bg_tab_active)
            textNotes.setTextColor(Color.WHITE)
            iconNotes.setColorFilter(Color.WHITE)

            tabLectures.background = null
            textLectures.setTextColor(Color.parseColor("#9E9E9E"))
            iconLectures.setColorFilter(Color.parseColor("#9E9E9E"))
        } else {
            tabLectures.setBackgroundResource(R.drawable.bg_tab_active)
            textLectures.setTextColor(Color.WHITE)
            iconLectures.setColorFilter(Color.WHITE)

            tabNotes.background = null
            textNotes.setTextColor(Color.parseColor("#9E9E9E"))
            iconNotes.setColorFilter(Color.parseColor("#9E9E9E"))
        }
    }

    private fun loadNotes() {
        // Fetch Notes from API for Higher Education
        com.simats.rankup.network.BackendApiService.api.getResources("Higher Education").enqueue(object : retrofit2.Callback<com.simats.rankup.network.GetResourcesResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.simats.rankup.network.GetResourcesResponse>,
                response: retrofit2.Response<com.simats.rankup.network.GetResourcesResponse>
            ) {
                if (response.isSuccessful && response.body()?.error == null) {
                    val apiResources = response.body()?.resources ?: listOf()
                    
                    // Filter NOTES
                    val notesModules = apiResources.filter { it.resource_type == "NOTES" }.map { res ->
                        LearningAdapter.LearningModule(
                            title = res.title,
                            type = "NOTES",
                            author = "By: ${res.author ?: "Unknown"}",
                            size = if (!res.tags.isNullOrEmpty()) res.tags.uppercase() else "PDF",
                            iconResId = R.drawable.ic_notes,
                            url = res.file_link
                        )
                    }.toMutableList()

                    rvModules.adapter = LearningAdapter(notesModules)
                } else {
                    Toast.makeText(this@HigherEduLearningActivity, "Failed to load notes", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.GetResourcesResponse>, t: Throwable) {
                Toast.makeText(this@HigherEduLearningActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadLectures() {
        // Fetch Video Links from API for Higher Education
        com.simats.rankup.network.BackendApiService.api.getResources("Higher Education").enqueue(object : retrofit2.Callback<com.simats.rankup.network.GetResourcesResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.simats.rankup.network.GetResourcesResponse>,
                response: retrofit2.Response<com.simats.rankup.network.GetResourcesResponse>
            ) {
                if (response.isSuccessful && response.body()?.error == null) {
                    val apiResources = response.body()?.resources ?: listOf()
                    
                    // Filter VIDEOs
                    val videoModules = apiResources.filter { it.resource_type == "VIDEO" }.map { res ->
                        LearningAdapter.LearningModule(
                            title = res.title,
                            type = "VIDEO",
                            author = "By: ${res.author ?: "Unknown"}",
                            size = if (!res.tags.isNullOrEmpty()) res.tags.uppercase() else "VIDEO LINK",
                            iconResId = R.drawable.ic_video,
                            url = res.file_link
                        )
                    }.toMutableList()

                    rvModules.adapter = LearningAdapter(videoModules)
                } else {
                    Toast.makeText(this@HigherEduLearningActivity, "Failed to load videos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.GetResourcesResponse>, t: Throwable) {
                Toast.makeText(this@HigherEduLearningActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_learn

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
                R.id.nav_learn -> true
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
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.nav_learn
    }
}
