package com.example.data

import android.content.Context
import android.content.SharedPreferences

enum class AppThemeMode {
    PURE_DARK,   // Pure Black AMOLED
    BLUE_DARK,   // Deep Blue Cyberpunk
    RGB          // Vibrant Neon RGB Glow
}

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("falambons_prefs", Context.MODE_PRIVATE)

    var currentUserId: Long
        get() = prefs.getLong(KEY_CURRENT_USER_ID, -1L)
        set(value) {
            prefs.edit().putLong(KEY_CURRENT_USER_ID, value).commit()
        }

    var themeMode: AppThemeMode
        get() {
            val name = prefs.getString(KEY_THEME_MODE, AppThemeMode.PURE_DARK.name)
            return try {
                AppThemeMode.valueOf(name ?: AppThemeMode.PURE_DARK.name)
            } catch (e: Exception) {
                AppThemeMode.PURE_DARK
            }
        }
        set(value) {
            prefs.edit().putString(KEY_THEME_MODE, value.name).commit()
        }

    var isWordWrapEnabled: Boolean
        get() = prefs.getBoolean(KEY_WORD_WRAP, false) // Default false for unlimited long line mode
        set(value) {
            prefs.edit().putBoolean(KEY_WORD_WRAP, value).commit()
        }

    var customBackgroundUri: String?
        get() = prefs.getString(KEY_BG_URI, null)
        set(value) {
            prefs.edit().putString(KEY_BG_URI, value).commit()
        }

    var backgroundOpacity: Float
        get() = prefs.getFloat(KEY_BG_OPACITY, 0.35f)
        set(value) {
            prefs.edit().putFloat(KEY_BG_OPACITY, value).commit()
        }

    var isFloatingPadEnabled: Boolean
        get() = prefs.getBoolean(KEY_FLOATING_PAD, false)
        set(value) {
            prefs.edit().putBoolean(KEY_FLOATING_PAD, value).commit()
        }

    var floatingPadText: String
        get() = prefs.getString(KEY_FLOATING_PAD_TEXT, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_FLOATING_PAD_TEXT, value).commit()
        }

    companion object {
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_WORD_WRAP = "word_wrap"
        private const val KEY_BG_URI = "bg_uri"
        private const val KEY_BG_OPACITY = "bg_opacity"
        private const val KEY_FLOATING_PAD = "floating_pad"
        private const val KEY_FLOATING_PAD_TEXT = "floating_pad_text"
    }
}
