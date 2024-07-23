package com.triskelapps.simpleappupdate.config

data class Configuration(
    val packageName: String,
    val versionCode: Int,
    val notificationStyle: NotificationStyle,
    val updateBarStyle: UpdateBarStyle? = null,
)
