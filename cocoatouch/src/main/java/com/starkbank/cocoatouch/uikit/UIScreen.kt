package com.starkbank.cocoatouch.uikit

import android.content.res.Resources
import com.starkbank.cocoatouch.foundation.NSObject

class UIScreen: NSObject() {

    companion object {
        var screenWidth: Int = 0
        get() {
            return Resources.getSystem().displayMetrics.widthPixels
        }
    }
}