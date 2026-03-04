package com.simats.rankup

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ResumeRequestManager {
    private const val PREF_NAME = "rankup_resume_requests"
    private const val KEY_REQUESTS = "key_requests"
    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveRequest(context: Context, request: ResumeRequest) {
        val requests = getAllRequests(context).toMutableList()
        val existingIndex = requests.indexOfFirst { it.requestId == request.requestId }
        if (existingIndex != -1) {
            requests[existingIndex] = request
        } else {
            requests.add(0, request)
        }
        
        val json = gson.toJson(requests)
        getPrefs(context).edit().putString(KEY_REQUESTS, json).apply()
    }

    fun getAllRequests(context: Context): List<ResumeRequest> {
        val json = getPrefs(context).getString(KEY_REQUESTS, null)
        return if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<ResumeRequest>>() {}.type
            gson.fromJson(json, type)
        }
    }

    fun getRequestsForStudent(context: Context, studentName: String): List<ResumeRequest> {
        return getAllRequests(context).filter { it.studentName.equals(studentName, ignoreCase = true) }
    }

    fun getRequestsForFaculty(context: Context, facultyId: String): List<ResumeRequest> {
        return getAllRequests(context).filter { it.facultyId == facultyId }
    }

    // Helper to add some dummy data for demo purposes if empty
    fun getRequestsWithDummy(context: Context, userRole: String, identifier: String): List<ResumeRequest> {
        var requests = if (userRole == "Student") {
            getRequestsForStudent(context, identifier)
        } else {
            getRequestsForFaculty(context, identifier)
        }
        
        // Ensure dummy data is always populated for demonstration
        if (getAllRequests(context).none { it.requestId == "REQ-001" }) {
            val dummy1 = ResumeRequest(
                requestId = "REQ-001",
                studentName = "John Doe",
                studentEmail = "john.doe@student.edu",
                facultyId = "FAC001",
                facultyName = "Dr. Emily Carter",
                resumeData = ResumeData("John Doe", "Software Engineer", "john.doe@student.edu", "555-0101", "New York", "Seeking a software engineering role...", "B.S. Comp Sci", "Intern at TechCorp", "Java, Kotlin", "English", ""),
                templateId = "MODERN",
                status = "Pending",
                feedback = "",
                timestamp = System.currentTimeMillis() - 86400000 // 1 day ago
            )
            val dummy2 = ResumeRequest(
                requestId = "REQ-002",
                studentName = "Jane Smith",
                studentEmail = "jane.smith@student.edu",
                facultyId = "FAC001",
                facultyName = "Dr. Emily Carter",
                resumeData = ResumeData("Jane Smith", "Data Analyst", "jane.smith@student.edu", "555-0202", "San Francisco", "Looking for data analysis position...", "M.S. Data Science", "Research Assistant", "Python, SQL", "English, Spanish", ""),
                templateId = "CLASSIC",
                status = "Allowed",
                feedback = "Great resume, well structured.",
                timestamp = System.currentTimeMillis() - 172800000 // 2 days ago
            )
            val dummy3 = ResumeRequest(
                requestId = "REQ-003",
                studentName = "Alice Johnson",
                studentEmail = "alice.j@student.edu",
                facultyId = "FAC002",
                facultyName = "Prof. Alan Turing",
                resumeData = ResumeData("Alice Johnson", "Frontend Developer", "alice.j@student.edu", "555-0303", "Austin", "Passionate about UI/UX...", "B.A. Design", "Freelance Web Dev", "HTML, CSS, JS, React", "English", ""),
                templateId = "MODERN",
                status = "Change",
                feedback = "Please add more details to your experience section.",
                timestamp = System.currentTimeMillis() - 43200000 // 12 hours ago
            )
            saveRequest(context, dummy1)
            saveRequest(context, dummy2)
            saveRequest(context, dummy3)
            
            // Re-fetch after saving dummy data
            requests = if (userRole == "Student") {
                getRequestsForStudent(context, identifier)
            } else {
                getRequestsForFaculty(context, identifier)
            }
        }
        
        // If a specific student logs in but they don't match the dummy names, we just show all requests for demo testing
        if (requests.isEmpty()) {
            requests = getAllRequests(context)
        }
        
        return requests
    }
}
