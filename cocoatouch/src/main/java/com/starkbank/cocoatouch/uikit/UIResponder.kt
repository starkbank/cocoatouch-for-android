package com.starkbank.cocoatouch.uikit
import android.view.View
import com.starkbank.cocoatouch.foundation.NSObject
import com.starkbank.cocoatouch.annotation.InternalCocoaTouchApi


open class UIResponder: NSObject() {

    @InternalCocoaTouchApi
    lateinit var widget: View

    @InternalCocoaTouchApi
    open fun viewWithTag(tag: Int): View {
        return this.widget.findViewById(tag)
    }
}