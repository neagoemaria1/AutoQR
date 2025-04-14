package com.autoqr.utils

object NetworkUtils {
    fun getBaseUrl(): String {
        return if (isEmulator()) {
            "https://10.0.2.2:5001/"
        } else {
            "https://192.168.1.138:41458/"
        }
    }

    private fun isEmulator(): Boolean {
        return (android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.lowercase().contains("emulator")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86"))
    }
}
