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

    fun moveFavoriteLeft(packageName: String) {
        val current = favorites.toMutableList()
        val index = current.indexOf(packageName)
        if (index > 0) {
            current.removeAt(index)
            current.add(index - 1, packageName)
            favorites = current
        }
    }

    fun moveFavoriteRight(packageName: String) {
        val current = favorites.toMutableList()
        val index = current.indexOf(packageName)
        if (index != -1 && index < current.size - 1) {
            current.removeAt(index)
            current.add(index + 1, packageName)
            favorites = current
        }
    }
    
    var backgroundDimAmount: Float
        get() = prefs.getFloat("backgroundDimAmount", 0.3f)
        set(value) = prefs.edit().putFloat("backgroundDimAmount", value).apply()

    var showAppLabels: Boolean
        get() = prefs.getBoolean("showAppLabels", false)
        set(value) = prefs.edit().putBoolean("showAppLabels", value).apply()
        
    var clockVisible: Boolean
        get() = prefs.getBoolean("clockVisible", true)
        set(value) = prefs.edit().putBoolean("clockVisible", value).apply()

    var enableParallax: Boolean
        get() = prefs.getBoolean("enableParallax", true)
        set(value) = prefs.edit().putBoolean("enableParallax", value).apply()
}
