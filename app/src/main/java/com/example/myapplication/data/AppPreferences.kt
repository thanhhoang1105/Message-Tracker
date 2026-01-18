package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manager for storing and retrieving application preferences using SharedPreferences.
 * Used to keep track of which apps are selected for notification monitoring.
 */
object AppPreferences {
    private const val PREF_NAME = "monitored_apps_pref"
    private const val KEY_PACKAGES = "selected_packages"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Retrieves the set of package names selected for monitoring.
     * Defaults to Facebook Messenger ("com.facebook.orca") if no selection has been made.
     */
    fun getSelectedPackages(context: Context): Set<String> {
        val prefs = getPrefs(context)
        // Returns the saved set or a default set containing Messenger
        return prefs.getStringSet(KEY_PACKAGES, setOf("com.facebook.orca")) ?: setOf("com.facebook.orca")
    }

    /**
     * Saves the updated set of selected package names to SharedPreferences.
     * @param packages Set of package strings to be stored.
     */
    fun saveSelectedPackages(context: Context, packages: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_PACKAGES, packages).apply()
    }
}
