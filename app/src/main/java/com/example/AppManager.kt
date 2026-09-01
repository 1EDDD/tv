package com.example

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppManager(private val context: Context) {

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val apps = mutableMapOf<String, AppInfo>()

        // 1. Query LEANBACK_LAUNCHER
        val leanbackIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val leanbackResolveInfos = packageManager.queryIntentActivities(leanbackIntent, 0)
        for (resolveInfo in leanbackResolveInfos) {
            val packageName = resolveInfo.activityInfo.packageName
            if (packageName == context.packageName) continue

            val name = resolveInfo.loadLabel(packageManager).toString()
            val icon = resolveInfo.loadIcon(packageManager)
            val launchIntent = packageManager.getLeanbackLaunchIntentForPackage(packageName) 
                ?: packageManager.getLaunchIntentForPackage(packageName)

            apps[packageName] = AppInfo(packageName, name, icon, launchIntent)
        }

        // 2. Query standard LAUNCHER
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val launcherResolveInfos = packageManager.queryIntentActivities(launcherIntent, 0)
        for (resolveInfo in launcherResolveInfos) {
            val packageName = resolveInfo.activityInfo.packageName
            if (packageName == context.packageName) continue
            if (apps.containsKey(packageName)) continue

            val name = resolveInfo.loadLabel(packageManager).toString()
            val icon = resolveInfo.loadIcon(packageManager)
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

            apps[packageName] = AppInfo(packageName, name, icon, launchIntent)
        }

        return@withContext apps.values.sortedBy { it.name.lowercase() }
    }
}
