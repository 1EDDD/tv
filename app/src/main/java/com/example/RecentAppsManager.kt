package com.example

import android.content.Context
import android.content.SharedPreferences

class RecentAppsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tv_launcher_recents", Context.MODE_PRIVATE)
    private val maxRecents = 8

    var recentPackages: List<String>
        get() = prefs.getString("recent_packages", null)?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        private set(value) = prefs.edit().putString("recent_packages", value.joinToString(",")).apply()

    fun addRecent(packageName: String) {
        if (packageName.isBlank()) return
        val current = recentPackages.toMutableList()
        current.remove(packageName)
        current.add(0, packageName)
        if (current.size > maxRecents) {
            recentPackages = current.subList(0, maxRecents)
        } else {
            recentPackages = current
        }
    }

    fun removeRecent(packageName: String) {
        val current = recentPackages.toMutableList()
        if (current.remove(packageName)) {
            recentPackages = current
        }
    }

    fun clearRecents() {
        prefs.edit().remove("recent_packages").apply()
    }
}
