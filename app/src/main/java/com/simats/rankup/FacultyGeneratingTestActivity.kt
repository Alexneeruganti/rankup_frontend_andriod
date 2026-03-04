package com.simats.rankup

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.GenerateTestRequest
import com.simats.rankup.network.GenerateTestResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FacultyGeneratingTestActivity : AppCompatActivity() {

    private lateinit var dots: List<View>
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var currentDotIndex = 0

    private val animateDots = object : java.lang.Runnable {
        override fun run() {
            if (!this@FacultyGeneratingTestActivity.isFinishing && ::dots.isInitialized) {
                // Reset all dots to 0.3 alpha
                dots.forEach { it.alpha = 0.3f }
                // Highlight current dot
                if (dots.isNotEmpty()) {
                    dots[currentDotIndex].alpha = 1.0f
                    currentDotIndex = (currentDotIndex + 1) % dots.size
                }
                // User requested a slow version of moving dots
                handler.postDelayed(this, 400)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_generating_test)

        dots = listOf(
            findViewById(R.id.dot1),
            findViewById(R.id.dot2),
            findViewById(R.id.dot3),
            findViewById(R.id.dot4),
            findViewById(R.id.dot5)
        )
        
        handler.post(animateDots)

        val jsonRequest = intent.getStringExtra("REQUEST_PAYLOAD")
        if (jsonRequest != null) {
             try {
                val request = Gson().fromJson(jsonRequest, GenerateTestRequest::class.java)
                generateTest(request)
             } catch (e: Exception) {
                Toast.makeText(this, "Failed to parse test settings", Toast.LENGTH_SHORT).show()
                finish()
             }
        } else {
            Toast.makeText(this, "No test settings provided", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(animateDots)
    }

    private fun generateTest(request: GenerateTestRequest) {
        BackendApiService.api.generateMockTest(request).enqueue(object : Callback<GenerateTestResponse> {
            override fun onResponse(
                call: Call<GenerateTestResponse>,
                response: Response<GenerateTestResponse>
            ) {
                if (response.isSuccessful && response.body()?.error == null) {
                    Toast.makeText(this@FacultyGeneratingTestActivity, "Mock Test Generated Successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@FacultyGeneratingTestActivity, "Generation Failed: ${response.body()?.error}", Toast.LENGTH_LONG).show()
                }
                finish()
            }

            override fun onFailure(call: Call<GenerateTestResponse>, t: Throwable) {
                Toast.makeText(this@FacultyGeneratingTestActivity, "Timeout/Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        })
    }
}
