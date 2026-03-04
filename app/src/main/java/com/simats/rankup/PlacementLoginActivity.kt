package com.simats.rankup

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.content.Intent
import android.widget.Button
import android.widget.Toast
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.PlacementLoginRequest
import com.simats.rankup.network.PlacementLoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlacementLoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_placement_login)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        // Password Visibility Toggle
        etPassword.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                if (event.rawX >= (etPassword.right - etPassword.compoundDrawables[2].bounds.width())) {
                    val selection = etPassword.selectionEnd
                    if (etPassword.transformationMethod is android.text.method.PasswordTransformationMethod) {
                        etPassword.transformationMethod = android.text.method.HideReturnsTransformationMethod.getInstance()
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye, 0)
                    } else {
                        etPassword.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye_off, 0)
                    }
                    etPassword.setSelection(selection)
                    return@setOnTouchListener true
                }
            }
            false
        }

        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        btnSignIn.setOnClickListener {
            val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_button_click)
            btnSignIn.startAnimation(scaleAnimation)
            
            val id = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter ID and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!(id.matches(Regex("^\\d{9}$")) || 
                  id.matches(Regex("^(?i)sse\\d{6}$")) || 
                  id.matches(Regex("^(?i)admin\\d{4}$")))) {
                Toast.makeText(this, "Invalid ID format (e.g. 123456789, sse123456, admin1234)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            // Backend API Call
            val request = PlacementLoginRequest(id, password)
            BackendApiService.api.placementLogin(request).enqueue(object : Callback<PlacementLoginResponse> {
                override fun onResponse(
                    call: Call<PlacementLoginResponse>,
                    response: Response<PlacementLoginResponse>
                ) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        Toast.makeText(this@PlacementLoginActivity, loginResponse?.message ?: "Login Successful", Toast.LENGTH_SHORT).show()
                        
                        // Save to SharedPreferences
                        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
                        with (sharedPref.edit()) {
                            putInt("USER_ID", loginResponse?.user_id ?: -1)
                            putString("USER_NAME", loginResponse?.name ?: "")
                            putString("USER_EMAIL", loginResponse?.email ?: "")
                            putString("USER_ROLE", loginResponse?.role ?: "Student")
                            apply()
                        }

                        // Navigate based on user role returned by the backend
                        val role = loginResponse?.role ?: "Student"
                        val intent = when {
                            role.equals("Admin", ignoreCase = true) -> Intent(this@PlacementLoginActivity, AdminDashboardActivity::class.java)
                            role.equals("Faculty", ignoreCase = true) -> Intent(this@PlacementLoginActivity, FacultyHomeActivity::class.java)
                            else -> Intent(this@PlacementLoginActivity, StudentHomeActivity::class.java) // Default to Student
                        }

                        startActivity(intent)
                        finish()
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    } else {
                        // Handle 400 or 401 Unauthorized errors
                        Toast.makeText(this@PlacementLoginActivity, "Error: Invalid ID or Password", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<PlacementLoginResponse>, t: Throwable) {
                    Toast.makeText(this@PlacementLoginActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                    t.printStackTrace()
                }
            })
        }

        // Forgot Password Clickable Span
        val forgotPasswordText = "Forgot Password? Click Here"
        val ssForgot = android.text.SpannableString(forgotPasswordText)
        val clickableSpanForgot = object : android.text.style.ClickableSpan() {
            override fun onClick(widget: android.view.View) {
                val intent = android.content.Intent(this@PlacementLoginActivity, ForgotPasswordActivity::class.java)
                intent.putExtra("EXTRA_SOURCE", "placement")
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            override fun updateDrawState(ds: android.text.TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = android.graphics.Color.parseColor("#1C52E6")
            }
        }
        val startForgot = forgotPasswordText.indexOf("Click Here")
        val endForgot = startForgot + "Click Here".length
        ssForgot.setSpan(clickableSpanForgot, startForgot, endForgot, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvForgotPassword.text = ssForgot
        tvForgotPassword.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        tvForgotPassword.highlightColor = android.graphics.Color.TRANSPARENT


    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
