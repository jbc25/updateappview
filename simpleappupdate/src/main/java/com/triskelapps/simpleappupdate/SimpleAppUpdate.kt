package com.triskelapps.simpleappupdate

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.triskelapps.simpleappupdate.config.WorkerConfig
import com.triskelapps.simpleappupdate.config.Configuration
import com.triskelapps.simpleappupdate.config.PeriodicCheckConfig
import com.triskelapps.simpleappupdate.config.UpdateBarStyle

class SimpleAppUpdate(private val context: Context) {

    private val TAG: String = SimpleAppUpdate::class.java.simpleName

    val TEMPLATE_URL_GOOGLE_PLAY_APP_HTTP: String =
        "https://play.google.com/store/apps/details?id=%s"
    val TEMPLATE_URL_GOOGLE_PLAY_APP_DIRECT: String = "market://details?id=%s"

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private var appUpdateInfo: AppUpdateInfo? = null
    private var onUpdateAvailable: () -> Unit = {}
    private var onCheckUpdateFinish: () -> Unit = {}

    companion object {

        var updateBarStyle: UpdateBarStyle? = null
        var periodicCheckConfig: PeriodicCheckConfig? = null

        @JvmStatic
        @JvmOverloads
        fun init(
            updateBarStyle: UpdateBarStyle? = null,
            periodicCheckConfig: PeriodicCheckConfig? = null,
        ) {
            SimpleAppUpdate.updateBarStyle = updateBarStyle
            SimpleAppUpdate.periodicCheckConfig = periodicCheckConfig

            sendRemoteLog("SimpleAppUpdate init")

            periodicCheckConfig?.let {
                scheduleAppUpdateCheckWork(periodicCheckConfig.context, periodicCheckConfig.workerConfig)
            }
        }

        private fun scheduleAppUpdateCheckWork(
            context: Context,
            workerConfig: WorkerConfig
        ) {
            val constraints: Constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val updateAppCheckWork: PeriodicWorkRequest = PeriodicWorkRequest.Builder(
                CheckAppUpdateWorker::class.java,
                workerConfig.repeatInterval, workerConfig.repeatIntervalTimeUnit,
                workerConfig.flexInterval, workerConfig.flexIntervalTimeUnit,
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "appUpdateCheckWork",
                ExistingPeriodicWorkPolicy.UPDATE, updateAppCheckWork
            ).state.observeForever { state: Operation.State ->

                sendRemoteLog("Periodic work status: $state")
            }
        }

    }

    init {
    }

    fun setUpdateAvailableListener(updateAvailableListener: () -> Unit = {}) {
        this.onUpdateAvailable = updateAvailableListener
    }

    fun setFinishListener(updateFinishListener: () -> Unit = {}) {
        this.onCheckUpdateFinish = updateFinishListener
    }

    fun checkUpdateAvailable() {

        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo

        appUpdateInfoTask
            .addOnCompleteListener { task: Task<AppUpdateInfo?> ->
                if (task.isSuccessful) {
                    appUpdateInfo = task.result
                    if (appUpdateInfo!!.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        onUpdateAvailable()
                    }
                } else {
                    Log.e(TAG, "checkUpdateAvailable: ", task.exception)
                }

                onCheckUpdateFinish()

            }
    }

    fun launchUpdate() {
        if (appUpdateInfo != null) {
            if (appUpdateInfo!!.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                val options: AppUpdateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                    .setAllowAssetPackDeletion(true).build()

                if (context is Activity) {
                    appUpdateManager.startUpdateFlow(appUpdateInfo!!, context, options)
                } else {
                    throw java.lang.IllegalStateException("context is not an Activity")
                }
            } else {
                openGooglePlay()
            }
        }

    }

    private fun openGooglePlay() {
        val httpUrl = String.format(TEMPLATE_URL_GOOGLE_PLAY_APP_HTTP, context.packageName)
        val directUrl = String.format(TEMPLATE_URL_GOOGLE_PLAY_APP_DIRECT, context.packageName)

        val directPlayIntent = Intent(Intent.ACTION_VIEW, Uri.parse(directUrl))
        try {
            context.startActivity(directPlayIntent)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(httpUrl)))
        }
    }

    fun onResume() {
        if (appUpdateInfo != null && appUpdateInfo!!.updateAvailability()
            == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
        ) {
            val options: AppUpdateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                .setAllowAssetPackDeletion(true).build()

            if (context is Activity) {
                appUpdateManager.startUpdateFlow(appUpdateInfo!!, context, options)
            } else {
                throw java.lang.IllegalStateException("context is not an Activity")
            }
        }
    }


}
