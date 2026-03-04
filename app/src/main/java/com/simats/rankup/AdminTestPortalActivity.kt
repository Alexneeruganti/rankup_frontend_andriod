package com.simats.rankup

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.rankup.network.ApiResponse
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.GetMockTestsResponse
import com.simats.rankup.network.MockTestResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminTestPortalActivity : AppCompatActivity() {

    private lateinit var rvAdminMockTests: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: AdminMockTestAdapter
    private val testsList = mutableListOf<MockTestResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_test_portal)

        rvAdminMockTests = findViewById(R.id.rvAdminMockTests)
        llEmptyState = findViewById(R.id.llEmptyState)
        progressBar = findViewById(R.id.progressBar)

        rvAdminMockTests.layoutManager = LinearLayoutManager(this)
        adapter = AdminMockTestAdapter(testsList) { test, pos -> deleteTest(test, pos) }
        rvAdminMockTests.adapter = adapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        fetchMockTests()
    }

    private fun fetchMockTests() {
        progressBar.visibility = View.VISIBLE
        rvAdminMockTests.visibility = View.GONE
        llEmptyState.visibility = View.GONE

        BackendApiService.api.getMockTests().enqueue(object : Callback<GetMockTestsResponse> {
            override fun onResponse(call: Call<GetMockTestsResponse>, response: Response<GetMockTestsResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body()?.error == null) {
                    testsList.clear()
                    response.body()?.mock_tests?.let { testsList.addAll(it) }
                    
                    if (testsList.isEmpty()) {
                        llEmptyState.visibility = View.VISIBLE
                    } else {
                        rvAdminMockTests.visibility = View.VISIBLE
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(this@AdminTestPortalActivity, "Failed: ${response.body()?.error}", Toast.LENGTH_SHORT).show()
                    llEmptyState.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<GetMockTestsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@AdminTestPortalActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                llEmptyState.visibility = View.VISIBLE
            }
        })
    }
    
    private fun deleteTest(test: MockTestResponse, position: Int) {
        Toast.makeText(this, "Deleting ${test.title}...", Toast.LENGTH_SHORT).show()
        
        BackendApiService.api.deleteMockTest(test.id).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.error == null) {
                    Toast.makeText(this@AdminTestPortalActivity, "Deleted successfully", Toast.LENGTH_SHORT).show()
                    adapter.removeAt(position)
                    
                    if (testsList.isEmpty()) {
                        rvAdminMockTests.visibility = View.GONE
                        llEmptyState.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@AdminTestPortalActivity, "Delete Failed: ${response.body()?.error}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@AdminTestPortalActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
