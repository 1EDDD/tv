package com.example

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class AppManager(private val context: Context) {

    private val iconCache = ConcurrentHashMap<String, Drawable>()
    private val bannerCache = ConcurrentHashMap<String, Drawable>()

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val appsMap = mutableMapOf<String, AppInfo>()

        // 1. Query LEANBACK_LAUNCHER (TV Optimized Apps)
        try {
            val leanbackIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            }
            val leanbackResolveInfos = packageManager.queryIntentActivities(leanbackIntent, 0)
            for (resolveInfo in leanbackResolveInfos) {
                val packageName = resolveInfo.activityInfo.packageName
                if (packageName == context.packageName) continue

                val appInfo = createAppInfoFromResolve(packageManager, resolveInfo, isLeanback = true)
                appsMap[packageName] = appInfo
            }
        } catch (e: Exception) {
            Log.e("AppManager", "Error querying leanback activities", e)
        }

        // 2. Query standard LAUNCHER
        try {
            val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val launcherResolveInfos = packageManager.queryIntentActivities(launcherIntent, 0)
            for (resolveInfo in launcherResolveInfos) {
                val packageName = resolveInfo.activityInfo.packageName
                if (packageName == context.packageName) continue
                if (appsMap.containsKey(packageName)) continue

                val appInfo = createAppInfoFromResolve(packageManager, resolveInfo, isLeanback = false)
                appsMap[packageName] = appInfo
            }
        } catch (e: Exception) {
            Log.e("AppManager", "Error querying standard activities", e)
        }

        return@withContext appsMap.values.sortedBy { it.name.lowercase() }
    }

    private fun createAppInfoFromResolve(
        pm: PackageManager,
        resolveInfo: ResolveInfo,
        isLeanback: Boolean
    ): AppInfo {
        val packageName = resolveInfo.activityInfo.packageName
        val name = resolveInfo.loadLabel(pm).toString()

        val icon = iconCache.getOrPut(packageName) {
            try {
                resolveInfo.loadIcon(pm) ?: resolveInfo.activityInfo.loadIcon(pm) ?: pm.defaultActivityIcon
            } catch (e: Exception) {
                pm.defaultActivityIcon
            }
        }

        val banner = if (isLeanback) {
            bannerCache.getOrPut(packageName) {
                try {
                    resolveInfo.activityInfo.loadBanner(pm) ?: resolveInfo.activityInfo.applicationInfo.loadBanner(pm) ?: icon
                } catch (e: Exception) {
                    icon
                }
            }
        } else null

        val launchIntent = if (isLeanback) {
            pm.getLeanbackLaunchIntentForPackage(packageName) ?: pm.getLaunchIntentForPackage(packageName)
        } else {
            pm.getLaunchIntentForPackage(packageName)
        }

        val category = detectAppCategory(packageName, name)

        return AppInfo(
            packageName = packageName,
            name = name,
            icon = icon,
            banner = banner,
            launchIntent = launchIntent,
            isLeanback = isLeanback,
            category = category
        )
    }

    private fun detectAppCategory(packageName: String, name: String): String {
        val lowerPkg = packageName.lowercase()
        val lowerName = name.lowercase()

        return when {
            lowerPkg.contains("youtube") || lowerPkg.contains("netflix") || lowerPkg.contains("primevideo") ||
            lowerPkg.contains("disney") || lowerPkg.contains("hulu") || lowerPkg.contains("hbo") ||
            lowerPkg.contains("max") || lowerPkg.contains("twitch") || lowerPkg.contains("crunchyroll") ||
            lowerPkg.contains("tubi") || lowerPkg.contains("pluto") || lowerName.contains("tv") ||
            lowerName.contains("stream") -> "Streaming"

            lowerPkg.contains("plex") || lowerPkg.contains("vlc") || lowerPkg.contains("kodi") ||
            lowerPkg.contains("player") || lowerPkg.contains("video") || lowerPkg.contains("gallery") ||
            lowerPkg.contains("mxplayer") || lowerPkg.contains("nova") -> "Media Player"

            lowerPkg.contains("spotify") || lowerPkg.contains("music") || lowerPkg.contains("audio") ||
            lowerPkg.contains("pandora") || lowerPkg.contains("tidal") || lowerPkg.contains("soundcloud") ||
            lowerPkg.contains("deezer") || lowerPkg.contains("tunein") -> "Music & Audio"

            lowerPkg.contains("game") || lowerPkg.contains("retro") || lowerPkg.contains("arcade") ||
            lowerPkg.contains("steam") || lowerPkg.contains("geforce") || lowerPkg.contains("play") -> "Games"

            lowerPkg.contains("chrome") || lowerPkg.contains("browser") || lowerPkg.contains("firefox") ||
            lowerPkg.contains("puffin") -> "Web Browser"

            lowerPkg.contains("setting") || lowerPkg.contains("file") || lowerPkg.contains("explorer") ||
            lowerPkg.contains("tool") || lowerPkg.contains("cleaner") -> "Utilities"

            else -> "Application"
        }
    }

    fun launchApp(appInfo: AppInfo): Boolean {
        return try {
            val intent = appInfo.launchIntent ?: context.packageManager.getLaunchIntentForPackage(appInfo.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("AppManager", "Failed to launch ${appInfo.packageName}", e)
            false
        }
    }

    fun openAppInfo(packageName: String): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("AppManager", "Failed to open App Info for $packageName", e)
            false
        }
    }

    fun uninstallApp(packageName: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                data = Uri.parse("package:$packageName")
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            } catch (e2: Exception) {
                Log.e("AppManager", "Failed to uninstall $packageName", e2)
                false
            }
        }
    }

    fun isDefaultHomeLauncher(): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName == context.packageName
        } catch (e: Exception) {
            false
        }
    }

    fun openHomeSettings() {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Log.e("AppManager", "Cannot open system home settings", e2)
            }
        }
    }
}
