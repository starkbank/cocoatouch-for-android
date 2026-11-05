package com.starkbank.cocoatouch.uikit

import android.content.res.Resources
import android.graphics.Color
import android.view.View
import android.view.ViewGroup


internal val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

internal val Float.dpToPx: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

internal fun View.removeFromParent() {
    this.parent?.let {
        val parentViewGroup = it as ViewGroup
        parentViewGroup.removeView(this)
    }
}

internal fun UIColor.isLight(): Boolean {
    val darkness =
        1 - (0.299 * Color.red(colorInt) + 0.587 * Color.green(colorInt) + 0.114 * Color.blue(colorInt)) / 255
    return darkness < 0.5
}
