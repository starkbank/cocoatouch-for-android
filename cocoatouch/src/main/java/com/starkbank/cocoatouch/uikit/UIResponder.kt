package com.starkbank.cocoatouch.uikit
import com.starkbank.cocoatouch.foundation.NSObject
import android.view.View


open class UIResponder: NSObject() {

    internal lateinit var widget: View

    internal open fun viewWithTag(tag: Int): View {
        return this.widget.findViewById(tag)
    }
}