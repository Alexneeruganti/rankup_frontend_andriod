package com.simats.rankup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.widget.ImageButton
import android.net.Uri
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.UserProfileResponse
import com.simats.rankup.network.ResumeReviewsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import androidx.activity.result.contract.ActivityResultContracts
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import com.simats.rankup.network.ProfilePicUploadResponse
import com.simats.rankup.network.RemoveProfilePicRequest
import com.simats.rankup.network.ApiResponse
import com.simats.rankup.network.PostTestimonialRequest
import androidx.appcompat.app.AlertDialog
import com.yalantis.ucrop.UCrop

class ProfileActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navView)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, StudentHomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    finish()
                }
                R.id.nav_find_faculty -> {
                    startActivity(Intent(this, MentorsActivity::class.java))
                }
                else -> Toast.makeText(this, "${menuItem.title} Clicked", Toast.LENGTH_SHORT).show()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        findViewById<ImageView>(R.id.imgProfile).setOnClickListener {
            showProfilePictureOptions()
        }

        setupBottomNavigation()
        setupLogout()
        fetchUserProfile()
        fetchStudentMentorship()
        fetchLeaderboardRank()

        findViewById<Button>(R.id.btnPostTestimonial).setOnClickListener {
            postTestimonial()
        }
    }



    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_profile

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, StudentHomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_ranks -> {
                    startActivity(Intent(this, RankingsActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_learn -> {
                    startActivity(Intent(this, LearningActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun setupLogout() {
        // Main content logout button
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            performLogout()
        }

        // Sidebar close button
        val navView = findViewById<NavigationView>(R.id.navView)
        val headerView = navView.getHeaderView(0)
        headerView.findViewById<ImageButton>(R.id.btnCloseDrawer)?.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Sidebar logout button
        val navFooter = findViewById<android.view.View>(R.id.navFooter)
        val btnTerminateSession = navFooter?.findViewById<android.view.View>(R.id.btnTerminateSession) ?: findViewById(R.id.btnTerminateSession)
        
        btnTerminateSession?.setOnClickListener {
            performLogout()
        }
        
        findViewById<androidx.cardview.widget.CardView>(R.id.cardResume).setOnClickListener {
             checkApprovedResumeAndNavigate()
        }
    }

    private fun performLogout() {
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, PlacementLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun checkApprovedResumeAndNavigate() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val studentId = sharedPref.getInt("USER_ID", -1)

        if (studentId == -1) {
            Toast.makeText(this, "Error: Student ID not found. Please log in.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, StudentResumeBuilderActivity::class.java))
            return
        }

        Toast.makeText(this, "Checking your final resume status...", Toast.LENGTH_SHORT).show()

        BackendApiService.api.getStudentResumes(studentId).enqueue(object : Callback<ResumeReviewsResponse> {
            override fun onResponse(call: Call<ResumeReviewsResponse>, response: Response<ResumeReviewsResponse>) {
                var handled = false
                if (response.isSuccessful && response.body()?.error == null) {
                    val resumes = response.body()?.resumes ?: emptyList()
                    val approvedResume = resumes.firstOrNull { it.status.equals("APPROVED", ignoreCase = true) }
                    
                    if (approvedResume != null && !approvedResume.resume_url.isNullOrEmpty()) {
                        handled = true
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(approvedResume.resume_url))
                        startActivity(intent)
                    }
                }
                
                if (!handled) {
                    Toast.makeText(this@ProfileActivity, "No approved resume found. Returning to builder.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ProfileActivity, StudentResumeBuilderActivity::class.java)
                    startActivity(intent)
                }
            }

            override fun onFailure(call: Call<ResumeReviewsResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Network Error: Opening Builder instead...", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@ProfileActivity, StudentResumeBuilderActivity::class.java)
                startActivity(intent)
            }
        })
    }
    
    private fun fetchUserProfile() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)

        if (userId == -1) return

        BackendApiService.api.getProfile(userId).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful && response.body()?.profile != null) {
                    val profile = response.body()?.profile!!
                    findViewById<TextView>(R.id.tvUserName).text = profile.name
                    findViewById<TextView>(R.id.tvUserDept).text = profile.department ?: "GENERAL"
                    findViewById<TextView>(R.id.tvUserPhone).text = profile.phone ?: "Not Provided"
                    
                    // Update chips if necessary (e.g. showing role or register number)
                    val layoutChips = findViewById<LinearLayout>(R.id.layoutChips)
                    if (layoutChips.childCount >= 2) {
                        (layoutChips.getChildAt(0) as? TextView)?.text = profile.register_number
                        (layoutChips.getChildAt(1) as? TextView)?.text = profile.role?.uppercase() ?: "STUDENT"
                    }
                    
                    if (!profile.profile_pic.isNullOrEmpty()) {
                        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
                        Glide.with(this@ProfileActivity)
                            .load(profile.profile_pic)
                            .placeholder(R.drawable.ic_profile)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .circleCrop()
                            .into(imgProfile)
                    }
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
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
                            val view = LayoutInflater.from(this@ProfileActivity)
                                .inflate(R.layout.item_profile_mentor, mentorContainer, false)

                            view.findViewById<TextView>(R.id.tvMentorName).text = mentor.name
                            view.findViewById<TextView>(R.id.tvMentorDept).text = "DEPARTMENT: ${mentor.department}"

                            Glide.with(this@ProfileActivity)
                                .load(mentor.profile_pic)
                                .placeholder(R.drawable.ic_profile)
                                .into(view.findViewById(R.id.imgMentorProfile))

                            mentorContainer?.addView(view)
                        }
                    } else {
                        val view = LayoutInflater.from(this@ProfileActivity)
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
                     findViewById<TextView>(R.id.tvLeaderboardRank)?.text = rankStr
                 } else {
                     findViewById<TextView>(R.id.tvLeaderboardRank)?.text = "NR"
                 }
             }

             override fun onFailure(call: Call<com.simats.rankup.network.LeaderboardResponse>, t: Throwable) {
                 findViewById<TextView>(R.id.tvLeaderboardRank)?.text = "NR"
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
                    Toast.makeText(this@ProfileActivity, "Profile picture removed", Toast.LENGTH_SHORT).show()
                    val imgProfile = findViewById<ImageView>(R.id.imgProfile)
                    imgProfile.setImageResource(R.drawable.ic_profile)
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to remove picture", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Network Error", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@ProfileActivity, "Profile picture updated!!", Toast.LENGTH_SHORT).show()
                        val newUrl = response.body()?.url
                        if (!newUrl.isNullOrEmpty()) {
                            val imgProfile = findViewById<ImageView>(R.id.imgProfile)
                            Glide.with(this@ProfileActivity)
                                .load(newUrl)
                                .placeholder(R.drawable.ic_profile)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .circleCrop()
                                .into(imgProfile)
                        }
                    } else {
                        Toast.makeText(this@ProfileActivity, "Failed to upload API", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProfilePicUploadResponse>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Failed to connect to server", Toast.LENGTH_SHORT).show()
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun postTestimonial() {
        val etTestimonial = findViewById<android.widget.EditText>(R.id.etTestimonial)
        val content = etTestimonial.text.toString().trim()

        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter a testimonial", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val studentId = sharedPref.getInt("USER_ID", -1)

        if (studentId == -1) {
            Toast.makeText(this, "User session error", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Posting testimonial...", Toast.LENGTH_SHORT).show()

        val request = PostTestimonialRequest(studentId, content)
        BackendApiService.api.postTestimonial(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Testimonial posted successfully!", Toast.LENGTH_SHORT).show()
                    etTestimonial.text.clear()
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to post testimonial", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        fetchUserProfile()
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_profile
    }

    data class Mentor(val name: String, val role: String, val dept: String, val isActive: Boolean)
}
