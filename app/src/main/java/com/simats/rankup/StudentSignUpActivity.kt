package com.simats.rankup

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class StudentSignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_sign_up)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Only allow students to create placement or higher education accounts
        val roles = arrayOf("Placement Student", "Higher Education")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        val spinRole = findViewById<AutoCompleteTextView>(R.id.spinRole)
        spinRole.setAdapter(adapter)

        animateEntry()

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            registerStudent()
        }
    }

    private fun registerStudent() {
        val name = findViewById<TextInputEditText>(R.id.etName).text.toString().trim()
        val id = findViewById<TextInputEditText>(R.id.etId).text.toString().trim()
        val email = findViewById<TextInputEditText>(R.id.etEmail).text.toString().trim()
        val password = findViewById<TextInputEditText>(R.id.etPassword).text.toString().trim()
        val age = findViewById<TextInputEditText>(R.id.etAge).text.toString().trim()
        val gender = findViewById<TextInputEditText>(R.id.etGender).text.toString().trim()
        val department = findViewById<TextInputEditText>(R.id.etDepartment).text.toString().trim()
        val role = findViewById<AutoCompleteTextView>(R.id.spinRole).text.toString().trim()

        if (name.isEmpty() || id.isEmpty() || email.isEmpty() || password.isEmpty() || department.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!com.simats.rankup.utils.ValidationUtils.isValidEmail(email)) {
            Toast.makeText(this, "Email must be a valid @gmail.com address", Toast.LENGTH_SHORT).show()
            return
        }

        if (!com.simats.rankup.utils.ValidationUtils.isValidPassword(password)) {
            Toast.makeText(this, "Password must be >= 8 chars, 1 uppercase, 1 number, 1 symbol", Toast.LENGTH_LONG).show()
            return
        }

        val btnSave = findViewById<Button>(R.id.btnRegister)
        btnSave.isEnabled = false
        btnSave.text = "Registering..."

        val request = com.simats.rankup.network.AddUserRequest(
            id = id,
            name = name,
            email = email,
            password = password,
            role = role,
            age = age,
            gender = gender,
            department = department
        )

        com.simats.rankup.network.BackendApiService.api.adminAddUser(request).enqueue(object : retrofit2.Callback<com.simats.rankup.network.ApiResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.simats.rankup.network.ApiResponse>,
                response: retrofit2.Response<com.simats.rankup.network.ApiResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(this@StudentSignUpActivity, "Registration Successful! Please login.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    btnSave.isEnabled = true
                    btnSave.text = "Register"
                    
                    try {
                        val errorJson = response.errorBody()?.string()
                        val errorObject = org.json.JSONObject(errorJson ?: "{}")
                        val errorMsg = errorObject.optString("error", "Failed to register")
                        Toast.makeText(this@StudentSignUpActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@StudentSignUpActivity, "Error: User exists or invalid data", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.ApiResponse>, t: Throwable) {
                btnSave.isEnabled = true
                btnSave.text = "Register"
                Toast.makeText(this@StudentSignUpActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun animateEntry() {
        val formLayout = findViewById<View>(R.id.formLayout)
        formLayout.translationY = 200f
        formLayout.alpha = 0f

        val animatorY = ObjectAnimator.ofFloat(formLayout, "translationY", 200f, 0f)
        val animatorAlpha = ObjectAnimator.ofFloat(formLayout, "alpha", 0f, 1f)

        animatorY.duration = 600
        animatorAlpha.duration = 600
        
        animatorY.interpolator = DecelerateInterpolator()
        
        animatorY.start()
        animatorAlpha.start()
    }
}
