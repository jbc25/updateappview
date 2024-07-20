package com.triskelapps.updateappviewsample

import android.app.Application
import com.triskelapps.updateappview.UpdateAppManager
import com.triskelapps.updateappview.config.CheckWorkerConfiguration
import com.triskelapps.updateappview.config.UpdateBarStyle
import java.util.concurrent.TimeUnit

class App : Application() {

    override fun onCreate() {
        super.onCreate()


        val updateBarStyle = UpdateBarStyle(R.color.black, R.color.white)
        val checkWorkerConfiguration =
            CheckWorkerConfiguration(1, TimeUnit.HOURS, 30, TimeUnit.MINUTES)
        UpdateAppManager.init(
            this, BuildConfig.VERSION_CODE, R.mipmap.ic_launcher,
            updateBarStyle, checkWorkerConfiguration
        )
    }
}