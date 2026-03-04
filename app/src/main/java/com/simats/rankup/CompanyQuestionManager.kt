package com.simats.rankup

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object CompanyQuestionManager {
    private const val PREFS_NAME = "RankUpCompanyQuestionsPrefs"
    private const val KEY_QUESTIONS = "company_questions_list"
    private val gson = Gson()

    fun saveQuestion(context: Context, question: CompanyQuestion) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val questionsList = getAllQuestions(context).toMutableList()
        questionsList.add(question)
        
        val json = gson.toJson(questionsList)
        prefs.edit().putString(KEY_QUESTIONS, json).apply()
    }

    fun deleteQuestion(context: Context, questionToDelete: CompanyQuestion) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val questionsList = getAllQuestions(context).toMutableList()
        // Find and remove matching item
        questionsList.removeAll { it.title == questionToDelete.title && it.company == questionToDelete.company }
        
        val json = gson.toJson(questionsList)
        prefs.edit().putString(KEY_QUESTIONS, json).apply()
    }

    fun getAllQuestions(context: Context): List<CompanyQuestion> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_QUESTIONS, null)

        val type: Type = object : TypeToken<List<CompanyQuestion>>() {}.type
        
        var questions: List<CompanyQuestion>? = null
        if (json != null) {
            questions = gson.fromJson(json, type)
        }
        
        if (questions == null || questions.isEmpty()) {
            questions = getInitialMockQuestions()
            val initialJson = gson.toJson(questions)
            prefs.edit().putString(KEY_QUESTIONS, initialJson).apply()
        }

        return questions
    }

    private fun getInitialMockQuestions(): List<CompanyQuestion> {
        return listOf(
            CompanyQuestion("Google", "Array Reversal", "Hard", "Reverse an array without using extra space.", "1 <= N <= 1000", "First line N, second line array."),
            CompanyQuestion("Amazon", "Rainwater Trapping", "Hard", "Calculate how much water can be trapped between blocks.", "1 <= N <= 10^5", "Array of heights."),
            CompanyQuestion("Microsoft", "Linked List Cycle", "Medium", "Detect if a cycle exists in a linked list.", "N nodes", "Head of list."),
            CompanyQuestion("Zoho", "Pattern Printing", "Medium", "Print the snake pattern.", "N <= 20", "Integer N")
        )
    }
}
