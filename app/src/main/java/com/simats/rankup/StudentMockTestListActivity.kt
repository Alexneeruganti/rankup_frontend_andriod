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
import com.simats.rankup.network.MockTestResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentMockTestListActivity : AppCompatActivity() {

    private lateinit var rvMockTests: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_mock_test_list)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        rvMockTests = findViewById(R.id.rvMockTests)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        progressBar = findViewById(R.id.progressBar)

        rvMockTests.layoutManager = LinearLayoutManager(this)
        fetchTests()
    }

    private fun fetchTests() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        
        BackendApiService.api.getMockTests().enqueue(object : Callback<GetMockTestsResponse> {
            override fun onResponse(call: Call<GetMockTestsResponse>, response: Response<GetMockTestsResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body()?.error == null) {
                    val tests = response.body()?.mock_tests ?: emptyList()
                    if (tests.isEmpty()) {
                        tvEmptyState.visibility = View.VISIBLE
                    } else {
                        rvMockTests.adapter = StudentMockTestAdapter(tests) { test ->
                            val intent = Intent(this@StudentMockTestListActivity, StudentTakeTestActivity::class.java)
                            intent.putExtra("TEST_ID", test.id)
                            intent.putExtra("TEST_TITLE", test.title)
                            intent.putExtra("DURATION", test.duration_minutes)
                            intent.putExtra("SOURCE_PORTAL", "Placement")
                            startActivity(intent)
                        }
                    }
                } else {
                    Toast.makeText(this@StudentMockTestListActivity, "Failed to load tests: ${response.body()?.error}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetMockTestsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@StudentMockTestListActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
