package com.simats.rankup

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Announcement(
    val id: String,
    val title: String,
    val message: String,
    val audience: String, // "All Users", "Students Only", "Faculty Only", "Specific Department"
    val timestamp: Long,
    val dateString: String
)

object AnnouncementManager {
    private const val PREF_NAME = "rankup_announcements"
    private const val KEY_ANNOUNCEMENTS = "key_announcements"
    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveAnnouncement(context: Context, announcement: Announcement) {
        val announcements = getAnnouncements(context).toMutableList()
        announcements.add(0, announcement) // Add to top
        
        val json = gson.toJson(announcements)
        getPrefs(context).edit().putString(KEY_ANNOUNCEMENTS, json).apply()
    }

    fun getAnnouncements(context: Context): List<Announcement> {
        val json = getPrefs(context).getString(KEY_ANNOUNCEMENTS, null)
        return if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<Announcement>>() {}.type
            gson.fromJson(json, type)
        }
    }

    fun getFilteredAnnouncements(context: Context, userType: String): List<Announcement> {
        val all = getAnnouncements(context)
        return all.filter { 
            it.audience == "All Users" || 
            (userType == "student" && it.audience == "Students Only") ||
            (userType == "faculty" && it.audience == "Faculty Only") ||
            (userType == "higher_edu" && it.audience == "Higher Education Students")
            // Department logic can be added here later
        }
    }
    
    fun clearAnnouncements(context: Context) {
        getPrefs(context).edit().remove(KEY_ANNOUNCEMENTS).apply()
    }
}
