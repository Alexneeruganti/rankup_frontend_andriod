package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class GetStartedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)

        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        btnGetStarted.setOnClickListener {
            // Navigate to the Career Choice Screen
            val intent = Intent(this, CareerChoiceActivity::class.java)
            startActivity(intent)
            // finish() // Optional: Keep back stack so user can go back
        }
    }
}
