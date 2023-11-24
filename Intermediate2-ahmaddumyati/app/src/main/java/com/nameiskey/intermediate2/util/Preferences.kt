package com.nameiskey.intermediate2.util

import android.content.Context
import com.nameiskey.intermediate2.model.User

internal class Preferences (context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setUser(value: User) {
        val editor = preferences.edit()
        editor.putString(ID, value.userId)
        editor.putString(NAME, value.name)
        editor.putString(TOKEN, value.token)
        editor.apply()
    }

    fun getUser(): User {
        val userId = preferences.getString(ID, "")
        val name = preferences.getString(NAME, "")
        val token = preferences.getString(TOKEN, "")

        return User(name.toString(), userId.toString(), token.toString())
    }

    companion object {
        private const val PREFS_NAME = "user_pref"
        private const val ID = "id"
        private const val NAME = "name"
        private const val TOKEN = "token"
    }
}