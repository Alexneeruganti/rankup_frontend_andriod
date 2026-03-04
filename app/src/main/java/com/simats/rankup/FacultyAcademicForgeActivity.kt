package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FacultyAcademicForgeActivity : AppCompatActivity() {

    private lateinit var modulesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_faculty_academic_forge)
            modulesContainer = findViewById(R.id.modulesContainer)
            setupButtons()

            // Add one initial module by default
            addSubject()
        } catch (e: Exception) {
            e.printStackTrace()
            // Show Dialog instead of Toast for persistent error viewing
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Error Occurred")
                .setMessage(e.toString())
                .setPositiveButton("OK") { _, _ -> finish() }
                .setNeutralButton("Copy") { _, _ ->
                    val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Error Log", e.toString())
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun setupButtons() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnGenerateBlueprint).setOnClickListener {
            if (validateInputs()) {
                val title = findViewById<EditText>(R.id.etTitle).text.toString().trim()
                val durationStr = findViewById<EditText>(R.id.etDuration).text.toString().trim()
                val duration = durationStr.toIntOrNull() ?: 120

                val subjectsList = mutableListOf<com.simats.rankup.network.AcademicSubject>()
                for (i in 0 until modulesContainer.childCount) {
                    val view = modulesContainer.getChildAt(i)
                    val name = view.findViewById<EditText>(R.id.etModuleName).text.toString().trim().ifEmpty { "Subject ${i + 1}" }
                    val easy = view.findViewById<EditText>(R.id.etModuleEasy).text.toString().toIntOrNull() ?: 0
                    val medium = view.findViewById<EditText>(R.id.etModuleMedium).text.toString().toIntOrNull() ?: 0
                    val hard = view.findViewById<EditText>(R.id.etModuleHard).text.toString().toIntOrNull() ?: 0

                    subjectsList.add(
                        com.simats.rankup.network.AcademicSubject(
                            name = name,
                            easy = easy,
                            medium = medium,
                            hard = hard
                        )
                    )
                }

                // Placeholder faculty_id. Ideally retrieved from session manager
                val facultyId = 1 
                val request = com.simats.rankup.network.AcademicBlueprintRequest(
                    faculty_id = facultyId,
                    title = title,
                    duration_minutes = duration,
                    subjects = subjectsList
                )

                val intent = Intent(this@FacultyAcademicForgeActivity, FacultyGeneratingAcademicTestActivity::class.java)
                intent.putExtra("REQUEST_PAYLOAD", com.google.gson.Gson().toJson(request))
                startActivity(intent)
                finish()
            }
        }

        findViewById<Button>(R.id.btnAddSubject).setOnClickListener {
            addSubject()
        }
    }

    private fun addSubject() {
        // Reuse item_module_config
        val subjectView = layoutInflater.inflate(R.layout.item_module_config, modulesContainer, false)
        
        val etName = subjectView.findViewById<EditText>(R.id.etModuleName)
        etName.hint = "Subject Name (e.g. Maths)"
        
        subjectView.findViewById<View>(R.id.btnRemoveModule).setOnClickListener {
            modulesContainer.removeView(subjectView)
        }

        modulesContainer.addView(subjectView)
    }

    private fun validateInputs(): Boolean {
        val title = findViewById<EditText>(R.id.etTitle).text.toString()
        val duration = findViewById<EditText>(R.id.etDuration).text.toString()
        
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a blueprint title", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (duration.isEmpty()) {
            Toast.makeText(this, "Please enter duration", Toast.LENGTH_SHORT).show()
            return false
        }

        if (modulesContainer.childCount == 0) {
            Toast.makeText(this, "Please add at least one subject", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate Subject Math
        for (i in 0 until modulesContainer.childCount) {
            val view = modulesContainer.getChildAt(i)
            val etName = view.findViewById<EditText>(R.id.etModuleName)
            val etTotal = view.findViewById<EditText>(R.id.etModuleTotal)
            val etEasy = view.findViewById<EditText>(R.id.etModuleEasy)
            val etMedium = view.findViewById<EditText>(R.id.etModuleMedium)
            val etHard = view.findViewById<EditText>(R.id.etModuleHard)

            val name = etName.text.toString().ifEmpty { "Subject ${i + 1}" }
            val totalStr = etTotal.text.toString()
            val total = totalStr.toIntOrNull() ?: 0
            val easy = etEasy.text.toString().toIntOrNull() ?: 0
            val medium = etMedium.text.toString().toIntOrNull() ?: 0
            val hard = etHard.text.toString().toIntOrNull() ?: 0

            if (totalStr.isEmpty()) {
                Toast.makeText(this, "$name: Enter total questions", Toast.LENGTH_SHORT).show()
                return false
            }

            if (easy + medium + hard != total) {
                Toast.makeText(this, "$name: Sum of Easy ($easy) + Medium ($medium) + Hard ($hard) must equal Total ($total)", Toast.LENGTH_LONG).show()
                return false
            }
        }

        return true
    }
}
