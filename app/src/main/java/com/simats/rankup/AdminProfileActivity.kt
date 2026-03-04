package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class AdminProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_profile)

        setupButtons()
        fetchUserProfile()
        fetchAdminStats()
    }

    private fun setupButtons() {
        // Back Button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Settings Button
        findViewById<Button>(R.id.btnSystemSettings).setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
        }

        // Change Password Button
        findViewById<Button>(R.id.btnChangePassword).setOnClickListener {
            startActivity(Intent(this, AdminChangePasswordActivity::class.java))
        }

        // Logout Button
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            // Clear any session data here (SharedPreferences, etc.)
            val intent = Intent(this, PlacementLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun fetchUserProfile() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)

        if (userId == -1) return

        BackendApiService.api.getProfile(userId).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful && response.body()?.profile != null) {
                    val profile = response.body()?.profile!!
                    findViewById<TextView>(R.id.tvAdminName).text = profile.name ?: "Administrator"
                    findViewById<TextView>(R.id.tvAdminRole).text = (profile.role ?: "ADMIN").uppercase()
                    findViewById<TextView>(R.id.tvAdminEmail).text = profile.email ?: "No Email"
                    
                    val imgProfile = findViewById<ImageView>(R.id.imgAdminProfile) ?: return
                    
                    imgProfile.setOnClickListener {
                        showProfilePictureOptions()
                    }

                    if (!profile.profile_pic.isNullOrEmpty()) {
                        val fullUrl = if (profile.profile_pic!!.startsWith("http")) profile.profile_pic else "${BackendApiService.BASE_URL.removeSuffix("/")}${profile.profile_pic}"
                        com.bumptech.glide.Glide.with(this@AdminProfileActivity)
                            .load(fullUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .circleCrop()
                            .into(imgProfile)
                    } else {
                        imgProfile.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@AdminProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showProfilePictureOptions() {
        val options = arrayOf("Choose New Picture", "Remove Profile Picture")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> imagePickerLauncher.launch("image/*")
                    1 -> removeProfilePicture()
                }
            }
            .show()
    }

    private fun removeProfilePicture() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)
        if (userId == -1) return

        val request = com.simats.rankup.network.RemoveProfilePicRequest(userId)
        BackendApiService.api.removeProfilePic(request).enqueue(object : Callback<com.simats.rankup.network.ApiResponse> {
            override fun onResponse(call: Call<com.simats.rankup.network.ApiResponse>, response: Response<com.simats.rankup.network.ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminProfileActivity, "Profile picture removed", Toast.LENGTH_SHORT).show()
                    findViewById<ImageView>(R.id.imgAdminProfile)?.setImageResource(R.drawable.ic_profile_placeholder)
                }
            }
            override fun onFailure(call: Call<com.simats.rankup.network.ApiResponse>, t: Throwable) {}
        })
    }

    private val cropImageLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultUri = com.yalantis.ucrop.UCrop.getOutput(result.data!!)
            resultUri?.let { uploadImage(it) }
        }
    }

    private val imagePickerLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
        uri?.let { sourceUri ->
            val destinationUri = android.net.Uri.fromFile(java.io.File(cacheDir, "cropped_profile_${System.currentTimeMillis()}.jpg"))
            val uCropIntent = com.yalantis.ucrop.UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(800, 800)
                .getIntent(this)
            cropImageLauncher.launch(uCropIntent)
        }
    }

    private fun uploadImage(fileUri: android.net.Uri) {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)
        if (userId == -1) return

        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()
        try {
            val inputStream = contentResolver.openInputStream(fileUri)
            val tempFile = java.io.File(cacheDir, "upload_profile.jpg")
            val outputStream = java.io.FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val requestFile = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/jpeg"), tempFile)
            val body = okhttp3.MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
            val userIdRequest = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), userId.toString())

            BackendApiService.api.uploadProfilePic(userIdRequest, body).enqueue(object : Callback<com.simats.rankup.network.ProfilePicUploadResponse> {
                override fun onResponse(call: Call<com.simats.rankup.network.ProfilePicUploadResponse>, response: Response<com.simats.rankup.network.ProfilePicUploadResponse>) {
                    if (response.isSuccessful && response.body()?.error == null) {
                        Toast.makeText(this@AdminProfileActivity, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                        val newUrl = response.body()?.url
                        if (!newUrl.isNullOrEmpty()) {
                            val imgProfile = findViewById<ImageView>(R.id.imgAdminProfile)
                            val fullUrl = if (newUrl.startsWith("http")) newUrl else "${BackendApiService.BASE_URL.removeSuffix("/")}$newUrl"
                            com.bumptech.glide.Glide.with(this@AdminProfileActivity)
                                .load(fullUrl)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .circleCrop()
                                .into(imgProfile)
                        }
                    }
                }
                override fun onFailure(call: Call<com.simats.rankup.network.ProfilePicUploadResponse>, t: Throwable) {
                    Toast.makeText(this@AdminProfileActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fetchAdminStats() {
        BackendApiService.api.getAdminStats().enqueue(object : Callback<com.simats.rankup.network.AdminStatsResponse> {
            override fun onResponse(call: Call<com.simats.rankup.network.AdminStatsResponse>, response: Response<com.simats.rankup.network.AdminStatsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!
                    findViewById<TextView>(R.id.tvTotalUsers).text = java.text.NumberFormat.getInstance().format(stats.total_users)
                    findViewById<TextView>(R.id.tvContentItems).text = java.text.NumberFormat.getInstance().format(stats.content_items ?: 0)
                    findViewById<TextView>(R.id.tvActiveFaculty).text = java.text.NumberFormat.getInstance().format(stats.active_faculty ?: 0)
                    findViewById<TextView>(R.id.tvAnnouncements).text = java.text.NumberFormat.getInstance().format(stats.announcements ?: 0)
                }
            }

            override fun onFailure(call: Call<com.simats.rankup.network.AdminStatsResponse>, t: Throwable) {
                // Silently fail or log
            }
        })
    }
}
