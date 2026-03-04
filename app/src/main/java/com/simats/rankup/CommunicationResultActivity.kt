package com.simats.rankup

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CommunicationResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_communication_result)

        val score = intent.getIntExtra("SCORE", 0)
        val total = intent.getIntExtra("TOTAL", 3)

        val tvResultStatus = findViewById<TextView>(R.id.tvResultStatus)
        val tvFinalScore = findViewById<TextView>(R.id.tvFinalScore)
        val tvFeedbackMsg = findViewById<TextView>(R.id.tvFeedbackMsg)
        val ivResultIcon = findViewById<ImageView>(R.id.ivResultIcon)
        val progressBar = findViewById<ProgressBar>(R.id.progressScore)
        val btnFinish = findViewById<Button>(R.id.btnFinish)

        tvFinalScore.text = "$score / $total"
        
        val percentage = (score.toFloat() / total.toFloat() * 100).toInt()
        progressBar.progress = percentage

        if (score == total) {
            tvResultStatus.text = "EXCELLENT!"
            tvFeedbackMsg.text = "Perfect score! You have outstanding communication skills."
            ivResultIcon.setImageResource(R.drawable.ic_check_circle)
        } else if (score >= total / 2) {
            tvResultStatus.text = "GOOD JOB!"
            tvFeedbackMsg.text = "Well done! You passed the communication round, but there's room for improvement."
            ivResultIcon.setImageResource(R.drawable.ic_medal)
        } else {
            tvResultStatus.text = "KEEP PRACTICING"
            tvFeedbackMsg.text = "Don't give up! Review the areas where you missed marks and try again."
            ivResultIcon.setImageResource(R.drawable.ic_warning)
        }

        btnFinish.setOnClickListener {
            finish()
        }
    }
}
