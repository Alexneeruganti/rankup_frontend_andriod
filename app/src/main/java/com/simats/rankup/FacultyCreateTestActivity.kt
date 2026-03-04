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

class FacultyCreateTestActivity : AppCompatActivity() {

    private lateinit var modulesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // Toast.makeText(this, "DEBUG: FacultyCreateTestActivity Launched", Toast.LENGTH_LONG).show() // Debug
            setContentView(R.layout.activity_faculty_create_test)
            modulesContainer = findViewById(R.id.modulesContainer)
            setupButtons()

            // Add one initial module by default
            addModule()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback crash catcher, though unlikely needed now
            Toast.makeText(this, "CRASH in FacultyCreateTest: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupButtons() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnGenerateTest).setOnClickListener {
            if (validateInputs()) {
                val title = findViewById<EditText>(R.id.etTestTitle).text.toString().trim()
                val durationStr = findViewById<EditText>(R.id.etDuration).text.toString().trim()
                val duration = durationStr.toIntOrNull() ?: 0

                val modulesList = mutableListOf<com.simats.rankup.network.MockTestModuleParams>()
                for (i in 0 until modulesContainer.childCount) {
                    val view = modulesContainer.getChildAt(i)
                    val name = view.findViewById<EditText>(R.id.etModuleName).text.toString().trim().ifEmpty { "Module ${i + 1}" }
                    val easy = view.findViewById<EditText>(R.id.etModuleEasy).text.toString().toIntOrNull() ?: 0
                    val medium = view.findViewById<EditText>(R.id.etModuleMedium).text.toString().toIntOrNull() ?: 0
                    val hard = view.findViewById<EditText>(R.id.etModuleHard).text.toString().toIntOrNull() ?: 0

                    modulesList.add(
                        com.simats.rankup.network.MockTestModuleParams(
                            name = name,
                            easy = easy,
                            medium = medium,
                            hard = hard,
                            coding = 0
                        )
                    )
                }

                val facultyId = 1 // Placeholder for logged-in faculty

                val request = com.simats.rankup.network.GenerateTestRequest(
                    faculty_id = facultyId,
                    title = title,
                    duration_minutes = duration,
                    modules = modulesList
                )

                val intent = Intent(this@FacultyCreateTestActivity, FacultyGeneratingTestActivity::class.java)
                intent.putExtra("REQUEST_PAYLOAD", com.google.gson.Gson().toJson(request))
                startActivity(intent)
                finish()
            }
        }

        findViewById<Button>(R.id.btnAddModule).setOnClickListener {
            addModule()
        }
    }

    private fun addModule() {
        val moduleView = layoutInflater.inflate(R.layout.item_module_config, modulesContainer, false)
        
        // Setup Remove Button
        moduleView.findViewById<View>(R.id.btnRemoveModule).setOnClickListener {
            modulesContainer.removeView(moduleView)
        }

        modulesContainer.addView(moduleView)
    }

    private fun validateInputs(): Boolean {
        val title = findViewById<EditText>(R.id.etTestTitle).text.toString()
        val duration = findViewById<EditText>(R.id.etDuration).text.toString()
        
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a test title", Toast.LENGTH_SHORT).show()
            return false
        }
        if (duration.isEmpty()) {
            Toast.makeText(this, "Please enter test duration", Toast.LENGTH_SHORT).show()
            return false
        }

        if (modulesContainer.childCount == 0) {
            Toast.makeText(this, "Please add at least one module", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate Each Module
        for (i in 0 until modulesContainer.childCount) {
            val moduleView = modulesContainer.getChildAt(i)
            if (!validateModule(moduleView, i + 1)) return false
        }

        return true
    }

    private fun validateModule(view: View, index: Int): Boolean {
        val etName = view.findViewById<EditText>(R.id.etModuleName)
        val etTotal = view.findViewById<EditText>(R.id.etModuleTotal)
        val etEasy = view.findViewById<EditText>(R.id.etModuleEasy)
        val etMedium = view.findViewById<EditText>(R.id.etModuleMedium)
        val etHard = view.findViewById<EditText>(R.id.etModuleHard)

        val name = etName.text.toString().ifEmpty { "Module $index" }
        val totalStr = etTotal.text.toString()
        val easyStr = etEasy.text.toString()
        val mediumStr = etMedium.text.toString()
        val hardStr = etHard.text.toString()

        if (totalStr.isEmpty()) {
            Toast.makeText(this, "$name: Enter total questions", Toast.LENGTH_SHORT).show()
            return false
        }

        val total = totalStr.toIntOrNull() ?: 0
        val easy = easyStr.toIntOrNull() ?: 0
        val medium = mediumStr.toIntOrNull() ?: 0
        val hard = hardStr.toIntOrNull() ?: 0

        if (easy + medium + hard != total) {
            Toast.makeText(this, "$name: Sum of Easy ($easy) + Medium ($medium) + Hard ($hard) must equal Total ($total)", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }
}
