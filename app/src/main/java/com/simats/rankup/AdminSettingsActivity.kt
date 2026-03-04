package com.simats.rankup

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.simats.rankup.network.AppSettingsRequest
import com.simats.rankup.network.AppSettingsResponse
import com.simats.rankup.network.ApiResponse
import com.simats.rankup.network.BackendApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminSettingsActivity : AppCompatActivity() {

    private lateinit var switchAptitude: SwitchMaterial
    private lateinit var switchCoding: SwitchMaterial
    private lateinit var switchLeaderboard: SwitchMaterial
    private lateinit var switchResources: SwitchMaterial
    private lateinit var switchPush: SwitchMaterial
    private lateinit var switchEmail: SwitchMaterial
    private lateinit var switchMaintenance: SwitchMaterial
    private lateinit var switchRegistrations: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_settings)

        // Init views
        switchAptitude = findViewById(R.id.switchAptitude)
        switchCoding = findViewById(R.id.switchCoding)
        switchLeaderboard = findViewById(R.id.switchLeaderboard)
        switchResources = findViewById(R.id.switchResources)
        switchPush = findViewById(R.id.switchPush)
        switchEmail = findViewById(R.id.switchEmail)
        switchMaintenance = findViewById(R.id.switchMaintenance)
        switchRegistrations = findViewById(R.id.switchRegistrations)

        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Fetch current settings on load
        fetchSettings()

        // Save
        findViewById<Button>(R.id.btnSaveSettings).setOnClickListener {
            saveSettings()
        }
    }

    private fun fetchSettings() {
        BackendApiService.api.getAdminSettings().enqueue(object : Callback<AppSettingsResponse> {
            override fun onResponse(call: Call<AppSettingsResponse>, response: Response<AppSettingsResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.settings != null) {
                    val settings = body.settings
                    switchAptitude.isChecked = settings.aptitude_tests
                    switchCoding.isChecked = settings.coding_practice
                    switchLeaderboard.isChecked = settings.leaderboard
                    switchResources.isChecked = settings.learning_resources
                    switchPush.isChecked = settings.push_notifications
                    switchEmail.isChecked = settings.email_notifications
                    switchMaintenance.isChecked = settings.maintenance_mode
                    switchRegistrations.isChecked = settings.new_registrations
                } else {
                    Toast.makeText(this@AdminSettingsActivity, "Failed to load current settings", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AppSettingsResponse>, t: Throwable) {
                Toast.makeText(this@AdminSettingsActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveSettings() {
        val request = AppSettingsRequest(
            aptitude_tests = switchAptitude.isChecked,
            coding_practice = switchCoding.isChecked,
            leaderboard = switchLeaderboard.isChecked,
            learning_resources = switchResources.isChecked,
            push_notifications = switchPush.isChecked,
            email_notifications = switchEmail.isChecked,
            maintenance_mode = switchMaintenance.isChecked,
            new_registrations = switchRegistrations.isChecked
        )

        BackendApiService.api.updateAdminSettings(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.error == null) {
                    Toast.makeText(this@AdminSettingsActivity, "Settings Saved Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AdminSettingsActivity, "Failed to save: ${response.body()?.error ?: "Unknown API response"}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@AdminSettingsActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
