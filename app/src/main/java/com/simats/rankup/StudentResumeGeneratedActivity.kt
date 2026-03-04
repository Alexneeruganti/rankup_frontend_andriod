package com.simats.rankup

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.OutputStream
import java.util.UUID
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.StudentMentorsResponse
import com.simats.rankup.network.FacultyMember
import com.simats.rankup.network.SubmitResumeRequest
import com.simats.rankup.network.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentResumeGeneratedActivity : AppCompatActivity() {

    private lateinit var resumeContainer: FrameLayout
    private var pendingPdfDocument: PdfDocument? = null

    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                saveDocumentToUri(uri)
            }
        } else {
            pendingPdfDocument?.close()
            pendingPdfDocument = null
            Toast.makeText(this, "Save cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_resume_generated)

        resumeContainer = findViewById(R.id.resumeContainer)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val templateId = intent.getStringExtra("TEMPLATE_ID") ?: "MODERN"
        populateData(templateId)

        findViewById<Button>(R.id.btnDownload).setOnClickListener {
            generatePdfFromView(resumeContainer)
        }

        findViewById<Button>(R.id.btnSendMentor).setOnClickListener {
            sendToMentor()
        }
    }

    private fun populateData(templateId: String) {
        val name = intent.getStringExtra("NAME") ?: ""
        val jobTitle = intent.getStringExtra("JOB_TITLE") ?: ""
        val email = intent.getStringExtra("EMAIL") ?: ""
        val phone = intent.getStringExtra("PHONE") ?: ""
        val location = intent.getStringExtra("LOCATION") ?: ""
        val objective = intent.getStringExtra("OBJECTIVE") ?: ""
        val education = intent.getStringExtra("EDUCATION") ?: ""
        val experience = intent.getStringExtra("EXPERIENCE") ?: ""
        val skills = intent.getStringExtra("SKILLS") ?: ""
        val languages = intent.getStringExtra("LANGUAGES") ?: ""
        val references = intent.getStringExtra("REFERENCES") ?: ""

        val layoutTemplate1 = findViewById<View>(R.id.layoutTemplate1)
        val layoutTemplate2 = findViewById<View>(R.id.layoutTemplate2)

        if (templateId == "CLASSIC") {
            // Template 2 Configuration
            layoutTemplate2.visibility = View.VISIBLE
            
            findViewById<TextView>(R.id.t2_name).text = name.uppercase()
            findViewById<TextView>(R.id.t2_jobTitle).text = jobTitle
            findViewById<TextView>(R.id.t2_phone).text = phone
            findViewById<TextView>(R.id.t2_email).text = email
            findViewById<TextView>(R.id.t2_location).text = location
            findViewById<TextView>(R.id.t2_objective).text = objective
            findViewById<TextView>(R.id.t2_education).text = education
            findViewById<TextView>(R.id.t2_experience).text = experience
            findViewById<TextView>(R.id.t2_skills).text = skills
            // Note: Template 2 image doesn't explicitly have languages/references blocks, so we omit or append to skills.

        } else {
            // Template 1 Configuration (Modern)
            layoutTemplate1.visibility = View.VISIBLE
            
            findViewById<TextView>(R.id.t1_name).text = name
            findViewById<TextView>(R.id.t1_jobTitle).text = jobTitle
            findViewById<TextView>(R.id.t1_phone).text = phone
            findViewById<TextView>(R.id.t1_email).text = email
            findViewById<TextView>(R.id.t1_location).text = location
            findViewById<TextView>(R.id.t1_objective).text = objective
            findViewById<TextView>(R.id.t1_education).text = education
            findViewById<TextView>(R.id.t1_experience).text = experience
            findViewById<TextView>(R.id.t1_skills).text = skills
            findViewById<TextView>(R.id.t1_languages).text = languages
            findViewById<TextView>(R.id.t1_references).text = references
        }
    }

    private fun generatePdfFromView(view: View) {
        Toast.makeText(this, "Generating PDF...", Toast.LENGTH_SHORT).show()

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val customCanvas = page.canvas
        customCanvas.drawBitmap(bitmap, 0f, 0f, null)
        pdfDocument.finishPage(page)

        pendingPdfDocument = pdfDocument
        launchSaveIntent()
    }

    private fun launchSaveIntent() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Resume_${System.currentTimeMillis()}.pdf")
        }
        createDocumentLauncher.launch(intent)
    }

    private fun saveDocumentToUri(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                pendingPdfDocument?.writeTo(outputStream)
                Toast.makeText(this, "Resume saved successfully!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pendingPdfDocument?.close()
            pendingPdfDocument = null
        }
    }

    private fun sendToMentor() {
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val studentId = sharedPref.getInt("USER_ID", -1)
        
        if (studentId == -1) {
            Toast.makeText(this, "Error: Student ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Fetching assigned mentors...", Toast.LENGTH_SHORT).show()

        BackendApiService.api.getStudentMentors(studentId).enqueue(object : Callback<StudentMentorsResponse> {
            override fun onResponse(call: Call<StudentMentorsResponse>, response: Response<StudentMentorsResponse>) {
                if (response.isSuccessful && response.body()?.error == null) {
                    val mentors = response.body()?.mentors ?: emptyList()
                    if (mentors.isEmpty()) {
                        Toast.makeText(this@StudentResumeGeneratedActivity, "You have not been assigned to any mentors yet.", Toast.LENGTH_LONG).show()
                        return
                    }

                    val mentorNames = mentors.map { it.name }.toTypedArray()
                    AlertDialog.Builder(this@StudentResumeGeneratedActivity)
                        .setTitle("Select a Mentor")
                        .setItems(mentorNames) { _, which ->
                            val selectedMentor = mentors[which]
                            confirmSendToMentor(selectedMentor, studentId)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    Toast.makeText(this@StudentResumeGeneratedActivity, "Failed to load mentors.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StudentMentorsResponse>, t: Throwable) {
                Toast.makeText(this@StudentResumeGeneratedActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun confirmSendToMentor(mentor: FacultyMember, studentId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Send Resume")
            .setMessage("Are you sure you want to send this resume to ${mentor.name} for review?")
            .setPositiveButton("Send") { _, _ ->
                proceedUploadAndSend(mentor, studentId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun proceedUploadAndSend(mentor: FacultyMember, studentId: Int) {
        // Normally, the PDF is generated and saved as a File locally.
        // For the sake of the MVP, since `submit-resume` expects a `resume_url`,
        // and we have an `/upload-file` endpoint mapped to `uploadFile()`, 
        // we first upload a temporary PDF file, grab URL, then submit it.

        Toast.makeText(this, "Uploading Resume to Server...", Toast.LENGTH_SHORT).show()
        
        // 1. Generate temp PDF
        val view = resumeContainer
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) { bgDrawable.draw(canvas) } else { canvas.drawColor(Color.WHITE) }
        view.draw(canvas)

        val tempFile = java.io.File(cacheDir, "resume_upload_${System.currentTimeMillis()}.pdf")
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
        pdfDocument.finishPage(page)

        try {
            java.io.FileOutputStream(tempFile).use { out ->
                pdfDocument.writeTo(out)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to create temp PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            return
        } finally {
            pdfDocument.close()
        }

        // 2. Upload file
        val requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/pdf"), tempFile)
        val body = okhttp3.MultipartBody.Part.createFormData("file", tempFile.name, requestBody)

        BackendApiService.api.uploadFile(body).enqueue(object : retrofit2.Callback<com.simats.rankup.network.FileUploadResponse> {
            override fun onResponse(call: retrofit2.Call<com.simats.rankup.network.FileUploadResponse>, response: retrofit2.Response<com.simats.rankup.network.FileUploadResponse>) {
                val fileUrl = response.body()?.file_url
                if (response.isSuccessful && fileUrl != null) {
                    // 3. Submit Resume Request
                    submitFinalResume(studentId, mentor.id.toInt(), fileUrl, mentor.name)
                } else {
                    Toast.makeText(this@StudentResumeGeneratedActivity, "Upload Failed: ${response.body()?.error}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.simats.rankup.network.FileUploadResponse>, t: Throwable) {
                Toast.makeText(this@StudentResumeGeneratedActivity, "Upload Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun submitFinalResume(studentId: Int, mentorId: Int, fileUrl: String, mentorName: String) {
        val request = SubmitResumeRequest(studentId, mentorId, fileUrl)
        
        BackendApiService.api.submitResume(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.error == null) {
                    Toast.makeText(this@StudentResumeGeneratedActivity, "Resume successfully submitted to $mentorName!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@StudentResumeGeneratedActivity, "Failed to submit: ${response.body()?.error}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@StudentResumeGeneratedActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
