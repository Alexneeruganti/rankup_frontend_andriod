package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity

class CareerChoiceActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_career_choice)

        val btnLaunchCareer = findViewById<Button>(R.id.btnLaunchCareer)
        val btnStartAcademic = findViewById<Button>(R.id.btnStartAcademic)

        btnLaunchCareer.setOnClickListener {
            val scaleAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_button_click)
            btnLaunchCareer.startAnimation(scaleAnimation)
            
            val intent = Intent(this, PlacementLoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnStartAcademic.setOnClickListener {
            val scaleAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_button_click)
            btnStartAcademic.startAnimation(scaleAnimation)

            val intent = Intent(this, HigherEduLoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun navigateToMain(goal: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("GOAL_SELECTION", goal)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
