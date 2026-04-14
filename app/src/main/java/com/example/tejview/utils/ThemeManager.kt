package com.example.tejview.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.tejview.R

/**
 * Manages theme preference persistence and provides
 * the correct theme resource ID for Dark/Light Aurora modes.
 */
object ThemeManager {

    private const val PREFS_NAME = "tejview_theme_prefs"
    private const val KEY_IS_DARK_MODE = "is_dark_mode"

    /**
     * Returns true if dark mode is active (default).
     */
    fun isDarkMode(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_DARK_MODE, true)
    }

    /**
     * Toggle between light and dark mode.
     * Returns the new dark mode state.
     */
    fun toggleTheme(context: Context): Boolean {
        val newIsDark = !isDarkMode(context)
        getPrefs(context).edit().putBoolean(KEY_IS_DARK_MODE, newIsDark).apply()
        return newIsDark
    }

    /**
     * Set a specific theme mode.
     */
    fun setDarkMode(context: Context, isDark: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_IS_DARK_MODE, isDark).apply()
    }

    /**
     * Get the theme resource ID to apply.
     */
    fun getThemeResId(context: Context): Int {
        return if (isDarkMode(context)) {
            R.style.Theme_TejView_Dark
        } else {
            R.style.Theme_TejView_Light
        }
    }

    /**
     * Apply the saved theme to the activity.
     * Call this BEFORE setContentView() in onCreate().
     */
    fun applyTheme(context: Context) {
        context.setTheme(getThemeResId(context))
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
