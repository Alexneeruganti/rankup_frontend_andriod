package com.simats.rankup

object StudentManager {
    data class StudentRequest(
        val name: String,
        val regNo: String,
        val dept: String,
        val year: String,
        val type: String // "MENTORSHIP" or "CLASS"
    )

    val menteeRequests = mutableListOf(
        StudentRequest("Alex Johnson", "CS202212", "Computer Science", "3rd Year", "MENTORSHIP"),
        StudentRequest("Sarah Lee", "CS202215", "Computer Science", "3rd Year", "MENTORSHIP")
    )

    val classRequests = mutableListOf(
        StudentRequest("User Name", "IT202305", "Information Technology", "2nd Year", "CLASS"),
        StudentRequest("Rahul Varma", "IT202308", "Information Technology", "2nd Year", "CLASS")
    )

    val myMentees = mutableListOf<StudentRequest>()
    val myClass = mutableListOf<StudentRequest>()

    fun acceptRequest(request: StudentRequest) {
        if (request.type == "MENTORSHIP") {
            menteeRequests.remove(request)
            myMentees.add(request)
        } else {
            classRequests.remove(request)
            myClass.add(request)
        }
    }

    fun declineRequest(request: StudentRequest) {
        if (request.type == "MENTORSHIP") {
            menteeRequests.remove(request)
        } else {
            classRequests.remove(request)
        }
    }
    fun removeStudent(student: StudentRequest) {
        if (student.type == "MENTORSHIP") {
            myMentees.remove(student)
        } else {
            myClass.remove(student)
        }
    }
}
