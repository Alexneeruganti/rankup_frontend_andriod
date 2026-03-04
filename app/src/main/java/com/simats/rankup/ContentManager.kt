package com.simats.rankup

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ContentManager {
    private const val PREF_NAME = "RankUpContent"
    private const val KEY_MODULES = "modules"

    fun saveContent(context: Context, module: LearningAdapter.LearningModule) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val existingJson = prefs.getString(KEY_MODULES, "[]")
        val jsonArray = JSONArray(existingJson)

        val newModuleJson = JSONObject().apply {
            put("title", module.title)
            put("type", module.type)
            put("author", module.author)
            put("size", module.size)
            put("iconResId", module.iconResId)
        }

        jsonArray.put(newModuleJson)

        prefs.edit().putString(KEY_MODULES, jsonArray.toString()).apply()
    }

    fun deleteContent(context: Context, moduleToDelete: LearningAdapter.LearningModule) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val existingJson = prefs.getString(KEY_MODULES, "[]")
        val jsonArray = JSONArray(existingJson)
        val newJsonArray = JSONArray()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            // Assuming title and author combination is unique enough
            if (!(obj.getString("title") == moduleToDelete.title && obj.getString("author") == moduleToDelete.author)) {
                newJsonArray.put(obj)
            }
        }
        
        prefs.edit().putString(KEY_MODULES, newJsonArray.toString()).apply()
    }

    fun getContents(context: Context): List<LearningAdapter.LearningModule> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_MODULES, "[]")
        val jsonArray = JSONArray(jsonString)
        val modules = mutableListOf<LearningAdapter.LearningModule>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            modules.add(
                LearningAdapter.LearningModule(
                    title = obj.getString("title"),
                    type = obj.getString("type"),
                    author = obj.getString("author"),
                    size = obj.getString("size"),
                    iconResId = obj.getInt("iconResId")
                )
            )
        }
        return modules
    }
}
