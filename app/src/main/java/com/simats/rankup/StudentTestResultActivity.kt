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
        val tvCongrats = findViewById<TextView>(R.id.tvCongrats)
        
        tvScoreDisplay.text = "$score / $total"
        
        if (total > 0) {
            val percentage = (score.toDouble() / total) * 100
            if (percentage >= 80) {
                tvCongrats.text = "EXCELLENT WORK!"
            } else if (percentage >= 50) {
                tvCongrats.text = "GOOD JOB!"
            } else {
                tvCongrats.text = "KEEP PRACTICING!"
            }
        } else {
            tvCongrats.text = "TEST COMPLETED!"
        }

        findViewById<Button>(R.id.btnBackToHome).setOnClickListener {
            navigateBackToPortal()
        }

        findViewById<Button>(R.id.btnViewLeaderboard).setOnClickListener {
            val intentLeaderboard = Intent(this, RankingsActivity::class.java)
            startActivity(intentLeaderboard)
            finish()
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
