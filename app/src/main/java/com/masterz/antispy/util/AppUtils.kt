package com.masterz.antispy.util

import android.content.Context
import android.content.pm.PackageManager

object AppUtils {
    fun getAppName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val label = pm.getApplicationLabel(appInfo)
            if (label.isNullOrBlank()) packageName else label.toString()
        } catch (e: Exception) {
            packageName
        }
    }
}
