package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ProgressBar
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.GetCompanyQuestionsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentCompanyQuestionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_company_questions)

        try {
            findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

            recyclerView = findViewById(R.id.recyclerQuestions)
            recyclerView.layoutManager = LinearLayoutManager(this)
        } catch (e: Exception) {
            showErrorDialog(e.toString())
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
                        onClick = { question ->
                            val intent = Intent(this@StudentCompanyQuestionsActivity, StudentCodingActivity::class.java)
                            intent.putExtra("EXTRA_QUESTION_TITLE", question.title)
                            intent.putExtra("EXTRA_DESC", question.description)
                            intent.putExtra("EXTRA_CONSTRAINTS", question.constraints)
                            intent.putExtra("EXTRA_INPUT_FORMAT", question.inputFormat)
                            startActivity(intent)
                        }
                    )
                } else {
                    android.widget.Toast.makeText(this@StudentCompanyQuestionsActivity, "Failed to load questions", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetCompanyQuestionsResponse>, t: Throwable) {
                if (progressBar != null) progressBar.visibility = View.GONE
                android.widget.Toast.makeText(this@StudentCompanyQuestionsActivity, "Network Error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showErrorDialog(errorMessage: String) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Error Opening Page")
            .setMessage(errorMessage)
            .setPositiveButton("Copy Error") { _, _ ->
                val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Error Message", errorMessage)
                clipboard.setPrimaryClip(clip)
                android.widget.Toast.makeText(this, "Error copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Close") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .create()
        dialog.show()
    }
}

data class CompanyQuestion(
    val id: Int,
    val company: String,
    val title: String,
    val difficulty: String,
    val description: String,
    val constraints: String,
    val inputFormat: String
)

class CompanyQuestionAdapter(
    private val questions: List<CompanyQuestion>,
    private val isFaculty: Boolean = false,
    private val onClick: (CompanyQuestion) -> Unit,
    private val onDelete: ((CompanyQuestion) -> Unit)? = null
) : RecyclerView.Adapter<CompanyQuestionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCompany: TextView = view.findViewById(R.id.tvCompany)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDifficulty: TextView = view.findViewById(R.id.tvDifficulty)
        val btnDelete: ImageButton? = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_company_question, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val question = questions[position]
        holder.tvCompany.text = question.company
        holder.tvTitle.text = question.title
        holder.tvDifficulty.text = question.difficulty
        holder.itemView.setOnClickListener { onClick(question) }
        
        if (isFaculty) {
            holder.btnDelete?.visibility = View.VISIBLE
            holder.btnDelete?.setOnClickListener { onDelete?.invoke(question) }
        } else {
            holder.btnDelete?.visibility = View.GONE
        }
    }

    override fun getItemCount() = questions.size
}
