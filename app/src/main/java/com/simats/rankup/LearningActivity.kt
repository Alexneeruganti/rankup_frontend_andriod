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

import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class LearningActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tabNotes: LinearLayout
    private lateinit var tabLectures: LinearLayout
    private lateinit var textNotes: TextView
    private lateinit var textLectures: TextView
    private lateinit var iconNotes: ImageView
    private lateinit var iconLectures: ImageView
    private lateinit var rvModules: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learning)

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navView)

        // Menu Button
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
        
        val navFooter = findViewById<View>(R.id.navFooter)
        val btnTerminateSession = navFooter?.findViewById<View>(R.id.btnTerminateSession) ?: findViewById(R.id.btnTerminateSession)
        
        btnTerminateSession?.setOnClickListener {
            Toast.makeText(this, "Terminating Session...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PlacementLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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

        // Setup Tabs
        setupTabLogic()

        // Fetch global settings to determine if Learning is enabled
        checkLearningEnabled()

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_learn // Set active item
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, StudentHomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_ranks -> {
                    startActivity(Intent(this, RankingsActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    overridePendingTransition(0, 0)
                    finish() // Close this activity to return to hierarchy or keep stack clean
                    true
                }
                R.id.nav_learn -> true
                R.id.nav_profile -> {
                     startActivity(Intent(this, ProfileActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun checkLearningEnabled() {
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
                    rvModules.visibility = View.VISIBLE
                    loadNotes()
                } else {
                    emptyMessage.visibility = View.VISIBLE
                    Toast.makeText(this@LearningActivity, "Learning resources are currently disabled.", Toast.LENGTH_SHORT).show()
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
            loadNotes()
        }

        tabLectures.setOnClickListener {
            updateTabVisuals(isNotes = false)
            loadLectures()
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
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val studentId = sharedPref.getInt("USER_ID", -1)
        
        com.simats.rankup.network.BackendApiService.api.getResources("Placement", if (studentId != -1) studentId else null).enqueue(object : retrofit2.Callback<com.simats.rankup.network.GetResourcesResponse> {
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
                            url = res.file_link,
                            id = res.id
                        )
                    }.toMutableList()

                    rvModules.adapter = LearningAdapter(notesModules)
                } else {
                    Toast.makeText(this@LearningActivity, "Failed to load notes", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.GetResourcesResponse>, t: Throwable) {
                Toast.makeText(this@LearningActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadLectures() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val studentId = sharedPref.getInt("USER_ID", -1)

        com.simats.rankup.network.BackendApiService.api.getResources("Placement", if (studentId != -1) studentId else null).enqueue(object : retrofit2.Callback<com.simats.rankup.network.GetResourcesResponse> {
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
                            url = res.file_link,
                            id = res.id
                        )
                    }.toMutableList()

                    rvModules.adapter = LearningAdapter(videoModules)
                } else {
                    Toast.makeText(this@LearningActivity, "Failed to load videos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.GetResourcesResponse>, t: Throwable) {
                Toast.makeText(this@LearningActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    override fun onResume() {
        super.onResume()
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_learn
        
        // Ensure data is freshly reloaded from DB if user backgrounded app after a deletion
        checkLearningEnabled()
    }
}
