package com.zigzura.droplets.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceManager {
    private const val PREF_NAME = "droplets_prefs"
    private const val KEY_FIRST_TIME_USER = "first_time_user"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isFirstTimeUser(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_FIRST_TIME_USER, true)
    }

    fun setWelcomeCompleted(context: Context) {
        getPreferences(context).edit()
            .putBoolean(KEY_FIRST_TIME_USER, false)
            .apply()
    }
}
