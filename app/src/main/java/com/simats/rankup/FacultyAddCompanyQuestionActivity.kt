package com.simats.rankup

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.rankup.network.AddCompanyQuestionRequest
import com.simats.rankup.network.ApiResponse
import com.simats.rankup.network.BackendApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FacultyAddCompanyQuestionActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_add_company_question)

        try {
            setupUI()
        } catch (e: Exception) {
            showErrorDialog(e.toString())
        }
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

    private fun setupUI() {
        progressBar = findViewById(R.id.progressBar)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val spinnerDifficulty = findViewById<Spinner>(R.id.spinnerDifficulty)
        ArrayAdapter.createFromResource(
            this,
            R.array.difficulty_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDifficulty.adapter = adapter
        }

        val btnPublish = findViewById<Button>(R.id.btnPublish)
        btnPublish.setOnClickListener {
            if (validateInputs()) {
                val company = findViewById<EditText>(R.id.etCompanyName).text.toString().trim()
                val title = findViewById<EditText>(R.id.etQuestionTitle).text.toString().trim()
                val diff = spinnerDifficulty.selectedItem.toString()
                val desc = findViewById<EditText>(R.id.etDescription).text.toString().trim()
                val constraints = findViewById<EditText>(R.id.etConstraints).text.toString().trim()
                val inputFmt = findViewById<EditText>(R.id.etInputFormat).text.toString().trim()

                btnPublish.isEnabled = false
                progressBar.visibility = View.VISIBLE

                val request = AddCompanyQuestionRequest(company, title, diff, desc, constraints, inputFmt)
                BackendApiService.api.addCompanyQuestion(request).enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        btnPublish.isEnabled = true
                        progressBar.visibility = View.GONE
                        if (response.isSuccessful) {
                            Toast.makeText(this@FacultyAddCompanyQuestionActivity, "Question Published Successfully!", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this@FacultyAddCompanyQuestionActivity, "Failed to publish question", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        btnPublish.isEnabled = true
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@FacultyAddCompanyQuestionActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (findViewById<EditText>(R.id.etCompanyName).text.isEmpty()) {
            Toast.makeText(this, "Enter Company Name", Toast.LENGTH_SHORT).show()
            return false
        }
        if (findViewById<EditText>(R.id.etQuestionTitle).text.isEmpty()) {
            Toast.makeText(this, "Enter Question Title", Toast.LENGTH_SHORT).show()
            return false
        }
        if (findViewById<EditText>(R.id.etDescription).text.isEmpty()) {
            Toast.makeText(this, "Enter Description", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
