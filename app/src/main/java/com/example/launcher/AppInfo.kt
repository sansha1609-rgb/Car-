package com.example.launcher

import android.graphics.drawable.Drawable

enum class AppCategory(val label: String) {
    ALL("All Apps"),
    NAVIGATION("Navigation"),
    MEDIA("Music & Audio"),
    COMMUNICATION("Phone & Contacts"),
    SYSTEM("System & Tools")
}

data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable? = null,
    val category: AppCategory = AppCategory.ALL,
    val isSystemApp: Boolean = false
)
