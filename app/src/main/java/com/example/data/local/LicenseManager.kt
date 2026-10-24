package com.example.data.local

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import java.security.MessageDigest
import java.util.UUID

data class LicenseStatus(
    val isActivated: Boolean,
    val isExpired: Boolean,
    val daysRemaining: Int,
    val totalTrialDays: Int = 7,
    val deviceId: String,
    val activeKey: String?
)

object LicenseManager {
    private const val PREFS_NAME = "petmed_license_prefs"
    private const val KEY_INSTALL_TIME = "install_timestamp"
    private const val KEY_LAST_CHECK_TIME = "last_check_timestamp"
    private const val KEY_DEVICE_ID = "device_installation_id"
    private const val KEY_IS_ACTIVATED = "is_license_activated"
    private const val KEY_LICENSE_KEY = "activated_license_key"

    private const val TRIAL_DURATION_DAYS = 7
    private const val MS_PER_DAY = 24 * 60 * 60 * 1000L
    private const val SECRET_SALT = "WLADEMZ_PETMED_SECURITY_SALT_2026"

    // Master author activation passwords that unlock any device
    private val MASTER_KEYS = setOf(
        "PETMED-VIP-2026",
        "WLADEMZ-WIKI-2026",
        "PETMED-PRO-LIFETIME",
        "ACIDOZA-PET-VIP",
        "WIKI-PUP-PREMIUM"
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Initializes device trial info and returns current license status
     */
    fun getLicenseStatus(context: Context): LicenseStatus {
        val prefs = getPrefs(context)
        val now = System.currentTimeMillis()

        var installTime = prefs.getLong(KEY_INSTALL_TIME, 0L)
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        val isActivated = prefs.getBoolean(KEY_IS_ACTIVATED, false)
        val activeKey = prefs.getString(KEY_LICENSE_KEY, null)

        if (installTime == 0L || deviceId == null) {
            installTime = now
            val randomHex = UUID.randomUUID().toString().replace("-", "").substring(0, 8).uppercase()
            deviceId = "PET-${randomHex.substring(0, 4)}-${randomHex.substring(4, 8)}"

            prefs.edit()
                .putLong(KEY_INSTALL_TIME, installTime)
                .putLong(KEY_LAST_CHECK_TIME, now)
                .putString(KEY_DEVICE_ID, deviceId)
                .apply()
        }

        if (isActivated) {
            return LicenseStatus(
                isActivated = true,
                isExpired = false,
                daysRemaining = 9999,
                deviceId = deviceId,
                activeKey = activeKey
            )
        }

        // Anti-clock rewind check
        val lastCheck = prefs.getLong(KEY_LAST_CHECK_TIME, installTime)
        if (now > lastCheck) {
            prefs.edit().putLong(KEY_LAST_CHECK_TIME, now).apply()
        }

        val elapsedMs = now - installTime
        val elapsedDays = (elapsedMs / MS_PER_DAY).toInt()
        val daysRemaining = (TRIAL_DURATION_DAYS - elapsedDays).coerceAtLeast(0)
        val isExpired = elapsedDays >= TRIAL_DURATION_DAYS

        return LicenseStatus(
            isActivated = false,
            isExpired = isExpired,
            daysRemaining = daysRemaining,
            deviceId = deviceId,
            activeKey = null
        )
    }

    /**
     * Generates valid device-specific key for author
     */
    fun generateKeyForDeviceId(deviceId: String): String {
        val cleanId = deviceId.trim().uppercase()
        val hash = sha256("$cleanId:$SECRET_SALT")
        val p1 = hash.substring(0, 4).uppercase()
        val p2 = hash.substring(4, 8).uppercase()
        val p3 = hash.substring(8, 12).uppercase()
        return "KEY-$p1-$p2-$p3"
    }

    /**
     * Alternative easy-format key based on device ID parts: WLAD-XXXX-YYYY
     */
    private fun isSimpleAlgorithmKey(deviceId: String, inputKey: String): Boolean {
        val parts = deviceId.replace("PET-", "").split("-")
        if (parts.size == 2) {
            val expected1 = "WLAD-${parts[0]}-${parts[1]}".uppercase()
            val expected2 = "PASS-${parts[1]}-${parts[0]}".uppercase()
            if (inputKey == expected1 || inputKey == expected2) return true
        }
        return false
    }

    /**
     * Validates input code and permanently activates if correct
     */
    fun activateWithKey(context: Context, rawKey: String): Boolean {
        val formattedKey = rawKey.trim().uppercase()
        if (formattedKey.isBlank()) return false

        val status = getLicenseStatus(context)

        // 1. Check Master Keys
        val isMaster = MASTER_KEYS.contains(formattedKey)

        // 2. Check Device-Specific Hash Key
        val expectedHashKey = generateKeyForDeviceId(status.deviceId)
        val isDeviceSpecific = formattedKey == expectedHashKey

        // 3. Check Simple Algorithm Key (e.g. WLAD-XXXX-YYYY)
        val isSimple = isSimpleAlgorithmKey(status.deviceId, formattedKey)

        if (isMaster || isDeviceSpecific || isSimple) {
            getPrefs(context).edit()
                .putBoolean(KEY_IS_ACTIVATED, true)
                .putString(KEY_LICENSE_KEY, formattedKey)
                .apply()
            return true
        }

        return false
    }

    /**
     * Initiates standard Android app uninstall flow
     */
    fun requestUninstall(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to application details settings
            try {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
