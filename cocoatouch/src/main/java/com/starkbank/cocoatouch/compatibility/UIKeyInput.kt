package com.starkbank.cocoatouch.compatibility

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.starkbank.cocoatouch.uikit.UIApplication

class UIKeyInput {

    companion object {

         fun getInputMethodManager(): InputMethodManager? {
            val activity: Activity = UIApplication.sharedApplication().activity()
            return activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        }

    }

}