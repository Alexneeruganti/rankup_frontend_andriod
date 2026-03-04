package com.simats.rankup

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class AddUserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        animateEntry()

        findViewById<Button>(R.id.btnSaveUser).setOnClickListener {
            saveUser()
        }
    }

    private fun saveUser() {
        val name = findViewById<TextInputEditText>(R.id.etName).text.toString().trim()
        val id = findViewById<TextInputEditText>(R.id.etId).text.toString().trim()
        val email = findViewById<TextInputEditText>(R.id.etEmail).text.toString().trim()
        val password = findViewById<TextInputEditText>(R.id.etPassword).text.toString().trim()
        val age = findViewById<TextInputEditText>(R.id.etAge).text.toString().trim()
        val gender = findViewById<TextInputEditText>(R.id.etGender).text.toString().trim()
        val department = findViewById<TextInputEditText>(R.id.etDepartment).text.toString().trim()
        
        val isStudent = findViewById<RadioButton>(R.id.rbStudent).isChecked
        val role = if (isStudent) "Student" else "Faculty"

        if (name.isEmpty() || id.isEmpty() || email.isEmpty() || password.isEmpty() || department.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!(id.matches(Regex("^\\d{9}$")) || 
              id.matches(Regex("^(?i)sse\\d{6}$")) || 
              id.matches(Regex("^(?i)admin\\d{4}$")))) {
            Toast.makeText(this, "Invalid ID format (e.g. 123456789, sse123456, admin1234)", Toast.LENGTH_SHORT).show()
            return
        }

        // Show a temporary loading indicator (e.g., disable button)
        val btnSave = findViewById<Button>(R.id.btnSaveUser)
        btnSave.isEnabled = false
        btnSave.text = "Saving..."

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
                    Toast.makeText(this@AddUserActivity, response.body()?.message ?: "$role Added Successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    btnSave.isEnabled = true
                    btnSave.text = "Save User"
                    
                    // Try to parse the error message from the backend
                    try {
                        val errorJson = response.errorBody()?.string()
                        val errorObject = org.json.JSONObject(errorJson ?: "{}")
                        val errorMsg = errorObject.optString("error", "Failed to add user")
                        Toast.makeText(this@AddUserActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@AddUserActivity, "Error: User exists or invalid data", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.ApiResponse>, t: Throwable) {
                btnSave.isEnabled = true
                btnSave.text = "Save User"
                Toast.makeText(this@AddUserActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
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
