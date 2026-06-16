package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences

data class UserAccount(
    val id: String,
    val email: String,
    val name: String,
    val handle: String,
    val role: String, // USER, CREATOR, ADMIN
    val avatarUrl: String,
    val phoneNumber: String = ""
)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: UserAccount) : AuthState()
    data class Error(val message: String) : AuthState()
}

class BharamputraAuth(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("bharamputra_prefs", Context.MODE_PRIVATE)

    fun getPersistedUser(): UserAccount? {
        val id = prefs.getString("user_id", null) ?: return null
        val email = prefs.getString("user_email", "") ?: ""
        val name = prefs.getString("user_name", "Bharamputra Explorer") ?: "Bharamputra Explorer"
        val handle = prefs.getString("user_handle", "@bharamputra_creator") ?: "@bharamputra_creator"
        val role = prefs.getString("user_role", "ADMIN") ?: "ADMIN" // Default as Admin for full experience
        val avatarUrl = prefs.getString("user_avatar", "river_shield") ?: "river_shield"
        val phone = prefs.getString("user_phone", "") ?: ""
        return UserAccount(id, email, name, handle, role, avatarUrl, phone)
    }

    fun persistUser(user: UserAccount) {
        prefs.edit().apply {
            putString("user_id", user.id)
            putString("user_email", user.email)
            putString("user_name", user.name)
            putString("user_handle", user.handle)
            putString("user_role", user.role)
            putString("user_avatar", user.avatarUrl)
            putString("user_phone", user.phoneNumber)
            apply()
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    // Modern email and password validation
    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 6
    }

    fun validatePhone(phone: String): Boolean {
        return phone.length >= 10 && phone.all { it.isDigit() || it == '+' }
    }
}
