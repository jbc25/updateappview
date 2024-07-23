package com.triskelapps.updateappviewsample

import android.app.Application
import com.triskelapps.simpleappupdate.SimpleAppUpdate
import com.triskelapps.simpleappupdate.config.NotificationStyle
import com.triskelapps.simpleappupdate.config.PeriodicCheckConfig
import com.triskelapps.simpleappupdate.config.UpdateBarStyle
import com.triskelapps.simpleappupdate.config.WorkerConfig
import java.util.concurrent.TimeUnit

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val updateBarStyle = UpdateBarStyle(R.color.black, R.color.white)

        val notificationStyle = NotificationStyle(R.mipmap.simple_app_update_notif_icon, R.color.red)
        val workerConfig =
            WorkerConfig(2, TimeUnit.HOURS, 60, TimeUnit.MINUTES)
        val periodicCheckConfig = PeriodicCheckConfig (
            this, BuildConfig.VERSION_CODE, notificationStyle, workerConfig
        )

        SimpleAppUpdate.init(updateBarStyle, periodicCheckConfig)
    }
}