package com.bayarsahintekin.permission

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Created by sahintekin on 13.11.2023.
 */

fun Context.scanForActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.scanForActivity()
        else -> {
            null
        }
    }
}