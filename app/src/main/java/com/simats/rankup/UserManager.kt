package com.simats.rankup

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object UserManager {
    private const val PREF_NAME = "rankup_users"
    private const val KEY_USERS = "key_users"
    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(context: Context, user: User) {
        val users = getAllUsers(context).toMutableList()
        // Check for duplicates (optional, based on ID)
        val existingIndex = users.indexOfFirst { it.id == user.id }
        if (existingIndex != -1) {
            users[existingIndex] = user // Update
        } else {
            users.add(0, user) // Add new to top
        }
        
        val json = gson.toJson(users)
        getPrefs(context).edit().putString(KEY_USERS, json).apply()
    }

    fun getAllUsers(context: Context): List<User> {
        val json = getPrefs(context).getString(KEY_USERS, null)
        return if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<User>>() {}.type
            gson.fromJson(json, type)
        }
    }

    fun getUsers(context: Context, role: String): List<User> {
        val all = getAllUsers(context)
        return all.filter { it.role.equals(role, ignoreCase = true) }
    }
    
    // Helper to get dummy data combined with saved data
    fun getUsersWithDummy(context: Context, role: String, dummyData: List<User>): List<User> {
        val saved = getUsers(context, role)
        // Combine, prioritizing saved users (or de-duplicating if needed)
        val uniqueIds = saved.map { it.id }.toSet()
        val uniqueDummy = dummyData.filter { it.id !in uniqueIds && it.role.equals(role, ignoreCase = true) }
        return saved + uniqueDummy
    }
}
