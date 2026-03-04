package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val frameTrack = findViewById<android.widget.FrameLayout>(R.id.frameTrack)
        val viewCar = findViewById<android.view.View>(R.id.viewCar)

        // Wait for layout to determine widths
        viewCar.post {
            val trackWidth = frameTrack.width.toFloat()
            val carWidth = viewCar.width.toFloat()
            val distance = trackWidth - carWidth

            // Animate translationX from 0 to distance
            val animator = android.animation.ObjectAnimator.ofFloat(viewCar, "translationX", 0f, distance)
            animator.duration = 700 // 1 second per pass
            animator.repeatCount = 1 // Play once + Repeat 2 times = 3 cycles
            animator.repeatMode = android.animation.ValueAnimator.RESTART
            animator.interpolator = android.view.animation.LinearInterpolator()
            
            animator.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    super.onAnimationEnd(animation)
                    val intent = Intent(this@SplashActivity, OnboardingActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            })
            animator.start()
        }
    }
}
