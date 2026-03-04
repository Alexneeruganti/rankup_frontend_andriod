package com.simats.rankup

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class FacultyUploadContentActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etTags: EditText
    private lateinit var etVideoLink: EditText
    private lateinit var btnTypePdf: LinearLayout
    private lateinit var btnTypeVideo: LinearLayout
    private lateinit var cardUpload: CardView
    private lateinit var cardVideoLink: CardView

    private var selectedFileUri: android.net.Uri? = null
    private var isPdfSelected = true

    private val pickFileLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedFileUri = uri
            findViewById<TextView>(R.id.btnChooseFile).text = "File Selected"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_upload_content)

        // Initialize Views
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etTags = findViewById(R.id.etTags)
        etVideoLink = findViewById(R.id.etVideoLink)
        btnTypePdf = findViewById(R.id.btnTypePdf)
        btnTypeVideo = findViewById(R.id.btnTypeVideo)
        cardUpload = findViewById(R.id.cardUpload)
        cardVideoLink = findViewById(R.id.cardVideoLink)

        setupNavigation()
        setupToggles()
        setupButtons()
    }

    private fun setupNavigation() {
        // Back Button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
             startActivity(Intent(this, FacultyHomeActivity::class.java))
             finish()
        }
    }

    private fun setupToggles() {
        btnTypePdf.setOnClickListener {
            if (!isPdfSelected) {
                isPdfSelected = true
                updateToggles()
            }
        }

        btnTypeVideo.setOnClickListener {
            if (isPdfSelected) {
                isPdfSelected = false
                updateToggles()
            }
        }
    }

    private fun updateToggles() {
        if (isPdfSelected) {
            // activate PDF
            btnTypePdf.setBackgroundResource(R.drawable.bg_button_toggle_active)
            setToggleTextColor(btnTypePdf, true)
            
            btnTypeVideo.setBackgroundResource(R.drawable.bg_button_toggle_inactive)
            setToggleTextColor(btnTypeVideo, false)

            // Show PDF upload, Hide Video Link
            cardUpload.visibility = View.VISIBLE
            cardVideoLink.visibility = View.GONE

        } else {
            // activate Video
            btnTypePdf.setBackgroundResource(R.drawable.bg_button_toggle_inactive)
            setToggleTextColor(btnTypePdf, false)

            btnTypeVideo.setBackgroundResource(R.drawable.bg_button_toggle_active)
            setToggleTextColor(btnTypeVideo, true)

            // Hide PDF upload, Show Video Link
            cardUpload.visibility = View.GONE
            cardVideoLink.visibility = View.VISIBLE
        }
    }

    private fun setToggleTextColor(layout: LinearLayout, isActive: Boolean) {
        // Assuming the layout has an ImageView at index 0 and TextView at index 1
        val icon = layout.getChildAt(0) as? android.widget.ImageView
        val text = layout.getChildAt(1) as? TextView

        if (isActive) {
            icon?.setColorFilter(Color.WHITE)
            text?.setTextColor(Color.WHITE)
        } else {
            icon?.setColorFilter(Color.parseColor("#757575"))
            text?.setTextColor(Color.parseColor("#757575"))
        }
    }

    private fun setupButtons() {
        findViewById<TextView>(R.id.btnPublish).setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val tags = etTags.text.toString().trim()
            val resourceType = if (isPdfSelected) "NOTES" else "VIDEO"
            var fileLink = "local_file" // Placeholder for actual file upload URL
            
            // Get category. The radio group logic will be added soon. Defaulting to Placement
            val rgCategory = findViewById<android.widget.RadioGroup>(R.id.rgCategory)
            val selectedCategoryId = rgCategory?.checkedRadioButtonId
            val category = if (selectedCategoryId == R.id.rbHigherEdu) {
                "Higher Education"
            } else {
                "Placement"
            }

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isPdfSelected) {
                if (selectedFileUri == null) {
                    Toast.makeText(this, "Please select a PDF file", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                uploadPdfThenPublish(title, description, tags, resourceType, category)
            } else {
                fileLink = etVideoLink.text.toString().trim()
                if (fileLink.isEmpty()) {
                    Toast.makeText(this, "Please enter a video link", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                publishResource(title, description, tags, resourceType, fileLink, category)
            }
        }

        findViewById<TextView>(R.id.btnChooseFile).setOnClickListener {
            pickFileLauncher.launch("application/pdf")
        }
    }

    private fun uploadPdfThenPublish(title: String, description: String, tags: String, resourceType: String, category: String) {
        val uri = selectedFileUri ?: return
        Toast.makeText(this, "Uploading PDF...", Toast.LENGTH_SHORT).show()

        val file = java.io.File(cacheDir, "upload_temp.pdf")
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                java.io.FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error reading file: ${e.message}", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/pdf"), file)
        val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestBody)

        com.simats.rankup.network.BackendApiService.api.uploadFile(body).enqueue(object : retrofit2.Callback<com.simats.rankup.network.FileUploadResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.rankup.network.FileUploadResponse>, response: retrofit2.Response<com.simats.rankup.network.FileUploadResponse>) {
                val fileUrl = response.body()?.file_url
                if (response.isSuccessful && fileUrl != null) {
                    publishResource(title, description, tags, resourceType, fileUrl, category)
                } else {
                    Toast.makeText(this@FacultyUploadContentActivity, "PDF Upload Failed: ${response.body()?.error}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.FileUploadResponse>, t: Throwable) {
                Toast.makeText(this@FacultyUploadContentActivity, "PDF Upload Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun publishResource(title: String, description: String, tags: String, resourceType: String, fileLink: String, category: String) {
        val facultyId = 1 // Hardcoded to 1 because 2 doesn't exist in the database (Foreign Key constraint)

        val request = com.simats.rankup.network.UploadResourceRequest(
            faculty_id = facultyId,
            title = title,
            description = description,
            tags = tags,
            category = category,
            resource_type = resourceType,
            file_link = fileLink
        )

        Toast.makeText(this, "Publishing Content...", Toast.LENGTH_SHORT).show()

        com.simats.rankup.network.BackendApiService.api.uploadResource(request).enqueue(object : retrofit2.Callback<com.simats.rankup.network.ApiResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.simats.rankup.network.ApiResponse>,
                response: retrofit2.Response<com.simats.rankup.network.ApiResponse>
            ) {
                if (response.isSuccessful && response.body()?.error == null) {
                    Toast.makeText(this@FacultyUploadContentActivity, "Content Published Successfully!", Toast.LENGTH_LONG).show()
                    
                    // Clear fields
                    etTitle.text.clear()
                    etDescription.text.clear()
                    etTags.text.clear()
                    etVideoLink.text.clear()
                    selectedFileUri = null
                    findViewById<TextView>(R.id.btnChooseFile).text = "Choose File"
                } else {
                    Toast.makeText(this@FacultyUploadContentActivity, "Failed: ${response.body()?.error ?: "Unknown Error"}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.ApiResponse>, t: Throwable) {
                Toast.makeText(this@FacultyUploadContentActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
