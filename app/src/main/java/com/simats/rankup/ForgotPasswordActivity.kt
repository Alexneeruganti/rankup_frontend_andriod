package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.simats.rankup.network.ApiResponse
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.ForgotPasswordRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSendOtp: MaterialButton
    private lateinit var progressBar: ProgressBar
    private var source: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etEmail = findViewById(R.id.etEmail)
        btnSendOtp = findViewById(R.id.btnSendOtp)
        progressBar = findViewById(R.id.progressBar)
        
        source = intent.getStringExtra("EXTRA_SOURCE")

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnSendOtp.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                return@setOnClickListener
            }

            // Show loading
            btnSendOtp.isEnabled = false
            progressBar.visibility = View.VISIBLE

            val request = ForgotPasswordRequest(email)
            BackendApiService.api.forgotPassword(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    btnSendOtp.isEnabled = true
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        Toast.makeText(this@ForgotPasswordActivity, "OTP Sent to $email", Toast.LENGTH_LONG).show()
                        
                        val intent = Intent(this@ForgotPasswordActivity, VerifyOtpActivity::class.java)
                        intent.putExtra("EMAIL", email)
                        intent.putExtra("EXTRA_SOURCE", source)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@ForgotPasswordActivity, "Error: User not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    btnSendOtp.isEnabled = true
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@ForgotPasswordActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
