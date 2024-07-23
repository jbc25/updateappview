package com.triskelapps.simpleappupdate

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("RestrictedApi")
class CheckAppUpdateWorker(
    context: Context,
    workerParams: WorkerParameters,
    val packageName: String = context.packageName
) :
    CoroutineWorker(context, workerParams) {

    private val TAG: String = CheckAppUpdateWorker::class.java.simpleName
    private val PREF_LAST_VERSION_NOTIFIED = "pref_last_version_notified"

    private val simpleAppUpdate = SimpleAppUpdate(context)
    private lateinit var prefs: SharedPreferences

    override suspend fun doWork(): Result {

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val lastVersionNotified = prefs.getInt(PREF_LAST_VERSION_NOTIFIED, 0)

        val versionCode = SimpleAppUpdate.periodicCheckConfig!!.versionCode // Worker only scheduled if this class is not null

        sendRemoteLog("Start worker. lastVersionNotified: $lastVersionNotified, version code: $versionCode")

        return if (lastVersionNotified < versionCode) {
            checkAppUpdateAvailable()
        } else {
            Result.success()
        }
    }


    private suspend fun checkAppUpdateAvailable(): Result = suspendCoroutine { continuation ->
        simpleAppUpdate.setUpdateAvailableListener {
            sendRemoteLog("onUpdateAvailable. sending notification")
            prepareAndShowNotification()
        }
        simpleAppUpdate.setFinishListener {
            sendRemoteLog("onFinish")
            continuation.resume(Result.success())
        }
        simpleAppUpdate.checkUpdateAvailable()
    }

    private fun prepareAndShowNotification() {

        NotificationUtils(applicationContext).showNotification(packageName)
        prefs.edit().putInt(PREF_LAST_VERSION_NOTIFIED, SimpleAppUpdate.periodicCheckConfig!!.versionCode).apply()
    }

}
