package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.simats.rankup.network.ApiResponse
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.ResetPasswordRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnReset: MaterialButton
    private lateinit var progressBar: ProgressBar
    private var targetEmail: String = ""
    private var source: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        targetEmail = intent.getStringExtra("EMAIL") ?: ""
        source = intent.getStringExtra("EXTRA_SOURCE")
        if (targetEmail.isEmpty()) {
            Toast.makeText(this, "Session Error", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnReset = findViewById(R.id.btnReset)
        progressBar = findViewById(R.id.progressBar)

        btnReset.setOnClickListener {
            val pass1 = etNewPassword.text.toString()
            val pass2 = etConfirmPassword.text.toString()

            if (pass1.isEmpty() || pass2.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass1 != pass2) {
                etConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }
            
            if (!com.simats.rankup.utils.ValidationUtils.isValidPassword(pass1)) {
                etNewPassword.error = "Password must be >= 8 chars, 1 uppercase, 1 number, 1 symbol"
                return@setOnClickListener
            }

            btnReset.isEnabled = false
            progressBar.visibility = View.VISIBLE

            val request = ResetPasswordRequest(targetEmail, pass1)
            BackendApiService.api.resetPassword(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    btnReset.isEnabled = true
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        Toast.makeText(this@ResetPasswordActivity, "Password Reset Successful! Please log in.", Toast.LENGTH_LONG).show()
                        
                        // Go back to the correct login screen
                        val intent = if (source == "higher_edu") {
                            Intent(this@ResetPasswordActivity, HigherEduLoginActivity::class.java)
                        } else {
                            Intent(this@ResetPasswordActivity, PlacementLoginActivity::class.java)
                        }
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@ResetPasswordActivity, "Reset Failed.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    btnReset.isEnabled = true
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@ResetPasswordActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
