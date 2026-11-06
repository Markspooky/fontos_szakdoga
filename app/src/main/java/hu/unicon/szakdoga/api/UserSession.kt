package hu.unicon.szakdoga.api

import android.content.Context
import com.google.gson.Gson
import hu.unicon.szakdoga.model.User

object UserSession {
    private const val PREFS_NAME = "user_session_prefs"
    private const val KEY_CURRENT_USER = "current_user"
    private val gson = Gson()

    fun getUser(context: Context): User? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CURRENT_USER, null) ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun saveUser(context: Context, user: User) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(user)
        prefs.edit().putString(KEY_CURRENT_USER, json).apply()
    }

    fun logout(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_CURRENT_USER).apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getUser(context) != null
    }

    fun isAdmin(context: Context): Boolean {
        val user = getUser(context) ?: return false
        return user.role.equals("ADMIN", ignoreCase = true)
    }
}
