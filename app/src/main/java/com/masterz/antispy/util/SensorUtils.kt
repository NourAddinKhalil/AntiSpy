package com.masterz.antispy.util

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Process

object SensorUtils {
    fun isCameraInUse(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_CAMERA, Process.myUid(), context.packageName)
            return mode == AppOpsManager.MODE_ALLOWED
        }
        return false
    }

    fun isMicInUse(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_RECORD_AUDIO, Process.myUid(), context.packageName)
            return mode == AppOpsManager.MODE_ALLOWED
        }
        return false
    }

    fun isGpsInUse(context: Context): Boolean {
        // GPS detection is limited; placeholder for now
        return false
    }
}
