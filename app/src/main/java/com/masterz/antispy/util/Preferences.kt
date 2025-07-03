package com.masterz.antispy.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object Preferences {
    private const val PREFS_NAME = "antispy_prefs"
    private const val KEY_DOT_COLOR = "dot_color"
    private const val KEY_DOT_CORNER = "dot_corner"
    private const val KEY_SHOW_CAMERA = "show_camera"
    private const val KEY_SHOW_MIC = "show_mic"
    private const val KEY_SHOW_GPS = "show_gps"

    fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDotColor(context: Context, default: Int): Int =
        getPrefs(context).getInt(KEY_DOT_COLOR, default)

    fun setDotColor(context: Context, color: Int) =
        getPrefs(context).edit { putInt(KEY_DOT_COLOR, color) }

    fun getDotCorner(context: Context, default: Int): Int =
        getPrefs(context).getInt(KEY_DOT_CORNER, default)

    fun setDotCorner(context: Context, corner: Int) =
        getPrefs(context).edit { putInt(KEY_DOT_CORNER, corner) }

    fun isCameraEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_SHOW_CAMERA, true)

    fun setCameraEnabled(context: Context, enabled: Boolean) =
        getPrefs(context).edit { putBoolean(KEY_SHOW_CAMERA, enabled) }

    fun isMicEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_SHOW_MIC, true)

    fun setMicEnabled(context: Context, enabled: Boolean) =
        getPrefs(context).edit { putBoolean(KEY_SHOW_MIC, enabled) }

    fun isGpsEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_SHOW_GPS, true)

    fun setGpsEnabled(context: Context, enabled: Boolean) =
        getPrefs(context).edit { putBoolean(KEY_SHOW_GPS, enabled) }
}
