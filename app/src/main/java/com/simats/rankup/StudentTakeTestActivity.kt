package com.simats.rankup

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.GetMockTestQuestionsResponse
import com.simats.rankup.network.MockTestQuestion
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.simats.rankup.coding.CompilerService
import com.simats.rankup.coding.PistonExclude
import com.simats.rankup.coding.PistonFile
import com.simats.rankup.coding.PistonResponse

class StudentTakeTestActivity : AppCompatActivity() {

    private lateinit var tvTestTitle: TextView
    private lateinit var tvTimer: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollContent: ScrollView
    private lateinit var tvQuestionNumber: TextView
    private lateinit var tvQuestionText: TextView
    private lateinit var rgOptions: RadioGroup
    private lateinit var rbOptionA: RadioButton
    private lateinit var rbOptionB: RadioButton
    private lateinit var rbOptionC: RadioButton
    private lateinit var rbOptionD: RadioButton
    private lateinit var btnNext: Button

    // Coding Elements
    private lateinit var llCodingEditor: LinearLayout
    private lateinit var tvCodingConstraints: TextView
    private lateinit var spinnerLanguage: Spinner
    private lateinit var etCodeEditor: EditText
    private lateinit var tvOutput: TextView
    private lateinit var btnRunCode: Button

    private var questions: List<MockTestQuestion> = emptyList()
    private var currentQuestionIndex = 0
    private var score = 0
    private var codingPassedList = mutableSetOf<Int>() // Tracks indices of passed coding questions
    private var testId: Int = -1
    private var testDurationMinutes = 10
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_take_test)

        testId = intent.getIntExtra("TEST_ID", -1)
        val title = intent.getStringExtra("TEST_TITLE") ?: "Mock Test"
        testDurationMinutes = intent.getIntExtra("DURATION", 10)

        initViews()
        tvTestTitle.text = title

        if (testId != -1) {
            fetchQuestions()
        } else {
            Toast.makeText(this, "Invalid Test ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        tvTestTitle = findViewById(R.id.tvTestTitle)
        tvTimer = findViewById(R.id.tvTimer)
        progressBar = findViewById(R.id.progressBar)
        scrollContent = findViewById(R.id.scrollContent)
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber)
        tvQuestionText = findViewById(R.id.tvQuestionText)
        rgOptions = findViewById(R.id.rgOptions)
        rbOptionA = findViewById(R.id.rbOptionA)
        rbOptionB = findViewById(R.id.rbOptionB)
        rbOptionC = findViewById(R.id.rbOptionC)
        rbOptionD = findViewById(R.id.rbOptionD)
        btnNext = findViewById(R.id.btnNext)

        llCodingEditor = findViewById(R.id.llCodingEditor)
        tvCodingConstraints = findViewById(R.id.tvCodingConstraints)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        etCodeEditor = findViewById(R.id.etCodeEditor)
        tvOutput = findViewById(R.id.tvOutput)
        btnRunCode = findViewById(R.id.btnRunCode)

        scrollContent.visibility = View.GONE

        btnNext.setOnClickListener {
            handleNextClick()
        }
        btnRunCode.setOnClickListener {
            runCode()
        }
    }

    private fun fetchQuestions() {
        progressBar.visibility = View.VISIBLE
        BackendApiService.api.getMockTestQuestions(testId).enqueue(object : Callback<GetMockTestQuestionsResponse> {
            override fun onResponse(call: Call<GetMockTestQuestionsResponse>, response: Response<GetMockTestQuestionsResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body()?.error == null) {
                    questions = response.body()?.questions ?: emptyList()
                    if (questions.isNotEmpty()) {
                        scrollContent.visibility = View.VISIBLE
                        startTimer()
                        loadQuestion(0)
                    } else {
                        Toast.makeText(this@StudentTakeTestActivity, "No questions found for this test.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@StudentTakeTestActivity, "Failed to load questions.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetMockTestQuestionsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@StudentTakeTestActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startTimer() {
        val totalMillis = testDurationMinutes * 60 * 1000L
        countDownTimer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                tvTimer.text = "00:00"
                Toast.makeText(this@StudentTakeTestActivity, "Time's up!", Toast.LENGTH_SHORT).show()
                submitTest()
            }
        }.start()
    }

    private fun loadQuestion(index: Int) {
        rgOptions.clearCheck()
        etCodeEditor.text.clear()
        tvOutput.text = "Awaiting standard execution log..."
        
        val q = questions[index]
        tvQuestionNumber.text = "Question ${index + 1} of ${questions.size} • ${q.module_name} (${q.difficulty})"
        tvQuestionText.text = q.question_text

        if (q.question_type == "CODING") {
            rgOptions.visibility = View.GONE
            llCodingEditor.visibility = View.VISIBLE
            tvCodingConstraints.text = "Constraints:\n${q.constraints ?: "None"}\n\nSample Input:\n${q.sample_input ?: "None"}\n\nExpected Output:\n${q.sample_output ?: "None"}"
        } else {
            llCodingEditor.visibility = View.GONE
            rgOptions.visibility = View.VISIBLE
            rbOptionA.text = q.option_a
            rbOptionB.text = q.option_b
            rbOptionC.text = q.option_c
            rbOptionD.text = q.option_d
        }

        if (index == questions.size - 1) {
            btnNext.text = "Submit Test"
        } else {
            btnNext.text = "Next Question"
        }
    }

    private fun handleNextClick() {
        val q = questions[currentQuestionIndex]
        
        if (q.question_type == "MCQ") {
            if (rgOptions.checkedRadioButtonId == -1) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
                return
            }

            var selectedOption = ""
            when (rgOptions.checkedRadioButtonId) {
                R.id.rbOptionA -> selectedOption = "A"
                R.id.rbOptionB -> selectedOption = "B"
                R.id.rbOptionC -> selectedOption = "C"
                R.id.rbOptionD -> selectedOption = "D"
            }

            if (selectedOption == q.correct_option) {
                score++
            }
        } else {
            // It's a coding question, check if they passed it already
            if (codingPassedList.contains(currentQuestionIndex)) {
                // Already scored in runCode
            } else {
                Toast.makeText(this, "Moving on without passing the tests.", Toast.LENGTH_SHORT).show()
            }
        }

        currentQuestionIndex++
        if (currentQuestionIndex < questions.size) {
            loadQuestion(currentQuestionIndex)
        } else {
            submitTest()
        }
    }

    private fun runCode() {
        val code = etCodeEditor.text.toString()
        if (code.isBlank()) {
            Toast.makeText(this, "Code cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val q = questions[currentQuestionIndex]
        val languageRaw = spinnerLanguage.selectedItem.toString()
        val (language, version) = mapLanguageToPiston(languageRaw)

        tvOutput.text = "Compiling and Running..."

        val expectedOutput = q.sample_output?.trim()

        val request = PistonExclude(
            language = language,
            version = version,
            files = listOf(PistonFile(content = code)),
            stdin = q.sample_input ?: ""
        )

        btnRunCode.isEnabled = false

        CompilerService.api.executeCode(request).enqueue(object : Callback<PistonResponse> {
            override fun onResponse(call: Call<PistonResponse>, response: Response<PistonResponse>) {
                btnRunCode.isEnabled = true
                if (response.isSuccessful) {
                    val result = response.body()?.run
                    if (result != null) {
                        val actualOutput = result.output.trim()
                        
                        if (actualOutput == expectedOutput) {
                            tvOutput.text = "Result: ✅ PASSED\n\nActual Output:\n$actualOutput"
                            Toast.makeText(this@StudentTakeTestActivity, "Test Cases Passed! Marks Secured.", Toast.LENGTH_SHORT).show()
                            if (!codingPassedList.contains(currentQuestionIndex)) {
                                score++
                                codingPassedList.add(currentQuestionIndex)
                            }
                        } else {
                            tvOutput.text = "Result: ❌ FAILED\n\nExpected:\n$expectedOutput\n\nActual:\n$actualOutput"
                        }
                    } else {
                        tvOutput.text = "Error: No output received."
                    }
                } else {
                    tvOutput.text = "API Error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<PistonResponse>, t: Throwable) {
                btnRunCode.isEnabled = true
                tvOutput.text = "Network Error: ${t.message}"
            }
        })
    }

    private fun mapLanguageToPiston(language: String): Pair<String, String> {
        return when (language.lowercase()) {
            "python" -> "python" to "3.10.0"
            "java" -> "java" to "15.0.2"
            "javascript" -> "javascript" to "18.15.0"
            "cpp", "c++" -> "c++" to "10.2.0"
            else -> "python" to "3.10.0"
        }
    }

    private fun submitTest() {
        countDownTimer?.cancel()
        
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val studentId = sharedPref.getInt("USER_ID", 1)

        val request = com.simats.rankup.network.SubmitTestResultRequest(
            student_id = studentId,
            test_id = testId,
            marks = score,
            total_marks = questions.size
        )

        com.simats.rankup.network.BackendApiService.api.submitTestResult(request).enqueue(object : Callback<com.simats.rankup.network.ApiResponse> {
            override fun onResponse(call: Call<com.simats.rankup.network.ApiResponse>, response: Response<com.simats.rankup.network.ApiResponse>) {
                val intentOut = android.content.Intent(this@StudentTakeTestActivity, StudentTestResultActivity::class.java)
                intentOut.putExtra("SCORE", score)
                intentOut.putExtra("TOTAL", questions.size)
                intentOut.putExtra("SOURCE_PORTAL", intent.getStringExtra("SOURCE_PORTAL") ?: "Placement")
                startActivity(intentOut)
                finish()
            }

            override fun onFailure(call: Call<com.simats.rankup.network.ApiResponse>, t: Throwable) {
                Toast.makeText(this@StudentTakeTestActivity, "Network Error Saving Results: ${t.message}", Toast.LENGTH_LONG).show()
                val intentOut = android.content.Intent(this@StudentTakeTestActivity, StudentTestResultActivity::class.java)
                intentOut.putExtra("SCORE", score)
                intentOut.putExtra("TOTAL", questions.size)
                intentOut.putExtra("SOURCE_PORTAL", intent.getStringExtra("SOURCE_PORTAL") ?: "Placement")
                startActivity(intentOut)
                finish()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
