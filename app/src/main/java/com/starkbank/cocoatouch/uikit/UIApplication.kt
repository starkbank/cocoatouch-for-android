package com.starkbank.cocoatouch.uikit
import android.content.Context


object UIApplication: UIResponder() {

    var delegate: UIApplicationDelegate? = null
    var isNotificationsEnabled: Boolean = true
    internal var activity: UIActivity? = null

    fun sharedApplication(): UIApplication {
        return this
    }

    fun context(): Context {
        return this.activity!!
    }

    fun activity(): UIActivity {
        return this.activity!!
    }
}