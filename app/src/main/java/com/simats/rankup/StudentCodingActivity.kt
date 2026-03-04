package com.simats.rankup

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.rankup.coding.CodingQuestion
import com.simats.rankup.coding.CompilerService
import com.simats.rankup.coding.PistonExclude
import com.simats.rankup.coding.PistonFile
import com.simats.rankup.coding.PistonResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.GenerateCodingDrillRequest
import com.simats.rankup.network.CodingDrillResponse
import com.simats.rankup.network.DynamicCodingDrill

class StudentCodingActivity : AppCompatActivity() {

    private lateinit var tvQuestionTitle: TextView
    private lateinit var tvQuestionDesc: TextView
    private lateinit var tvConstraints: TextView
    private lateinit var spinnerDifficulty: Spinner
    private lateinit var spinnerLanguage: Spinner
    private lateinit var etCodeEditor: EditText
    private lateinit var tvOutput: TextView
    
    private lateinit var etCustomInput: EditText
    
    private var currentQuestion: DynamicCodingDrill? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_coding)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        tvQuestionTitle = findViewById(R.id.tvQuestionTitle)
        tvQuestionDesc = findViewById(R.id.tvQuestionDesc)
        tvConstraints = findViewById(R.id.tvConstraints)
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        etCodeEditor = findViewById(R.id.etCodeEditor)
        etCustomInput = findViewById(R.id.etCustomInput)
        tvOutput = findViewById(R.id.tvOutput)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btnGenerateQuestion).setOnClickListener {
            val difficulty = spinnerDifficulty.selectedItem.toString()
            Toast.makeText(this, "Generating $difficulty AI Challenge...", Toast.LENGTH_SHORT).show()
            tvOutput.text = "Contacting AI Core for $difficulty Challenge..."
            
            val request = GenerateCodingDrillRequest(difficulty = difficulty)
            BackendApiService.api.generateCodingDrill(request).enqueue(object : Callback<CodingDrillResponse> {
                override fun onResponse(call: Call<CodingDrillResponse>, response: Response<CodingDrillResponse>) {
                    if (response.isSuccessful && response.body()?.error == null) {
                        val drill = response.body()?.drill
                        if (drill != null) {
                            currentQuestion = drill
                            displayQuestion(drill)
                        } else {
                            Toast.makeText(this@StudentCodingActivity, "Empty drill received.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@StudentCodingActivity, "Generation Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CodingDrillResponse>, t: Throwable) {
                    Toast.makeText(this@StudentCodingActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        findViewById<Button>(R.id.btnRun).setOnClickListener {
            runCode()
        }
    }

    private fun displayQuestion(question: DynamicCodingDrill) {
        tvQuestionTitle.text = question.title
        tvQuestionDesc.text = question.description
        tvConstraints.text = "Constraints:\n${question.constraints}\n\nInput Format:\n${question.input_format}"
        tvOutput.text = "Ready to compile..."
    }

    // Mock handling for Intent Data removed for Dynamic AI Focus

    private fun runCode() {
        val code = etCodeEditor.text.toString()
        if (code.isBlank()) {
            Toast.makeText(this, "Code cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val languageRaw = spinnerLanguage.selectedItem.toString()
        val (language, version) = mapLanguageToPiston(languageRaw)

        tvOutput.text = "Compiling and Running on HackerEarth Engine..."

        val customInputText = etCustomInput.text.toString().trim()
        
        var stdin = customInputText
        var expectedOutput: String? = null
        var isVerifying = false

        // If no custom input, use question's default test case
        if (customInputText.isEmpty() && currentQuestion != null) {
            stdin = currentQuestion!!.sample_input
            expectedOutput = currentQuestion!!.sample_output
            isVerifying = true
        }

        val request = PistonExclude(
            language = language,
            version = version,
            files = listOf(PistonFile(content = code)),
            stdin = stdin
        )

        CompilerService.api.executeCode(request).enqueue(object : Callback<PistonResponse> {
            override fun onResponse(call: Call<PistonResponse>, response: Response<PistonResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()?.run
                    if (result != null) {
                        val actualOutput = result.output.trim()
                        
                        if (isVerifying && expectedOutput != null) {
                            val status = if (actualOutput == expectedOutput.trim()) "✅ PASSED" else "❌ FAILED"
                            tvOutput.text = "Result: $status\n\nSample Input Data:\n$stdin\n\nExpected Output:\n$expectedOutput\n\nActual Output:\n$actualOutput"
                            
                            if (actualOutput == expectedOutput.trim()) {
                                Toast.makeText(this@StudentCodingActivity, "Correct Answer! +10 Coins", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (customInputText.isNotEmpty()) {
                                tvOutput.text = "Custom Input:\n$stdin\n\nOutput:\n$actualOutput"
                            } else {
                                tvOutput.text = actualOutput
                            }
                        }
                    } else {
                        tvOutput.text = "Error: No output received."
                    }
                } else {
                    tvOutput.text = "API Error: ${response.code()} ${response.message()}"
                }
            }

            override fun onFailure(call: Call<PistonResponse>, t: Throwable) {
                tvOutput.text = "Network Error: ${t.message}"
            }
        })
    }

    private fun mapLanguageToPiston(language: String): Pair<String, String> {
        return when (language.lowercase()) {
            "python" -> "python" to "3.10.0"
            "java" -> "java" to "15.0.2"
            "javascript" -> "javascript" to "18.15.0"
            "cpp" -> "c++" to "10.2.0"
            else -> "python" to "3.10.0"
        }
    }
}
