package com.example

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    SETTINGS,
    SEARCH,
    REORDER_FAVORITES,
    AMBIENT_SCREENSAVER
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val appManager = AppManager(application)
    val settingsManager = SettingsManager(application)
    val recentAppsManager = RecentAppsManager(application)

    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _visibleApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val visibleApps: StateFlow<List<AppInfo>> = _visibleApps.asStateFlow()

    private val _favorites = MutableStateFlow<List<AppInfo>>(emptyList())
    val favorites: StateFlow<List<AppInfo>> = _favorites.asStateFlow()

    private val _recentApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val recentApps: StateFlow<List<AppInfo>> = _recentApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isDefaultLauncher = MutableStateFlow(true)
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    private val _selectedApp = MutableStateFlow<AppInfo?>(null)
    val selectedApp: StateFlow<AppInfo?> = _selectedApp.asStateFlow()

    private val _parallaxOffset = MutableStateFlow(0f)
    val parallaxOffset: StateFlow<Float> = _parallaxOffset.asStateFlow()

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadApps()
        }
    }

    init {
        loadApps()
        checkDefaultLauncher()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        try {
            application.registerReceiver(packageReceiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(packageReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadApps() {
        viewModelScope.launch {
            val apps = appManager.getInstalledApps()
            _installedApps.value = apps

            val installedPackages = apps.map { it.packageName }.toSet()
            val currentHidden = settingsManager.hiddenApps
            val validHidden = currentHidden.filter { installedPackages.contains(it) }.toSet()
            if (currentHidden != validHidden) {
                settingsManager.hiddenApps = validHidden
            }

            val visibleAppList = apps.filterNot { validHidden.contains(it.packageName) }
            _visibleApps.value = visibleAppList

            // If user has no favorites yet, populate defaults from top apps
            if (settingsManager.favorites.isEmpty() && visibleAppList.isNotEmpty()) {
                val defaultFavs = visibleAppList.take(6).map { it.packageName }
                settingsManager.favorites = defaultFavs
            }

            updateFavorites(visibleAppList)
            updateRecentApps(visibleAppList)
        }
    }

    private fun updateFavorites(apps: List<AppInfo>) {
        val favPackageNames = settingsManager.favorites
        val favApps = favPackageNames.mapNotNull { pkg ->
            apps.find { it.packageName == pkg }
        }
        _favorites.value = favApps
    }

    private fun updateRecentApps(apps: List<AppInfo>) {
        val recents = recentAppsManager.recentPackages
        val recentAppList = recents.mapNotNull { pkg ->
            apps.find { it.packageName == pkg }
        }
        _recentApps.value = recentAppList
    }

    fun launchApp(appInfo: AppInfo): Boolean {
        val success = appManager.launchApp(appInfo)
        if (success) {
            recentAppsManager.addRecent(appInfo.packageName)
            if (settingsManager.rememberLastFocus) {
                settingsManager.lastFocusedPackage = appInfo.packageName
            }
            updateRecentApps(_visibleApps.value)
        }
        return success
    }

    fun setSelectedApp(appInfo: AppInfo?, indexOffset: Float = 0f) {
        _selectedApp.value = appInfo
        _parallaxOffset.value = indexOffset
    }

    fun toggleFavorite(appInfo: AppInfo) {
        val currentFavs = settingsManager.favorites
        if (currentFavs.contains(appInfo.packageName)) {
            settingsManager.removeFavorite(appInfo.packageName)
        } else {
            settingsManager.addFavorite(appInfo.packageName)
        }
        updateFavorites(_visibleApps.value)
    }

    fun moveFavoriteLeft(appInfo: AppInfo): Int {
        val newIndex = settingsManager.moveFavoriteLeft(appInfo.packageName)
        updateFavorites(_visibleApps.value)
        return newIndex
    }

    fun moveFavoriteRight(appInfo: AppInfo): Int {
        val newIndex = settingsManager.moveFavoriteRight(appInfo.packageName)
        updateFavorites(_visibleApps.value)
        return newIndex
    }

    fun swapFavorites(idx1: Int, idx2: Int) {
        settingsManager.swapFavorites(idx1, idx2)
        updateFavorites(_visibleApps.value)
    }

    fun toggleAppVisibility(packageName: String) {
        settingsManager.toggleAppVisibility(packageName)
        loadApps()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun checkDefaultLauncher() {
        viewModelScope.launch {
            _isDefaultLauncher.value = appManager.isDefaultHomeLauncher()
        }
    }

    fun openHomeSettings() {
        appManager.openHomeSettings()
    }
}
