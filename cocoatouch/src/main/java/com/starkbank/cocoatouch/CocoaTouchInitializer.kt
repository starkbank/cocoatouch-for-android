package com.starkbank.cocoatouch

import android.content.Context
import androidx.startup.Initializer
import com.starkbank.cocoatouch.uikit.UIApplication


class CocoaTouchInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        UIApplication.initialize(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
