package com.simats.rankup

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AdminContentHubActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminContentAdapter
    private lateinit var emptyStateView: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_content_hub)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        emptyStateView = findViewById(R.id.emptyState)
        recyclerView = findViewById(R.id.recyclerContentHub)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdminContentAdapter(emptyList()) { module ->
            showDeleteConfirmation(module)
        }
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadContent()
    }

    private fun loadContent() {
        val progressBar = android.widget.ProgressBar(this).apply {
             layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = android.view.Gravity.CENTER }
        }
        val contentContainer = recyclerView.parent as android.view.ViewGroup
        contentContainer.addView(progressBar)

        com.simats.rankup.network.BackendApiService.api.getResources().enqueue(object : retrofit2.Callback<com.simats.rankup.network.GetResourcesResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.rankup.network.GetResourcesResponse>, response: retrofit2.Response<com.simats.rankup.network.GetResourcesResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body()?.error == null) {
                    val apiResources = response.body()?.resources ?: emptyList()
                    val modules = apiResources.map { res ->
                        LearningAdapter.LearningModule(
                            title = res.title,
                            type = res.resource_type,
                            author = res.author ?: "Unknown",
                            size = if (!res.tags.isNullOrEmpty()) res.tags.uppercase() else res.resource_type,
                            iconResId = if (res.resource_type == "VIDEO") R.drawable.ic_video else R.drawable.ic_notes,
                            url = res.file_link,
                            id = res.id // We'll need to add id to LearningModule or use a Tag
                        )
                    }
                    
                    if (modules.isEmpty()) {
                        emptyStateView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        emptyStateView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateData(modules)
                    }
                } else {
                    Toast.makeText(this@AdminContentHubActivity, "Failed to load content", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.GetResourcesResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@AdminContentHubActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmation(module: LearningAdapter.LearningModule) {
        AlertDialog.Builder(this)
            .setTitle("Delete Content")
            .setMessage("Are you sure you want to delete '${module.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                val resourceId = module.id // Assuming we added id to LearningModule
                if (resourceId != -1) {
                    com.simats.rankup.network.BackendApiService.api.deleteResource(resourceId).enqueue(object : retrofit2.Callback<com.simats.rankup.network.ApiResponse> {
                        override fun onResponse(call: retrofit2.Call<com.simats.rankup.network.ApiResponse>, response: retrofit2.Response<com.simats.rankup.network.ApiResponse>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@AdminContentHubActivity, "Content deleted successfully", Toast.LENGTH_SHORT).show()
                                loadContent()
                            } else {
                                Toast.makeText(this@AdminContentHubActivity, "Failed to delete content", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.ApiResponse>, t: Throwable) {
                            Toast.makeText(this@AdminContentHubActivity, "Network Error", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    // Fallback for local content if any (shouldn't happen now)
                    ContentManager.deleteContent(this, module)
                    loadContent()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
