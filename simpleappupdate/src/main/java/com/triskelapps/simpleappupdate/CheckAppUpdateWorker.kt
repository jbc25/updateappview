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
class CheckAppUpdateWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val TAG: String = CheckAppUpdateWorker::class.java.simpleName
    private val PREF_LAST_VERSION_NOTIFIED = "pref_last_version_notified"

    val packageName: String = context.packageName

    private val simpleAppUpdate = SimpleAppUpdate(context)
    private lateinit var prefs: SharedPreferences

    companion object {
        val VERSION_CODE = "version_code"
    }

    override suspend fun doWork(): Result {
        //sendRemoteLog("Start worker. ")

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val lastVersionNotified = prefs.getInt(PREF_LAST_VERSION_NOTIFIED, 0)

        val versionCode = inputData.getInt(VERSION_CODE, -1)

        //sendRemoteLog("Start worker. lastVersionNotified: $lastVersionNotified, version code: $versionCode")

        return if (lastVersionNotified < versionCode) {
            checkAppUpdateAvailable()
        } else {
            Result.success()
        }
    }


    private suspend fun checkAppUpdateAvailable(): Result = suspendCoroutine { continuation ->
        simpleAppUpdate.setUpdateAvailableListener {
            //sendRemoteLog("onUpdateAvailable. sending notification")
            prepareAndShowNotification()
        }
        simpleAppUpdate.setFinishListener {
            //sendRemoteLog("onFinish")
            continuation.resume(Result.success())
        }
        simpleAppUpdate.checkUpdateAvailable()
    }

    private fun prepareAndShowNotification() {

        NotificationUtils(applicationContext).showNotification(packageName)

        val versionCode = inputData.getInt(VERSION_CODE, -1)
        prefs.edit().putInt(PREF_LAST_VERSION_NOTIFIED, versionCode).apply()
    }

}
