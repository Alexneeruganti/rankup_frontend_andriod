package com.simats.rankup

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.rankup.network.ApiResponse
import com.simats.rankup.network.BackendApiService
import com.simats.rankup.network.GetUsersResponse
import com.simats.rankup.network.AdminResetPasswordRequest
import com.simats.rankup.network.UpdateUserStatusRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserManagementActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var tabStudents: TextView
    private lateinit var tabFaculty: TextView
    private lateinit var adapter: UserManagementAdapter
    private val allUsers = mutableListOf<User>()
    private var currentRole = "Student" // or "Faculty"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        initViews()
        setupDummyData()
        setupRecyclerView()
        setupTabs()
        setupSearch()
        setupActions()
    }

    private fun initViews() {
        rvUsers = findViewById(R.id.rvUsers)
        etSearch = findViewById(R.id.etSearch)
        tabStudents = findViewById(R.id.tabStudents)
        tabFaculty = findViewById(R.id.tabFaculty)
        
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupDummyData() {
        // Initial Dummy Logic moved to loadData()
    }
    
    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        allUsers.clear()
        
        BackendApiService.api.adminGetUsers().enqueue(object : Callback<GetUsersResponse> {
            override fun onResponse(call: Call<GetUsersResponse>, response: Response<GetUsersResponse>) {
                if (response.isSuccessful && response.body()?.error == null) {
                    val backendUsers = response.body()?.users ?: emptyList()
                    // Convert UserResponse to User objects for the Adapter
                    val appUsers = backendUsers.map { u ->
                        User(
                            id = u.id ?: "0",
                            name = u.name ?: "Unknown",
                            email = u.email ?: "No Email",
                            role = u.role ?: "Student",
                            status = u.status ?: "Active",
                            placementStatus = u.placement_status,
                            profileImage = R.drawable.ic_profile_placeholder, // default
                            department = u.department ?: "N/A",
                            password = "password123", // placeholder logic or skip if not needed in list
                            age = u.age?.toString() ?: "N/A",
                            gender = u.gender ?: "N/A",
                            registerNumber = u.register_number
                        )
                    }
                    allUsers.addAll(appUsers)
                    updateList()
                } else {
                    Toast.makeText(this@UserManagementActivity, "Database Error: ${response.body()?.error}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetUsersResponse>, t: Throwable) {
                Toast.makeText(this@UserManagementActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = UserManagementAdapter(
            userList = filterUsers(currentRole, ""),
            onResetPasswordClick = { user ->
                showResetPasswordDialog(user)
            },
            onBlockUserClick = { user ->
                val newStatus = if (user.status == "Active") "Blocked" else "Active"
                val request = UpdateUserStatusRequest(newStatus)
                
                BackendApiService.api.adminUpdateUserStatus(user.id, request).enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful && response.body()?.error == null) {
                            user.status = newStatus
                            Toast.makeText(this@UserManagementActivity, "${user.name} has been ${newStatus.lowercase()}.", Toast.LENGTH_SHORT).show()
                            updateList()
                        } else {
                            Toast.makeText(this@UserManagementActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(this@UserManagementActivity, "Network Error", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        )
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = adapter
    }

    private fun showResetPasswordDialog(user: User) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reset_password, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
        
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val tvSubtitle = dialogView.findViewById<TextView>(R.id.tvSubtitle)
        val etNewPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNewPassword)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btnSave)
        
        tvSubtitle.text = "Enter new password for ${user.name}"
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnSave.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()
            if (newPassword.isNotEmpty()) {
                if (!com.simats.rankup.utils.ValidationUtils.isValidPassword(newPassword)) {
                    etNewPassword.error = "Password must be >= 8 chars, 1 uppercase, 1 number, 1 symbol"
                    return@setOnClickListener
                }
                val request = AdminResetPasswordRequest(newPassword)
                BackendApiService.api.adminResetPassword(user.id, request).enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful && response.body()?.error == null) {
                            Toast.makeText(this@UserManagementActivity, "Password Updated Successfully", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(this@UserManagementActivity, "Failed to update password", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(this@UserManagementActivity, "Network Error", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                etNewPassword.error = "Password cannot be empty"
            }
        }
        
        dialog.show()
    }

    private fun setupTabs() {
        tabStudents.setOnClickListener {
            if (currentRole != "Student") {
                currentRole = "Student"
                updateTabUI()
                updateList()
            }
        }

        tabFaculty.setOnClickListener {
            if (currentRole != "Faculty") {
                currentRole = "Faculty"
                updateTabUI()
                updateList()
            }
        }
    }

    private fun updateTabUI() {
        if (currentRole == "Student") {
            tabStudents.setBackgroundResource(R.drawable.bg_tab_selected)
            tabStudents.setTextColor(Color.WHITE)
            tabFaculty.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabFaculty.setTextColor(Color.parseColor("#757575"))
        } else {
            tabFaculty.setBackgroundResource(R.drawable.bg_tab_selected)
            tabFaculty.setTextColor(Color.WHITE)
            tabStudents.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabStudents.setTextColor(Color.parseColor("#757575"))
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupActions() {
        findViewById<ImageButton>(R.id.btnAddUser).setOnClickListener {
            val intent = android.content.Intent(this, AddUserActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateList() {
        val query = etSearch.text.toString()
        val filtered = filterUsers(currentRole, query)
        adapter.updateList(filtered)
    }

    private fun filterUsers(role: String, query: String): List<User> {
        return allUsers.filter { user ->
            user.role.equals(role, ignoreCase = true) &&
            (user.name.contains(query, ignoreCase = true) ||
             user.id.contains(query, ignoreCase = true) ||
             user.email.contains(query, ignoreCase = true) ||
             user.department.contains(query, ignoreCase = true) ||
             (user.registerNumber ?: "").contains(query, ignoreCase = true))
        }
    }
}
