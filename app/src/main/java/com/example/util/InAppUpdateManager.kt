package com.example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.CalendarContract
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

data class UpdateCadenceInfo(
    val daysSinceUpdate: Long,
    val isCadenceDue: Boolean,
    val message: String
)

class InAppUpdateManager(private val context: Context) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private var installListener: InstallStateUpdatedListener? = null

    companion object {
        private const val TAG = "InAppUpdateManager"
        const val UPDATE_REQUEST_CODE = 9001
        const val THREE_MONTHS_DAYS = 90L
    }

    /**
     * Checks if a new version is available on Google Play.
     */
    fun checkForUpdate(
        onUpdateAvailable: (AppUpdateInfo) -> Unit,
        onNoUpdate: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    Log.d(TAG, "Update available: versionCode ${appUpdateInfo.availableVersionCode()}")
                    onUpdateAvailable(appUpdateInfo)
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    onUpdateAvailable(appUpdateInfo)
                } else {
                    Log.d(TAG, "No update available or not distributed via Play Store")
                    onNoUpdate()
                }
            }.addOnFailureListener { error ->
                Log.w(TAG, "Failed to check for app update: ${error.message}")
                onError(error.message ?: "Chyba pri kontrole aktualizácií")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during check: ${e.message}", e)
            onError(e.message ?: "Nepodarilo sa skontrolovať aktualizácie")
        }
    }

    /**
     * Starts flexible in-app update flow using ActivityResultLauncher.
     */
    fun startFlexibleUpdate(
        activity: Activity,
        appUpdateInfo: AppUpdateInfo,
        onDownloaded: () -> Unit
    ) {
        registerInstallListener(onDownloaded)
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                activity,
                UPDATE_REQUEST_CODE
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error starting update flow: ${e.message}")
        }
    }

    fun registerInstallListener(onDownloaded: () -> Unit) {
        unregisterInstallListener()
        installListener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                Log.d(TAG, "Update downloaded successfully, ready to install")
                onDownloaded()
            }
        }
        appUpdateManager.registerListener(installListener!!)
    }

    fun unregisterInstallListener() {
        installListener?.let {
            try {
                appUpdateManager.unregisterListener(it)
            } catch (e: Exception) {
                // Ignore
            }
            installListener = null
        }
    }

    /**
     * Completes downloaded update by restarting the app.
     */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    /**
     * Computes how many days elapsed since the app build/install time
     * to evaluate the 3-month cadence (90 days).
     */
    fun getCadenceInfo(): UpdateCadenceInfo {
        return try {
            val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            val lastUpdateMillis = packageInfo.lastUpdateTime
            val elapsedMillis = System.currentTimeMillis() - lastUpdateMillis
            val days = (elapsedMillis / (1000L * 60 * 60 * 24)).coerceAtLeast(0)

            if (days >= THREE_MONTHS_DAYS) {
                UpdateCadenceInfo(
                    daysSinceUpdate = days,
                    isCadenceDue = true,
                    message = "Posledná aktualizácia pred $days dňami. Odporúča sa nové 3-mesačné vydanie na Google Play!"
                )
            } else {
                val remaining = THREE_MONTHS_DAYS - days
                UpdateCadenceInfo(
                    daysSinceUpdate = days,
                    isCadenceDue = false,
                    message = "Verzia je v rámci 3-mesačného cyklu ($days dní od inštalácie, ďalšia o ~$remaining dní)."
                )
            }
        } catch (e: Exception) {
            UpdateCadenceInfo(
                daysSinceUpdate = 0,
                isCadenceDue = false,
                message = "3-mesačný cyklus aktualizácií: Odporúčané vydávať verziu každých 90 dní."
            )
        }
    }

    /**
     * Opens user's calendar app with pre-filled 3-month reminder event.
     */
    fun addReminderToCalendar(context: Context) {
        val threeMonthsLater = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000L)
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "🚀 Aktualizácia VetPet Diary (Google Play)")
            putExtra(CalendarContract.Events.DESCRIPTION, "3-mesačný cyklus: Zvýšiť versionCode v build.gradle.kts, vygenerovať AAB a nahrať do Google Play Console.")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, threeMonthsLater)
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: schedule notification in app
            NotificationHelper.scheduleReminder(
                context = context,
                reminderId = 99999,
                title = "🚀 Čas na aktualizáciu aplikácie!",
                message = "Uplynuli 3 mesiace. Pripravte nové vydanie VetPet Diary na Google Play.",
                triggerAtMillis = threeMonthsLater
            )
        }
    }
}
