package com.simats.rankup

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.rankup.technical.TechQuestion
import com.simats.rankup.technical.TechnicalRepository

class TechnicalRoundActivity : AppCompatActivity() {

    private lateinit var spinnerTopic: Spinner
    private lateinit var tvQuestionCount: TextView
    private lateinit var tvQuestionText: TextView
    private lateinit var tvAnswerText: TextView
    private lateinit var btnShowAnswer: Button
    private lateinit var btnNext: Button
    
    private var questionList: List<TechQuestion> = emptyList()
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_technical_round)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        spinnerTopic = findViewById(R.id.spinnerTopic)
        tvQuestionCount = findViewById(R.id.tvQuestionCount)
        tvQuestionText = findViewById(R.id.tvQuestionText)
        tvAnswerText = findViewById(R.id.tvAnswerText)
        btnShowAnswer = findViewById(R.id.btnShowAnswer)
        btnNext = findViewById(R.id.btnNextQuestion)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btnStartTopic).setOnClickListener {
            val topic = spinnerTopic.selectedItem.toString()
            startQuiz(topic)
        }

        btnShowAnswer.setOnClickListener {
            if (tvAnswerText.visibility == View.VISIBLE) {
                tvAnswerText.visibility = View.GONE
                btnShowAnswer.text = "SHOW ANSWER"
            } else {
                tvAnswerText.visibility = View.VISIBLE
                btnShowAnswer.text = "HIDE ANSWER"
            }
        }

        btnNext.setOnClickListener {
            showNextQuestion()
        }
    }

    private fun startQuiz(topic: String) {
        questionList = TechnicalRepository.getQuestions(topic)
        if (questionList.isEmpty()) {
            Toast.makeText(this, "No questions available for this topic yet.", Toast.LENGTH_SHORT).show()
            return
        }
        currentIndex = -1
        showNextQuestion()
    }

    private fun showNextQuestion() {
        if (questionList.isEmpty()) {
            Toast.makeText(this, "Please select a topic and click Start.", Toast.LENGTH_SHORT).show()
            return
        }

        currentIndex++
        if (currentIndex >= questionList.size) {
            Toast.makeText(this, "Topic Completed! Reshuffling...", Toast.LENGTH_SHORT).show()
            currentIndex = 0
            questionList = questionList.shuffled()
        }

        val q = questionList[currentIndex]
        tvQuestionText.text = q.question
        tvAnswerText.text = q.answer
        tvAnswerText.visibility = View.GONE
        btnShowAnswer.text = "SHOW ANSWER"
        tvQuestionCount.text = "Question ${currentIndex + 1}/${questionList.size}"
    }
}
