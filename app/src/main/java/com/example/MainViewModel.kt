package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appManager = AppManager(application)
    val settingsManager = SettingsManager(application)

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _favorites = MutableStateFlow<List<AppInfo>>(emptyList())
    val favorites: StateFlow<List<AppInfo>> = _favorites.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            val apps = appManager.getInstalledApps()
            _installedApps.value = apps
            updateFavorites(apps)
        }
    }

    private fun updateFavorites(apps: List<AppInfo>) {
        val favPackageNames = settingsManager.favorites
        val favApps = favPackageNames.mapNotNull { pkg ->
            apps.find { it.packageName == pkg }
        }
        _favorites.value = favApps
    }

    fun toggleFavorite(appInfo: AppInfo) {
        val currentFavs = settingsManager.favorites
        if (currentFavs.contains(appInfo.packageName)) {
            settingsManager.removeFavorite(appInfo.packageName)
        } else {
            settingsManager.addFavorite(appInfo.packageName)
        }
        updateFavorites(_installedApps.value)
    }

    fun moveFavoriteLeft(appInfo: AppInfo) {
        settingsManager.moveFavoriteLeft(appInfo.packageName)
        updateFavorites(_installedApps.value)
    }

    fun moveFavoriteRight(appInfo: AppInfo) {
        settingsManager.moveFavoriteRight(appInfo.packageName)
        updateFavorites(_installedApps.value)
    }

    fun openSettings() {
        _showSettings.value = true
    }

    fun closeSettings() {
        _showSettings.value = false
    }
}
