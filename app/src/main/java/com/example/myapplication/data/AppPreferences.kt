package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val PREF_NAME = "monitored_apps_pref"
    private const val KEY_PACKAGES = "selected_packages"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Lấy danh sách các gói đã chọn (Mặc định luôn có Messenger)
    fun getSelectedPackages(context: Context): Set<String> {
        val prefs = getPrefs(context)
        // Mặc định trả về Messenger nếu chưa chọn gì
        return prefs.getStringSet(KEY_PACKAGES, setOf("com.facebook.orca")) ?: setOf("com.facebook.orca")
    }

    // Lưu danh sách mới
    fun saveSelectedPackages(context: Context, packages: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_PACKAGES, packages).apply()
    }
}