package com.simats.rankup

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object LeaderboardManager {
    private const val PREF_NAME = "LeaderboardPrefs"
    private const val KEY_IS_PUBLIC = "is_public_view_enabled"
    private const val KEY_STUDENT_RANKS = "student_ranks"

    data class StudentRank(
        val rank: Int,
        val name: String,
        val department: String,
        val points: Int
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isPublicViewEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_PUBLIC, true)
    }

    fun setPublicViewEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_IS_PUBLIC, enabled).apply()
    }

    fun getRankings(context: Context): List<StudentRank> {
        val json = getPrefs(context).getString(KEY_STUDENT_RANKS, null)
        return if (json != null) {
            val type = object : TypeToken<List<StudentRank>>() {}.type
            Gson().fromJson(json, type)
        } else {
            // Default Data
            val defaultData = listOf(
                StudentRank(1, "Ananya Singh", "CSE", 3200),
                StudentRank(2, "Rahul Kumar", "CSE", 2850),
                StudentRank(3, "Vikram Patel", "IT", 2640),
                StudentRank(4, "Priya Sharma", "CSE", 2480),
                StudentRank(5, "Arjun Mehta", "IT", 2390),
                StudentRank(6, "Sneha Reddy", "CSE", 2280),
                StudentRank(7, "Rohan Gupta", "Mech", 2150),
                StudentRank(8, "Kavya Iyer", "IT", 2080),
                StudentRank(9, "Aditya Verma", "Civil", 1950),
                StudentRank(10, "Ishita Desai", "CSE", 1870)
            )
            saveRankings(context, defaultData)
            defaultData
        }
    }
    
    // Helper to save rankings
    private fun saveRankings(context: Context, rankings: List<StudentRank>) {
        val json = Gson().toJson(rankings)
        getPrefs(context).edit().putString(KEY_STUDENT_RANKS, json).apply()
    }

    fun resetRankings(context: Context) {
        // Simulating a reset by setting points to 0 for demo purposes, 
        // or re-generating a "New Semester" list. 
        // For visual effect, let's just clear the points or shuffle.
        // Let's create an empty/initial state.
        
        val resetData = listOf(
             StudentRank(1, "Player 1", "CSE", 0),
             StudentRank(2, "Player 2", "IT", 0),
             StudentRank(3, "Player 3", "ECE", 0)
        )
        saveRankings(context, resetData)
    }

    fun resetMonthlyRankings(context: Context) {
        // Similar to reset for now
        val resetData = listOf(
             StudentRank(1, "Ananya Singh", "CSE", 50),
             StudentRank(2, "Rahul Kumar", "CSE", 40),
             StudentRank(3, "Vikram Patel", "IT", 30)
        )
        saveRankings(context, resetData)
    }
}
