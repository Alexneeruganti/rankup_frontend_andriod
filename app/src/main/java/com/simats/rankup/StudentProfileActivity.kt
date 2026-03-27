package com.simats.rankup

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import android.widget.ImageView
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import com.simats.rankup.network.ProfilePicUploadResponse
import com.simats.rankup.network.RemoveProfilePicRequest
import com.simats.rankup.network.ApiResponse
import androidx.appcompat.app.AlertDialog
import com.yalantis.ucrop.UCrop

class StudentProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_profile)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.imgProfile).setOnClickListener {
            showProfilePictureOptions()
        }

        fetchUserProfile()
        fetchStudentMentorship()
        fetchLeaderboardRank()
    }

    private fun fetchUserProfile() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)

        if (userId == -1) return

        BackendApiService.api.getProfile(userId).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful && response.body()?.profile != null) {
                    val profile = response.body()?.profile!!
                    findViewById<TextView>(R.id.tvUserName).text = profile.name ?: "Student User"
                    findViewById<TextView>(R.id.tvUserDept).text = profile.department?.uppercase() ?: "GENERAL"
                    findViewById<TextView>(R.id.tvYear).text = profile.register_number ?: "N/A"
                    
                    if (!profile.profile_pic.isNullOrEmpty()) {
                        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
                        Glide.with(this@StudentProfileActivity)
                            .load(BackendApiService.getFullUrl(profile.profile_pic))
                            .placeholder(R.drawable.ic_profile)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .circleCrop()
                            .into(imgProfile)
                    }
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@StudentProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchStudentMentorship() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val studentId = sharedPref.getInt("USER_ID", -1)

        if (studentId == -1) return

        BackendApiService.api.getStudentMentors(studentId).enqueue(object : Callback<com.simats.rankup.network.StudentMentorsResponse> {
            override fun onResponse(call: Call<com.simats.rankup.network.StudentMentorsResponse>, response: Response<com.simats.rankup.network.StudentMentorsResponse>) {
                val mentorContainer = findViewById<LinearLayout>(R.id.mentorContainer)
                mentorContainer?.removeAllViews()

                if (response.isSuccessful && response.body()?.mentors != null) {
                    val mentors = response.body()?.mentors!!
                    if (mentors.isNotEmpty()) {
                        for (mentor in mentors) {
                            val view = LayoutInflater.from(this@StudentProfileActivity)
                                .inflate(R.layout.item_profile_mentor, mentorContainer, false)

                            view.findViewById<TextView>(R.id.tvMentorName).text = mentor.name
                            view.findViewById<TextView>(R.id.tvMentorDept).text = "DEPARTMENT: ${mentor.department}"

                            Glide.with(this@StudentProfileActivity)
                                .load(BackendApiService.getFullUrl(mentor.profile_pic))
                                .placeholder(R.drawable.ic_profile)
                                .into(view.findViewById(R.id.imgMentorProfile))

                            mentorContainer?.addView(view)
                        }
                    } else {
                        val view = LayoutInflater.from(this@StudentProfileActivity)
                                .inflate(R.layout.item_profile_mentor, mentorContainer, false)
                        view.findViewById<TextView>(R.id.tvMentorName).text = "Not Assigned"
                        view.findViewById<TextView>(R.id.tvMentorDept).text = "Pending"
                        mentorContainer?.addView(view)
                    }
                }
            }

            override fun onFailure(call: Call<com.simats.rankup.network.StudentMentorsResponse>, t: Throwable) {
                // Ignore failure UI
            }
        })
    }

    private fun fetchLeaderboardRank() {
         val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
         val studentId = sharedPref.getInt("USER_ID", -1)

         if (studentId == -1) return

         BackendApiService.api.getTestLeaderboard().enqueue(object : Callback<com.simats.rankup.network.LeaderboardResponse> {
             override fun onResponse(call: Call<com.simats.rankup.network.LeaderboardResponse>, response: Response<com.simats.rankup.network.LeaderboardResponse>) {
                 if (response.isSuccessful && response.body()?.leaderboard != null) {
                     val board = response.body()?.leaderboard!!
                     val rankIndex = board.indexOfFirst { it.student_id == studentId }
                     val rankStr = if (rankIndex != -1) "#${rankIndex + 1}" else "NR"
                     findViewById<TextView>(R.id.tvLeaderboardRank).text = rankStr
                 } else {
                     findViewById<TextView>(R.id.tvLeaderboardRank).text = "NR"
                 }
             }

             override fun onFailure(call: Call<com.simats.rankup.network.LeaderboardResponse>, t: Throwable) {
                 findViewById<TextView>(R.id.tvLeaderboardRank).text = "NR"
             }
         })
     }

    private fun showProfilePictureOptions() {
        val options = arrayOf("Choose New Picture", "Remove Profile Picture")
        AlertDialog.Builder(this)
            .setTitle("Profile Picture")
            .setItems(options) { dialog, which ->
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

        Toast.makeText(this, "Removing picture...", Toast.LENGTH_SHORT).show()

        val request = RemoveProfilePicRequest(userId)
        BackendApiService.api.removeProfilePic(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@StudentProfileActivity, "Profile picture removed", Toast.LENGTH_SHORT).show()
                    val imgProfile = findViewById<ImageView>(R.id.imgProfile)
                    imgProfile.setImageResource(R.drawable.ic_profile)
                } else {
                    Toast.makeText(this@StudentProfileActivity, "Failed to remove picture", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@StudentProfileActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let { uploadImage(it) }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            cropError?.printStackTrace()
            Toast.makeText(this, "Crop error", Toast.LENGTH_SHORT).show()
        }
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { sourceUri ->
            val destinationUri = Uri.fromFile(File(cacheDir, "cropped_profile_${System.currentTimeMillis()}.jpg"))
            val uCropIntent = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(800, 800)
                .getIntent(this)
            cropImageLauncher.launch(uCropIntent)
        }
    }

    private fun uploadImage(fileUri: Uri) {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)
        if (userId == -1) return

        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()

        try {
            val inputStream = contentResolver.openInputStream(fileUri)
            val tempFile = File(cacheDir, "upload_profile.jpg")
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), tempFile)
            val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
            val userIdRequest = RequestBody.create(MediaType.parse("text/plain"), userId.toString())

            BackendApiService.api.uploadProfilePic(userIdRequest, body).enqueue(object : Callback<ProfilePicUploadResponse> {
                override fun onResponse(call: Call<ProfilePicUploadResponse>, response: Response<ProfilePicUploadResponse>) {
                    if (response.isSuccessful && response.body()?.error == null) {
                        Toast.makeText(this@StudentProfileActivity, "Profile picture updated!!", Toast.LENGTH_SHORT).show()
                        val newUrl = response.body()?.url
                        if (!newUrl.isNullOrEmpty()) {
                            val imgProfile = findViewById<ImageView>(R.id.imgProfile)
                            Glide.with(this@StudentProfileActivity)
                                .load(BackendApiService.getFullUrl(newUrl))
                                .placeholder(R.drawable.ic_profile)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .circleCrop()
                                .into(imgProfile)
                        }
                    } else {
                        Toast.makeText(this@StudentProfileActivity, "Failed to upload API", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProfilePicUploadResponse>, t: Throwable) {
                    Toast.makeText(this@StudentProfileActivity, "Failed to connect to server", Toast.LENGTH_SHORT).show()
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
        }
    }
}
