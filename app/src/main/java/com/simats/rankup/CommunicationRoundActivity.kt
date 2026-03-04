package com.simats.rankup

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class CommunicationRoundActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tvRoundTitle: TextView
    private lateinit var tvRoundInstructions: TextView
    private lateinit var tvPassage: TextView
    private lateinit var btnPlayAudio: Button
    private lateinit var tvQuestion: TextView
    private lateinit var etAnswer: EditText
    private lateinit var btnSubmit: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvFeedback: TextView
    
    private var tts: TextToSpeech? = null
    
    private enum class Round { READING, LISTENING, GRAMMAR, RESULT }
    private var currentRound = Round.READING
    private var score = 0
    private var roundStep = 0 // To track sub-steps within a round if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_communication_round)
        
        tts = TextToSpeech(this, this)
        initViews()
        setupListeners()
        loadRound(Round.READING)
    }

    private fun initViews() {
        tvRoundTitle = findViewById(R.id.tvRoundTitle)
        tvRoundInstructions = findViewById(R.id.tvRoundInstructions)
        tvPassage = findViewById(R.id.tvPassage)
        btnPlayAudio = findViewById(R.id.btnPlayAudio)
        tvQuestion = findViewById(R.id.tvQuestion)
        etAnswer = findViewById(R.id.etAnswer)
        btnSubmit = findViewById(R.id.btnSubmitAnswer)
        progressBar = findViewById(R.id.progressBarRound)
        tvFeedback = findViewById(R.id.tvFeedback)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupListeners() {
        btnPlayAudio.setOnClickListener {
            val text = tvPassage.text.toString()
            speak(text)
        }

        btnSubmit.setOnClickListener {
            evaluateAnswer()
        }
    }

    private fun loadRound(round: Round) {
        currentRound = round
        etAnswer.setText("")
        tvFeedback.text = ""
        btnSubmit.isEnabled = true
        btnSubmit.text = "SUBMIT"
        
        when (round) {
            Round.READING -> {
                tvRoundTitle.text = "ROUND 1: READING"
                progressBar.progress = 33
                tvRoundInstructions.text = "Read the passage and answer the question."
                tvPassage.text = "The Industrial Revolution was a period of major industrialization that took place during the late 1700s and early 1800s. It began in Great Britain and quickly spread to the world."
                tvQuestion.text = "Where did the Industrial Revolution begin?"
                btnPlayAudio.visibility = View.GONE
                tvPassage.visibility = View.VISIBLE
            }
            Round.LISTENING -> {
                tvRoundTitle.text = "ROUND 2: LISTENING"
                progressBar.progress = 66
                tvRoundInstructions.text = "Listen to the audio and answer the question."
                tvPassage.text = "Artificial Intelligence is intelligence demonstrated by machines, as opposed to the natural intelligence displayed by animals including humans."
                // Hide text for listening round!
                tvPassage.visibility = View.GONE 
                btnPlayAudio.visibility = View.VISIBLE
                tvQuestion.text = "What is Artificial Intelligence demonstrated by?"
            }
            Round.GRAMMAR -> {
                tvRoundTitle.text = "ROUND 3: GRAMMAR"
                progressBar.progress = 90
                tvRoundInstructions.text = "Fill in the blank with the correct word."
                tvPassage.text = "She _______ (go) to the market yesterday."
                tvPassage.visibility = View.VISIBLE
                btnPlayAudio.visibility = View.GONE
                tvQuestion.text = "Type the correct form of the verb 'go'."
            }
            Round.RESULT -> {
                tvRoundTitle.text = "COMPLETED"
                progressBar.progress = 100
                tvRoundInstructions.text = "You have completed all rounds."
                tvPassage.text = "Total Score: $score / 3"
                tvQuestion.text = if (score >= 2) "Great Job!" else "Keep Practicing."
                etAnswer.visibility = View.GONE
                btnSubmit.text = "FINISH"
                btnSubmit.setOnClickListener { finish() }
                btnPlayAudio.visibility = View.GONE
            }
        }
    }

    private fun evaluateAnswer() {
        val answer = etAnswer.text.toString().trim().lowercase()
        var isCorrect = false
        var correctAnswer = ""

        when (currentRound) {
            Round.READING -> {
                correctAnswer = "great britain"
                if (answer.contains("britain")) { isCorrect = true }
            }
            Round.LISTENING -> {
                correctAnswer = "machines"
                if (answer.contains("machines")) { isCorrect = true }
            }
            Round.GRAMMAR -> {
                correctAnswer = "went"
                if (answer == "went") { isCorrect = true }
            }
            else -> return
        }

        if (isCorrect) {
            score++
            tvFeedback.text = "✅ Correct!"
            tvFeedback.setTextColor(0xFF00C853.toInt())
        } else {
            tvFeedback.text = "❌ Incorrect. Answer: $correctAnswer"
            tvFeedback.setTextColor(0xFFD50000.toInt())
        }

        btnSubmit.isEnabled = false // Prevent multiple submissions
        
        // Delay before next round
        etAnswer.postDelayed({
            when (currentRound) {
                Round.READING -> loadRound(Round.LISTENING)
                Round.LISTENING -> loadRound(Round.GRAMMAR)
                Round.GRAMMAR -> loadRound(Round.RESULT)
                else -> {}
            }
        }, 2000)
    }
    
    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "TTS Language not supported", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
        super.onDestroy()
    }
}
