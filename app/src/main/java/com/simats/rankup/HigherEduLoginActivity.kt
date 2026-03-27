package com.simats.rankup

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.PlacementLoginRequest
import com.simats.rankup.network.PlacementLoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HigherEduLoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_higher_edu_login)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvCreateAccount = findViewById<TextView>(R.id.tvCreateAccount)

        tvCreateAccount.setOnClickListener {
            startActivity(Intent(this, StudentSignUpActivity::class.java))
        }

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

        btnSignIn.setOnClickListener {
            val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_button_click)
            btnSignIn.startAnimation(scaleAnimation)
            
            val id = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter ID and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Backend API Call (Unified Login)
            val request = PlacementLoginRequest(id, password)
            BackendApiService.api.placementLogin(request).enqueue(object : Callback<PlacementLoginResponse> {
                override fun onResponse(
                    call: Call<PlacementLoginResponse>,
                    response: Response<PlacementLoginResponse>
                ) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        val role = loginResponse?.role ?: "Student"

                        if (role.equals("Placement Student", ignoreCase = true) || role.equals("Student", ignoreCase = true)) {
                            Toast.makeText(this@HigherEduLoginActivity, "Access Denied: Please use the Placement Login portal.", Toast.LENGTH_LONG).show()
                            return
                        }

                        Toast.makeText(this@HigherEduLoginActivity, loginResponse?.message ?: "Login Successful", Toast.LENGTH_SHORT).show()
                        
                        // Save to SharedPreferences
                        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
                        with (sharedPref.edit()) {
                            putInt("USER_ID", loginResponse?.user_id ?: -1)
                            putString("USER_NAME", loginResponse?.name ?: "")
                            putString("USER_EMAIL", loginResponse?.email ?: "")
                            putString("USER_ROLE", loginResponse?.role ?: "Student")
                            apply()
                        }

                        // Navigate based on user role
                        val intent = when {
                            role.equals("Admin", ignoreCase = true) -> {
                                Intent(this@HigherEduLoginActivity, AdminDashboardActivity::class.java)
                            }
                            role.equals("Faculty", ignoreCase = true) -> {
                                Intent(this@HigherEduLoginActivity, SubscriptionActivity::class.java).apply {
                                    putExtra("NEXT_ACTIVITY", FacultyHomeActivity::class.java.name)
                                }
                            }
                            else -> {
                                Intent(this@HigherEduLoginActivity, SubscriptionActivity::class.java).apply {
                                    putExtra("NEXT_ACTIVITY", HigherEduHomeActivity::class.java.name)
                                }
                            }
                        }

                        startActivity(intent)
                        finish()
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    } else {
                        // Handle 403 (Maintenance) or 401 (Invalid Credentials)
                        val errorMsg = if (response.code() == 403) "Maintenance: Only Admin can log in" else "Invalid ID or Password"
                        Toast.makeText(this@HigherEduLoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<PlacementLoginResponse>, t: Throwable) {
                    Toast.makeText(this@HigherEduLoginActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        // Forgot Password Clickable Span
        val forgotPasswordText = "Forgot Password? Click Here"
        val ssForgot = android.text.SpannableString(forgotPasswordText)
        val clickableSpanForgot = object : android.text.style.ClickableSpan() {
            override fun onClick(widget: android.view.View) {
                val intent = android.content.Intent(this@HigherEduLoginActivity, ForgotPasswordActivity::class.java)
                intent.putExtra("EXTRA_SOURCE", "higher_edu")
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
