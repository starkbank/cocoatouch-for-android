package com.starkbank.cocoatouch.uikit
import android.view.View
import com.starkbank.cocoatouch.foundation.NSObject


open class UIResponder: NSObject() {

    internal lateinit var widget: View

    internal open fun viewWithTag(tag: Int): View {
        return this.widget.findViewById(tag)
    }
}