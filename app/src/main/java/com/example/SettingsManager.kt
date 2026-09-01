package com.example

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tv_launcher_settings", Context.MODE_PRIVATE)

    var favorites: List<String>
        get() = prefs.getString("favorites", null)?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        set(value) = prefs.edit().putString("favorites", value.joinToString(",")).apply()

    fun addFavorite(packageName: String) {
        val current = favorites.toMutableList()
        if (!current.contains(packageName)) {
            current.add(packageName)
            favorites = current
        }
    }

    fun removeFavorite(packageName: String) {
        val current = favorites.toMutableList()
        if (current.remove(packageName)) {
            favorites = current
        }
    }

    fun moveFavoriteLeft(packageName: String): Int {
        val current = favorites.toMutableList()
        val index = current.indexOf(packageName)
        if (index > 0) {
            current.removeAt(index)
            current.add(index - 1, packageName)
            favorites = current
            return index - 1
        }
        return index
    }

    fun moveFavoriteRight(packageName: String): Int {
        val current = favorites.toMutableList()
        val index = current.indexOf(packageName)
        if (index != -1 && index < current.size - 1) {
            current.removeAt(index)
            current.add(index + 1, packageName)
            favorites = current
            return index + 1
        }
        return index
    }

    fun swapFavorites(index1: Int, index2: Int) {
        val current = favorites.toMutableList()
        if (index1 in current.indices && index2 in current.indices && index1 != index2) {
            val temp = current[index1]
            current[index1] = current[index2]
            current[index2] = temp
            favorites = current
        }
    }

    var backgroundPreset: String
        get() = prefs.getString("backgroundPreset", "nebula") ?: "nebula"
        set(value) = prefs.edit().putString("backgroundPreset", value).apply()

    var backgroundDimAmount: Float
        get() = prefs.getFloat("backgroundDimAmount", 0.35f)
        set(value) = prefs.edit().putFloat("backgroundDimAmount", value).apply()

    var showAppLabels: String
        get() = prefs.getString("showAppLabels", "focused_only") ?: "focused_only"
        set(value) = prefs.edit().putString("showAppLabels", value).apply()

    var clockVisible: Boolean
        get() = prefs.getBoolean("clockVisible", true)
        set(value) = prefs.edit().putBoolean("clockVisible", value).apply()

    var dateVisible: Boolean
        get() = prefs.getBoolean("dateVisible", true)
        set(value) = prefs.edit().putBoolean("dateVisible", value).apply()

    var showRecentApps: Boolean
        get() = prefs.getBoolean("showRecentApps", true)
        set(value) = prefs.edit().putBoolean("showRecentApps", value).apply()

    var enableParallax: Boolean
        get() = prefs.getBoolean("enableParallax", true)
        set(value) = prefs.edit().putBoolean("enableParallax", value).apply()

    var cardScale: Float
        get() = prefs.getFloat("cardScale", 1.15f)
        set(value) = prefs.edit().putFloat("cardScale", value).apply()

    var animationSpeed: String
        get() = prefs.getString("animationSpeed", "smooth") ?: "smooth"
        set(value) = prefs.edit().putString("animationSpeed", value).apply()

    var performanceMode: Boolean
        get() = prefs.getBoolean("performanceMode", false)
        set(value) = prefs.edit().putBoolean("performanceMode", value).apply()

    var rememberLastFocus: Boolean
        get() = prefs.getBoolean("rememberLastFocus", true)
        set(value) = prefs.edit().putBoolean("rememberLastFocus", value).apply()

    var lastFocusedPackage: String
        get() = prefs.getString("lastFocusedPackage", "") ?: ""
        set(value) = prefs.edit().putString("lastFocusedPackage", value).apply()

    var ambientScreensaverMinutes: Int
        get() = prefs.getInt("ambientScreensaverMinutes", 5)
        set(value) = prefs.edit().putInt("ambientScreensaverMinutes", value).apply()
}
