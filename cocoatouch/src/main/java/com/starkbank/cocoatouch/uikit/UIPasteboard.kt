package com.starkbank.cocoatouch.uikit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class UIPasteboard {
    companion object {
        var general = UIPasteboard()
    }
    var string: String? = null
    set(value) {
        field = value
        val clip = ClipData.newPlainText("general", value)
        val clipboard = UIApplication.context().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(clip)
    }
}