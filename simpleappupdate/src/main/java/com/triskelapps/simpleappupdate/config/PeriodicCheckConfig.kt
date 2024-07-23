package com.triskelapps.simpleappupdate.config

import android.content.Context

data class PeriodicCheckConfig(
    val context: Context,
    val versionCode: Int,
    val notificationStyle: NotificationStyle,
    val workerConfig: WorkerConfig = WorkerConfig()
)
