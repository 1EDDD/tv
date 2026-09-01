package com.example

import android.content.Intent
import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val name: String,
    val icon: Drawable,
    val banner: Drawable? = null,
    val launchIntent: Intent?,
    val isLeanback: Boolean = false,
    val category: String = "App"
)
