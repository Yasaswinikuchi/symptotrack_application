package com.symtotrack

/**
 * Central configuration for the app.
 *
 * SERVER_IP = "10.0.2.2" works for BOTH emulators and physical devices:
 *   - Emulator  : 10.0.2.2 is the built-in alias for the host machine's localhost.
 *   - Physical  : Gradle's `installDebug` task automatically runs
 *                 `adb reverse tcp:80 tcp:80` after every install, which tunnels
 *                 the phone's port 80 through the USB cable to localhost on this PC.
 *
 * Result: No Wi-Fi IP changes, no Windows Firewall rules, no manual edits — ever.
 */
object AppConfig {
    // Detect if running on an emulator
    private fun isEmulator(): Boolean {
        return android.os.Build.FINGERPRINT.startsWith("generic") ||
                android.os.Build.MODEL.contains("Emulator") ||
                android.os.Build.MODEL.contains("Android SDK built for x86")
    }

    // Choose appropriate host IP based on environment
    val BASE_URL: String = if (isEmulator()) {
        "http://10.0.2.2/symto_tracker/api"
    } else {
        "http://${BuildConfig.SERVER_IP}/symto_tracker/api"
    }
    // ----- Dark‑Mode Scheduler -----
    const val KEY_DARK_START = "dark_start_hour"
    const val KEY_DARK_END   = "dark_end_hour"

    fun getInt(context: android.content.Context, key: String, default: Int): Int {
        return context.getSharedPreferences("prefs", android.content.Context.MODE_PRIVATE)
            .getInt(key, default)
    }

    fun setInt(context: android.content.Context, key: String, value: Int) {
        context.getSharedPreferences("prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .putInt(key, value)
            .apply()
    }
}



