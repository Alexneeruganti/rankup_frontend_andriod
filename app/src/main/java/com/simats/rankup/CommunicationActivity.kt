package com.simats.rankup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.CommunicationExercise
import com.simats.rankup.network.CommunicationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class CommunicationActivity : AppCompatActivity() {

    private lateinit var tvParagraph: TextView
    private lateinit var tvMcq1Question: TextView
    private lateinit var rgMcq1: RadioGroup
    private lateinit var tvMcq2Question: TextView
    private lateinit var rgMcq2: RadioGroup
    private lateinit var tvFibQuestion: TextView
    private lateinit var rgFib: RadioGroup
    private lateinit var tvReadingPrompt: TextView
    private lateinit var etReadingAnswer: EditText
    private lateinit var btnMic: View
    private lateinit var btnSubmit: Button
    
    private var allExercises: List<CommunicationExercise> = emptyList()
    private var speechTargetAnswer: String = ""
    private var isSpeechCorrect: Boolean = false

    private val speechResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val spokenText: String? =
                result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let {
                    it[0]
                }
            spokenText?.let {
                etReadingAnswer.setText(it)
                checkSpeechCorrectness(it)
            }
        }
    }

    private fun checkSpeechCorrectness(spoken: String) {
        if (speechTargetAnswer.isEmpty()) return
        
        val normalizedSpoken = spoken.lowercase().trim().replace(Regex("[^a-z0-9 ]"), "")
        val normalizedTarget = speechTargetAnswer.lowercase().trim().replace(Regex("[^a-z0-9 ]"), "")
        
        isSpeechCorrect = normalizedSpoken == normalizedTarget
        
        if (isSpeechCorrect) {
            Toast.makeText(this, "✅ Speech Matched!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "❌ Match Failed. Try again!", Toast.LENGTH_SHORT).show()
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startSpeechToText()
        } else {
            Toast.makeText(this, "Microphone permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_communication)

        initViews()
        fetchExercises()
    }

    private fun initViews() {
        tvParagraph = findViewById(R.id.tvParagraph)
        tvMcq1Question = findViewById(R.id.tvMcq1Question)
        rgMcq1 = findViewById(R.id.rgMcq1)
        tvMcq2Question = findViewById(R.id.tvMcq2Question)
        rgMcq2 = findViewById(R.id.rgMcq2)
        tvFibQuestion = findViewById(R.id.tvFibQuestion)
        rgFib = findViewById(R.id.rgFib)
        tvReadingPrompt = findViewById(R.id.tvReadingPrompt)
        etReadingAnswer = findViewById(R.id.etReadingAnswer)
        btnMic = findViewById(R.id.btnMic)
        btnSubmit = findViewById(R.id.btnSubmit)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        btnMic.setOnClickListener {
            checkPermissionAndStartSpeech()
        }

        btnSubmit.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun fetchExercises() {
        BackendApiService.api.getCommunicationExercises().enqueue(object : Callback<CommunicationResponse> {
            override fun onResponse(call: Call<CommunicationResponse>, response: Response<CommunicationResponse>) {
                if (response.isSuccessful && response.body()?.exercises != null) {
                    allExercises = response.body()!!.exercises
                    displayExercises(allExercises)
                }
            }
            override fun onFailure(call: Call<CommunicationResponse>, t: Throwable) {
                Toast.makeText(this@CommunicationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayExercises(list: List<CommunicationExercise>) {
        if (list.isEmpty()) return

        // 1. MCQ Section
        val paraItems = list.filter { it.type == "para_ques" }
        if (paraItems.isNotEmpty()) {
            val it1 = paraItems[0]
            tvParagraph.text = it1.paragraph
            tvMcq1Question.visibility = View.VISIBLE
            rgMcq1.visibility = View.VISIBLE
            tvMcq1Question.text = "1. ${it1.question}"
            findViewById<RadioButton>(R.id.rbMcq1A).text = it1.option_a
            findViewById<RadioButton>(R.id.rbMcq1B).text = it1.option_b
            findViewById<RadioButton>(R.id.rbMcq1C).text = it1.option_c
            findViewById<RadioButton>(R.id.rbMcq1D).text = it1.option_d
            
            if (paraItems.size > 1) {
                val it2 = paraItems[1]
                tvMcq2Question.visibility = View.VISIBLE
                rgMcq2.visibility = View.VISIBLE
                tvMcq2Question.text = "2. ${it2.question}"
                findViewById<RadioButton>(R.id.rbMcq2A).text = it2.option_a
                findViewById<RadioButton>(R.id.rbMcq2B).text = it2.option_b
                findViewById<RadioButton>(R.id.rbMcq2C).text = it2.option_c
                findViewById<RadioButton>(R.id.rbMcq2D).text = it2.option_d
            } else {
                tvMcq2Question.visibility = View.GONE
                rgMcq2.visibility = View.GONE
            }
        }

        // 2. Fill in Blanks Section
        val fibItem = list.find { it.type == "blanks" }
        fibItem?.let {
            tvFibQuestion.text = it.question
            findViewById<RadioButton>(R.id.rbFibA).text = it.option_a
            findViewById<RadioButton>(R.id.rbFibB).text = it.option_b
            findViewById<RadioButton>(R.id.rbFibC).text = it.option_c
            findViewById<RadioButton>(R.id.rbFibD).text = it.option_d
        }

        // 3. Speech Section
        val speechItem = list.find { it.type == "speech_to_text" }
        speechItem?.let {
            tvReadingPrompt.text = it.answer // Explicitly show the text to be read
            speechTargetAnswer = it.answer
        }
    }

    private fun checkPermissionAndStartSpeech() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startSpeechToText()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Read: $speechTargetAnswer")
        }
        try {
            speechResultLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Speech not supported", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAndSubmit() {
        if (allExercises.isEmpty()) return

        val m1Selected = rgMcq1.checkedRadioButtonId
        val fibSelected = rgFib.checkedRadioButtonId

        if (m1Selected == -1 || fibSelected == -1) {
            Toast.makeText(this, "Please answer all questions before submitting", Toast.LENGTH_SHORT).show()
            return
        }

        var score = 0
        var total = 0
        
        // Score MCQ 1
        val m1Item = allExercises.find { it.type == "para_ques" }
        m1Item?.let {
            total++
            val selectedText = findViewById<RadioButton>(m1Selected).text.toString()
            val correctText = when(it.answer) {
                "A" -> it.option_a; "B" -> it.option_b; "C" -> it.option_c; "D" -> it.option_d
                else -> it.answer
            }
            if (selectedText == correctText) score++
        }

        // Score FIB
        val fibItem = allExercises.find { it.type == "blanks" }
        fibItem?.let {
            total++
            val selectedText = findViewById<RadioButton>(fibSelected).text.toString()
            if (selectedText == it.answer) score++
        }

        // Score Speech
        total++
        if (isSpeechCorrect) score++

        // Navigate to Result Page
        val intent = Intent(this, CommunicationResultActivity::class.java)
        intent.putExtra("SCORE", score)
        intent.putExtra("TOTAL", total)
        startActivity(intent)
        finish()
    }
}
