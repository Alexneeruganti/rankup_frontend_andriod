package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StudentResumeFormActivity : AppCompatActivity() {

    private lateinit var templateId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_resume_form)

        templateId = intent.getStringExtra("TEMPLATE_ID") ?: "MODERN"

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etJobTitle = findViewById<EditText>(R.id.etJobTitle)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etLocation = findViewById<EditText>(R.id.etLocation)
        val etObjective = findViewById<EditText>(R.id.etObjective)
        val etEducation = findViewById<EditText>(R.id.etEducation)
        val etExperience = findViewById<EditText>(R.id.etExperience)
        val etSkills = findViewById<EditText>(R.id.etSkills)
        val etLanguages = findViewById<EditText>(R.id.etLanguages)
        val etReferences = findViewById<EditText>(R.id.etReferences)

        findViewById<Button>(R.id.btnGenerate).setOnClickListener {
            if (etFullName.text.isEmpty() || etEmail.text.isEmpty()) {
                Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val emailStr = etEmail.text.toString().trim()
            val phoneStr = etPhone.text.toString().trim()

            if (!com.simats.rankup.utils.ValidationUtils.isValidEmail(emailStr)) {
                Toast.makeText(this, "Valid @gmail.com email required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phoneStr.isNotEmpty() && !com.simats.rankup.utils.ValidationUtils.isValidMobile(phoneStr)) {
                Toast.makeText(this, "Valid 10-digit mobile number required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, StudentResumeGeneratedActivity::class.java)
            intent.putExtra("TEMPLATE_ID", templateId)
            intent.putExtra("NAME", etFullName.text.toString())
            intent.putExtra("JOB_TITLE", etJobTitle.text.toString())
            intent.putExtra("EMAIL", etEmail.text.toString())
            intent.putExtra("PHONE", etPhone.text.toString())
            intent.putExtra("LOCATION", etLocation.text.toString())
            intent.putExtra("OBJECTIVE", etObjective.text.toString())
            intent.putExtra("EDUCATION", etEducation.text.toString())
            intent.putExtra("EXPERIENCE", etExperience.text.toString())
            intent.putExtra("SKILLS", etSkills.text.toString())
            intent.putExtra("LANGUAGES", etLanguages.text.toString())
            intent.putExtra("REFERENCES", etReferences.text.toString())
            startActivity(intent)
        }
    }
}
