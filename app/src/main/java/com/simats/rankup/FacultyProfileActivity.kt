package com.simats.rankup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.bumptech.glide.Glide
import android.widget.ImageView

class FacultyProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_profile)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish() // Go back to previous activity (Home)
        }

        findViewById<TextView>(R.id.btnProfileLogout).setOnClickListener {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PlacementLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        findViewById<TextView>(R.id.btnManageRequests).setOnClickListener {
            startActivity(Intent(this, FacultyStudentRequestsActivity::class.java))
        }

        fetchUserProfile()
        fetchMentees()
    }

    private fun fetchUserProfile() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)

        if (userId == -1) return

        findViewById<ImageView>(R.id.imgProfileAvatar).setOnClickListener {
            showProfilePictureOptions()
        }

        BackendApiService.api.getProfile(userId).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful && response.body()?.profile != null) {
                    val profile = response.body()?.profile!!
                    findViewById<TextView>(R.id.tvProfileName).text = profile.name ?: "Unknown"
                    findViewById<TextView>(R.id.tvProfileRole).text = (profile.role ?: "FACULTY").uppercase()
                    findViewById<TextView>(R.id.tvProfileDept).text = profile.department ?: "N/A"
                    findViewById<TextView>(R.id.tvProfileEmail).text = profile.email ?: "No Email"
                    findViewById<TextView>(R.id.tvProfilePhone).text = profile.phone ?: "Not Provided"

                    val imgProfileAvatar = findViewById<ImageView>(R.id.imgProfileAvatar)
                    if (!profile.profile_pic.isNullOrEmpty()) {
                        val fullUrl = if (profile.profile_pic!!.startsWith("http")) profile.profile_pic else "${BackendApiService.BASE_URL.removeSuffix("/")}${profile.profile_pic}"
                        Glide.with(this@FacultyProfileActivity)
                            .load(fullUrl)
                            .placeholder(R.drawable.ic_profile)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .circleCrop()
                            .into(imgProfileAvatar)
                    } else {
                        imgProfileAvatar.setImageResource(R.drawable.ic_profile)
                    }
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@FacultyProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
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
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)
        if (userId == -1) return

        val request = com.simats.rankup.network.RemoveProfilePicRequest(userId)
        BackendApiService.api.removeProfilePic(request).enqueue(object : Callback<com.simats.rankup.network.ApiResponse> {
            override fun onResponse(call: Call<com.simats.rankup.network.ApiResponse>, response: Response<com.simats.rankup.network.ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@FacultyProfileActivity, "Profile picture removed", Toast.LENGTH_SHORT).show()
                    findViewById<ImageView>(R.id.imgProfileAvatar).setImageResource(R.drawable.ic_profile)
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
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
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
                        Toast.makeText(this@FacultyProfileActivity, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                        val newUrl = response.body()?.url
                        if (!newUrl.isNullOrEmpty()) {
                            val imgProfile = findViewById<ImageView>(R.id.imgProfileAvatar)
                            val fullUrl = if (newUrl.startsWith("http")) newUrl else "${BackendApiService.BASE_URL.removeSuffix("/")}$newUrl"
                            Glide.with(this@FacultyProfileActivity)
                                .load(fullUrl)
                                .placeholder(R.drawable.ic_profile)
                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .circleCrop()
                                .into(imgProfile)
                        }
                    }
                }
                override fun onFailure(call: Call<com.simats.rankup.network.ProfilePicUploadResponse>, t: Throwable) {
                    Toast.makeText(this@FacultyProfileActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fetchMentees() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val facultyId = sharedPref.getInt("USER_ID", -1)

        if (facultyId == -1) return

        BackendApiService.api.getMentees(facultyId).enqueue(object : Callback<com.simats.rankup.network.MenteesResponse> {
            override fun onResponse(call: Call<com.simats.rankup.network.MenteesResponse>, response: Response<com.simats.rankup.network.MenteesResponse>) {
                val container = findViewById<android.widget.LinearLayout>(R.id.containerMentors)
                container.removeAllViews()

                if (response.isSuccessful && response.body()?.mentees != null) {
                    val mentees = response.body()?.mentees!!
                    if (mentees.isEmpty()) {
                        val emptyText = TextView(this@FacultyProfileActivity)
                        emptyText.text = "No mentored students yet."
                        emptyText.setPadding(32, 32, 32, 32)
                        container.addView(emptyText)
                        return
                    }

                    for (student in mentees) {
                        try {
                            val view = layoutInflater.inflate(R.layout.item_profile_mentor, container, false)
                            val tvName = view.findViewById<TextView>(R.id.tvMentorName)
                            val tvRole = view.findViewById<TextView>(R.id.tvMentorRole)
                            val tvDept = view.findViewById<TextView>(R.id.tvMentorDept)

                            tvName?.text = student.student_name ?: "Unknown"
                            tvRole?.text = "STUDENT MENTEE"
                            tvDept?.text = "${student.department ?: "GENERAL"} • ${student.register_number ?: "N/A"}"

                            container.addView(view)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    val emptyText = TextView(this@FacultyProfileActivity)
                    emptyText.text = "No mentored students matched."
                    emptyText.setPadding(32, 32, 32, 32)
                    container.addView(emptyText)
                }
            }

            override fun onFailure(call: Call<com.simats.rankup.network.MenteesResponse>, t: Throwable) {
                Toast.makeText(this@FacultyProfileActivity, "Failed to load mentees", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
