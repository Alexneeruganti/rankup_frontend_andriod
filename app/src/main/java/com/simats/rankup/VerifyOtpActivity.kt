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
import com.simats.rankup.network.VerifyOtpRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifyOtpActivity : AppCompatActivity() {

    private lateinit var etOtp: TextInputEditText
    private lateinit var btnVerify: MaterialButton
    private lateinit var progressBar: ProgressBar
    private var targetEmail: String = ""
    private var source: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_otp)

        targetEmail = intent.getStringExtra("EMAIL") ?: ""
        source = intent.getStringExtra("EXTRA_SOURCE")
        if (targetEmail.isEmpty()) {
            Toast.makeText(this, "Session Error", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        etOtp = findViewById(R.id.etOtp)
        btnVerify = findViewById(R.id.btnVerify)
        progressBar = findViewById(R.id.progressBar)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnVerify.setOnClickListener {
            val otp = etOtp.text.toString().trim()

            if (otp.length != 6) {
                etOtp.error = "Enter 6-digit OTP"
                return@setOnClickListener
            }

            btnVerify.isEnabled = false
            progressBar.visibility = View.VISIBLE

            val request = VerifyOtpRequest(targetEmail, otp)
            BackendApiService.api.verifyOtp(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    btnVerify.isEnabled = true
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        Toast.makeText(this@VerifyOtpActivity, "OTP Verified successfully!", Toast.LENGTH_SHORT).show()
                        
                        val intent = Intent(this@VerifyOtpActivity, ResetPasswordActivity::class.java)
                        intent.putExtra("EMAIL", targetEmail)
                        intent.putExtra("EXTRA_SOURCE", source)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@VerifyOtpActivity, "Invalid or Expired OTP", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    btnVerify.isEnabled = true
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@VerifyOtpActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
