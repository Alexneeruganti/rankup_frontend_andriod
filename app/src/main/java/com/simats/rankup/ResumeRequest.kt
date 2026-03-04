package com.simats.rankup

data class ResumeData(
    val name: String,
    val jobTitle: String,
    val email: String,
    val phone: String,
    val location: String,
    val objective: String,
    val education: String,
    val experience: String,
    val skills: String,
    val languages: String,
    val references: String
)

data class ResumeRequest(
    val requestId: String,
    val studentName: String,
    val studentEmail: String,
    val facultyId: String,
    val facultyName: String,
    val resumeData: ResumeData,
    val templateId: String,
    var status: String, // "Pending", "Allowed", "Change"
    var feedback: String,
    val timestamp: Long
)
