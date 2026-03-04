package com.simats.rankup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AdminAnnouncementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_announcement)

        // Back Button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Inputs
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etBody = findViewById<EditText>(R.id.etBody)
        val previewTitle = findViewById<TextView>(R.id.tvPreviewTitle)
        val previewBody = findViewById<TextView>(R.id.tvPreviewBody)

        // Live Preview - Title
        etTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                previewTitle.text = if (s.isNullOrEmpty()) "Announcement Title" else s
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Live Preview - Body
        etBody.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                previewBody.text = if (s.isNullOrEmpty()) "Your message will appear here..." else s
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Publish Button
        findViewById<Button>(R.id.btnPublish).setOnClickListener {
            val title = etTitle.text.toString().trim()
            val body = etBody.text.toString().trim()
            
            if (title.isEmpty() || body.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Get Scope
            val selectedScope = when {
                findViewById<RadioButton>(R.id.rbAll).isChecked -> "All Users"
                findViewById<RadioButton>(R.id.rbStudents).isChecked -> "Students Only"
                findViewById<RadioButton>(R.id.rbFaculty).isChecked -> "Faculty Only"
                findViewById<RadioButton>(R.id.rbHigherEdu).isChecked -> "Higher Education Students"
                else -> "Specific Department"
            }

            // Create Announcement Object
            val timestamp = System.currentTimeMillis()
            val dateString = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
            
            val request = com.simats.rankup.network.AnnouncementRequest(
                id = timestamp.toString(),
                title = title,
                message = body,
                audience = selectedScope,
                timestamp = timestamp,
                date_string = dateString
            )

            // Save using Network
            com.simats.rankup.network.BackendApiService.api.addAnnouncement(request).enqueue(object : retrofit2.Callback<com.simats.rankup.network.ApiResponse> {
                override fun onResponse(call: retrofit2.Call<com.simats.rankup.network.ApiResponse>, response: retrofit2.Response<com.simats.rankup.network.ApiResponse>) {
                    if (response.isSuccessful && response.body()?.error == null) {
                        Toast.makeText(this@AdminAnnouncementActivity, "Announcement Published to $selectedScope!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@AdminAnnouncementActivity, "Failed to publish: ${response.body()?.error}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.ApiResponse>, t: Throwable) {
                    Toast.makeText(this@AdminAnnouncementActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
