package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.GetMockTestsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HigherEduMockTestListActivity : AppCompatActivity() {

    private lateinit var rvMockTests: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_higher_edu_mock_test_list)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        rvMockTests = findViewById(R.id.rvMockTests)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        progressBar = findViewById(R.id.progressBar)

        rvMockTests.layoutManager = LinearLayoutManager(this)
        fetchAcademicTests()
    }

    private fun fetchAcademicTests() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        
        BackendApiService.api.getMockTests(category = "Higher Education").enqueue(object : Callback<GetMockTestsResponse> {
            override fun onResponse(call: Call<GetMockTestsResponse>, response: Response<GetMockTestsResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body()?.error == null) {
                    val tests = response.body()?.mock_tests ?: emptyList()
                    if (tests.isEmpty()) {
                        tvEmptyState.visibility = View.VISIBLE
                    } else {
                        // Reusing StudentMockTestAdapter as it just binds the test title and duration
                        rvMockTests.adapter = StudentMockTestAdapter(tests) { test ->
                            val intent = Intent(this@HigherEduMockTestListActivity, StudentTakeTestActivity::class.java)
                            intent.putExtra("TEST_ID", test.id)
                            intent.putExtra("TEST_TITLE", test.title)
                            intent.putExtra("DURATION", test.duration_minutes)
                            intent.putExtra("SOURCE_PORTAL", "HigherEducation")
                            startActivity(intent)
                        }
                    }
                } else {
                    Toast.makeText(this@HigherEduMockTestListActivity, "Failed to load tests: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetMockTestsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@HigherEduMockTestListActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
