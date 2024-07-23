package com.triskelapps.simpleappupdate.config

import androidx.annotation.ColorRes
import androidx.annotation.StyleRes


data class UpdateBarStyle @JvmOverloads constructor (
    @ColorRes val backgroundColor: Int,
    @ColorRes val foregroundElementsColor: Int,
    @StyleRes val textStyle: Int? = null,
)
