package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StudentTestResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_test_result)

        val score = intent.getIntExtra("SCORE", 0)
        val total = intent.getIntExtra("TOTAL", 0)

        val tvScoreDisplay = findViewById<TextView>(R.id.tvScoreDisplay)
        tvScoreDisplay.text = "$score / $total"

        findViewById<Button>(R.id.btnBackToHome).setOnClickListener {
            navigateBackToPortal()
        }
    }
    
    // Disable back button to prevent returning to a submitted test
    override fun onBackPressed() {
        navigateBackToPortal()
    }

    private fun navigateBackToPortal() {
        val sourcePortal = intent.getStringExtra("SOURCE_PORTAL")
        val targetActivity = if (sourcePortal == "HigherEducation") {
            HigherEduHomeActivity::class.java
        } else {
            StudentHomeActivity::class.java
        }

        val intentOut = Intent(this, targetActivity)
        intentOut.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intentOut)
        finish()
    }
}
