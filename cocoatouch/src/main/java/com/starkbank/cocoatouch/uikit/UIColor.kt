package com.starkbank.cocoatouch.uikit

import android.graphics.Color

class UIColor(
    val hex: String,
    val colorInt: Int = Color.parseColor(hex)
) {
    companion object{}
}