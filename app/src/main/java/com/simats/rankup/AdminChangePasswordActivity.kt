package com.simats.rankup

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.ChangePasswordRequest
import com.simats.rankup.network.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_change_password)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val etOld = findViewById<EditText>(R.id.etOldPassword)
        val etNew = findViewById<EditText>(R.id.etNewPassword)
        val etConfirm = findViewById<EditText>(R.id.etConfirmPassword)
        val btnUpdate = findViewById<Button>(R.id.btnUpdatePassword)

        btnUpdate.setOnClickListener {
            val oldPass = etOld.text.toString()
            val newPass = etNew.text.toString()
            val confirmPass = etConfirm.text.toString()

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
            val userId = sharedPref.getInt("USER_ID", -1)

            if (userId == -1) {
                Toast.makeText(this, "User session error", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = ChangePasswordRequest(userId, oldPass, newPass)
            BackendApiService.api.changePassword(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AdminChangePasswordActivity, "Password updated!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AdminChangePasswordActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@AdminChangePasswordActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
