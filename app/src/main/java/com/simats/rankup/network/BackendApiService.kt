package com.simats.rankup.network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class PlacementLoginRequest(
    val id: String,
    val password: String
)

data class PlacementLoginResponse(
    val message: String?,
    val error: String?,
    val user_id: Int?,
    val name: String?,
    val register_number: String?,
    val email: String?,
    val role: String?
)

data class AddUserRequest(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val age: String,
    val gender: String,
    val department: String
)

data class ApiResponse(
    val message: String?,
    val error: String?
)

data class FacultyMember(
    val id: String,
    val name: String,
    val department: String,
    val register_number: String?,
    val profile_pic: String?
)

data class FacultyResponse(
    val message: String?,
    val error: String?,
    val faculty: List<FacultyMember>?
)

data class JoinRequest(
    val request_id: Int,
    val student_id: Int,
    val student_name: String,
    val department: String,
    val request_type: String,
    val created_at: String
)

data class SendJoinRequestPayload(
    val student_id: Int,
    val faculty_id: Int,
    val request_type: String
)

data class JoinRequestsResponse(
    val message: String?,
    val error: String?,
    val requests: List<JoinRequest>?
)

data class AssignedStudent(
    val student_id: Int,
    val student_name: String,
    val department: String,
    val register_number: String?,
    val joined_at: String,
    val overall_score: Double = 0.0,
    val aptitude_score: Double = 0.0,
    val coding_score: Double = 0.0
)

data class MenteesResponse(
    val message: String?,
    val error: String?,
    val mentees: List<AssignedStudent>?
)

data class ClassStudentsResponse(
    val message: String?,
    val error: String?,
    val students: List<AssignedStudent>?
)

data class UpdateStatusRequest(
    val request_id: Int,
    val status: String
)

data class RemoveMemberRequest(
    val faculty_id: Int,
    val student_id: Int
)

data class UploadResourceRequest(
    val faculty_id: Int,
    val title: String,
    val description: String,
    val tags: String,
    val category: String, // "Placement" or "Higher Education"
    val resource_type: String,
    val file_link: String
)

data class ResourceResponse(
    val id: Int,
    val title: String,
    val description: String?,
    val tags: String?,
    val resource_type: String,
    val file_link: String,
    val author: String?,
    val created_at: String?
)

data class GetResourcesResponse(
    val message: String?,
    val error: String?,
    val resources: List<ResourceResponse>?
)

data class SubmitResumeRequest(
    val student_id: Int,
    val mentor_id: Int,
    val resume_url: String
)

data class ReviewResumeRequest(
    val review_id: Int,
    val status: String,
    val feedback: String? = null
)

data class ResumeReview(
    val review_id: Int,
    val student_id: Int?,
    val student_name: String?,
    val mentor_id: Int?,
    val mentor_name: String?,
    val department: String?,
    val resume_url: String,
    val status: String,
    val feedback: String?,
    val created_at: String
)

data class ResumeReviewsResponse(
    val message: String?,
    val error: String?,
    val resumes: List<ResumeReview>?
)

data class StudentMentorsResponse(
    val message: String?,
    val error: String?,
    val mentors: List<FacultyMember>?
)

data class FileUploadResponse(
    val message: String?,
    val error: String?,
    val file_url: String?
)

data class ForgotPasswordRequest(
    val email: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class ResetPasswordRequest(
    val email: String,
    val new_password: String
)

data class AddCompanyQuestionRequest(
    val company: String,
    val title: String,
    val difficulty: String,
    val description: String,
    val constraints: String,
    val input_format: String
)

data class CompanyQuestionResponse(
    val id: Int,
    val company: String,
    val title: String,
    val difficulty: String,
    val description: String,
    val constraints: String,
    val input_format: String,
    val created_at: String?
)

data class GetCompanyQuestionsResponse(
    val message: String?,
    val error: String?,
    val questions: List<CompanyQuestionResponse>?
)

data class MockTestModuleParams(
    val name: String,
    val easy: Int,
    val medium: Int,
    val hard: Int,
    val coding: Int = 0
)

data class GenerateTestRequest(
    val faculty_id: Int,
    val title: String,
    val duration_minutes: Int,
    val modules: List<MockTestModuleParams>
)

data class GenerateTestResponse(
    val message: String?,
    val error: String?,
    val test_id: Int?
)

data class MockTestResponse(
    val id: Int,
    val faculty_id: Int,
    val title: String,
    val duration_minutes: Int,
    val created_at: String?,
    val category: String?
)

data class GetMockTestsResponse(
    val message: String?,
    val error: String?,
    val mock_tests: List<MockTestResponse>?
)

data class ProfilePicUploadResponse(
    val message: String?,
    val error: String?,
    val url: String?
)

data class UserResponse(
    val id: String?,
    val name: String?,
    val email: String?,
    val role: String?,
    val status: String?,
    val placement_status: String?,
    val gender: String?,
    val age: Int?,
    val department: String?,
    val profile_pic: String?,
    val register_number: String?
)

data class GetUsersResponse(
    val message: String?,
    val error: String?,
    val users: List<UserResponse>?
)

data class UpdateUserStatusRequest(
    val status: String
)

data class AdminResetPasswordRequest(
    val password: String
)

data class MockTestQuestion(
    val id: Int,
    val test_id: Int,
    val module_name: String,
    val question_type: String, // "MCQ" or "CODING"
    val question_text: String,
    val option_a: String?,
    val option_b: String?,
    val option_c: String?,
    val option_d: String?,
    val correct_option: String?,
    val difficulty: String,
    val explanation: String?,
    val constraints: String?,
    val sample_input: String?,
    val sample_output: String?
)

data class SubmitTestResultRequest(
    val student_id: Int,
    val test_id: Int,
    val marks: Int,
    val total_marks: Int
)

data class LeaderboardEntry(
    val student_id: Int,
    val name: String,
    val register_number: String,
    val score: Int,
    val profile_pic: String?
)

data class LeaderboardResponse(
    val message: String?,
    val error: String?,
    val leaderboard: List<LeaderboardEntry>?
)

data class GenerateCodingDrillRequest(
    val difficulty: String
)

data class DynamicCodingDrill(
    val title: String,
    val description: String,
    val difficulty: String,
    val constraints: String,
    val input_format: String,
    val sample_input: String,
    val sample_output: String
)

data class CodingDrillResponse(
    val message: String?,
    val error: String?,
    val drill: DynamicCodingDrill?
)

data class GetMockTestQuestionsResponse(
    val message: String?,
    val error: String?,
    val questions: List<MockTestQuestion>?
)

data class AppSettingsResponse(
    val message: String?,
    val error: String?,
    val settings: AppSettingsRequest?
)

data class UserProfileData(
    val id: Int?,
    val name: String?,
    val register_number: String?,
    val email: String?,
    val role: String?,
    val age: Int?,
    val gender: String?,
    val department: String?,
    val profile_pic: String?,
    val phone: String?
)

data class UserProfileResponse(
    val message: String?,
    val error: String?,
    val profile: UserProfileData?
)

data class AppSettingsRequest(
    val aptitude_tests: Boolean,
    val coding_practice: Boolean,
    val leaderboard: Boolean,
    val learning_resources: Boolean,
    val push_notifications: Boolean,
    val email_notifications: Boolean,
    val maintenance_mode: Boolean,
    val new_registrations: Boolean
)

data class AnnouncementRequest(
    val id: String,
    val title: String,
    val message: String,
    val audience: String,
    val timestamp: Long,
    val date_string: String
)

data class AnnouncementResponse(
    val id: String,
    val title: String,
    val message: String,
    val audience: String,
    val timestamp: Long,
    val date_string: String
)

data class AnnouncementListResponse(
    val message: String?,
    val error: String?,
    val announcements: List<AnnouncementResponse>?
)

data class PostTestimonialRequest(
    val student_id: Int,
    val content: String
)

data class CommunicationExercise(
    val id: Int,
    val type: String, // 'para_ques', 'blanks', 'speech_to_text'
    val paragraph: String?,
    val question: String,
    val option_a: String?,
    val option_b: String?,
    val option_c: String?,
    val option_d: String?,
    val answer: String,
    val created_at: String?
)

data class CommunicationResponse(
    val message: String,
    val exercises: List<CommunicationExercise>
)

interface BackendApi {
    @POST("placement-login")
    fun placementLogin(@Body request: PlacementLoginRequest): Call<PlacementLoginResponse>

    @POST("admin-add-user")
    fun adminAddUser(@Body request: AddUserRequest): Call<ApiResponse>

    @POST("add-announcement")
    fun addAnnouncement(@Body request: AnnouncementRequest): Call<ApiResponse>

    @retrofit2.http.GET("get-announcements")
    fun getAnnouncements(@retrofit2.http.Query("category") category: String? = null): Call<AnnouncementListResponse>

    @POST("post-testimonial")
    fun postTestimonial(@Body request: PostTestimonialRequest): Call<ApiResponse>

    @retrofit2.http.GET("get-communication-exercises")
    fun getCommunicationExercises(): Call<CommunicationResponse>

    @retrofit2.http.GET("get-faculty")
    fun getFaculty(): Call<FacultyResponse>

    @retrofit2.http.GET("get-join-requests/{faculty_id}")
    fun getJoinRequests(@retrofit2.http.Path("faculty_id") facultyId: Int): Call<JoinRequestsResponse>

    @retrofit2.http.GET("get-mentees/{faculty_id}")
    fun getMentees(@retrofit2.http.Path("faculty_id") facultyId: Int): Call<MenteesResponse>

    @retrofit2.http.GET("get-class-students/{faculty_id}")
    fun getClassStudents(@retrofit2.http.Path("faculty_id") facultyId: Int): Call<ClassStudentsResponse>

    @POST("update-join-request")
    fun updateJoinRequest(@Body request: UpdateStatusRequest): Call<ApiResponse>

    @POST("send-join-request")
    fun sendJoinRequest(@Body request: SendJoinRequestPayload): Call<ApiResponse>

    @POST("upload-resource")
    fun uploadResource(@Body request: UploadResourceRequest): Call<ApiResponse>

    @retrofit2.http.Multipart
    @POST("upload-file")
    fun uploadFile(@retrofit2.http.Part file: okhttp3.MultipartBody.Part): Call<FileUploadResponse>

    @retrofit2.http.GET("get-resources")
    fun getResources(
        @retrofit2.http.Query("category") category: String? = null,
        @retrofit2.http.Query("student_id") studentId: Int? = null
    ): Call<GetResourcesResponse>

    @retrofit2.http.DELETE("delete-resource/{resource_id}")
    fun deleteResource(@retrofit2.http.Path("resource_id") resourceId: Int): Call<ApiResponse>

    @POST("submit-resume")
    fun submitResume(@Body request: SubmitResumeRequest): Call<ApiResponse>

    @retrofit2.http.GET("get-mentor-resumes/{mentor_id}")
    fun getMentorResumes(@retrofit2.http.Path("mentor_id") mentorId: Int): Call<ResumeReviewsResponse>

    @retrofit2.http.GET("get-student-resumes/{student_id}")
    fun getStudentResumes(@retrofit2.http.Path("student_id") studentId: Int): Call<ResumeReviewsResponse>

    @POST("review-resume")
    fun reviewResume(@Body request: ReviewResumeRequest): Call<ApiResponse>

    @retrofit2.http.GET("get-student-mentors/{student_id}")
    fun getStudentMentors(@retrofit2.http.Path("student_id") studentId: Int): Call<StudentMentorsResponse>

    @POST("forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<ApiResponse>

    @POST("verify-otp")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<ApiResponse>

    @POST("reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<ApiResponse>

    @POST("add-company-question")
    fun addCompanyQuestion(@Body request: AddCompanyQuestionRequest): Call<ApiResponse>

    @retrofit2.http.GET("get-company-questions")
    fun getCompanyQuestions(): Call<GetCompanyQuestionsResponse>

    @retrofit2.http.DELETE("delete-company-question/{question_id}")
    fun deleteCompanyQuestion(@retrofit2.http.Path("question_id") questionId: Int): Call<ApiResponse>

    @POST("generate-mock-test")
    fun generateMockTest(@Body request: GenerateTestRequest): Call<GenerateTestResponse>


    @retrofit2.http.GET("get-mock-test-questions/{test_id}")
    fun getMockTestQuestions(@retrofit2.http.Path("test_id") testId: Int): Call<GetMockTestQuestionsResponse>

    @retrofit2.http.DELETE("delete-mock-test/{test_id}")
    fun deleteMockTest(@retrofit2.http.Path("test_id") testId: Int): Call<ApiResponse>

    @retrofit2.http.GET("admin-get-users")
    fun adminGetUsers(@retrofit2.http.Query("role") role: String? = null): Call<GetUsersResponse>

    @retrofit2.http.PUT("admin-update-user-status/{user_id}")
    fun adminUpdateUserStatus(
        @retrofit2.http.Path("user_id") userId: String,
        @Body request: UpdateUserStatusRequest
    ): Call<ApiResponse>

    @retrofit2.http.PUT("admin-reset-password/{user_id}")
    fun adminResetPassword(
        @retrofit2.http.Path("user_id") userId: String,
        @Body request: AdminResetPasswordRequest
    ): Call<ApiResponse>

    @POST("submit-test-result")
    fun submitTestResult(@Body request: SubmitTestResultRequest): Call<ApiResponse>

    @retrofit2.http.GET("test-leaderboard")
    fun getTestLeaderboard(@retrofit2.http.Query("category") category: String? = null): Call<LeaderboardResponse>

    @POST("generate-coding-drill")
    fun generateCodingDrill(@Body request: GenerateCodingDrillRequest): Call<CodingDrillResponse>

    @retrofit2.http.GET("admin-settings")
    fun getAdminSettings(): Call<AppSettingsResponse>

    @POST("admin-settings")
    fun updateAdminSettings(@Body request: AppSettingsRequest): Call<ApiResponse>

    @retrofit2.http.GET("get-profile/{user_id}")
    fun getProfile(@retrofit2.http.Path("user_id") userId: Int): Call<UserProfileResponse>

    @retrofit2.http.GET("admin-stats")
    fun getAdminStats(): Call<AdminStatsResponse>

    @retrofit2.http.GET("faculty-stats/{faculty_id}")
    fun getFacultyStats(@retrofit2.http.Path("faculty_id") facultyId: Int): Call<FacultyStatsResponse>

    @retrofit2.http.GET("student-stats/{student_id}")
    fun getStudentStats(@retrofit2.http.Path("student_id") studentId: Int): Call<StudentStatsResponse>

    @retrofit2.http.GET("get-mock-tests")
    fun getMockTests(@retrofit2.http.Query("category") category: String? = null): Call<GetMockTestsResponse>

    @POST("change-password")
    fun changePassword(@Body request: ChangePasswordRequest): Call<ApiResponse>

    @POST("generate-academic-mock-test")
    fun generateAcademicMockTest(@Body request: AcademicBlueprintRequest): Call<GeneratedAcademicTestResponse>

    @retrofit2.http.Multipart
    @POST("upload-profile-pic")
    fun uploadProfilePic(
        @retrofit2.http.Part("user_id") userId: okhttp3.RequestBody,
        @retrofit2.http.Part file: okhttp3.MultipartBody.Part
    ): Call<ProfilePicUploadResponse>

    @POST("remove-profile-pic")
    fun removeProfilePic(@Body request: RemoveProfilePicRequest): Call<ApiResponse>

    @POST("remove-mentee")
    fun removeMentee(@Body request: RemoveMemberRequest): Call<ApiResponse>

    @POST("remove-class-student")
    fun removeClassStudent(@Body request: RemoveMemberRequest): Call<ApiResponse>
}

data class RemoveProfilePicRequest(
    val user_id: Int
)

data class AdminStatsResponse(
    val total_users: Int,
    val placement_percentage: Double,
    val active_tasks: Int,
    val content_items: Int?,
    val active_faculty: Int?,
    val announcements: Int?
)

data class FacultyStatsResponse(
    val classes: Int,
    val active_tests: Int,
    val submissions: Int,
    val pending_requests: Int,
    val academic_progress: Int
)

data class StudentStatsResponse(
    val average_score: Double,
    val aptitude_score: Double,
    val coding_score: Double,
    val training_progress: Int
)

data class ChangePasswordRequest(
    val user_id: Int,
    val old_password: String,
    val new_password: String
)

data class AcademicBlueprintRequest(
    val faculty_id: Int,
    val title: String,
    val duration_minutes: Int,
    val subjects: List<AcademicSubject>
)

data class AcademicSubject(
    val name: String,
    val easy: Int,
    val medium: Int,
    val hard: Int
)

data class GeneratedAcademicTestResponse(
    val message: String,
    val test_id: Int,
    val questions: List<MockTestQuestion>
)

object BackendApiService {
    // Note: 10.0.2.2 is the default IP for Android Emulator to connect to localhost on the host machine.
    const val BASE_URL = "http://180.235.121.253:8098/"

    private val okHttpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val api: BackendApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendApi::class.java)
    }

    fun getFullUrl(url: String?): String? {
        if (url.isNullOrEmpty()) return null
        if (url.startsWith("http://") || url.startsWith("https://")) return url
        val cleanUrl = if (url.startsWith("/")) url.substring(1) else url
        val baseUrl = if (BASE_URL.endsWith("/")) BASE_URL else "$BASE_URL/"
        return baseUrl + cleanUrl
    }
}
