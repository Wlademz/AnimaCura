package com.example.data.local

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import java.security.MessageDigest
import java.util.UUID

enum class LicenseTier(
    val title: String,
    val maxPets: Int,
    val priceEur: String,
    val billingCycle: String,
    val badge: String,
    val description: String,
    val googlePlayProductId: String
) {
    BASIC(
        title = "Základ (do 2 zvierat)",
        maxPets = 2,
        priceEur = "1,99 €",
        billingCycle = "na 3 mesiace (pri každom update)",
        badge = "ZÁKLAD",
        description = "Štvrťročné predplatné pre 1 až 2 zvieratká. Platí sa každé 3 mesiace pri aktualizácii cez Google Play.",
        googlePlayProductId = "petmed_sub_basic_3m"
    ),
    FAMILY_VIP(
        title = "Rodina VIP (do 5 zvierat)",
        maxPets = 5,
        priceEur = "4,99 €",
        billingCycle = "jednorazovo / ročne",
        badge = "RODINA",
        description = "Pre domácnosti a milovníkov zvierat s viacerými miláčikmi (do 5 zvierat).",
        googlePlayProductId = "petmed_vip_family"
    ),
    BREEDER_PRO(
        title = "Chovateľ PRO (do 15 zvierat)",
        maxPets = 15,
        priceEur = "14,99 €",
        billingCycle = "jednorazovo / ročne",
        badge = "PRO",
        description = "Pre chovateľské stanice a chovateľov s viacerými vrhmi a chovnými jedincami (do 15 zvierat).",
        googlePlayProductId = "petmed_pro_breeder"
    ),
    ORGANIZATION(
        title = "Útulok & Karanténna stanica",
        maxPets = 99999,
        priceEur = "69,99 €",
        billingCycle = "na 12 mesiacov",
        badge = "ORGANIZÁCIA",
        description = "Ročná licencia pre karanténne stanice, útulky, záchranné stanice a OZ (150+ zvierat, neobmedzene).",
        googlePlayProductId = "petmed_sub_org_annual"
    )
}

data class LicenseStatus(
    val isActivated: Boolean,
    val isExpired: Boolean,
    val daysRemaining: Int,
    val totalTrialDays: Int = 7,
    val deviceId: String,
    val activeKey: String?,
    val tier: LicenseTier,
    val maxPets: Int = tier.maxPets
)

object LicenseManager {
    private const val PREFS_NAME = "petmed_license_prefs"
    private const val KEY_INSTALL_TIME = "install_timestamp"
    private const val KEY_LAST_CHECK_TIME = "last_check_timestamp"
    private const val KEY_DEVICE_ID = "device_installation_id"
    private const val KEY_IS_ACTIVATED = "is_license_activated"
    private const val KEY_LICENSE_KEY = "activated_license_key"
    private const val KEY_LICENSE_TIER = "activated_license_tier"

    private const val TRIAL_DURATION_DAYS = 7
    private const val MS_PER_DAY = 24 * 60 * 60 * 1000L
    private const val SECRET_SALT = "WLADEMZ_PETMED_SECURITY_SALT_2026"

    // Master author activation passwords
    private val MASTER_ORGANIZATION_KEYS = setOf(
        "PETMED-VIP-2026",
        "WLADEMZ-WIKI-2026",
        "PETMED-PRO-LIFETIME",
        "ACIDOZA-PET-VIP",
        "WIKI-PUP-PREMIUM",
        "ORGANIZATION-UNLIMITED-2026",
        "UTULOK-PRO-2026",
        "KARANTENA-VIP-2026"
    )

    private val MASTER_BREEDER_KEYS = setOf(
        "BREEDER-PRO-2026",
        "CHOVATEL-VIP-2026"
    )

    private val MASTER_FAMILY_KEYS = setOf(
        "FAMILY-VIP-2026",
        "RODINA-VIP-2026"
    )

    private val MASTER_BASIC_KEYS = setOf(
        "BASIC-3M-2026",
        "ZAKLAD-2026"
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
        val savedTierName = prefs.getString(KEY_LICENSE_TIER, null)

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
            val tier = try {
                if (savedTierName != null) LicenseTier.valueOf(savedTierName) else LicenseTier.ORGANIZATION
            } catch (_: Exception) {
                LicenseTier.ORGANIZATION
            }
            return LicenseStatus(
                isActivated = true,
                isExpired = false,
                daysRemaining = 9999,
                deviceId = deviceId,
                activeKey = activeKey,
                tier = tier,
                maxPets = tier.maxPets
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

        val activeTier = LicenseTier.BASIC

        return LicenseStatus(
            isActivated = false,
            isExpired = isExpired,
            daysRemaining = daysRemaining,
            deviceId = deviceId,
            activeKey = null,
            tier = activeTier,
            maxPets = activeTier.maxPets
        )
    }

    /**
     * Checks if user can add another pet according to their tier limit
     */
    fun canAddPet(context: Context, currentPetCount: Int): Boolean {
        val status = getLicenseStatus(context)
        return currentPetCount < status.maxPets
    }

    /**
     * Completes a purchase via Google Play In-App Billing
     */
    fun purchaseTierViaGooglePlay(context: Context, tier: LicenseTier) {
        val orderId = "GPA.${System.currentTimeMillis()}-${tier.googlePlayProductId}"
        saveActivation(context, orderId, tier)
    }

    /**
     * Generates valid device-specific key for author for a particular tier
     */
    fun generateKeyForDeviceId(deviceId: String, tier: LicenseTier = LicenseTier.FAMILY_VIP): String {
        val cleanId = deviceId.trim().uppercase()
        val tierPrefix = when (tier) {
            LicenseTier.ORGANIZATION -> "ORG"
            LicenseTier.BREEDER_PRO -> "PRO"
            LicenseTier.FAMILY_VIP -> "FAM"
            LicenseTier.BASIC -> "BAS"
        }
        val hash = sha256("$cleanId:$tierPrefix:$SECRET_SALT")
        val p1 = hash.substring(0, 4).uppercase()
        val p2 = hash.substring(4, 8).uppercase()
        return "KEY-$tierPrefix-$p1-$p2"
    }

    /**
     * Validates input code and permanently activates if correct
     */
    fun activateWithKey(context: Context, rawKey: String): Boolean {
        val formattedKey = rawKey.trim().uppercase()
        if (formattedKey.isBlank()) return false

        val status = getLicenseStatus(context)

        // 1. Check Master Keys
        if (MASTER_ORGANIZATION_KEYS.contains(formattedKey)) {
            saveActivation(context, formattedKey, LicenseTier.ORGANIZATION)
            return true
        }
        if (MASTER_BREEDER_KEYS.contains(formattedKey)) {
            saveActivation(context, formattedKey, LicenseTier.BREEDER_PRO)
            return true
        }
        if (MASTER_FAMILY_KEYS.contains(formattedKey)) {
            saveActivation(context, formattedKey, LicenseTier.FAMILY_VIP)
            return true
        }
        if (MASTER_BASIC_KEYS.contains(formattedKey)) {
            saveActivation(context, formattedKey, LicenseTier.BASIC)
            return true
        }

        // 2. Check Device-Specific Hash Keys for each tier
        for (tier in listOf(LicenseTier.ORGANIZATION, LicenseTier.BREEDER_PRO, LicenseTier.FAMILY_VIP, LicenseTier.BASIC)) {
            val expectedHashKey = generateKeyForDeviceId(status.deviceId, tier)
            if (formattedKey == expectedHashKey) {
                saveActivation(context, formattedKey, tier)
                return true
            }
        }

        // Legacy format check (e.g. WLAD-XXXX-YYYY, KEY-p1-p2-p3)
        val legacyKey = generateLegacyKey(status.deviceId)
        if (formattedKey == legacyKey || isSimpleAlgorithmKey(status.deviceId, formattedKey)) {
            saveActivation(context, formattedKey, LicenseTier.ORGANIZATION)
            return true
        }

        return false
    }

    private fun generateLegacyKey(deviceId: String): String {
        val cleanId = deviceId.trim().uppercase()
        val hash = sha256("$cleanId:$SECRET_SALT")
        val p1 = hash.substring(0, 4).uppercase()
        val p2 = hash.substring(4, 8).uppercase()
        val p3 = hash.substring(8, 12).uppercase()
        return "KEY-$p1-$p2-$p3"
    }

    private fun isSimpleAlgorithmKey(deviceId: String, inputKey: String): Boolean {
        val parts = deviceId.replace("PET-", "").split("-")
        if (parts.size == 2) {
            val expected1 = "WLAD-${parts[0]}-${parts[1]}".uppercase()
            val expected2 = "PASS-${parts[1]}-${parts[0]}".uppercase()
            if (inputKey == expected1 || inputKey == expected2) return true
        }
        return false
    }

    private fun saveActivation(context: Context, key: String, tier: LicenseTier) {
        getPrefs(context).edit()
            .putBoolean(KEY_IS_ACTIVATED, true)
            .putString(KEY_LICENSE_KEY, key)
            .putString(KEY_LICENSE_TIER, tier.name)
            .apply()
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
        } catch (_: Exception) {
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
