package com.starkbank.cocoatouch.uikit
import com.starkbank.cocoatouch.foundation.NSObject


open class UIResponder: NSObject() {

    internal lateinit var widget: Any

    internal open fun viewWithTag(tag: Int): Any {
        return Any()
    }
}