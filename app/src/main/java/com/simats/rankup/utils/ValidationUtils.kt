package com.simats.rankup.utils

import android.util.Patterns

object ValidationUtils {
    
    fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        val hasUppercase = password.any { it.isUpperCase() }
        val hasNumber = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }
        
        return hasUppercase && hasNumber && hasSymbol
    }

    fun isValidEmail(email: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return false
        return email.endsWith("@gmail.com", ignoreCase = true)
    }

    fun isValidMobile(mobile: String): Boolean {
        return mobile.length == 10 && mobile.all { it.isDigit() }
    }
}
