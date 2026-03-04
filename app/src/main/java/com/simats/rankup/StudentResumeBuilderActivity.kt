package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class StudentResumeBuilderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_resume_builder)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val cardTemplateModern = findViewById<CardView>(R.id.cardTemplateModern)
        val cardTemplateClassic = findViewById<CardView>(R.id.cardTemplateClassic)

        findViewById<android.view.View>(R.id.btnViewStatus)?.setOnClickListener {
            startActivity(Intent(this, StudentResumeStatusActivity::class.java))
        }

        cardTemplateModern.setOnClickListener {
            proceedToForm("MODERN")
        }

        cardTemplateClassic.setOnClickListener {
            proceedToForm("CLASSIC")
        }
    }

    private fun proceedToForm(templateId: String) {
        val intent = Intent(this, StudentResumeFormActivity::class.java)
        intent.putExtra("TEMPLATE_ID", templateId)
        startActivity(intent)
    }
}
