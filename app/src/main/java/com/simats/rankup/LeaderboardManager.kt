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
            val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val userName = sharedPref.getString("USER_NAME", "User Name") ?: "User Name"
            val defaultData = listOf(
                StudentRank(1, userName, "Student", 0)
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
        val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "User Name") ?: "User Name"
        
        val resetData = listOf(
             StudentRank(1, userName, "Student", 0)
        )
        saveRankings(context, resetData)
    }

    fun resetMonthlyRankings(context: Context) {
        val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "User Name") ?: "User Name"
        
        val resetData = listOf(
             StudentRank(1, userName, "Student", 0)
        )
        saveRankings(context, resetData)
    }
}
