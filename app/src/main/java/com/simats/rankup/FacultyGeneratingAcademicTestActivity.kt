package com.simats.rankup

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.simats.rankup.network.AcademicBlueprintRequest
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.GeneratedAcademicTestResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FacultyGeneratingAcademicTestActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View
    private val animators = mutableListOf<ObjectAnimator>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_generating_academic_test)

        tvStatus = findViewById(R.id.tvStatus)
        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)
        dot3 = findViewById(R.id.dot3)

        startAnimatingDots()

        val payloadStr = intent.getStringExtra("REQUEST_PAYLOAD")
        if (payloadStr != null) {
            val request = Gson().fromJson(payloadStr, AcademicBlueprintRequest::class.java)
            generateAcademicTest(request)
        } else {
            Toast.makeText(this, "No blueprint data received.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startAnimatingDots() {
        val dots = listOf(dot1, dot2, dot3)
        for (i in dots.indices) {
            val animator = ObjectAnimator.ofFloat(dots[i], "alpha", 0.2f, 1f, 0.2f)
            animator.duration = 600
            animator.repeatCount = ValueAnimator.INFINITE
            animator.startDelay = (i * 200).toLong()
            animator.start()
            animators.add(animator)
        }
    }

    private fun stopAnimatingDots() {
        for (animator in animators) {
            animator.cancel()
        }
    }

    private fun generateAcademicTest(request: AcademicBlueprintRequest) {
        tvStatus.text = "CALLING AI FORGE..."
        
        BackendApiService.api.generateAcademicMockTest(request).enqueue(object : Callback<GeneratedAcademicTestResponse> {
            override fun onResponse(
                call: Call<GeneratedAcademicTestResponse>,
                response: Response<GeneratedAcademicTestResponse>
            ) {
                stopAnimatingDots()
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null && result.test_id != null) {
                        Toast.makeText(this@FacultyGeneratingAcademicTestActivity, "Success! Generated Academic Test ID: ${result.test_id}", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@FacultyGeneratingAcademicTestActivity, "Failed to generate academic test.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@FacultyGeneratingAcademicTestActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("GenerateTest", "Error Body: ${response.errorBody()?.string()}")
                    finish()
                }
            }

            override fun onFailure(call: Call<GeneratedAcademicTestResponse>, t: Throwable) {
                stopAnimatingDots()
                Toast.makeText(this@FacultyGeneratingAcademicTestActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("GenerateTest", "Exception", t)
                finish()
            }
        })
    }
}
