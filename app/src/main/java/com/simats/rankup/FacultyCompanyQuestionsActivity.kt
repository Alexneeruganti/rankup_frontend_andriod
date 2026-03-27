package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ProgressBar
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.ApiResponse
import com.simats.rankup.network.GetCompanyQuestionsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FacultyCompanyQuestionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_company_questions)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerQuestions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<FloatingActionButton>(R.id.fabAddQuestion).setOnClickListener {
            startActivity(Intent(this, FacultyAddCompanyQuestionActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchQuestions()
    }

    private fun fetchQuestions() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        if (progressBar != null) progressBar.visibility = View.VISIBLE
        
        BackendApiService.api.getCompanyQuestions().enqueue(object : Callback<GetCompanyQuestionsResponse> {
            override fun onResponse(call: Call<GetCompanyQuestionsResponse>, response: Response<GetCompanyQuestionsResponse>) {
                if (progressBar != null) progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val apiQuestions = response.body()?.questions ?: emptyList()
                    val questions = apiQuestions.map { 
                        CompanyQuestion(it.id, it.company, it.title, it.difficulty, it.description, it.constraints, it.input_format)
                    }
                    
                    recyclerView.adapter = CompanyQuestionAdapter(
                        questions = questions,
                        isFaculty = true,
                        onClick = { question ->
                            Toast.makeText(this@FacultyCompanyQuestionsActivity, "Viewing: ${question.title}", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = { question ->
                            BackendApiService.api.deleteCompanyQuestion(question.id).enqueue(object : Callback<ApiResponse> {
                                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(this@FacultyCompanyQuestionsActivity, "Question Deleted", Toast.LENGTH_SHORT).show()
                                        fetchQuestions() // Refresh list
                                    } else {
                                        Toast.makeText(this@FacultyCompanyQuestionsActivity, "Delete Failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                                    Toast.makeText(this@FacultyCompanyQuestionsActivity, "Network Error", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    )
                } else {
                    Toast.makeText(this@FacultyCompanyQuestionsActivity, "Failed to load questions", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetCompanyQuestionsResponse>, t: Throwable) {
                if (progressBar != null) progressBar.visibility = View.GONE
                Toast.makeText(this@FacultyCompanyQuestionsActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
