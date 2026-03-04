package com.simats.rankup

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.rankup.network.ApiResponse
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.JoinRequest
import com.simats.rankup.network.JoinRequestsResponse
import com.simats.rankup.network.MenteesResponse
import com.simats.rankup.network.ClassStudentsResponse
import com.simats.rankup.network.UpdateStatusRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FacultyStudentRequestsActivity : AppCompatActivity() {

    private lateinit var tabMentee: TextView
    private lateinit var tabClass: TextView
    private lateinit var subTabMentees: TextView
    private lateinit var subTabClass: TextView
    private lateinit var rvRequests: RecyclerView
    private lateinit var adapter: StudentRequestAdapter
    
    private var currentFacultyId = -1
    
    // Live Server Data Store
    private var allPendingRequests = listOf<JoinRequest>()
    private var allAcceptedMentees = listOf<JoinRequest>() // Coming in future update
    private var allAcceptedClass = listOf<JoinRequest>() // Coming in future update
    
    private enum class ViewState {
        MENTEE_REQ, CLASS_REQ, MY_MENTEES, MY_CLASS
    }
    
    private var currentState = ViewState.MENTEE_REQ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_student_requests)
        
        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        currentFacultyId = sharedPref.getInt("USER_ID", -1)

        if (currentFacultyId == -1) {
            Toast.makeText(this, "Session Expired. Please Log In.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tabMentee = findViewById(R.id.tabMentee)
        tabClass = findViewById(R.id.tabClass)
        subTabMentees = findViewById(R.id.subTabMentees)
        subTabClass = findViewById(R.id.subTabClass)
        rvRequests = findViewById(R.id.rvRequests)

        setupNavigation()
        setupTabs()
        setupList()
    }

    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupTabs() {
        tabMentee.setOnClickListener {
            updateState(ViewState.MENTEE_REQ)
        }

        tabClass.setOnClickListener {
            updateState(ViewState.CLASS_REQ)
        }
        
        subTabMentees.setOnClickListener {
            updateState(ViewState.MY_MENTEES)
        }
        
        subTabClass.setOnClickListener {
            updateState(ViewState.MY_CLASS)
        }
    }
    
    private fun updateState(newState: ViewState) {
        currentState = newState
        updateTabVisuals()
        filterAndDisplayRequests() // Switch lists locally if possible
    }

    private fun updateTabVisuals() {
        resetTab(tabMentee)
        resetTab(tabClass)
        resetTab(subTabMentees)
        resetTab(subTabClass)

        when (currentState) {
            ViewState.MENTEE_REQ -> activateTab(tabMentee)
            ViewState.CLASS_REQ -> activateTab(tabClass)
            ViewState.MY_MENTEES -> activateTab(subTabMentees)
            ViewState.MY_CLASS -> activateTab(subTabClass)
        }
    }
    
    private fun resetTab(tab: TextView) {
        tab.background = null
        tab.setTextColor(Color.parseColor("#757575"))
    }
    
    private fun activateTab(tab: TextView) {
        tab.setBackgroundResource(R.drawable.bg_button_faculty)
        tab.setTextColor(Color.WHITE)
    }

    private fun setupList() {
        rvRequests.layoutManager = LinearLayoutManager(this)
        
        adapter = StudentRequestAdapter(emptyList(), { req, accepted ->
            
            if (currentState == ViewState.MY_MENTEES || currentState == ViewState.MY_CLASS) {
                // HANDLE REMOVAL
                val removeReq = com.simats.rankup.network.RemoveMemberRequest(currentFacultyId, req.student_id)
                val call = if (currentState == ViewState.MY_MENTEES) {
                    BackendApiService.api.removeMentee(removeReq)
                } else {
                    BackendApiService.api.removeClassStudent(removeReq)
                }
                
                call.enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@FacultyStudentRequestsActivity, "Student Removed", Toast.LENGTH_SHORT).show()
                            fetchAcceptedStudents()
                        } else {
                            Toast.makeText(this@FacultyStudentRequestsActivity, "Removal Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(this@FacultyStudentRequestsActivity, "Network Error", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                // Handle Action API Call (Join Requests)
                val newStatus = if (accepted) "APPROVED" else "REJECTED"
                val updateReq = UpdateStatusRequest(req.request_id, newStatus)
                
                BackendApiService.api.updateJoinRequest(updateReq).enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@FacultyStudentRequestsActivity, "Request $newStatus", Toast.LENGTH_SHORT).show()
                            fetchPendingRequests() // Refresh the data from backend
                            fetchAcceptedStudents() // Refresh accepted lists if it was approved
                        } else {
                             Toast.makeText(this@FacultyStudentRequestsActivity, "Update Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(this@FacultyStudentRequestsActivity, "Network Error", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            
        }, { req ->
            // Profile Click Handler
            val intent = android.content.Intent(this, StudentProfileActivity::class.java)
            intent.putExtra("name", req.student_name)
            intent.putExtra("dept", req.department)
            intent.putExtra("year", "N/A") // From API later
            startActivity(intent)
        })
        
        rvRequests.adapter = adapter
        
        // Initial Fetch
        fetchPendingRequests()
        fetchAcceptedStudents()
    }

    private fun fetchAcceptedStudents() {
        BackendApiService.api.getMentees(currentFacultyId).enqueue(object : Callback<MenteesResponse> {
            override fun onResponse(call: Call<MenteesResponse>, response: Response<MenteesResponse>) {
                if (response.isSuccessful) {
                    val mentees = response.body()?.mentees ?: emptyList()
                    allAcceptedMentees = mentees.map {
                        JoinRequest(
                            request_id = 0,
                            student_id = it.student_id,
                            student_name = it.student_name,
                            department = it.department,
                            request_type = "MENTORSHIP",
                            created_at = it.joined_at
                        )
                    }
                    if (currentState == ViewState.MY_MENTEES) filterAndDisplayRequests()
                    else updateCounts()
                }
            }
            override fun onFailure(call: Call<MenteesResponse>, t: Throwable) { }
        })

        BackendApiService.api.getClassStudents(currentFacultyId).enqueue(object : Callback<ClassStudentsResponse> {
            override fun onResponse(call: Call<ClassStudentsResponse>, response: Response<ClassStudentsResponse>) {
                if (response.isSuccessful) {
                    val students = response.body()?.students ?: emptyList()
                    allAcceptedClass = students.map {
                        JoinRequest(
                            request_id = 0,
                            student_id = it.student_id,
                            student_name = it.student_name,
                            department = it.department,
                            request_type = "CLASS",
                            created_at = it.joined_at
                        )
                    }
                    if (currentState == ViewState.MY_CLASS) filterAndDisplayRequests()
                    else updateCounts()
                }
            }
            override fun onFailure(call: Call<ClassStudentsResponse>, t: Throwable) { }
        })
    }

    private fun fetchPendingRequests() {
        BackendApiService.api.getJoinRequests(currentFacultyId).enqueue(object : Callback<JoinRequestsResponse> {
            override fun onResponse(call: Call<JoinRequestsResponse>, response: Response<JoinRequestsResponse>) {
                 if (response.isSuccessful) {
                     allPendingRequests = response.body()?.requests ?: emptyList()
                     filterAndDisplayRequests()
                 } else {
                     Toast.makeText(this@FacultyStudentRequestsActivity, "Failed to load requests", Toast.LENGTH_SHORT).show()
                 }
            }

            override fun onFailure(call: Call<JoinRequestsResponse>, t: Throwable) {
                Toast.makeText(this@FacultyStudentRequestsActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun filterAndDisplayRequests() {
         var isAcceptedList = false
         var filteredData = listOf<JoinRequest>()
         
         when (currentState) {
            ViewState.MENTEE_REQ -> {
                filteredData = allPendingRequests.filter { it.request_type == "MENTORSHIP" }
                isAcceptedList = false
            }
            ViewState.CLASS_REQ -> {
                filteredData = allPendingRequests.filter { it.request_type == "CLASS" }
                isAcceptedList = false
            }
            ViewState.MY_MENTEES -> {
                filteredData = allAcceptedMentees
                isAcceptedList = true
            }
            ViewState.MY_CLASS -> {
                filteredData = allAcceptedClass
                isAcceptedList = true
            }
        }
        
        adapter.updateData(filteredData, isAcceptedList)
        updateCounts()
    }
    
    private fun updateCounts() {
        val menteeReqCount = allPendingRequests.count { it.request_type == "MENTORSHIP" }
        val classReqCount = allPendingRequests.count { it.request_type == "CLASS" }
        
        tabMentee.text = "Mentee Req ($menteeReqCount)"
        tabClass.text = "Class Req ($classReqCount)"
        subTabMentees.text = "My Mentees (${allAcceptedMentees.size})"
        subTabClass.text = "My Class (${allAcceptedClass.size})"
    }
}
